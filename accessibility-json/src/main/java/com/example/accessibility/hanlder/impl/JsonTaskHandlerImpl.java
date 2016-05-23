package com.example.accessibility.hanlder.impl;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Message;
import android.view.accessibility.AccessibilityEvent;

import com.example.accessibility.bean.ActionInfo;
import com.example.accessibility.bean.IntentInfo;
import com.example.accessibility.bean.ProcessInfo;
import com.example.accessibility.bean.TaskInfo;
import com.example.accessibility.hanlder.BaseTaskHandler;

import java.util.List;
import java.util.Map;

/**
 * Created by xingxiaogang on 2016/5/23.
 */
public class JsonTaskHandlerImpl extends BaseTaskHandler {

    private List<TaskInfo> mTaskInfos;
    private Map<Integer, ProcessInfo> mProcessInfosMap;
    private Map<Integer, IntentInfo> mIntentInfosMap;
    private Map<Integer, ActionInfo> mActionInfosMap;

    private ProcessInfo mProcessInfo;

    JsonTaskHandlerImpl(AccessibilityService service) {
        super(service);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

    }

    public void init() {

    }

    private class ParseJsonTask extends Thread {

        private Context mContext;

        public ParseJsonTask(Context context) {
            this.mContext = context;
        }

        @Override
        public void run() {
            super.run();
            //todo 初始化各个json
        }
    }
}
