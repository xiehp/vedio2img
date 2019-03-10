package study;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class BasicEvents {

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
			public void run() {
				new BasicEvents("G:\\video\\無彩限のファントム·ワールド 02.mp4");
			}
		});
	}

	public BasicEvents(String args) {
		frame = new JFrame("My First Media Player");
		frame.setBounds(100, 100, 600, 400);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			
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
			
			public void actionPerformed(ActionEvent e) {
				String seed = seedInputField.getText();
				if (mediaPlayerComponent.getMediaPlayer().isSeekable()) {
					mediaPlayerComponent.getMediaPlayer().skip(Integer.valueOf(seed));
				} else {
					System.out.println("不能Seek");
				}
			}
		});
		pauseButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().pause();
			}
		});

		rewindButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(-10000);
			}
		});

		skipButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(10000);
			}
		});

		mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			
			public void playing(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(new Runnable() {
					
					public void run() {
						frame.setTitle(String.format("My First Media Player - %s",
								mediaPlayerComponent.getMediaPlayer().getMediaMeta().getTitle()));
					}
				});
			}

			
			public void finished(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(new Runnable() {
					
					public void run() {
						closeWindow();
					}
				});
			}

			
			public void error(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(new Runnable() {
					
					public void run() {
						JOptionPane.showMessageDialog(frame, "Failed to play media", "Error",
								JOptionPane.ERROR_MESSAGE);
						closeWindow();
					}
				});
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
