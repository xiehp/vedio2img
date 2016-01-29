package xie.v2i.app;

import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import xie.v2i.core.MeidaLoador;

public class Video2Image {
	static Logger logger = LoggerFactory.getLogger(Video2Image.class);

	public static void main(String[] args) {

		BasicConfigurator.configure();
		new NativeDiscovery().discover();

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					MeidaLoador meidaLoador = new MeidaLoador("G:\\video\\無彩限のファントム·ワールド 02.mp4");

					Thread thread = new Thread(new Runnable() {
						public void run() {

							try {
								Thread.sleep(5000);
								meidaLoador.pause();
								logger.debug("do meidaLoador.pause()");

								Thread.sleep(2000);
								long time = 700000;
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});

					// while (true) {
					// meidaLoador.setTime(time);
					// while (true) {
					// if (meidaLoador.isRefreshedAfterChangeTime()) {
					// meidaLoador.saveImage();
					// break;
					// }
					// Thread.sleep(100);
					// }
					// time += 2000;
					// }

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

}
