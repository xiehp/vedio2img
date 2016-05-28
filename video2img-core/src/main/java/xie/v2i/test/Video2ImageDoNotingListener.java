package xie.v2i.test;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xie.v2i.listener.Video2ImageAdapter;

public class Video2ImageDoNotingListener extends Video2ImageAdapter {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	int waitTime;

	public Video2ImageDoNotingListener(int waitTime) {
		this.waitTime = waitTime;
	}

	@Override
	public void isRefreshedAfterChangeTime(long setTime, long originalTime, BufferedImage image) {
		logger.info("isRefreshedAfterChangeTime, setTime:" + setTime + ", originalTime" + originalTime + ", image " + image);
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
