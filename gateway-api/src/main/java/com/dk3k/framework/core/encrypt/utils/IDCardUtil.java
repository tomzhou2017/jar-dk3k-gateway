package com.dk3k.framework.core.encrypt.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public final class IDCardUtil {
    public static HashMap<String, Integer> getSexAndAgeByIdentyNo(String identyNo) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        int length = identyNo.length();
        if (length != 18 && length != 15) {
            map.put("age", 99);
            map.put("sex", 1);
        } else {
            // 430822198601292335
            String dates = "";
            int sex = 2;
            if (length == 18) {
                int se = Integer.valueOf(identyNo.substring(length - 2, length - 1)) % 2;// 倒数第二位
                dates = identyNo.substring(6, 10) + identyNo.substring(10, 12) + identyNo.substring(12, 14);
                if (se == 0) {// 女
                    sex = 2;
                } else {// 男
                    sex = 1;
                }
            } else if (length == 15) {
                int se = Integer.valueOf(identyNo.substring(length - 1)) % 2;// 倒数第一位
                if (se == 0) {// 女
                    sex = 2;
                } else {// 男
                    sex = 1;
                }
                dates = "19" + identyNo.substring(6, 8) + identyNo.substring(8, 10) + identyNo.substring(10, 12);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            // 得到当前
            String cDay = sdf.format(new Date()).substring(0, 8);
            // 得到生日
            int age = 99;
            try {
                Date date = sdf.parse(cDay);
                Date mydate = sdf.parse(dates);
                long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
                age = (int) (day / 365);
            } catch (ParseException e) {
                // e.printStackTrace();
                map.put("age", 100);
                map.put("sex", 1);
            }
            map.put("age", age);
            map.put("sex", sex);
        }
        return map;
    }
}
