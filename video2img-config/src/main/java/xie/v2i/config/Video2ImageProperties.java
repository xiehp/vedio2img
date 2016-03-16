package xie.v2i.config;

public class Video2ImageProperties {
	

	public final static String KEY_runMode = "runMode";
	public final static String KEY_width = "width";
	public final static String KEY_height = "height";
	public final static String KEY_startTime = "startTime";
	public final static String KEY_endTime = "endTime";
	public final static String KEY_timeInterval = "timeInterval";
	public final static String KEY_specifyTimes = "specifyTimes";

	public final static String KEY_id = "id";
	public final static String KEY_forceUpload = "forceUpload";
	
	
	/** 运行模式 定时间隔 */
	public final static String RUN_MODE_INTERVAL = "0";
	/** 运行模式 指定时间 */
	public final static String RUN_MODE_SPECIAL = "1";

	/** 运行模式 */
	public String runMode = RUN_MODE_INTERVAL;

	public int width = 1280;

	public int height = 720;

	/** 开始时间 */
	public long startTime = 0;

	/** 开始时间 */
	public long endTime = 0;

	/** 时间间隔 */
	public long timeInterval = 60000;

	/** 指定时间 */
	public long[] specifyTimes;
}
