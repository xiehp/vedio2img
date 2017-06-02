package xie.v2i.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import xie.common.utils.XWaitChange;
import xie.common.utils.XWaitTime;
import xie.v2i.utils.CImage;

public class MeidaLoador {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private JFrame frame;

	private JPanel videoSurface;

	private BufferedImage bufferedImage;
	private int[] rgbBufferRef;

	private DirectMediaPlayerComponent mediaPlayerComponent;
	private DirectMediaPlayer meidaLoador;

	private int width = 1280;

	private int height = 720;

	private long totalTime = 0;

	// 判断参数
	/** 视频是否已经载入 */
	private boolean isVideoLoaded = false;
	/** 视频是否已经暂停播放 (初始认为非暂停状态，所以应该在视频载入后才能判断该值) */
	private boolean isDoPauseActionFlg = false;;

	/** 判断视频帧是否已经改变或者超时 */
	private XWaitChange xWaitChange = new XWaitChange(0);

	/** 视频帧是否已经可用 当前该判断用于是否超时 */
	private boolean isOnDisplayTimeoutFlg = false;
	/** 判断超时的时间 */
	private long ON_DISPLAY_TIMEOUT_VALUE = 30000;
	/** 认为的超时时间之前，需要做某些事情，如超时之前需要判断图像是否已经发生变化 */
	private long BEFORE_ON_DISPLAY_TIMEOUT_VALUE = ON_DISPLAY_TIMEOUT_VALUE - 5000;

	private boolean isStopedFlg = true;

	// 比较参数
	/** 每次图像改变后，缓存该图像，留作和下一个图像比较用 */
	private int[] preRgb;
	private boolean rgbBufferChangedFlg = false;

	/** 当前设定的时间 */
	private long nowSetedTime;

	// 测试参数
	private int paintComponent = 0;
	private int onDisplay = 0;
	private int display = 0;
	private Date startTime;
	private long 校准真实时间 = 0;
	private long 校准视频时间 = 0;
	private int mediaPlayerComponent_Display = 0;
	private long timeChanged = 0;

	private long preTime = 0;

	// 控制参数
	private boolean showVideoImageFlg = true;

