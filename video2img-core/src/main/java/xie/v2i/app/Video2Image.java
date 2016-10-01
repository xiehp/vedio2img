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

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
								logger.info("视频截图处理程序开始执行");

								while (!meidaLoador.isVideoLoaded()) {
									logger.info("视频已载入。");
									Thread.sleep(100);
								}
								Thread.sleep(2000);
								

								logger.info("开始处理前暂停视频");
								meidaLoador.pause();
								logger.info("已执行 meidaLoador.pause()");

								XWaitTime xWaitTime = new XWaitTime(20000);
								while (!meidaLoador.isDoPauseAction()) {
									if (xWaitTime.isTimeout()) {
										logger.error("载入后暂停视频超时");
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
								logger.info("开始处理前随便执行一个时间");
								meidaLoador.setTime(1000);
								Thread.sleep(500);
								while (true) {
									if (meidaLoador.isRefreshedAfterChangeTime(meidaLoador.getTime())) {
										break;
									}
									if (meidaLoador.isOnDisplayTimeoutFlg()) {
										logger.error("视频初始化设置时间超时，继续开始截图。");
										Thread.sleep(500);
										break;
									}
									Thread.sleep(100);
								}

								logger.info("开始截图循环处理");
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

										if (meidaLoador.isStoped()) {
											logger.warn("meidaLoador 发生了主动停止。");
											break;
										}

										// 截图超时，则重新开始暂停
										if (meidaLoador.isOnDisplayTimeoutFlg()) {
											logger.warn("截图发生超时，发送开始命令");
											meidaLoador.start();
											Thread.sleep(10000);

											logger.warn("截图发生超时，发送暂停命令");
											meidaLoador.pause();
											Thread.sleep(10000);

											logger.warn("截图发生超时，重新设置时间");
											meidaLoador.setTime(time);
										}

										if (meidaLoador.getPastTime() > 300000) {
											// 300秒没反应，关闭
											logger.info("300秒没反应，关闭");
											throw new RuntimeException("300秒没反应，关闭");
										}

										Thread.sleep(200);
									}

									if (Video2ImageProperties.RUN_MODE_SPECIAL.equals(config.runMode)) {
										// 判断是否是最后一个指定时间
										if (config.specifyTimes.length <= runCount + 1) {
											// 截图循环结束
											logger.info("已经是最后一个指定时间,结束循环");
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
												logger.info("下一个时间超出结束时间,结束循环");
												break;
											}
										}
									} else {
										break;
									}

									// 判断是否已经超出视频总时间
									long totalTime = meidaLoador.getTotalTime();
									if (totalTime > 0 && time >= totalTime) {
										// 截图循环结束
										logger.info("已经超出视频总时间,结束循环");
										break;
									}

									runCount++;
								}

								isProcessSuccess = true;

							} catch (InterruptedException e) {
								logger.error(e.toString(), e);
							} catch (Exception e) {
								logger.error(e.toString(), e);
							} finally {
								closeMediaLoader();
							}
						}
					});

					thread.start();

				} catch (Exception e) {
					logger.debug(e.toString(), e);

					// closeMediaLoader();
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
		logger.info("closeMediaLoader begin");
		meidaLoador.release();
		meidaLoador.dispose();
		isProcessing = false;
		logger.info("closeMediaLoader end");
	}

	public boolean isClosed() {
		return !isProcessing;
	}
}
