package xie.v2i.listener;

import java.awt.image.BufferedImage;

public interface Video2ImageListener {

	/**
	 * 在改变视频时间后，视频刷新好了
	 * 
	 * @param time 截图时间戳，微妙
	 * @param image 截图
	 */
	void isRefreshedAfterChangeTime(long time, BufferedImage image);

	void setTotalTime(long totalTime);

}
