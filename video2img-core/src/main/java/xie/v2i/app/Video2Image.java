package xie.v2i.app;

import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import xie.common.utils.XWaitTime;
import xie.v2i.config.Video2ImageProperties;
import xie.v2i.core.MeidaLoador;
import xie.v2i.listener.Video2ImageListener;

public class Video2Image {

	static Logger logger = LoggerFactory.getLogger(Video2Image.class);

	private MeidaLoador meidaLoador;

	/** 监听事件 */
	private Video2ImageListener listener;

	/** 视频MRL */
	private String mrl;

	// private int width = 1280;
	//
	// private int height = 720;
	//
	// /** 开始时间 */
	// private long startTime = 0;
	//
	// /** 时间间隔 */
	// private long timeInterval = 60000;
	//
	// /** 指定时间 */
	// private long[] specifyTimes;

	Video2ImageProperties video2ImageProperties = new Video2ImageProperties();

	private boolean isProcessing = false;

	private boolean isProcessSuccess = false;

	// mrl : G:\\video\\無彩限のファントム·ワールド 02.mp4

	public Video2Image(String mrl) {
		this.mrl = mrl;
	}

	public Video2Image(String mrl, Video2ImageListener video2ImageListener) {
		this.mrl = mrl;
		this.listener = video2ImageListener;
	}

	public Video2ImageProperties getVideo2ImageProperties() {
		return video2ImageProperties;
	}

	public void setVideo2ImageProperties(Video2ImageProperties video2ImageProperties) {
		this.video2ImageProperties = video2ImageProperties;
	}

	public String getRunMode() {
		return video2ImageProperties.runMode;
	}

	public void setRunMode(String runMode) {
		video2ImageProperties.runMode = runMode;
	}

	public void setSize(int width, int height) {
		video2ImageProperties.width = width;
		video2ImageProperties.height = height;
	}

	public long getStartTime() {
		return video2ImageProperties.startTime;
	}

	public void setStartTime(long startTime) {
		video2ImageProperties.startTime = startTime;
	}

	public long getEndTime() {
		return video2ImageProperties.endTime;
	}

	public void setEndTime(long endTime) {
		video2ImageProperties.endTime = endTime;
	}

	public long getTimeInterval() {
		return video2ImageProperties.timeInterval;
	}

	public void setTimeInterval(long timeInterval) {
		video2ImageProperties.timeInterval = timeInterval;
	}

	public long[] getSpecifyTimes() {
		return video2ImageProperties.specifyTimes;
	}

	public void setSpecifyTimes(long[] specifyTimes) {
		video2ImageProperties.specifyTimes = specifyTimes;
	}

	public void run() {

		isProcessing = true;
		isProcessSuccess = false;

		BasicConfigurator.configure();
		new NativeDiscovery().discover();

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					meidaLoador = new MeidaLoador(mrl, video2ImageProperties.width, video2ImageProperties.height);

					Thread thread = new Thread(new Runnable() {
						public void run() {

							try {
								while (!meidaLoador.isVideoLoaded()) {
									Thread.sleep(100);
								}
								meidaLoador.pause();
								logger.debug("do meidaLoador.pause()");

								XWaitTime xWaitTime = new XWaitTime(3000);
								while (!meidaLoador.isDoStopAction()) {
									if (xWaitTime.isTimeout()) {
										break;
									}

									Thread.sleep(100);
								}

								long time = video2ImageProperties.startTime;
								if (Video2ImageProperties.RUN_MODE_INTERVAL.equals(video2ImageProperties.runMode)) {
									time = video2ImageProperties.startTime;
								} else if (Video2ImageProperties.RUN_MODE_SPECIAL.equals(video2ImageProperties.runMode)) {
									// 指定第一个
									time = video2ImageProperties.specifyTimes[0];
								} else {
									time = video2ImageProperties.startTime;
								}

								int totalTimeCount = 0;
								int runCount = 0;
								while (true) {
									// 设置截图时间
									meidaLoador.setTime(time);

									// 进行截图
									while (true) {
										if (meidaLoador.isRefreshedAfterChangeTime(time)) {
											// meidaLoador.saveImage();
											listener.setTotalTime(meidaLoador.getTotalTime());
											listener.isRefreshedAfterChangeTime(meidaLoador.getTime(), meidaLoador.getBufferedImage());
											break;
										}
										Thread.sleep(100);
									}

									if (Video2ImageProperties.RUN_MODE_SPECIAL.equals(video2ImageProperties.runMode)) {
										// 判断是否是最后一个指定时间
										if (video2ImageProperties.specifyTimes.length <= runCount + 1) {
											// 截图循环结束
											break;
										}

										// 指定下一个
										time = video2ImageProperties.specifyTimes[runCount + 1];
									} else if (Video2ImageProperties.RUN_MODE_INTERVAL.equals(video2ImageProperties.runMode)) {
										// 下一个时间
										time += video2ImageProperties.timeInterval;

										if (video2ImageProperties.endTime > 0) {
											// 判断下一个时间是否超出结束时间
											if (time > video2ImageProperties.endTime) {
												// 截图循环结束
												break;
											}
										}
									} else {
										break;
									}

									// 判断是否已经超出视频总时间
									long totalTime = meidaLoador.getTotalTime();
									if (totalTime > 0 && time > totalTime) {
										// 截图循环结束
										break;
									}

									runCount++;
								}

								isProcessSuccess = true;
								closeMediaLoader();

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								closeMediaLoader();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								closeMediaLoader();
							}
						}
					});

					thread.start();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					closeMediaLoader();
				}
			}
		});
	}

	public static void main(String[] args) {
		Video2Image video2Image = new Video2Image("G:\\video\\無彩限のファントム·ワールド 02.mp4");
		video2Image.run();
	}

	/**
	 * 是否正在处理
	 * 
	 * @return
	 */
	public boolean isProcessing() {
		return isProcessing || meidaLoador.isPlaying();
	}

	public boolean isProcessSuccess() {
		return isProcessSuccess;
	}

	public void closeMediaLoader() {
		// meidaLoador.stop();
		meidaLoador.release();
		meidaLoador.dispose();
		isProcessing = false;
	}

	public boolean isClosed() {
		return !isProcessing;
	}
}
