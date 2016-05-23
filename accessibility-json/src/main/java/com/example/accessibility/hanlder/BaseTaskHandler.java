package com.example.accessibility.hanlder;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.accessibility.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by xingxiaogang on 2016/5/23.
 * 抽象了一些常用方法
 */
public abstract class BaseTaskHandler extends Handler implements ITaskHandler {
    private static final String TAG = "test_handler";

    private AccessibilityService service;

    protected BaseTaskHandler(AccessibilityService service) {
        this.service = service;
    }

    protected final AccessibilityService getService() {
        return service;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected final AccessibilityNodeInfo getRootNode() {
        return service.getRootInActiveWindow();
    }

    protected final AccessibilityNodeInfo findNodeById(String id, int parents) {
        return findNodeById(id, 0, parents);
    }

    protected final AccessibilityNodeInfo findNodeById(String id, int index, int parents) {
        return findNode(id, 1, index, parents);
    }

    protected final AccessibilityNodeInfo findNodeByText(String id, int parents) {
        return findNodeById(id, 0, parents);
    }

    protected final AccessibilityNodeInfo findNodeByText(String id, int index, int parents) {
        return findNode(id, 0, index, parents);
    }

    /**
     * @param method  类型 0:根据text 1:根据id
     * @param key     text [*] 或 id: [包名]:id/[id]
     * @param index   结果集中的第几个
     * @param parents 第几层父布局,默认为0,负数则表示第几层子布局
     **/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private AccessibilityNodeInfo findNode(String key, int method, int index, int parents) {
        AccessibilityNodeInfo accessibilityNodeInfo = null;
        final AccessibilityNodeInfo root = getRootNode();
        if (root == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfos = Collections.EMPTY_LIST;
        switch (method) {
            case 0: {
                nodeInfos = root.findAccessibilityNodeInfosByText(key);
                break;
            }
            case 1: {
                nodeInfos = root.findAccessibilityNodeInfosByViewId(key);
                break;
            }
        }
        if (!nodeInfos.isEmpty()) {
            accessibilityNodeInfo = nodeInfos.size() > index ? nodeInfos.get(index) : nodeInfos.get(0);
            if (parents > 0) {
                for (int i = 0; i < parents; i++) {
                    if (accessibilityNodeInfo != null) {
                        accessibilityNodeInfo = accessibilityNodeInfo.getParent();
                    }
                }
            } else {
                for (int i = 0; i > parents; i--) {
                    if (accessibilityNodeInfo != null && accessibilityNodeInfo.getChildCount() > 0) {
                        accessibilityNodeInfo = accessibilityNodeInfo.getChild(0);
                    }
                }
            }
        }
        return accessibilityNodeInfo;
    }

    protected final String getViewId(String idstr) {
        return service.getPackageName() + ":id/" + idstr;
    }

    protected final boolean performNodeAction(AccessibilityNodeInfo nodeInfo, int action) {
        return nodeInfo != null && nodeInfo.performAction(action);
    }

    protected final void printChilds(AccessibilityNodeInfo root) {
        if (root != null) {
            Log.d(TAG, "====root start======name:" + root.getClassName() + ",child:" + root.getChildCount());
            int childCount = root.getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo child = root.getChild(i);
                    printChilds(child);
                }
            } else {
                Log.d(TAG, "====root end======name:" + root.getClassName() + ",child:" + root.getChildCount());
            }
        } else {
            Log.d(TAG, "====root is null=======");
        }
    }

    //    protected final void finishAccessibility() {
//        Intent intent = new Intent(Statics.ACCESSIBILITY_SERVER_ACTION);
//        intent.putExtra(Statics.Key.COMMAND, Statics.STOP);
//        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getService().getApplication());
//        localBroadcastManager.sendBroadcast(intent);
//    }

    /**************************任务重试**************************/

    protected final void retryTask(long timeDelay, String id) {
        retryTask(timeDelay, id, 1);
    }

    protected final void retryTask(long timeDelay, String id, int tryTimes) {
        if (id == null) {
            throw new RuntimeException("id can't be null");
        }
        if (tryTimes < 1) {
            throw new RuntimeException("tryTimes should >=1");
        }
        Message message = obtainMessage(R.id.retry_task, id);
        message.arg1 = tryTimes;
        message.arg2 = (int) timeDelay;
        sendMessageDelayed(message, timeDelay);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == R.id.retry_task) {
            int times = msg.arg1;
            int timeDelay = msg.arg2;
            String id = (String) msg.obj;
            boolean success = onRetryTask(id);
            //不成功则重试
            if (!success) {
                if (times > 1) {
                    Message message = obtainMessage(R.id.retry_task, id);
                    message.arg1 = --times;
                    sendMessageDelayed(message, timeDelay);
                }
            }
        }
    }

    protected boolean onRetryTask(String id) {
        return false;
    }
}
