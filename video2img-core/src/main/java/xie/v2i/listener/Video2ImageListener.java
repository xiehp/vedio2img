package xie.v2i.listener;

import java.awt.image.BufferedImage;

public interface Video2ImageListener {

	/**
	 * 在改变视频时间后，视频刷新好了
	 * 
	 * @param setTime 预计设置的时间，微妙
	 * @param originalTime 原始的时间，会和预计时间差几微妙，微妙
	 * @param image 截图
	 */
	void isRefreshedAfterChangeTime(long setTime, long originalTime, BufferedImage image);

	void setTotalTime(long totalTime);

}
