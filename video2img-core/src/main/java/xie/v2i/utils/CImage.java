package xie.v2i.utils;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CImage {
	private final static Logger logger = LoggerFactory.getLogger(CImage.class);
	public static void saveImage(BufferedImage image, long militime, File folder) {

		try {
			File file = new File(folder, militime + ".jpg");
			if (!ImageIO.write(image, "jpg", file)) {
				//System.out.println(militime + ".jpg");
				logger.debug(file.getAbsolutePath());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(militime + ".jpg");
			e.printStackTrace();
		}
	}
}
