package study;

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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import xie.v2i.utils.CImage;

public class DirectRendering {
	private static final int width = 600;

	private static final int height = 400;

	private final JFrame frame;

	private final JPanel videoSurface;

	private final BufferedImage image;

	private int paintComponent = 0;
	private int onDisplay = 0;

	private final DirectMediaPlayerComponent mediaPlayerComponent;

	public static void main(final String[] args) {
		BasicConfigurator.configure();
		new NativeDiscovery().discover();
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				new DirectRendering(args);
			}
		});
	}

	public DirectRendering(String[] args) {
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
		//frame.setContentPane(videoSurface);
		frame.add(videoSurface, BorderLayout.CENTER);
		image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
				.createCompatibleImage(width, height);
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
		mediaPlayerComponent.getMediaPlayer().playMedia("G:\\video\\無彩限のファントム·ワールド 02.mp4");
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
			System.out.println("paintComponent" + paintComponent);

			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(image, null, 0, 0);
			g2.fillRect(0, 0, 40, 40);
			g2.setColor(Color.white);
			g2.drawString(paintComponent + "", 10, 10);
			g2.drawString(String.valueOf(mediaPlayerComponent.getMediaPlayer().getTime()), 10, 20);

			try {
//				if (!ImageIO.write(image, "jpg", new File("D:\\work\\temp\\" + paintComponent + ".jpg"))) {
//					System.out.println(paintComponent + ".jpg");
//				}
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
			System.out.println("onDisplay" + onDisplay);

			image.setRGB(0, 0, width, height, rgbBuffer, 0, width);
			videoSurface.repaint();

			try {
				//Thread.sleep(30);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
