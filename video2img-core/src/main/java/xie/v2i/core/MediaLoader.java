package xie.v2i.core;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.*;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;
import uk.co.caprica.vlcj.support.Info;
import xie.common.utils.XWaitChange;
import xie.common.utils.XWaitTime;
import xie.v2i.utils.CImage;

/**
 *
 */
public class MediaLoader {

	/** 判断超时的时间 */
	private static final long ON_DISPLAY_TIMEOUT_VALUE = 30000;
	/** 认为的超时时间之前，需要做某些事情，如超时之前需要判断图像是否已经发生变化 */
	private static final long BEFORE_ON_DISPLAY_TIMEOUT_VALUE = ON_DISPLAY_TIMEOUT_VALUE - 5000;
	private BufferedImage bufferedImage;
	private XWaitTime checkRate = new XWaitTime(200);
	private int display = 0;
	private MyMediaPlayerFactory factory;
	private JFrame frame;
	private int height = 1080;
	/** 视频是否已经暂停播放 (初始认为非暂停状态，所以应该在视频载入后才能判断该值) */
	private boolean isDoPauseActionFlg = false;
	/** 视频帧是否已经可用 当前该判断用于是否超时 */
	private boolean isOnDisplayTimeoutFlg = false;
	// 判断参数
	private boolean isStoppedFlg = true;
	/** 视频是否已经载入 */
	private boolean isVideoLoaded = false;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private EmbeddedMediaPlayer mediaPlayer;
	private CallbackMediaPlayerComponent mediaPlayerComponent;
	private int mediaPlayerComponentDisplay = 0;
	/** 当前设定的时间 */
	private long nowSettingTime;
	private int onDisplay = 0;

	// 比较参数
	// 测试参数
	private int paintComponent = 0;
	/** 每次图像改变后，缓存该图像，留作和下一个图像比较用 */
	private int[] preRgb;
	private long preTime = 0;
	private boolean rgbBufferChangedFlg = false;
	private int[] rgbBufferRef;
	// 控制参数
	private boolean showVideoImageFlg = true;
	private Date startTime;
	private XWaitTime surfaceRepeatWaitTime = new XWaitTime(500);
	private long timeChanged = 0;
	private long totalTime = 0;
	private VideoSurfacePanel videoSurface;
	private int width = 1920;
	private final int[] rgbBuffer = new int[width * height];
	/** 判断视频帧是否已经改变或者超时 */
	private XWaitChange xWaitChange = new XWaitChange(0);
	private long 校准真实时间 = 0;
	private long 校准视频时间 = 0;

	public MediaLoader(String mrl) throws InvocationTargetException, InterruptedException {
		init(mrl, 1920, 1080);
	}

	private void init(final String mrl, final int width, final int height) throws InvocationTargetException, InterruptedException {
		this.width = width;
		this.height = height;
		startTime = new Date();
		bufferedImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
		bufferedImage.setAccelerationPriority(1.0f);
		String[] embeddedMediaPlayerArgs = {
				"--video-title=vlcj video output",
				"--no-snapshot-preview",
				"--quiet",
				"--intf=dummy"
		};
		factory = new MyMediaPlayerFactory(embeddedMediaPlayerArgs);


		BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
			@Override
			public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
				return new RV32BufferFormat(width, height);
			}

