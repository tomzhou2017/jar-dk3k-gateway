package com.dk3k.framework.core.mybatis;

import java.io.Serializable;

import lombok.Data;

@Data
public class Sort implements Serializable {

    private static final long serialVersionUID = 7026434198845897214L;
    private String property;
    private String direction;

}
