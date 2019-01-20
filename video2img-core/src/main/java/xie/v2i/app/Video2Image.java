package xie.v2i.app;

import java.io.File;

import javax.swing.*;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import xie.common.exception.XRuntimeException;
import xie.common.utils.XWaitTime;
import xie.v2i.config.Video2ImageProperties;
import xie.v2i.core.MediaLoader;
import xie.v2i.listener.Video2ImageListener;

public class Video2Image {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private MediaLoader mediaLoader;

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


	private static String NATIVE_LIBRARY_SEARCH_PATH = "D:\\work\\workspace\\github\\AnimeShotSiteProject\\video2image\\video2img-core\\vlc64";
	private void loadVlc() {
		BasicConfigurator.configure();

		// 自动搜索
//		boolean found = new NativeDiscovery().discover();
//		System.out.println(found);
//		if (found) {
//			System.out.println(LibVlc.INSTANCE.libvlc_get_version());
//		}

		// 指定目录
		String userDir = new File(System.getProperty("user.dir"), "vlc64").getAbsolutePath();
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), NATIVE_LIBRARY_SEARCH_PATH);
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), userDir);
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "D:\\soft\\vlc\\vlc64");
		logger.info(LibVlc.INSTANCE.libvlc_get_version());
	}

	public void run() {
		loadVlc();



		isProcessing = true;
		isProcessSuccess = false;

		SwingUtilities.invokeLater(() -> {
			try {
				if (!new File(mrl).exists()) {
					logger.info("文件不存在，" + mrl);
					//throw new FileNotFoundException("文件不存在，" + mrl);
				}

				mediaLoader = new MediaLoader(mrl, config.width, config.height);

				Thread thread = new Thread(() -> {
					try {
						logger.info("视频截图处理程序开始执行");

						while (!mediaLoader.isVideoLoaded()) {
							logger.info("视频未载入。");
							Thread.sleep(100);
						}
						logger.info("视频已载入。");
						Thread.sleep(2000);

						logger.info("开始处理前暂停视频");
						mediaLoader.pause();
						logger.info("已执行 mediaLoader.pause()");

						XWaitTime xWaitTime = new XWaitTime(20000);
						while (!mediaLoader.isDoPauseAction()) {
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

						int runCount = 0;

						// 先随便指定一个时间
						logger.info("开始处理前随便执行一个时间,防止黑屏等待");
						long half = mediaLoader.getTotalTime() / 2;
						half = (half - half % 1000) + 473;
						mediaLoader.setTime(half);
						Thread.sleep(500);
						while (true) {
							if (mediaLoader.isRefreshedAfterChangeTime(mediaLoader.getTime())) {
								break;
							}
							if (mediaLoader.isOnDisplayTimeoutFlg()) {
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
							mediaLoader.setTime(time);

							// 等待图像刷新，进行截图
							while (true) {
								if (mediaLoader.isRefreshedAfterChangeTime(mediaLoader.getTime())) {
									// mediaLoader.saveImage();
									if (listener != null) {
										listener.setTotalTime(mediaLoader.getTotalTime());
										try {
											// 如果当前真实视频时间在设定时间0.5秒之前，则执行播放操作
											long difTime = time - mediaLoader.getTime();
											if (difTime > 500) {
												logger.warn("当前真实视频时间在设定时间{}秒之前，开始播放调整视频", difTime);
												mediaLoader.playToSpecialTime(time);
												mediaLoader.updateRgbToPre();
											}

											// 执行截图
											listener.isRefreshedAfterChangeTime(time, mediaLoader.getTime(), mediaLoader.getBufferedImage());
										} catch (Exception e) {
											throw e;
										}
									} else {
										// 测试用
										Thread.sleep(2000);
									}
									break;
								}

								if (mediaLoader.isStoped()) {
									logger.warn("mediaLoader 发生了主动停止。");
									break;
								}

								// 截图超时，则重新开始暂停
								if (mediaLoader.isOnDisplayTimeoutFlg()) {
									logger.warn("截图发生超时，发送开始命令");
									mediaLoader.start();
									Thread.sleep(10000);

									logger.warn("截图发生超时，发送暂停命令");
									mediaLoader.pause();
									Thread.sleep(10000);

									logger.warn("截图发生超时，重新设置时间");
									mediaLoader.setTime(time);
								}

								// 300秒没反应，判断截图数量，看是否符合预期，并结束循环
								if (mediaLoader.getPastTime() > 300000) {
									boolean isSuccess = false;
									if (listener != null && Video2ImageProperties.RUN_MODE_INTERVAL.equals(config.runMode)) {
										// 定时截图才需要判断
										isSuccess = listener.canSuccessExit(config.timeInterval);
									}

									if (isSuccess) {
										logger.info("300秒没反应，已截取完整，结束循环");
										break;
									} else {
										logger.error("300秒没反应，未截取完整，抛出异常");
										throw new XRuntimeException("300秒没反应，未截取完整，抛出异常");
									}
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
								logger.warn("未知截图方式,结束循环");
								break;
							}

							// 判断是否已经超出视频总时间
							long totalTime = mediaLoader.getTotalTime();
							if (totalTime > 0 && time >= totalTime) {
								// 截图循环结束
								logger.info("已经超出视频总时间,结束循环");
								break;
							}

							runCount++;
						}

						if (listener != null) {
							listener.onSuccessComplete();
						}

						isProcessSuccess = true;

					} catch (InterruptedException e) {
						logger.error("接收到中断请求：", e);
					} catch (Exception e) {
						logger.error(e.toString(), e);
					} finally {
						closeMediaLoader();
					}
				});

				thread.start();

			} catch (Exception e) {
				logger.debug(e.toString(), e);

				// closeMediaLoader();
			}
		});
	}

	public static void main(String[] args) {
		Video2Image video2Image = new Video2Image("G:\\video\\[LoliHouse] Violet Evergarden - CM01 [WebRip 1920x1080 HEVC-yuv420p10 AAC ASS].mkv");
		video2Image.run();
	}

	/**
	 * 是否正在处理
	 * 
	 * @return 是否正在处理
	 */
	public boolean isProcessing() {
		return isProcessing || mediaLoader.isPlaying();
	}

	public boolean isProcessSuccess() {
		return isProcessSuccess;
	}

	public void closeMediaLoader() {
		// mediaLoader.stop();
		logger.info("closeMediaLoader begin");
		mediaLoader.release();
		mediaLoader.dispose();
		isProcessing = false;
		logger.info("closeMediaLoader end");
	}

	public boolean isClosed() {
		return !isProcessing;
	}
}