			@Override
			public void allocatedBuffers(ByteBuffer[] buffers) {

			}
		};
		// BufferFormatCallback bufferFormatCallback = (sourceWidth, sourceHeight) -> new RV32BufferFormat(width, height);

		SwingUtilities.invokeAndWait(() -> {
			frame = new JFrame("Direct Media Player");
			// frame.setBounds(100, 100, width, height);
			frame.setBounds(100, 100, 1300, 800);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					// mediaPlayerComponent.release();
					mediaPlayer.release();
					factory.release();
					System.exit(0);
				}
			});

			videoSurface = new VideoSurfacePanel();
			frame.getContentPane().add(videoSurface, BorderLayout.CENTER);

			JPanel controlsPane = new JPanel();
			JTextField seedInputField = new JTextField();
			seedInputField.setBounds(0, 0, 200, 20);
			seedInputField.setText("38000");
			controlsPane.add(seedInputField);
			JButton showVideoButton = new JButton("显示视频图像");
			controlsPane.add(showVideoButton);
			JButton seedInputButton = new JButton("SeekTime");
			controlsPane.add(seedInputButton);
			JButton seedPositionInputButton = new JButton("SeekPosition");
			controlsPane.add(seedPositionInputButton);
			JButton pauseButton = new JButton("Pause");
			controlsPane.add(pauseButton);
			JButton rewindButton = new JButton("Rewind");
			controlsPane.add(rewindButton);
			JButton skipButton = new JButton("Skip");
			controlsPane.add(skipButton);
			JButton nextFrameButton = new JButton("Next Frame");
			controlsPane.add(nextFrameButton);
			JButton savePicButton = new JButton("save pic");
			controlsPane.add(savePicButton);
			frame.add(controlsPane, BorderLayout.SOUTH);

			showVideoButton.addActionListener(e -> {
				showVideoImageFlg = !showVideoImageFlg;
			});

			seedInputButton.addActionListener(e -> {
				long value = Long.valueOf(seedInputField.getText());
				setTime(value);
			});

			seedPositionInputButton.addActionListener(e -> {
				long value = Long.valueOf(seedInputField.getText());
				mediaPlayer.controls().setPosition(value * 1.0f / totalTime);
			});

			pauseButton.addActionListener(e -> pause());

			rewindButton.addActionListener(e -> {
				long timeInputValue = Long.valueOf(seedInputField.getText());
				mediaPlayer.controls().skipTime(-timeInputValue);
				logger.info("" + mediaPlayer.status().time());
				logger.info("" + mediaPlayer.status().position());
			});

			skipButton.addActionListener(e -> {
				long timeInputValue = Long.valueOf(seedInputField.getText());
				mediaPlayer.controls().skipTime(timeInputValue);
				logger.info("" + mediaPlayer.status().time());
				logger.info("" + mediaPlayer.status().position());
			});

			nextFrameButton.addActionListener(e -> {
				mediaPlayer.controls().nextFrame();
				logger.info("" + mediaPlayer.status().time());
				logger.info("" + mediaPlayer.status().position());
			});

			savePicButton.addActionListener(e -> CImage.saveImage(bufferedImage, mediaPlayer.status().time(), new File("D:\\work\\temp\\bbb")));
		});

		// 创建播放用组件
		mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
		mediaPlayer.videoSurface().set(factory.videoSurfaces().newVideoSurface(bufferFormatCallback, new TutorialRenderCallbackAdapter(), true));
		mediaPlayer.media().play(mrl);

		// myMediaPlayerFactory = new MyMediaPlayerFactory(embeddedMediaPlayerArgs);
		// CallbackMediaPlayerSpec callbackMediaPlayerSpec = MediaPlayerSpecs.callbackMediaPlayerSpec();
		// callbackMediaPlayerSpec
		// .withFactory(myMediaPlayerFactory)
		// .withRenderCallback(new TutorialRenderCallbackAdapter())
		// .withVideoSurfaceComponent(videoSurface)
		// .withBufferFormatCallback(bufferFormatCallback)
		// // .withRenderCallback(new TutorialRenderCallbackAdapter())
		// // .withVideoSurfaceComponent(videoSurface)
		// // .withBufferFormatCallback(bufferFormatCallback)
		// ;
		//
		// // mediaPlayerComponent = new CallbackMediaPlayerComponent();
		// mediaPlayerComponent = new CallbackMediaPlayerComponent(callbackMediaPlayerSpec) {
		//
		// @Override
		// protected void onPaintOverlay(Graphics2D g2) {
		// g2.fillRect(0, 0, 250, 300);
		// }
		//
		// };
		// frame.add(mediaPlayerComponent, BorderLayout.CENTER);

		mediaPlayer.events().addMediaPlayerEventListener(
				new MediaPlayerEventAdapter() {
					// });
					// mediaPlayerComponent = new CallbackMediaPlayerComponent(bufferFormatCallback) {

					// TODO display(EmbeddedMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
					// @Override
					// public void display(EmbeddedMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
					// isStoppedFlg = false;
					// super.display(mediaPlayer, nativeBuffers, bufferFormat);
					// mediaPlayerComponentDisplay++;
					// }

					@Override
					public void playing(MediaPlayer mediaPlayer) {
						isStoppedFlg = false;
					}

					@Override
					public void paused(MediaPlayer mediaPlayer) {
						isDoPauseActionFlg = true;
					}

					// protected String[] onGetMediaPlayerFactoryExtraArgs() {
					// return new String[] { "--no-drop-late-frames", "--no-skip-frames", "--sout-ts-use-key-frames", "--grayscale"};
					// }

					@Override
					public void stopped(MediaPlayer mediaPlayer) {
						isStoppedFlg = true;
					}

					@Override
					public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
						timeChanged = newTime;
					}

					@Override
					public void mediaPlayerReady(MediaPlayer mediaPlayer) {
						resetTotalTime();
						mediaPlayer.marquee().enable(false);
						mediaPlayer.media().info().textTracks().removeAll(mediaPlayer.media().info().textTracks());
					}

					protected MediaPlayerFactory onGetMediaPlayerFactory() {
						String[] args = onGetMediaPlayerFactoryExtraArgs();
						String[] extraArgs = onGetMediaPlayerFactoryExtraArgs();
						if (extraArgs.length > 0) {
							List<String> combinedArgs = new ArrayList<>(args.length + extraArgs.length);
							combinedArgs.addAll(Arrays.asList(args));
							combinedArgs.addAll(Arrays.asList(extraArgs));
							args = combinedArgs.toArray(args);
						}
						logger.debug("args={}", Arrays.toString(args));
						return new MyMediaPlayerFactory(args);
					}

					String[] onGetMediaPlayerFactoryExtraArgs() {
						return new String[] {
								// "--no-video",
								// "--logfile=D:\\temp\\vlc.log",
								// "--aspect-ratio=4:3",
								// "--mirror-split=1",
								// "--hevc-force-fps=4",
								// "--no-grayscale", // 灰度视频输出 (默认关闭)
								// "--no-input-fast-seek",
								// "--input-repeat=5",
								// "--start-time=1.1",
								// "--stop-time==3.1"
						};
					}

				});

		// MyMediaPlayerFactory myMediaPlayerFactory = (MyMediaPlayerFactory) mediaPlayerComponent.mediaPlayerFactory();
		logger.info("libvlc的版本：{}", LibVlc.libvlc_get_version());
		logger.info("使用的解码器：{}", LibVlc.libvlc_get_compiler());

		frame.setVisible(true);
		// mediaPlayer = mediaPlayerComponent.mediaPlayer();
		// mediaPlayer.media().play(mrl);
	}

	public void pause() {
		logger.info("接受到暂停命令，向播放器发出暂停命令");
		mediaPlayer.submit(() -> mediaPlayer.controls().pause());
		logger.info("暂停成功");
	}

	public long resetTotalTime() {
		totalTime = mediaPlayer.media().info().duration();
		return totalTime;
	}

	public MediaLoader(String mrl, int width, int height) throws InvocationTargetException, InterruptedException {
		init(mrl, width, height);
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		// args = new String[] { "D:\\Program Files\\VideoLAN\\VLC" };
		// args = new String[] { "D:\\soft\\vlc\\vlc-2.2.1-win64" };
		// args = new String[] { "D:\\soft\\vlc\\vlc-2.2.4-win64" };
		if (args.length > 0) {
			String livVlcPath = args[0];
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), livVlcPath);
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcCoreLibraryName(), livVlcPath);
			// System.setProperty("jna.library.path", livVlcPath);
			System.setProperty("VLC_PLUGIN_PATH", livVlcPath + "\\plugins");
		}
		new NativeDiscovery().discover();

		// logger.info(LibVlc.INSTANCE.libvlc_get_version());
		// SwingUtilities.invokeLater(() -> {
		// String mrl = "G:\\video\\無彩限のファントム·ワールド 02.mp4";
		// File fileMrl = new File("E:\\AnimeShotSIte\\anime\\M\\命运之夜\\UBW\\[Kamigami] Fate stay night UBW - 03 [1080p x265 Ma10p FLAC Sub(Eng,Jap)].mkv");
		// File fileMrl = new File("E:\\AnimeShotSIte\\anime\\C\\超时空要塞\\Δ\\[dmhy][Macross_Delta][18][x264_aac][GB_BIG5][1080P_mkv].mkv");
		File fileMrl = new File("E:\\AnimeShotSIte\\anime\\M\\命运之夜\\UBW\\[Kamigami] Fate stay night UBW - 03 [1080p x265 Ma10p FLAC Sub(Eng,Jap)].mkv");
		// MediaInfo mediaInfo = MediaInfo.mediaInfo(fileMrl.getAbsolutePath());
		// logger.info(mediaInfo.toString());

		// MediaLoader mediaLoader = new MediaLoader(fileMrl.getAbsolutePath(), 640,480);
		MediaLoader mediaLoader = null;
		try {
			mediaLoader = new MediaLoader(fileMrl.getAbsolutePath(), 1920, 1080);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// mediaLoader.setPlaySpeed(0.125f);
		mediaLoader.start();
		// });
	}

	public void start() {
		logger.info("接受到开始命令，向播放器发出开始命令");
		mediaPlayer.submit(() -> mediaPlayer.controls().start());
		logger.info("开始成功");
	}

	public boolean checkRgbChanged() {
		return checkRgbChanged(preRgb, rgbBufferRef, -1);
	}

	/**
	 * 检查图像是否改变了
	 *
	 * @param checkStep 像素点检查间隔
	 */
	private boolean checkRgbChanged(int[] cacheRgb, int[] realRgb, int checkStep) {
		if (checkStep < 1) {
			checkStep = 11;
		}
		boolean changedFrontFlg = false;

		for (int i = 0; i < cacheRgb.length; i = i + checkStep) {
			if (cacheRgb[i] != realRgb[i]) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					logger.warn(e.toString());
				}
				changedFrontFlg = true;
				break;
			}
		}

		logger.info("图像比较的checkStep：{}, 结果：{}", checkStep, changedFrontFlg);
		return changedFrontFlg;
	}

	public void dispose() {
		// mediaPlayer.submit(() -> {
		frame.dispose();
		isVideoLoaded = false;
		// });
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public long getPastTime() {
		return xWaitChange.getPastTime();
	}

	public long getTime() {
		return mediaPlayer.status().time();
	}

	public void setTime(long time) {
		// mediaPlayer.submit(() -> {
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
		nowSettingTime = time;
		mediaPlayer.controls().setTime(time);

		// 重置参数
		xWaitChange.resetCompareValue(time);
		isOnDisplayTimeoutFlg = false;

		logger.info("setTime end: " + time);
		// });
	}

	public long getTotalTime() {
		return totalTime;
	}

	/**
	 * 初始认为非暂停状态，所以应该在视频载入后才能判断该值
	 */
	public boolean isDoPauseAction() {
		return isDoPauseActionFlg;
	}

	public boolean isOnDisplayTimeoutFlg() {
		return isOnDisplayTimeoutFlg;
	}

	public boolean isPlaying() {
		return mediaPlayer.status().isPlaying();
	}

	public boolean isRefreshedAfterChangeTime(long time) {
		// return xWaitChange.isChanged(time) || isOnDisplayTimeoutFlg;
		// return xWaitChange.isChanged(time) || rgbBufferChangedFlg;
		// return rgbBufferChangedFlg || isOnDisplayTimeoutFlg;

		checkRgbChangedAfterChangedOrWaitTime(mediaPlayer.status().time(), rgbBufferRef);
		return rgbBufferChangedFlg;
	}

	private void checkRgbChangedAfterChangedOrWaitTime(long mediaTime, int[] mediaRgbBufferRef) {
		if (mediaRgbBufferRef == null) {
			// 还未生成任何图像
			return;
		}

		// 检查图像改变的抽样
		int checkStep = 999;
		if (xWaitChange.getPastTime() > BEFORE_ON_DISPLAY_TIMEOUT_VALUE) {
			// 超时，则检查所有图像上的点
			checkStep = 1;
		} else if (xWaitChange.isChanged(mediaTime)) {
			// 播放器返回的时间改变了，PS：最新版返回的时间不会再改变，因此该条将无效
			checkStep = 13;
		} else if (checkRate.isTimeout()) {
			// 由于xWaitChange.isChanged(mediaTime)可能无效，因此改为每秒check一次
			checkStep = 100;
			checkRate.setTimeout(checkRate.getTimeout() + 500);
		}

		if (checkStep < 999) {
			checkRate.resetNowtime();
			checkRate.setTimeout(200);
			if (!rgbBufferChangedFlg) {
				boolean changedFlg = false;
				// 如果图片没有改变过， 则先将当前图像进行复制
				if (preRgb == null || preRgb.length != mediaRgbBufferRef.length) {
					preRgb = new int[mediaRgbBufferRef.length];
					changedFlg = true;
				} else {
					// 还未超时，只判断图像的一部分，超时后，判断完整图像
					changedFlg = checkRgbChanged(preRgb, mediaRgbBufferRef, checkStep);
					if (nowSettingTime <= 5000) {
						// 如果设定的播放时间小于5秒，可能图像还处于黑色状态，则直接返回改变信息
						logger.info("设定的播放时间小于5秒，可能图像还处于黑色状态，则直接返回改变信息");
						changedFlg = true;
					}
				}

				if (changedFlg) {
					System.arraycopy(mediaRgbBufferRef, 0, preRgb, 0, mediaRgbBufferRef.length);
					logger.info("图像已改变, setTime:{}, mediaTime:{}", nowSettingTime, mediaTime);
					rgbBufferChangedFlg = true;
				}
			}
		}
	}

	public boolean isStopped() {
		return isStoppedFlg;
	}

	public boolean isVideoLoaded() {
		return isVideoLoaded;
	}

	/**
	 * 通过直接播放到指定时间<br>
	 * PS:超过一定时间自动暂停<br>
	 * PS:由于1080P播放速度问题，速度调整为1/4，因此超时时间也相应增加4倍<br>
	 *
	 * @param specialTime specialTime
	 * @throws InterruptedException InterruptedException
	 */
	public void playToSpecialTime(long specialTime) throws InterruptedException {
		long beforeSkipTime = mediaPlayer.status().time();
		long skipTime = specialTime - beforeSkipTime;
		logger.warn("开始调整时间到{}, 当前时间点{}, 需要调整时间{}", specialTime, beforeSkipTime, skipTime);
		if (skipTime < 100) {
			logger.warn("指定时间小于当前视频时间，无法调整，必须大于100毫秒才能执行。", skipTime);
			return;
		}

		// 关闭字幕，播放速度调整为1/4
		closeSubtitle();

		mediaPlayer.controls().start();
		XWaitTime playWaitTime = new XWaitTime(skipTime * 8 + 2500);
		while (true) {
			if (specialTime - mediaPlayer.status().time() < 100) {
				logger.info("达到时间，停止", skipTime);
				mediaPlayer.controls().pause();
				Thread.sleep(2000);
				break;
			}
			if (playWaitTime.isTimeout()) {
				logger.warn("超时直接停止", skipTime);
				mediaPlayer.controls().pause();
				Thread.sleep(2000);
				break;
			}
			Thread.sleep(50);
		}
	}

	/**
	 * 关闭字幕
	 */
	public void closeSubtitle() {
		// TODO mediaLoader.setSpu(-1);
	}

	public void release() {
		// mediaPlayerComponent.release(true); // TODO
		// mediaPlayerComponent.release();
		// mediaPlayer.submit(() -> {
		mediaPlayer.release();
		factory.release();
		isVideoLoaded = false;
		// });
	}

	public File saveImage(File toFile) {
		long time = mediaPlayer.status().time();
		logger.debug("saveImage time: {}", time);
		return CImage.saveImage(bufferedImage, toFile);
	}

	/**
	 * 设置播放速度
	 */
	public void setPlaySpeed(float speed) {
		mediaPlayer.controls().setRate(speed);
	}

	public void setShowVideoImageFlg(boolean showVideoImageFlg) {
		this.showVideoImageFlg = showVideoImageFlg;
	}

	public void skip(long skipTime) {
		long time = mediaPlayer.status().time() + skipTime;
		setTime(time);
	}

	public void stop() {
		mediaPlayer.controls().stop();
	}

	/**
	 * 更新比较用rgb数组
	 */
	public void updateRgbToPre() {
		logger.info("updateRgbToPre(), 重新设置了比较rgb数组");
		System.arraycopy(rgbBufferRef, 0, preRgb, 0, rgbBufferRef.length);
	}

	/**
	 *
	 */
	private class TutorialRenderCallbackAdapter extends RenderCallbackAdapter {

		private TutorialRenderCallbackAdapter() {
			super(new int[width * height]);
		}

		// @Override
		// public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
		// super.display(mediaPlayer, nativeBuffers, bufferFormat);
		//
		// ++display;
		// }

		@Override
		protected void onDisplay(MediaPlayer mediaPlayer, int[] rgbBuffer) {
			// Simply copy buffer to the image and repaint
			++onDisplay;
			isVideoLoaded = true;
			// logger.info("onDisplay" + onDisplay);

			long nowTime = mediaPlayer.status().time();

			if (校准真实时间 == 0) {
				校准真实时间 = new Date().getTime();
				校准视频时间 = nowTime;
			} else {
				long diffRealTime = new Date().getTime() - 校准真实时间;
				long diffVideoTime = nowTime - 校准视频时间;
				if (diffRealTime - diffVideoTime > 500 || diffRealTime - diffVideoTime < -500) {

					logger.debug("dif真实时间 :{}, dif视频时间{}: ", diffRealTime, diffVideoTime);

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

			checkRgbChangedAfterChangedOrWaitTime(mediaPlayer.status().time(), rgbBufferRef);

			if (surfaceRepeatWaitTime.isTimeoutAndResetStartTime()) {
				logger.info("onDisplay nowTime: " + nowTime);
				videoSurface.repaint();
			}
			// videoSurface.repaint();
		}
	}

	/**
	 *
	 */
	private class TutorialRenderCallbackAdapter2 implements RenderCallback {

		@Override
		public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
			ByteBuffer bb = nativeBuffers[0];
			IntBuffer ib = bb.asIntBuffer();
			ib.get(rgbBuffer);

			/* RGB to GRAYScale conversion example */
			for (int i = 0; i < rgbBuffer.length; i++) {
				int argb = rgbBuffer[i];
				int b = (argb & 0xFF);
				int g = ((argb >> 8) & 0xFF);
				int r = ((argb >> 16) & 0xFF);
				int grey = (r + g + b + g) >> 2; // performance optimized - not real grey!
				rgbBuffer[i] = (grey << 16) + (grey << 8) + grey;
			}

			bufferedImage.setRGB(0, 0, width, height, rgbBuffer, 0, width);
			videoSurface.repaint();
		}
	}

	/**
	 *
	 */
	private class VideoSurfacePanel extends JPanel {

		Graphics2D g2D;
		private int lineHeight = 0;

		private VideoSurfacePanel() {
			setBackground(Color.black);
			setOpaque(true);
			setPreferredSize(new Dimension(width, height));
			setMinimumSize(new Dimension(width, height));
			setMaximumSize(new Dimension(width, height));
		}

		@Override
		public void paint(Graphics g) {
			++paintComponent;
			// logger.info("paintComponent" + paintComponent);

			// 画图片
			g2D = (Graphics2D) g;
			if (showVideoImageFlg) {
				g2D.drawImage(bufferedImage, null, 0, 0);
			}

			// 输出信息
			g2D.fillRect(0, 0, 250, 300);
			g2D.setColor(Color.white);
			lineHeight = 10;
			if (factory != null) {
				drawStringLine("vlcVersion: " + LibVlc.libvlc_get_version());
				drawStringLine("vlcCompiler: " + LibVlc.libvlc_get_compiler());
				drawStringLine("jnaLibraryPath: " + Info.getInstance().jnaLibraryPath());
				drawStringLine("vlcjVersion: " + Info.getInstance().vlcjVersion());
			}
			drawStringLine("totalTime: " + totalTime);
			drawStringLine("宽高: " + width + "," + height);
			drawStringLine("startTime: " + startTime);
			drawStringLine("nowTime: " + new Date());

			drawStringLine("paintComponent: " + paintComponent);
			drawStringLine("onDisplay: " + onDisplay);
			drawStringLine("display: " + display);
			drawStringLine("nowSettingTime: " + nowSettingTime);
			drawStringLine("getMediaPlayer().status().time(): " + mediaPlayer.status().time());
			drawStringLine("preTime: " + preTime);
			drawStringLine("timeChanged: " + timeChanged);
			drawStringLine("xWaitChange.pastTime : " + xWaitChange.getPastTime());
			drawStringLine("isOnDisplayTimeoutFlg: " + isOnDisplayTimeoutFlg);
			// drawStringLine("isRefreshedAfterChangeTime: " + isRefreshedAfterChangeTime(mediaPlayer.status().time()));
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
			drawStringLine("mediaPlayerComponentDisplay: " + mediaPlayerComponentDisplay);

			drawStringLine("isStoppedFlg: " + isStoppedFlg);
			drawStringLine("isDoPauseActionFlg: " + isDoPauseActionFlg);
			drawStringLine("showVideoImageFlg: " + showVideoImageFlg);

			// try {
			// // if (!ImageIO.write(image, "jpg", new File("D:\\work\\temp\\"
			// // + paintComponent + ".jpg"))) {
			// // logger.info(paintComponent + ".jpg");
			// // }
			// } catch (Exception e) {
			// logger.info(paintComponent + ".jpg");
			// e.printStackTrace();
			// }
		}

		private void drawStringLine(String value) {
			g2D.drawString(value, 20, lineHeight);
			lineHeight += 10;
		}

	}

}
