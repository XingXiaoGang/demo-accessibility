package com.example.accessibility.hanlder;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.accessibility.R;
import com.example.accessibility.bean.node.ScrollNode;

import java.util.Collections;
import java.util.List;

/**
 * Created by xingxiaogang on 2016/5/23.
 * 抽象了一些常用方法
 */
public abstract class BaseTaskHandler extends Handler implements ITaskHandler {

    protected static final String TAG = "test_handler";

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
    protected final AccessibilityNodeInfo findNode(String key, int method, int index, int parents) {
        AccessibilityNodeInfo accessibilityNodeInfo = null;
        final AccessibilityNodeInfo root = getRootNode();
        if (root == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfos = null;
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
        if (nodeInfos == null) {
            nodeInfos = Collections.EMPTY_LIST;
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected final AccessibilityNodeInfo finNode(String key, String className) {
        AccessibilityNodeInfo accessibilityNodeInfo = null;
        final AccessibilityNodeInfo root = getRootNode();
        if (root == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfos = root.findAccessibilityNodeInfosByViewId(key);
        if (nodeInfos == null) {
            nodeInfos = Collections.EMPTY_LIST;
        }
        if (!nodeInfos.isEmpty()) {
            if (className != null) {
                for (AccessibilityNodeInfo nodeInfo : nodeInfos) {
                    if (nodeInfo.getClassName().toString().equals(className)) {
                        accessibilityNodeInfo = nodeInfo;
                        break;
                    }
                }
            } else {
                accessibilityNodeInfo = nodeInfos.get(0);
            }
        }
        return accessibilityNodeInfo;
    }

    //这里的遍历不能用递归 很蛋疼
    protected final boolean scrollNode(ScrollNode scrollNode) {
        final String className = scrollNode.className;
        final AccessibilityNodeInfo root = getRootNode();
        if (root == null) {
            return false;
        }
        AccessibilityNodeInfo res = null;

        out:
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo temp = root.getChild(i);
            if (className.contains(temp.getClassName().toString())) {
                res = temp;
                break out;
            } else if (temp.getChildCount() > 0) {
                for (int j = 0; j < temp.getChildCount(); j++) {
                    AccessibilityNodeInfo temp1 = temp.getChild(j);
                    if (className.contains(temp1.getClassName().toString())) {
                        res = temp1;
                        break out;
                    } else if (temp1.getChildCount() > 0) {
                        for (int k = 0; k < temp1.getChildCount(); k++) {
                            AccessibilityNodeInfo temp2 = temp1.getChild(k);
                            if (className.contains(temp2.getClassName().toString())) {
                                res = temp2;
                                break out;
                            }
                        }
                    }
                }
            }
        }
        return res != null && res.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    //智能点击
    protected final boolean intelligentClickNode(AccessibilityNodeInfo nodeInfo) {
        boolean res = false;
        if (nodeInfo.isClickable()) {
            res = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        //往上一层找
        if (!res) {
            AccessibilityNodeInfo parent = nodeInfo.getParent();
            if (parent.isClickable()) {
                res = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            if (!res) {
                int childCount = parent.getChildCount();
                //找同级别的
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo accessibilityNodeInfo = parent.getChild(i);
                    if (!res && accessibilityNodeInfo.isClickable()) {
                        res = accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
            //往上二层
            if (!res) {
                AccessibilityNodeInfo parentP = parent.getParent();
                if (parentP != null && parentP.isClickable()) {
                    res = parentP.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
        Log.d("test_access", "intelligentClickNode res:" + res);
        return res;
    }

    //智能点击
    protected final boolean intelligentCheckNode(AccessibilityNodeInfo nodeInfo) {
        boolean res = false;
        if (nodeInfo.isCheckable()) {
            res = nodeInfo.isChecked() || nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        //往上一层找
        if (!res) {
            AccessibilityNodeInfo parent = nodeInfo.getParent();
            if (parent.isCheckable()) {
                res = parent.isChecked() || parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            if (!res) {
                int childCount = parent.getChildCount();
                //找同级别的
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo brotherNode = parent.getChild(i);
                    if (!res && brotherNode.isCheckable()) {
                        res = brotherNode.isChecked() || brotherNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
            //往上二层
            if (!res) {
                AccessibilityNodeInfo parentP = parent.getParent();
                if (parentP != null && parentP.isCheckable()) {
                    res = parentP.isChecked() || parentP.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
        Log.d("test_access", "intelligentCheckNode res:" + res);
        return res;
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

    public Context getContext() {
        return service;
    }

    /**************************
     * 任务重试
     **************************/

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
