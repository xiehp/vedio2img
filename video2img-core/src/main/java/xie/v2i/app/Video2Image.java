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

	Video2ImageProperties config = new Video2ImageProperties();

	private boolean isProcessing = false;

	private boolean isProcessSuccess = false;

	/**
	 * @param mrl G:\\video\\無彩限のファントム·ワールド 02.mp4
	 */
	public Video2Image(String mrl) {
		this.mrl = mrl;
	}

	/**
	 * @param mrl G:\\video\\無彩限のファントム·ワールド 02.mp4
	 */
	public Video2Image(String mrl, Video2ImageListener video2ImageListener) {
		this.mrl = mrl;
		this.listener = video2ImageListener;
	}

	public Video2ImageProperties getVideo2ImageProperties() {
		return config;
	}

	public void setVideo2ImageProperties(Video2ImageProperties video2ImageProperties) {
		this.config = video2ImageProperties;
	}

	public String getRunMode() {
		return config.runMode;
	}

	public void setRunMode(String runMode) {
		config.runMode = runMode;
	}

	public void setSize(int width, int height) {
		config.width = width;
		config.height = height;
	}

	public long getStartTime() {
		return config.startTime;
	}

	public void setStartTime(long startTime) {
		config.startTime = startTime;
	}

	public long getEndTime() {
		return config.endTime;
	}

	public void setEndTime(long endTime) {
		config.endTime = endTime;
	}

	public long getTimeInterval() {
		return config.timeInterval;
	}

	public void setTimeInterval(long timeInterval) {
		config.timeInterval = timeInterval;
	}

	public long[] getSpecifyTimes() {
		return config.specifyTimes;
	}

	public void setSpecifyTimes(long[] specifyTimes) {
		config.specifyTimes = specifyTimes;
	}

	public void run() {

		isProcessing = true;
		isProcessSuccess = false;

		BasicConfigurator.configure();
		new NativeDiscovery().discover();

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					meidaLoador = new MeidaLoador(mrl, config.width, config.height);

					Thread thread = new Thread(new Runnable() {
						public void run() {

							try {
								while (!meidaLoador.isVideoLoaded()) {
									Thread.sleep(100);
								}
								Thread.sleep(2000);
								meidaLoador.pause();
								logger.debug("do meidaLoador.pause()");

								XWaitTime xWaitTime = new XWaitTime(20000);
								while (!meidaLoador.isDoPauseAction()) {
									if (xWaitTime.isTimeout()) {
										break;
									}

									Thread.sleep(100);
								}

								long time = config.startTime;
								if (Video2ImageProperties.RUN_MODE_INTERVAL.equals(config.runMode)) {
									time = config.startTime;
								} else if (Video2ImageProperties.RUN_MODE_SPECIAL.equals(config.runMode)) {
									// 指定第一个
									time = config.specifyTimes[0];
								} else {
									time = config.startTime;
								}

								int totalTimeCount = 0;
								int runCount = 0;

								// 先随便指定一个时间
								meidaLoador.setTime(2600);
								Thread.sleep(500);
								while (true) {
									if (meidaLoador.isRefreshedAfterChangeTime(meidaLoador.getTime())) {
										break;
									}
									Thread.sleep(100);
								}
								Thread.sleep(500);

								// 开始截图
								while (true) {
									// 设置截图时间
									meidaLoador.setTime(time);

									// 进行截图
									while (true) {
										if (meidaLoador.isRefreshedAfterChangeTime(meidaLoador.getTime())) {
											// meidaLoador.saveImage();
											if (listener != null) {
												listener.setTotalTime(meidaLoador.getTotalTime());
												try {
													listener.isRefreshedAfterChangeTime(time, meidaLoador.getTime(), meidaLoador.getBufferedImage());
												} catch (Exception e) {
													throw e;
												}
											} else {
												// 测试用
												Thread.sleep(2000);
											}
											break;
										}
										Thread.sleep(100);
									}

									if (Video2ImageProperties.RUN_MODE_SPECIAL.equals(config.runMode)) {
										// 判断是否是最后一个指定时间
										if (config.specifyTimes.length <= runCount + 1) {
											// 截图循环结束
											break;
										}

										// 指定下一个
										time = config.specifyTimes[runCount + 1];
									} else if (Video2ImageProperties.RUN_MODE_INTERVAL.equals(config.runMode)) {
										// 下一个时间
										time += config.timeInterval;

										if (config.endTime > 0) {
											// 判断下一个时间是否超出结束时间
											if (time > config.endTime) {
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
								e.printStackTrace();
								closeMediaLoader();
							} catch (Exception e) {
								e.printStackTrace();
								closeMediaLoader();
							}
						}
					});

					thread.start();

				} catch (Exception e) {
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
		logger.info("closeMediaLoader");
		meidaLoador.release();
		meidaLoador.dispose();
		isProcessing = false;
	}

	public boolean isClosed() {
		return !isProcessing;
	}
}
