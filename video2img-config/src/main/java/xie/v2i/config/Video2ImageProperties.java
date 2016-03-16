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
	
	
	/** ����ģʽ ��ʱ��� */
	public final static String RUN_MODE_INTERVAL = "0";
	/** ����ģʽ ָ��ʱ�� */
	public final static String RUN_MODE_SPECIAL = "1";

	/** ����ģʽ */
	public String runMode = RUN_MODE_INTERVAL;

	public int width = 1280;

	public int height = 720;

	/** ��ʼʱ�� */
	public long startTime = 0;

	/** ��ʼʱ�� */
	public long endTime = 0;

	/** ʱ���� */
	public long timeInterval = 60000;

	/** ָ��ʱ�� */
	public long[] specifyTimes;
}
