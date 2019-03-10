package study;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.*;

import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.media.callback.CallbackMedia;
import uk.co.caprica.vlcj.media.callback.nonseekable.FileInputStreamMedia;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;



public class UsingJavaIO {

	private final JFrame frame;

	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

	private final JButton pauseButton;

	private final JButton rewindButton;

	private final JButton skipButton;

	public static void main(final String[] args) {
		new NativeDiscovery().discover();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new UsingJavaIO("G:\\video\\無彩限のファントム·ワールド 02.mp4");
			}
		});
	}

	public UsingJavaIO(String args) {
		
		
		
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
		pauseButton = new JButton("Pause");
		controlsPane.add(pauseButton);
		rewindButton = new JButton("Rewind");
		controlsPane.add(rewindButton);
		skipButton = new JButton("Skip");
		controlsPane.add(skipButton);
		contentPane.add(controlsPane, BorderLayout.SOUTH);

		pauseButton.addActionListener(e -> mediaPlayerComponent.getMediaPlayer().pause());

		rewindButton.addActionListener(e -> mediaPlayerComponent.getMediaPlayer().skip(-10000));

		skipButton.addActionListener(e -> mediaPlayerComponent.getMediaPlayer().skip(10000));

		mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void playing(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> frame.setTitle(String.format("My First Media Player - %s",
						mediaPlayerComponent.getMediaPlayer().getMediaMeta().getTitle())));
			}

			@Override
			public void finished(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> closeWindow());
			}

			@Override
			public void error(MediaPlayer mediaPlayer) {
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(frame, "Failed to play media", "Error",
							JOptionPane.ERROR_MESSAGE);
					closeWindow();
				});
			}
		});

		frame.setContentPane(contentPane);
		frame.setVisible(true);

//		mediaPlayerComponent.getMediaPlayer().playMedia(args);
		
//		Media media = new RandomAccessFileMedia(new File(args));
//		mediaPlayerComponent.getMediaPlayer().playMedia(media);

		CallbackMedia media = new FileInputStreamMedia(new File(args));
		mediaPlayerComponent.getMediaPlayer().playMedia(media);

//		Media media = new SimpleMedia(args);
//		mediaPlayerComponent.getMediaPlayer().playMedia(media);
	}

	private void closeWindow() {
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}
}
