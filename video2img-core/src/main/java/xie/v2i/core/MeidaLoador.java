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
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcjinfo.MediaInfo;
import xie.v2i.utils.CImage;
import xie.v2i.utils.XWaitChange;

public class MeidaLoador {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final int width = 1920;

	private static final int height = 1080;

	private final JFrame frame;

	private final JPanel videoSurface;

	private final BufferedImage image;

	private int paintComponent = 0;
	private int onDisplay = 0;

	private boolean showAnimeFlg = true;

	private long preTime = 0;

	private long 校准真实时间 = 0;
	private long 校准视频时间 = 0;

	private final DirectMediaPlayerComponent mediaPlayerComponent;

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
			g2.fillRect(0, 0, 40, 40);
			g2.setColor(Color.white);
			g2.drawString(paintComponent + "", 10, 10);
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
			// System.out.println("onDisplay" + onDisplay);

			long nowTime = mediaPlayerComponent.getMediaPlayer().getTime();

			if (校准真实时间 == 0) {
				校准真实时间 = new Date().getTime();
				校准视频时间 = nowTime;
			} else {
				long dif真实时间 = new Date().getTime() - 校准真实时间;
				long dif视频时间 = nowTime - 校准视频时间;
				if (dif真实时间 - dif视频时间 > 500 || dif真实时间 - dif视频时间 < -500) {

					logger.info("dif真实时间 :{}, dif视频时间{}: ", dif真实时间, dif视频时间);

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

	public void saveImage() {
		long time = mediaPlayerComponent.getMediaPlayer().getTime();
		logger.debug("saveImage time: {}", time);
		CImage.saveImage(image, time, new File("D:\\work\\temp\\bbb"));
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

	public boolean isRefreshedAfterChangeTime() {
		return xWaitChange.isChanged();
	}

	public void setShowVideo(boolean showAnimeFlg) {
		this.showAnimeFlg = showAnimeFlg;
	}
}
