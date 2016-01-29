package xie.v2i.utils;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xie.v2i.app.Video2Image;

public class XWaitChange {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private Object compareValue;

	private long timeout;

	private long beginTime;

	private boolean isChangedFlg;

	public XWaitChange(Object compareValue) {
		init(compareValue, 0);
	}

	/**
	 * 
	 * @param compareValue
	 * @param timeout
	 *            毫秒
	 */
	public XWaitChange(Object compareValue, long timeout) {
		init(compareValue, timeout);
	}

	private void init(Object compareValue, long timeout) {
		this.compareValue = compareValue;
		this.timeout = timeout;
		beginTime = (new Date()).getTime();
		isChangedFlg = false;
		logger.debug("beginTime: {}, compareValue: {}", beginTime, compareValue);
	}

	public void setCompareValue(Object compareValue) {
		init(compareValue, timeout);
	}

	public boolean isChanged() {
		return isChangedFlg;
	}

	public boolean isChanged(Object value) {

		if (isChangedFlg) {
			return true;
		}

		doCompare(value);

		return isChangedFlg;
	}

	protected void doCompare(Object value) {
		if (compareValue.equals(value)) {
			if (timeout > 0) {
				// 设置超时的话， 如果超时，则认为已经改变了
				long nowTime = (new Date()).getTime();
				if (nowTime - beginTime > timeout) {
					isChangedFlg = true;
					logger.debug("isChanged timeout: {}, {}", compareValue, value);
				}
			}
		} else {
			isChangedFlg = true;
			logger.debug("isChanged value: {}, {}", compareValue, value);
		}
	}
}
