package com.dk3k.framework.core.exception;

import com.dk3k.framework.exception.BaseException;

/**
 * 表示数据提交失败。
 * <p style="display:none">modifyRecord</p>
 * @author qiuyangjun
 * @date 2013年12月30日 上午11:50:00
 * @since
 * @version
 */
public class DataCommitException extends BaseException {

	private static final long serialVersionUID = 1669843560642800254L;

	public DataCommitException() {
		super("dataCommitFailed");
	}

	public DataCommitException(String messageKey, Throwable cause) {
		super(messageKey, cause);
	}

	public DataCommitException(String messageKey) {
		super(messageKey);
	}

	public DataCommitException(Throwable cause) {
		super("dataCommitFailed", cause);
	}

}
