package xie.v2i.test;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xie.v2i.app.Video2Image;
import xie.v2i.config.Video2ImageProperties;

public class TestRunVideo {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] args) throws Exception {
		TestRunVideo testRunVideo = new TestRunVideo();
		testRunVideo.runTask(null);

		System.exit(0);
	}

	public void runTask(Map<String, Object> paramMap) throws Exception {
		run(null, paramMap);
	}

	public int run(String[] args, Map<String, Object> paramMap) throws Exception {
		try {
			logger.info("begin process animeEpisodeId: " + paramMap);

			Video2ImageDoNotingListener saveImageListener = new Video2ImageDoNotingListener(500);
			// File fileMrl = new File("F:\\AnimeShotSite\\anime\\2016\\美少女战士\\Crystal\\资源\\[AWS] 美少女战士 Sailor Moon Crystal Ⅲ 28[GB]\\[AWS] 美少女战士 Sailor Moon Crystal Ⅲ 28[GB][1080p x264 AAC][036B4C1E].mp4");
			// File fileMrl = new File("E:\\AnimeShotSIte\\anime\\G\\干物妹！小埋\\[Kamigami] Himouto! Umaru-chan - 05 [1920x1080 x264 AAC Sub(Chs,Cht,Jap)].mkv");
//			File fileMrl = new File("G:\\video\\[LoliHouse] Violet Evergarden - CM01 [WebRip 1920x1080 HEVC-yuv420p10 AAC ASS].mkv");
			File fileMrl = new File("M:\\AnimeShotSite\\anime\\Z\\紫罗兰永恒花园\\第一季\\[FLsnow][Violet_Evergarden][Extra_Episode][BDRIP][HEVC_FLACx2][4K-YUV444P12].mkv");



			Video2Image video2Image = new Video2Image(fileMrl.getAbsolutePath(), saveImageListener);
			video2Image.setRunMode(Video2ImageProperties.RUN_MODE_INTERVAL);
			// video2Image.setRunMode(Video2ImageProperties.RUN_MODE_SPECIAL);
			Long startTime = null;
			Long endTime = null;
			Long interval = 1000L;
			if (interval != null) {
				video2Image.setTimeInterval(interval);
			}
			if (startTime != null) {
				video2Image.setStartTime(startTime);
			}
			if (endTime != null) {
				video2Image.setEndTime(endTime);
			}
			video2Image.setSpecifyTimes(new long[] { 40000, 40000, 40000 });
			video2Image.getVideo2ImageProperties().width = 1920 * 2;
			video2Image.getVideo2ImageProperties().height = 1080 * 2;
//			 video2Image.getVideo2ImageProperties().width = 1920;
//			 video2Image.getVideo2ImageProperties().height = 1080;
//			video2Image.getVideo2ImageProperties().width = 640;
//			video2Image.getVideo2ImageProperties().height = 360;

			video2Image.run();

			while (!video2Image.isClosed()) {
				Thread.sleep(5000);
			}

			if (video2Image.isProcessSuccess()) {
				logger.info("process 成功 : ");
			} else {
				logger.error("process 失败");
			}

		} catch (Exception e) {
			logger.error("process 失败", e);
			throw e;
		}

		return 0;
	}
}