	public static void main(String[] args) {
		BasicConfigurator.configure();

		// args = new String[] { "D:\\Program Files\\VideoLAN\\VLC" };
		// args = new String[] { "D:\\soft\\vlc\\vlc-2.2.1-win64" };
		// args = new String[] { "D:\\soft\\vlc\\vlc-2.2.4-win64" };
		if (args.length > 0) {
			String livVlcPath = args[0];
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), livVlcPath);
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcCoreName(), livVlcPath);
			// System.setProperty("jna.library.path", livVlcPath);
			System.setProperty("VLC_PLUGIN_PATH", livVlcPath + "\\plugins");
		} else {
		}
		new NativeDiscovery().discover();

		// System.out.println(LibVlc.INSTANCE.libvlc_get_version());
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				// String mrl = "G:\\video\\無彩限のファントム·ワールド 02.mp4";
				// File fileMrl = new File("E:\\AnimeShotSIte\\anime\\M\\命运之夜\\UBW\\[Kamigami] Fate stay night UBW - 03 [1080p x265 Ma10p FLAC Sub(Eng,Jap)].mkv");
				// File fileMrl = new File("E:\\AnimeShotSIte\\anime\\C\\超时空要塞\\Δ\\[dmhy][Macross_Delta][18][x264_aac][GB_BIG5][1080P_mkv].mkv");
				File fileMrl = new File("E:\\AnimeShotSIte\\anime\\M\\命运之夜\\UBW\\[Kamigami] Fate stay night UBW - 03 [1080p x265 Ma10p FLAC Sub(Eng,Jap)].mkv");
				// MediaInfo mediaInfo = MediaInfo.mediaInfo(fileMrl.getAbsolutePath());
				// System.out.println(mediaInfo.toString());

				// MeidaLoador meidaLoador = new MeidaLoador(fileMrl.getAbsolutePath(), 640,480);
				MeidaLoador meidaLoador = new MeidaLoador(fileMrl.getAbsolutePath(), 1920, 1080);
				//meidaLoador.setPlaySpeed(0.125f);
				meidaLoador.start();
			}
		});
	}

	public MeidaLoador(String mrl) {
		init(mrl, 1280, 720);
	}

	public MeidaLoador(String mrl, int width, int height) {
		init(mrl, width, height);
	}

	public void init(final String mrl, final int width, final int height) {
		this.width = width;
		this.height = height;
		startTime = new Date();

		frame = new JFrame("Direct Media Player");
		// frame.setBounds(100, 100, width, height);
		frame.setBounds(100, 100, 1300, 800);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		JPanel controlsPane = new JPanel();
		JTextField seedInputField = new JTextField();
		seedInputField.setBounds(0, 0, 200, 20);
		seedInputField.setText("38000");
		controlsPane.add(seedInputField);
		JButton showVideoButton = new JButton("显示视频图像");
		controlsPane.add(showVideoButton);
		JButton seedInputButton = new JButton("SeekTime");
		controlsPane.add(seedInputButton);
		JButton seedPostionInputButton = new JButton("SeekPostion");
		controlsPane.add(seedPostionInputButton);
		JButton pauseButton = new JButton("Pause");
		controlsPane.add(pauseButton);
		JButton rewindButton = new JButton("Rewind");
		controlsPane.add(rewindButton);
		JButton skipButton = new JButton("Skip");
		controlsPane.add(skipButton);
		JButton savePicButton = new JButton("save pic");
		controlsPane.add(savePicButton);
		frame.add(controlsPane, BorderLayout.SOUTH);

		showVideoButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (showVideoImageFlg) {
					showVideoImageFlg = false;
				} else {
					showVideoImageFlg = true;
				}
			}
		});

		seedInputButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				long value = Long.valueOf(seedInputField.getText());
				setTime(value);
			}
		});

		seedPostionInputButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				long value = Long.valueOf(seedInputField.getText());
				mediaPlayerComponent.getMediaPlayer().setPosition(value * 1.0f / totalTime);
			}
		});

		pauseButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				pause();
			}
		});

		rewindButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				long timeInputValue = Long.valueOf(seedInputField.getText());
				mediaPlayerComponent.getMediaPlayer().skip(-timeInputValue);
				System.out.println(mediaPlayerComponent.getMediaPlayer().getTime());
				System.out.println(mediaPlayerComponent.getMediaPlayer().getPosition());
			}
		});

		skipButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				long timeInputValue = Long.valueOf(seedInputField.getText());
				mediaPlayerComponent.getMediaPlayer().skip(timeInputValue);
				System.out.println(mediaPlayerComponent.getMediaPlayer().getTime());
				System.out.println(mediaPlayerComponent.getMediaPlayer().getPosition());
			}
		});

		savePicButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CImage.saveImage(bufferedImage, mediaPlayerComponent.getMediaPlayer().getTime(), new File("D:\\work\\temp\\bbb"));
			}
		});

		videoSurface = new VideoSurfacePanel();
		// frame.setContentPane(videoSurface);
		frame.add(videoSurface, BorderLayout.CENTER);
		bufferedImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
		BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
			public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
				return new RV32BufferFormat(width, height);
			}
		};

		mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {

			protected RenderCallback onGetRenderCallback() {
				return new TutorialRenderCallbackAdapter();
			}

			// protected String[] onGetMediaPlayerFactoryExtraArgs() {
			// return new String[] { "--no-drop-late-frames", "--no-skip-frames", "--sout-ts-use-key-frames", "--grayscale"};
			// }

			@Override
			public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
				isStopedFlg = false;
				super.display(mediaPlayer, nativeBuffers, bufferFormat);
				mediaPlayerComponent_Display++;
			}

			@Override
			public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
				timeChanged = newTime;
			}

			@Override
			public void mediaDurationChanged(final MediaPlayer mediaPlayer, final long newDuration) {
				// SwingUtilities.invokeLater(new Runnable() {
				// @Override
				// public void run() {
				// totalTime = newDuration;
				// }
				// });

				totalTime = newDuration;
			}

			@Override
			public void stopped(MediaPlayer mediaPlayer) {
				isStopedFlg = true;
				super.stopped(mediaPlayer);

			}

			@Override
			public void paused(MediaPlayer mediaPlayer) {
				isDoPauseActionFlg = true;
			}

			@Override
			public void playing(MediaPlayer mediaPlayer) {
				isStopedFlg = false;
			}
		};

		frame.setVisible(true);
		mediaPlayerComponent.getMediaPlayer().playMedia(mrl);
		meidaLoador = mediaPlayerComponent.getMediaPlayer();
	}

	private class VideoSurfacePanel extends JPanel {

		private static final long serialVersionUID = 967236967413552009L;

		private VideoSurfacePanel() {
			setBackground(Color.black);
			setOpaque(true);
			setPreferredSize(new Dimension(width, height));
			setMinimumSize(new Dimension(width, height));
			setMaximumSize(new Dimension(width, height));
		}

		private int lineHeight = 0;
		Graphics2D g2D;

		private void drawStringLine(String value) {
			g2D.drawString(value, 20, lineHeight);
			lineHeight += 10;
		}

		protected void paintComponent(Graphics g) {
			++paintComponent;
			// System.out.println("paintComponent" + paintComponent);

			// 画图片
			g2D = (Graphics2D) g;
			if (showVideoImageFlg) {
				g2D.drawImage(bufferedImage, null, 0, 0);
			}

			// 输出信息
			g2D.fillRect(0, 0, 250, 300);
			g2D.setColor(Color.white);
			lineHeight = 0;
			drawStringLine("totalTime: " + totalTime);
			drawStringLine("宽高: " + width + "," + height);
			drawStringLine("startTime: " + startTime);
			drawStringLine("nowTime: " + new Date());

			drawStringLine("paintComponent: " + paintComponent);
			drawStringLine("onDisplay: " + onDisplay);
			drawStringLine("display: " + display);
			drawStringLine("nowSetedTime: " + nowSetedTime);
			drawStringLine("getMediaPlayer().getTime(): " + mediaPlayerComponent.getMediaPlayer().getTime());
			drawStringLine("preTime: " + preTime);
			drawStringLine("timeChanged: " + timeChanged);
			drawStringLine("xWaitChange.pastTime : " + xWaitChange.getPastTime());
			drawStringLine("isOnDisplayTimeoutFlg: " + isOnDisplayTimeoutFlg);
			// drawStringLine("isRefreshedAfterChangeTime: " + isRefreshedAfterChangeTime(mediaPlayerComponent.getMediaPlayer().getTime()));
			drawStringLine("isVideoLoaded: " + isVideoLoaded);
			drawStringLine("校准真实时间: " + 校准真实时间);
			drawStringLine("校准视频时间: " + 校准视频时间);
			drawStringLine("rgbBufferRef: " + rgbBufferRef);
			drawStringLine("bufferedImage: " + bufferedImage);
			drawStringLine("preRgb: " + preRgb);
			if (preRgb == null) {
				drawStringLine("preRgb length: " + null);
			} else {
				drawStringLine("preRgb length: " + preRgb.length);
			}
			drawStringLine("rgbBufferChangedFlg: " + rgbBufferChangedFlg);
			drawStringLine("mediaPlayerComponent_Display: " + mediaPlayerComponent_Display);

			drawStringLine("isStopedFlg: " + isStopedFlg);
			drawStringLine("isDoPauseActionFlg: " + isDoPauseActionFlg);
			drawStringLine("showVideoImageFlg: " + showVideoImageFlg);

			// try {
			// // if (!ImageIO.write(image, "jpg", new File("D:\\work\\temp\\"
			// // + paintComponent + ".jpg"))) {
			// // System.out.println(paintComponent + ".jpg");
			// // }
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// System.out.println(paintComponent + ".jpg");
			// e.printStackTrace();
			// }
		}
	}

	private class TutorialRenderCallbackAdapter extends RenderCallbackAdapter {

		private TutorialRenderCallbackAdapter() {
			super(new int[width * height]);
		}

		@Override
		public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffer, BufferFormat bufferFormat) {
			super.display(mediaPlayer, nativeBuffer, bufferFormat);

			++display;
		}

		protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
			// Simply copy buffer to the image and repaint
			++onDisplay;
			isVideoLoaded = true;
			// System.out.println("onDisplay" + onDisplay);

			long nowTime = mediaPlayerComponent.getMediaPlayer().getTime();

			if (校准真实时间 == 0) {
				校准真实时间 = new Date().getTime();
				校准视频时间 = nowTime;
			} else {
				long dif真实时间 = new Date().getTime() - 校准真实时间;
				long dif视频时间 = nowTime - 校准视频时间;
				if (dif真实时间 - dif视频时间 > 500 || dif真实时间 - dif视频时间 < -500) {

					logger.debug("dif真实时间 :{}, dif视频时间{}: ", dif真实时间, dif视频时间);

					校准真实时间 = new Date().getTime();
					校准视频时间 = nowTime;
				}
			}

			if (nowTime != preTime) {
				preTime = nowTime;
				logger.info("onDisplay nowTime: " + nowTime);
			}

			// xWaitChange.isChanged(nowTime);
			if (xWaitChange.getPastTime() > ON_DISPLAY_TIMEOUT_VALUE) {
				isOnDisplayTimeoutFlg = true;
				logger.debug("isOnDisplayTimeoutFlg: " + isOnDisplayTimeoutFlg + "," + xWaitChange.getPastTime());
			}

			bufferedImage.setRGB(0, 0, width, height, rgbBuffer, 0, width);
			rgbBufferRef = rgbBuffer;

			checkRgbChangedAfterChangedOrWaitTime(mediaPlayer.getTime(), rgbBufferRef);

			videoSurface.repaint();
		}
	}

	private void checkRgbChangedAfterChangedOrWaitTime(long mediaTime, int[] mediaRgbBufferRef) {
		if (mediaRgbBufferRef == null) {
			// 还未生成任何图像
			return;
		}

		if (xWaitChange.isChanged(mediaTime) || xWaitChange.getPastTime() > BEFORE_ON_DISPLAY_TIMEOUT_VALUE) {
			if (!rgbBufferChangedFlg) {
				boolean changedFlg = false;
				// 如果图片没有改变过， 则先将当前图像进行复制
				if (preRgb == null || preRgb.length != mediaRgbBufferRef.length) {
					preRgb = new int[mediaRgbBufferRef.length];
					changedFlg = true;
				} else {
					// 还未超时，只判断图像的一部分，超时后，判断完整图像
					if (xWaitChange.getPastTime() < BEFORE_ON_DISPLAY_TIMEOUT_VALUE) {
						changedFlg = checkRgbChanged(preRgb, mediaRgbBufferRef, 13);
					} else {
						changedFlg = checkRgbChanged(preRgb, mediaRgbBufferRef, 1);
					}
				}

				if (changedFlg) {
					System.arraycopy(mediaRgbBufferRef, 0, preRgb, 0, mediaRgbBufferRef.length);
					logger.info("图像已改变, setTime:{}, mediaTime:{}", nowSetedTime, mediaTime);
					rgbBufferChangedFlg = true;
				}
			}
		}
	}

	public boolean checkRgbChanged() {
		return checkRgbChanged(preRgb, rgbBufferRef, -1);
	}

	/**
	 * 检查图像是否改变了
	 * 
	 * @param cacheRgb
	 * @param realRgb
	 * @param checkStep 像素点检查间隔
	 * @return
	 */
	private boolean checkRgbChanged(int[] cacheRgb, int[] realRgb, int checkStep) {
		if (checkStep < 1) {
			checkStep = 11;
		}
		boolean changedFrontFlg = false;

		for (int i = 0; i < cacheRgb.length; i = i + checkStep) {
			if (cacheRgb[i] != realRgb[i]) {
				changedFrontFlg = true;
				break;
			}
		}

		if (changedFrontFlg) {
			return true;
		} else {
			return false;
		}
	}

	public File saveImage(File toFile) {
		long time = mediaPlayerComponent.getMediaPlayer().getTime();
		logger.debug("saveImage time: {}", time);
		File file = CImage.saveImage(bufferedImage, toFile);
		return file;
	}

	public long getTime() {
		long time = mediaPlayerComponent.getMediaPlayer().getTime();
		return time;
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public void pause() {
		logger.info("接受到暂停命令，向播放器发出暂停命令");
		mediaPlayerComponent.getMediaPlayer().pause();
		logger.info("暂停成功");
	}

	public void start() {
		logger.info("接受到开始命令，向播放器发出开始命令");
		mediaPlayerComponent.getMediaPlayer().start();
		logger.info("开始成功");
	}

	public void skip(long skipTime) {
		long time = mediaPlayerComponent.getMediaPlayer().getTime() + skipTime;
		setTime(time);
	}

	public void setTime(long time) {
		logger.info("setTime start: " + time);

		// 改变时间前先复制当前图像
		if (!rgbBufferChangedFlg) {
			// 如果图片没有改变过， 则先将当前图像进行复制
			if (preRgb == null || preRgb.length != rgbBufferRef.length) {
				preRgb = new int[rgbBufferRef.length];
			}
			System.arraycopy(rgbBufferRef, 0, preRgb, 0, rgbBufferRef.length);
			logger.info("setTime(), 重新设置了比较rgb数组");
		}
		rgbBufferChangedFlg = false;

		// 设置时间
		nowSetedTime = time;
		mediaPlayerComponent.getMediaPlayer().setTime(time);

		// 重置参数
		xWaitChange.resetCompareValue(time);
		isOnDisplayTimeoutFlg = false;

		logger.info("setTime end: " + time);
	}

	public boolean isRefreshedAfterChangeTime(long time) {
		// return xWaitChange.isChanged(time) || isOnDisplayTimeoutFlg;
		// return xWaitChange.isChanged(time) || rgbBufferChangedFlg;
		// return rgbBufferChangedFlg || isOnDisplayTimeoutFlg;

		checkRgbChangedAfterChangedOrWaitTime(mediaPlayerComponent.getMediaPlayer().getTime(), rgbBufferRef);
		return rgbBufferChangedFlg;
	}

	public long getPastTime() {
		return xWaitChange.getPastTime();
	}

	public boolean isOnDisplayTimeoutFlg() {
		return isOnDisplayTimeoutFlg;
	}

	public void setShowVideoImageFlg(boolean showVideoImageFlg) {
		this.showVideoImageFlg = showVideoImageFlg;
	}

	public boolean isPlaying() {
		return mediaPlayerComponent.getMediaPlayer().isPlaying();
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void stop() {
		mediaPlayerComponent.getMediaPlayer().stop();
	}

	public boolean isStoped() {
		return isStopedFlg;
	}

	/**
	 * 初始认为非暂停状态，所以应该在视频载入后才能判断该值
	 * 
	 * @return
	 */
	public boolean isDoPauseAction() {
		return isDoPauseActionFlg;
	}

	public boolean isVideoLoaded() {
		return isVideoLoaded;
	}

	public void release() {
		mediaPlayerComponent.release();
		isVideoLoaded = false;
	}

	public void dispose() {
		frame.dispose();
		isVideoLoaded = false;
	}

	/**
	 * 关闭字幕
	 */
	public void closeSubtitle() {
		meidaLoador.setSpu(-1);
	}

	/**
	 * 设置播放速度
	 */
	public void setPlaySpeed(float speed) {
		meidaLoador.setRate(speed);
	}

	/**
	 * 通过直接播放到指定时间<br>
	 * PS:超过一定时间自动暂停<br>
	 * PS:由于1080P播放速度问题，速度调整为1/4，因此超时时间也相应增加4倍<br>
	 * 
	 * @param skipTime
	 * @throws InterruptedException
	 */
	public void playToSpecialTime(long specialTime) throws InterruptedException {
		long beforeSkipTime = meidaLoador.getTime();
		long skipTime = specialTime - beforeSkipTime;
		logger.warn("开始调整时间到{}, 当前时间点{}, 需要调整时间{}", specialTime, beforeSkipTime, skipTime);
		if (skipTime < 100) {
			logger.warn("指定时间小于当前视频时间，无法调整，必须大于100毫秒才能执行。", skipTime);
			return;
		}

		// 关闭字幕，播放速度调整为1/4
		closeSubtitle();

		meidaLoador.start();
		XWaitTime playWaitTime = new XWaitTime(skipTime * 8 + 2500);
		while (true) {
			if (specialTime - meidaLoador.getTime() < 100) {
				logger.info("达到时间，停止", skipTime);
				meidaLoador.pause();
				Thread.sleep(2000);
				break;
			}
			if (playWaitTime.isTimeout()) {
				logger.warn("超时直接停止", skipTime);
				meidaLoador.pause();
				Thread.sleep(2000);
				break;
			}
			Thread.sleep(50);
		}
	}

	/**
	 * 更新比较用rgb数组
	 */
	public void updateRgbToPre() {
		logger.info("updateRgbToPre(), 重新设置了比较rgb数组");
		System.arraycopy(rgbBufferRef, 0, preRgb, 0, rgbBufferRef.length);
	}
}
