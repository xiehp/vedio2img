package study;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.*;

import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class StudyPlayer1 {

	private final JFrame frame;

	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

	private final JTextField seedInputField;
	private final JButton seedInputButton;

	private final JButton pauseButton;

	private final JButton rewindButton;

	private final JButton skipButton;

	public static void main(final String[] args) {
		new NativeDiscovery().discover();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new StudyPlayer1("G:\\video\\無彩限のファントム·ワールド 02.mp4");
			}
		});
	}

	public StudyPlayer1(String args) {
		frame = new JFrame("My First Media Player");
		frame.setBounds(100, 100, 600, 400);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println(e);
				mediaPlayerComponent.release();
				System.exit(0);
			}
		});

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());

		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);

		JPanel controlsPane = new JPanel();
		seedInputField = new JTextField();
		seedInputField.setBounds(0, 0, 200, 20);
		seedInputField.setText("1000000");
		controlsPane.add(seedInputField);
		seedInputButton = new JButton("Seek");
		controlsPane.add(seedInputButton);
		pauseButton = new JButton("Pause");
		controlsPane.add(pauseButton);
		rewindButton = new JButton("Rewind");
		controlsPane.add(rewindButton);
		skipButton = new JButton("Skip");
		controlsPane.add(skipButton);
		contentPane.add(controlsPane, BorderLayout.SOUTH);

		seedInputButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						String seed = seedInputField.getText();
						if (seed == null || "".equals(seed)) {
							return;
						}

						if (mediaPlayerComponent.getMediaPlayer().isSeekable()) {
							mediaPlayerComponent.getMediaPlayer().skip(Integer.valueOf(seed));

							long time = mediaPlayerComponent.getMediaPlayer().getTime();
							System.out.println(time);
							System.out.println(mediaPlayerComponent.getMediaPlayer().getPosition());

							//BufferedImage image = mediaPlayerComponent.getMediaPlayer().getSnapshot();
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mediaPlayerComponent.getMediaPlayer().saveSnapshot(new File("D:\\work\\temp\\StudyPlayer1\\", time + ".jpg"));
							//CImage.saveImage(image, time, new File("D:\\work\\temp\\StudyPlayer1"));
						} else {
							System.out.println("不能Seek");
						}
					}
				});
			}
		});
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().pause();
			}
		});

		rewindButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(-10000);
				System.out.println(mediaPlayerComponent.getMediaPlayer().getTime());
				System.out.println(mediaPlayerComponent.getMediaPlayer().getPosition());
			}
		});

		skipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(10000);
				System.out.println(mediaPlayerComponent.getMediaPlayer().getTime());
				System.out.println(mediaPlayerComponent.getMediaPlayer().getPosition());
			}
		});

		mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void playing(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.setTitle(String.format("My First Media Player - %s",
								mediaPlayerComponent.getMediaPlayer().getMediaMeta().getTitle()));
					}
				});
			}

			@Override
			public void finished(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						closeWindow();
					}
				});
			}

			@Override
			public void error(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(frame, "Failed to play media", "Error",
								JOptionPane.ERROR_MESSAGE);
						closeWindow();
					}
				});
			}

			@Override
			public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
				System.out.println(newTime);
			}
		});

		frame.setContentPane(contentPane);
		frame.setVisible(true);

		mediaPlayerComponent.getMediaPlayer().playMedia(args);
	}

	private void closeWindow() {
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}
}
