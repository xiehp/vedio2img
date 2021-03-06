package xie.v2i.listener;

import java.awt.image.BufferedImage;

public abstract class Video2ImageAdapter implements Video2ImageListener {
	private long totalTime;

	@Override
	public void isRefreshedAfterChangeTime(long setTime, long originalTime, BufferedImage image) {
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onSuccessComplete() {

	}

	@Override
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public long getTotalTime() {
		return totalTime;
	}

	@Override
	public boolean canSuccessExit(long timeInterval) {
		return false;
	}
}
