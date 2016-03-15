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

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcjinfo.MediaInfo;
import xie.common.utils.XWaitChange;
import xie.v2i.utils.CImage;

public class MeidaLoador {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private JFrame frame;

	private JPanel videoSurface;

	private BufferedImage image;

	private DirectMediaPlayerComponent mediaPlayerComponent;

	private int width = 1280;

	private int height = 720;

	private long totalTime = 0;

	private int paintComponent = 0;
	private int onDisplay = 0;

	private boolean showAnimeFlg = true;

	private long preTime = 0;

	private long 校准真实时间 = 0;
	private long 校准视频时间 = 0;

	/** 视频是否已经载入 */
	private boolean isVideoLoaded = false;
	/** 视频是否已经停止播放 (初始认为非停止状态，所以应该在视频载入后才能判断该值) */
	private boolean isDoStopActionFlg = false;;

	private XWaitChange xWaitChange = new XWaitChange(0, 5000);

	public static void main(final String[] args) {
		BasicConfigurator.configure();
		new NativeDiscovery().discover();
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				String mrl = "G:\\video\\無彩限のファントム·ワールド 02.mp4";

				MediaInfo mediaInfo = MediaInfo.mediaInfo(mrl);
				System.out.println(mediaInfo.toString());

				new MeidaLoador(mrl);
			}
		});
	}

	public MeidaLoador(String mrl) {
		init(mrl, 1280, 720);
	}

	public MeidaLoador(String mrl, int width, int height) {
		init(mrl, width, height);
	}

	public void init(String mrl, int width, int height) {
		this.width = width;
		this.height = height;

		frame = new JFrame("Direct Media Player");
		frame.setBounds(100, 100, width, height);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		JPanel controlsPane = new JPanel();
		JTextField seedInputField = new JTextField();
		seedInputField.setBounds(0, 0, 200, 20);
		seedInputField.setText("1000000");
		controlsPane.add(seedInputField);
		JButton seedInputButton = new JButton("Seek");
		controlsPane.add(seedInputButton);
		JButton pauseButton = new JButton("Pause");
		controlsPane.add(pauseButton);
		JButton rewindButton = new JButton("Rewind");
		controlsPane.add(rewindButton);
		JButton skipButton = new JButton("Skip");
		controlsPane.add(skipButton);
		JButton savePicButton = new JButton("save pic");
		controlsPane.add(savePicButton);
		frame.add(controlsPane, BorderLayout.SOUTH);

		pauseButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().pause();
			}
		});

		rewindButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(-10000);
				System.out.println(mediaPlayerComponent.getMediaPlayer().getTime());
				System.out.println(mediaPlayerComponent.getMediaPlayer().getPosition());
			}
		});

		skipButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(10000);
				System.out.println(mediaPlayerComponent.getMediaPlayer().getTime());
				System.out.println(mediaPlayerComponent.getMediaPlayer().getPosition());
			}
		});

		savePicButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CImage.saveImage(image, mediaPlayerComponent.getMediaPlayer().getTime(), new File("D:\\work\\temp\\bbb"));
			}
		});
		videoSurface = new VideoSurfacePanel();
		// frame.setContentPane(videoSurface);
		frame.add(videoSurface, BorderLayout.CENTER);
		image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
		BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {

			public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
				return new RV32BufferFormat(width, height);
			}
		};

		mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {

			protected RenderCallback onGetRenderCallback() {
				return new TutorialRenderCallbackAdapter();
			}

			@Override
			public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						totalTime = newDuration;
					}
				});
			}

			@Override
			public void stopped(MediaPlayer mediaPlayer) {
				super.stopped(mediaPlayer);

				isDoStopActionFlg = true;
			}

			@Override
			public void paused(MediaPlayer mediaPlayer) {
				super.paused(mediaPlayer);
			}

			@Override
			public void playing(MediaPlayer mediaPlayer) {
				super.playing(mediaPlayer);
			}
		};

		frame.setVisible(true);
		mediaPlayerComponent.getMediaPlayer().playMedia(mrl);
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

		protected void paintComponent(Graphics g) {
			++paintComponent;
			// System.out.println("paintComponent" + paintComponent);

			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(image, null, 0, 0);
			g2.fillRect(0, 0, 200, 100);
			g2.setColor(Color.white);
			g2.drawString("paintComponent: " + paintComponent, 10, 10);
			g2.drawString("onDisplay: " + onDisplay, 10, 20);
			g2.drawString(String.valueOf(mediaPlayerComponent.getMediaPlayer().getTime()), 10, 20);

			try {
				// if (!ImageIO.write(image, "jpg", new File("D:\\work\\temp\\"
				// + paintComponent + ".jpg"))) {
				// System.out.println(paintComponent + ".jpg");
				// }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println(paintComponent + ".jpg");
				e.printStackTrace();
			}
		}
	}

	private class TutorialRenderCallbackAdapter extends RenderCallbackAdapter {

		private TutorialRenderCallbackAdapter() {
			super(new int[width * height]);
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
				// CImage.saveImage(image, mediaPlayerComponent.getMediaPlayer().getTime(), new File("D:\\work\\temp\\bbb"));
				preTime = nowTime;
				logger.info("onDisplay nowTime: " + nowTime);
			}

			xWaitChange.isChanged(nowTime);

			image.setRGB(0, 0, width, height, rgbBuffer, 0, width);

			if (showAnimeFlg) {
				videoSurface.repaint();
			}

			try {
				// Thread.sleep(30);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public File saveImage() {
		long time = mediaPlayerComponent.getMediaPlayer().getTime();
		logger.debug("saveImage time: {}", time);
		File file = CImage.saveImage(image, time, new File("D:\\work\\temp\\bbb"));
		return file;
	}

	public long getTime() {
		long time = mediaPlayerComponent.getMediaPlayer().getTime();
		return time;
	}

	public BufferedImage getBufferedImage() {
		return image;
	}

	public void pause() {
		mediaPlayerComponent.getMediaPlayer().pause();
	}

	public void setTime(long time) {
		mediaPlayerComponent.getMediaPlayer().setTime(time);

		xWaitChange.setCompareValue(time);
	}

	public void skip(long skipTime) {
		long time = mediaPlayerComponent.getMediaPlayer().getTime() + skipTime;
		setTime(time);
	}

	public boolean isRefreshedAfterChangeTime(long time) {
		return xWaitChange.isChanged(time);
	}

	public void setShowVideo(boolean showAnimeFlg) {
		this.showAnimeFlg = showAnimeFlg;
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

	/**
	 * 初始认为非停止状态，所以应该在视频载入后才能判断该值
	 * 
	 * @return
	 */
	public boolean isDoStopAction() {
		return isDoStopActionFlg;
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
}
