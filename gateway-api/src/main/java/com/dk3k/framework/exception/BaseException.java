package com.dk3k.framework.exception;

public class BaseException extends RuntimeException {

	private static final long serialVersionUID = -3088104317098344394L;
    private Object[] messageArgs;

    public BaseException() {
		super("unknown");
	}

	public BaseException(String messageKey, Throwable cause, Object... messageArgs) {
		super(messageKey, cause);
        this.messageArgs = messageArgs;
	}

	public BaseException(String messageKey, Object... messageArgs) {
		super(messageKey);
        this.messageArgs = messageArgs;
	}

	public BaseException(Throwable cause) {
		super("unknown", cause);
	}
	
//	public String getLocalizedMessage() {
//		String errorMessageKey = "error." +  getMessage();
//		MessageSource messageSource = (MessageSource) ContextUtils.getBean("messageSource");
//		return messageSource.getMessage(errorMessageKey, messageArgs, "", LocaleContextHolder.getLocale());
//    }
	
}
