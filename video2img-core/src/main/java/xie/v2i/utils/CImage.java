package xie.v2i.utils;

import java.awt.image.BufferedImage;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xie.common.exception.XRuntimeException;
import xie.common.image.XImageUtils;

public class CImage {
	private final static Logger logger = LoggerFactory.getLogger(CImage.class);

	/**
	 * 获得带.jpg的完整文件名
	 * 
	 * @param fileName
	 * @param folder
	 * @return
	 */
	public static File getFilePath(Object fileName, File folder) {
		String imageFileName = fileName.toString();
		if (!imageFileName.toLowerCase().endsWith(".jpg")) {
			imageFileName = imageFileName + ".jpg";
		}

		imageFileName = imageFileName.replace("?", "？");
		File file = new File(folder, imageFileName);
		return file;
	}

	public static File saveImage(BufferedImage image, Object fileName, File folder) {
		File filePath = getFilePath(fileName, folder);

		return saveImage(image, filePath);
	}

	public static File saveImage(BufferedImage image, File filePath) {
		try {
			if (!filePath.getParentFile().exists()) {
				filePath.getParentFile().mkdirs();
			}
			XImageUtils.writeWithQuality(image, "jpeg", filePath, 0.9f);
			// if (!ImageIO.write(image, "jpeg", filePath)) {
			// logger.debug("文件保存失败, " + filePath.getAbsolutePath());
			// filePath = null;
			// }

			return filePath;
		} catch (Exception e) {
			logger.error("文件保存失败, " + filePath.getAbsolutePath());
			throw new XRuntimeException(e);
		}

	}
}
