package xie.v2i.listener;

import java.awt.image.BufferedImage;

public interface Video2ImageListener {

	/**
	 * 在改变视频时间后，视频刷新好了。<br>
	 * 执行需要的业务逻辑。<br>
	 * 
	 * @param setTime 预计设置的时间，微妙
	 * @param originalTime 原始的时间，会和预计时间差几微妙，微妙
	 * @param image 截图
	 */
	void isRefreshedAfterChangeTime(long setTime, long originalTime, BufferedImage image);

	/**
	 * 设置视频总时间
	 */
	void setTotalTime(long totalTime);

	/**
	 * 遇到异常时，是否可以正常退出
	 * 
	 * @param timeInterval 截图定时间隔
	 */
	boolean canSuccessExit(long timeInterval);

}
