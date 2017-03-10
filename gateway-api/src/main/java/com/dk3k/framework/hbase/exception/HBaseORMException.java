package com.dk3k.framework.hbase.exception;

@SuppressWarnings("serial")
public class HBaseORMException extends Exception {

    public HBaseORMException(String msg) {
        super(msg);
    }

    public HBaseORMException(Exception e) {
        super(e);
    }

    public HBaseORMException(String message, Throwable cause) {
        super(message, cause);
    }
}
