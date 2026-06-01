package com.example.attendance.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    // どんなクラスから呼ばれてもいいように、専用の汎用ロガーを1つだけ用意する
    private static final Logger logger = LoggerFactory.getLogger("ApplicationLogger");

    public static void info(String message, Object... args) {
        logger.info(message, args);
    }

    public static void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public static void error(String message, Object... args) {
        logger.error(message, args);
    }
    
    public static void debug(String message, Object... args) {
        logger.debug(message, args);
    }
}