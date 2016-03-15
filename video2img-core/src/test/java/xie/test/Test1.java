package xie.test;

import java.io.File;

import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tietuku.entity.main.PostImage;

import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import xie.v2i.app.Video2Image;
import xie.v2i.core.MeidaLoador;

public class Test1 {
	public static void main(String[] args) {
		run();
	}
	
	static Logger logger = LoggerFactory.getLogger(Video2Image.class);
	
	public static void run() {

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
								while (true) {
									meidaLoador.setTime(time);
									while (true) {
										if (meidaLoador.isRefreshedAfterChangeTime(time)) {
											File file = meidaLoador.saveImage();
											
											String responseStr = PostImage.doUpload(file);
											System.out.println(responseStr);
											break;
										}
										Thread.sleep(100);
									}
									time += 2000;
								}

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					
					thread.start();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
