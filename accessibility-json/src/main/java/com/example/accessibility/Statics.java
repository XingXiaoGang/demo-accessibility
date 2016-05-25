package com.example.accessibility;

/**
 * Created by xingxiaogang on 2016/5/20.
 */
public class Statics {
    public static final String ACCESSIBILITY_SERVER_ACTION = "com.gang.accessibility.server.AccessibilityService";
    public static final String ACCESSIBILITY_CLIENT_ACTION = "com.gang.accessibility.client.AccessibilityService";

    public static class Key {
        public static final String ACTION = "command";
        public static final String CODE = "code";
        public static final String MESSAGE = "message";
        public static final String PROGRESS_ALL = "progress_all";
        public static final String PROGRESS = "progress";
        public static final String SUCCESS = "success";
    }

    public static class Code {
        public static final int ERROR_CODE_ROOT_NODE_NULL = 101;//辅助功能未开启导致
        public static final int ERROR_CODE_NO_NODE = 102;//node查找失败
    }
}
