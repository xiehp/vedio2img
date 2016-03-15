package xie.v2i.utils;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CImage {
	private final static Logger logger = LoggerFactory.getLogger(CImage.class);

	public static File getFilePath(long militime, File folder) {
		File file = new File(folder, militime + ".jpg");
		return file;
	}

	public static File saveImage(BufferedImage image, long militime, File folder) {
		File filePath = getFilePath(militime, folder);

		return saveImage(image, filePath);
	}

	public static File saveImage(BufferedImage image, File filePath) {
		try {
			if (!filePath.getParentFile().exists()) {
				filePath.getParentFile().mkdirs();
			}

			if (!ImageIO.write(image, "jpg", filePath)) {
				// System.out.println(militime + ".jpg");
				logger.debug(filePath.getAbsolutePath());
				filePath = null;
			}

			return filePath;
		} catch (Exception e) {
			logger.error(filePath.getAbsolutePath());
			e.printStackTrace();
		}

		return null;
	}
}
