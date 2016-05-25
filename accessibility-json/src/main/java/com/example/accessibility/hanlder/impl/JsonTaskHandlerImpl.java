package com.example.accessibility.hanlder.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.accessibility.AccessibilityClient;
import com.example.accessibility.R;
import com.example.accessibility.SettingAccessibilityService;
import com.example.accessibility.Statics;
import com.example.accessibility.bean.ActionInfo;
import com.example.accessibility.bean.IntentInfo;
import com.example.accessibility.bean.ProcessInfo;
import com.example.accessibility.bean.TaskInfo;
import com.example.accessibility.bean.node.LocateNode;
import com.example.accessibility.bean.node.OperationNode;
import com.example.accessibility.bean.node.ScrollNode;
import com.example.accessibility.hanlder.BaseTaskHandler;
import com.example.accessibility.parser.ActionParser;
import com.example.accessibility.parser.IntentParser;
import com.example.accessibility.parser.ProcessParser;
import com.example.accessibility.parser.TasksParser;
import com.example.accessibility.rom.bean.RomInfo;
import com.example.accessibility.utils.IntentUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xingxiaogang on 2016/5/23.
 */
public class JsonTaskHandlerImpl extends BaseTaskHandler {

    private List<TaskInfo> mTaskInfos;
    private Map<Integer, ProcessInfo> mProcessInfosMap;
    private Map<Integer, IntentInfo> mIntentInfosMap;
    private Map<Integer, ActionInfo> mActionInfosMap;

    private ProcessInfo mCurrentProcessInfo;
    private ExcuteTask mExcuteTask;
    private boolean isRunnig;

    public JsonTaskHandlerImpl(SettingAccessibilityService service) {
        super(service);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void start() {
        new ParseJsonTask(getContext()).start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void finish() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isPrepared() {
        final List<TaskInfo> taskInfos = mTaskInfos;
        final Map<Integer, ProcessInfo> processInfosMap = mProcessInfosMap;
        final Map<Integer, IntentInfo> intentInfosMap = mIntentInfosMap;
        final Map<Integer, ActionInfo> actionInfosMap = mActionInfosMap;
        //合法性查检
        if (taskInfos != null && !taskInfos.isEmpty() && processInfosMap != null && !processInfosMap.isEmpty() && intentInfosMap != null && !intentInfosMap.isEmpty() && actionInfosMap != null && !actionInfosMap.isEmpty()) {
            for (TaskInfo taskInfo : taskInfos) {
                ProcessInfo processInfo = processInfosMap.get(taskInfo.processId);
                if (processInfo != null) {
                    final IntentInfo intentInfo = intentInfosMap.get(processInfo.intentId);
                    if (intentInfo == null) {
                        throw new RuntimeException("accessibility json error :Id为 " + taskInfo.processId + " 的IntentInfo未配置");
                    }
                    if (processInfo.actionId == null || processInfo.actionId.length == 0) {
                        throw new RuntimeException("accessibility json error :ProcessInfo 里的Action未配置");
                    }
                    for (int i : processInfo.actionId) {
                        final ActionInfo actionInfo = actionInfosMap.get(i);
                        if (actionInfo == null) {
                            throw new RuntimeException("accessibility json error :Id为 " + i + " 的ActionInfo未配置");
                        }
                    }
                } else {
                    throw new RuntimeException("accessibility json error :Id为 " + taskInfo.processId + " 的ProcessInfo未配置");
                }
            }
        } else {
            throw new RuntimeException("accessibility json error:json信息不完整");
        }
        return true;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == R.id.parse_json_return) {
            if (isPrepared()) {
                //加载json成功 进入执行
                Log.e("test_access", "开始执行辅助操作...");
                mExcuteTask = new ExcuteTask();
                mExcuteTask.start();
                isRunnig = true;
            } else {
                //信息初始化失败
                Log.e("test_access", "中断：json配置信息初始化失败");
            }
        } else if (msg.what == R.id.action_error) {
            Intent intent = new Intent(Statics.ACCESSIBILITY_CLIENT_ACTION);
            intent.putExtra(Statics.Key.ACTION, R.id.action_error);
            intent.putExtra(Statics.Key.CODE, msg.arg1);
            intent.putExtra(Statics.Key.MESSAGE, String.valueOf(msg.obj));
            sendBroadcastToClient(intent);

        } else if (msg.what == R.id.action_progress_update) {
            Intent intent = new Intent(Statics.ACCESSIBILITY_CLIENT_ACTION);
            intent.putExtra(Statics.Key.ACTION, R.id.action_progress_update);
            intent.putExtra(Statics.Key.PROGRESS_ALL, msg.arg1);
            intent.putExtra(Statics.Key.PROGRESS, msg.arg2);
            intent.putExtra(Statics.Key.MESSAGE, String.valueOf(msg));
            sendBroadcastToClient(intent);
        } else if (msg.what == R.id.action_finish) {
            Intent intent = new Intent(Statics.ACCESSIBILITY_CLIENT_ACTION);
            intent.putExtra(Statics.Key.ACTION, R.id.action_finish);
            intent.putExtra(Statics.Key.SUCCESS, msg.arg1 == 0);
            sendBroadcastToClient(intent);
        }
    }

    //通信
    private void sendBroadcastToClient(Intent intent) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getService().getApplication());
        localBroadcastManager.sendBroadcast(intent);
    }

    //根据romid执行后续的json解析
    private class ParseJsonTask extends Thread {

        private Context mContext;

        public ParseJsonTask(Context context) {
            this.mContext = context;
        }

        @Override
        public void run() {
            super.run();
            //获得rom信息
            final RomInfo romInfo = AccessibilityClient.getInstance(getService().getApplication()).getRomInfo();
            if (romInfo != null) {
                int romId = romInfo.romId;
                //获得对应rom的辅助任务信息
                final TasksParser.TasksResult result = new TasksParser(mContext, romId).parse();
                if (result != null) {
                    JsonTaskHandlerImpl.this.mTaskInfos = result.taskInfos;
                    int[] taskIds = new int[mTaskInfos.size()];
                    for (int i = 0; i < mTaskInfos.size(); i++) {
                        taskIds[i] = mTaskInfos.get(i).processId;
                    }
                    //获得对应辅助任务的具体执行信息
                    final ProcessParser.ProcessInfoResult result1 = new ProcessParser(mContext, taskIds).parse();
                    if (result1 != null) {
                        JsonTaskHandlerImpl.this.mProcessInfosMap = result1.processInfos;

                        //待解析的id集合
                        int[] intentIds = new int[mProcessInfosMap.size()];
                        List<int[]> actionIds = new ArrayList<>();

                        Set<Map.Entry<Integer, ProcessInfo>> set = mProcessInfosMap.entrySet();
                        Iterator<Map.Entry<Integer, ProcessInfo>> iterator = set.iterator();
                        int index = 0;
                        while (iterator.hasNext()) {
                            Map.Entry<Integer, ProcessInfo> entry = iterator.next();
                            final ProcessInfo info = entry.getValue();
                            intentIds[index] = info.intentId;
                            actionIds.add(info.actionId);
                            index++;
                        }
                        for (int i = 0; i < mTaskInfos.size(); i++) {
                            taskIds[i] = mTaskInfos.get(i).processId;
                        }
                        //获得具体执行的具体intent 和 关键字等信息
                        final IntentParser.IntentResult result2 = new IntentParser(mContext, intentIds).parse();
                        final ActionParser.ActionResult result3 = new ActionParser(mContext, actionIds).parse();

                        if (result2 != null) {
                            JsonTaskHandlerImpl.this.mIntentInfosMap = result2.intentInfo;
                        }
                        if (result3 != null) {
                            JsonTaskHandlerImpl.this.mActionInfosMap = result3.actionInfo;
                        }
                    }
                }
            }
            sendEmptyMessage(R.id.parse_json_return);
        }
    }

    //执行核心
    private class ExcuteTask extends Thread {

        private int current;
        private boolean interrupt;

        public ExcuteTask() {

        }

        @Override
        public void run() {
            super.run();

            final List<TaskInfo> taskInfos = mTaskInfos;
            final Map<Integer, ProcessInfo> processInfosMap = mProcessInfosMap;
            final Map<Integer, IntentInfo> intentInfosMap = mIntentInfosMap;
            final Map<Integer, ActionInfo> actionInfosMap = mActionInfosMap;

            boolean hasErro = false;
            int erroCode = -1;
            for (int i = 0; i < taskInfos.size(); i++) {
                final TaskInfo taskInfo = taskInfos.get(i);
                //通知进度
                current = i;
                Message message = obtainMessage(R.id.action_progress_update, taskInfos.size(), current, taskInfo.title);
                sendMessage(message);

                final ProcessInfo processInfo = processInfosMap.get(taskInfo.processId);
                mCurrentProcessInfo = processInfo;
                Log.w("test_access", "开始辅助项目:" + processInfo.describe);
                //最终执行要用到的信息
                final IntentInfo intentInfo = intentInfosMap.get(processInfo.intentId);
                final List<ActionInfo> actions = new ArrayList<>();
                for (int ac : processInfo.actionId) {
                    actions.add(actionInfosMap.get(ac));
                }

                if (!startAction(intentInfo, actions)) {
                    hasErro = true;
                    erroCode = intentInfo.id;

                    //通知错误
                    Message msg = obtainMessage(R.id.action_error, erroCode, 0, "task exec failed:intentId:" + erroCode + ",action:" + actions);
                    sendMessage(msg);

                    Log.w("test_access", "辅助项目执行失败：" + actions);
                }

                if (interrupt) {
                    //通知错误
                    Message msg = obtainMessage(R.id.action_error, erroCode, 0, "taskInterrupt.");
                    sendMessage(msg);
                    Log.e("test_access", "辅助功能中断");
                    break;
                }
            }
            //通知完成
            Message message = obtainMessage(R.id.action_progress_update, hasErro ? 1 : 0, erroCode);
            sendMessage(message);
            Log.e("test_access", "辅助操作执行完毕.... has error:" + hasErro);
            Toast.makeText(getService(), "所有操作已执行完成" + hasErro, Toast.LENGTH_LONG).show();
        }

        /**
         * todo 有待改善一下json结构 以更好的适匹一个操作涉及多个包名的情况，即 ProcessInfo里面的 intent_id也改为数组
         **/
        private boolean startAction(final IntentInfo intentInfo, final List<ActionInfo> actionInfo) {
            Log.d("test_access", "====startAction====" + intentInfo);
            final List<Intent> intentList = createIntent(intentInfo);
            if (intentList.size() == 1) {
                final Intent intent = intentList.get(0);
                if (runAction(intent, actionInfo)) {
                    Log.e("test_access", "runAction 失败1：" + actionInfo);
                }
                if (interrupt) {
                    return false;
                }
            } else if (intentList.size() == actionInfo.size()) {
                for (int i = 0; i < intentList.size(); i++) {
                    final Intent intent = intentList.get(i);
                    if (runAction(intent, actionInfo)) {
                        Log.e("test_access", "runAction 失败2：" + actionInfo);
                    }
                }
            } else {
                throw new RuntimeException("不支持的对应关系：intent数量和action数量不一一致!");
            }
            Log.w("test_access", "辅助项目完成:" + intentInfo.id);
            return true;
        }

        private boolean runAction(Intent intent, List<ActionInfo> actionInfos) {
            Log.w("test_access", "runAction：" + actionInfos);
            IntentUtils.startActivity(getService(), intent);
            for (int i = 0; i < actionInfos.size(); i++) {
                final ActionInfo actionInfo = actionInfos.get(i);
                //窗口等待
                if (actionInfo.needWaitWindow) {
                    Log.d("test_access", "sleep:" + actionInfo.needWaitTime);
                    try {
                        Thread.sleep(actionInfo.needWaitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //为了增加兼容性 强制等待300毫秒
                    Log.d("test_access", "sleep：300");
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //============================查找node================================
                AccessibilityNodeInfo rootInfo = getService().getRootInActiveWindow();
                if (rootInfo == null) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    rootInfo = getService().getRootInActiveWindow();
                }

                if (rootInfo != null) {
                    LocateNode locateNode = actionInfo.locateNode;
                    ScrollNode scrollNode = actionInfo.scrollNode;
                    AccessibilityNodeInfo nodeInfo = findNodeInRoot(locateNode);

                    //找不到时的 重试机制
                    int tryTimes = 5;
                    while (nodeInfo == null) {
                        if (scrollNode != null) {
                            if (!scrollNode(scrollNode)) {
                                break;
                            }
                        } else {
                            if (tryTimes == 0) {
                                break;
                            }
                            tryTimes--;
                        }
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        nodeInfo = findNodeInRoot(locateNode);
                        Log.d("test_access", "查找重试 time:" + tryTimes + ",res:" + (nodeInfo != null));
                        if (nodeInfo != null) {
                            break;
                        }
                    }

                    if (nodeInfo != null) {
                        //=========================执行node最终操作===============================
                        if (!performNodeAction(nodeInfo, actionInfo)) {
                            Log.e("test_access", "======执行action失败=========");
                            interrupt = true;
                            return false;
                        }
                    } else {
                        Message msg = obtainMessage(R.id.action_error, Statics.Code.ERROR_CODE_NO_NODE, 0, actionInfo);
                        sendMessage(msg);
                    }
                } else {
                    Log.d("test_access", "======AccessibilityNodeInfo root null=========");
                    Message msg = obtainMessage(R.id.action_error, Statics.Code.ERROR_CODE_ROOT_NODE_NULL, 0, "===========taskInterrupt=============");
                    sendMessage(msg);
                    interrupt = true;
                    return false;
                }
            }
            return true;
        }

        private boolean performNodeAction(AccessibilityNodeInfo nodeInfo, ActionInfo actionInfo) {
            boolean res = false;
            //先验证checkNode
            if (actionInfo.checkNode != null) {
                res = intelligentCheckNode(nodeInfo);
            }
            if (!res && actionInfo.operationNode != null) {
                OperationNode operationNode = actionInfo.operationNode;
                switch (operationNode.behavior) {
                    case "click": {
                        res = intelligentClickNode(nodeInfo);
                        break;
                    }
                    default: {
                        throw new RuntimeException("不支持的node behavior");
                    }
                }
            }
            return res;
        }

        //查找匹配node
        private AccessibilityNodeInfo findNodeInRoot(LocateNode locateNode) {
            AccessibilityNodeInfo nodeInfo = null;
            final String className = locateNode.className;
            final String[] keys = locateNode.findTexts;
            final String id = locateNode.idName;
            final int index = locateNode.itemIndex;
            if (keys != null) {
                for (String key : keys) {
                    nodeInfo = findNodeByText(key, index, 0);
                    if (nodeInfo != null) {
                        break;
                    }
                }
            }
            if (nodeInfo == null && id != null) {
                nodeInfo = finNode(id, className);
            }
            if (nodeInfo != null) {
                Log.d("test_access", "findNodeInRoot res:" + nodeInfo.getClassName() + "," + nodeInfo.getText());
            } else {
                Log.d("test_access", "findNodeInRoot res: null");
            }
            return nodeInfo;
        }

        private List<Intent> createIntent(IntentInfo intentInfo) {
            List<Intent> intents = new ArrayList<>();

            //有可能涉及多个包名
            if (intentInfo.packageName != null) {
                Intent intent = new Intent();
                if (intentInfo.activityName != null) {
                    intent.setComponent(new ComponentName(intentInfo.packageName, intentInfo.activityName));
                } else {
                    intent.setPackage(intentInfo.packageName);
                }
                if (intentInfo.action != null) {
                    intent.setAction(intentInfo.action);
                }
                if (intentInfo.data != null) {
                    intent.setData(Uri.parse(intentInfo.data));
                }
                if (intentInfo.extra != null) {
                    String[] extr = intentInfo.extra.split("=");
                    intent.putExtra(extr[0], extr[1]);
                }
                intents.add(intent);
            }

            if (intentInfo.packageName1 != null) {
                Intent intent = IntentUtils.qureyLaunchIntent(getService(), intentInfo.packageName1);
                if (intent != null) {
                    intents.add(intent);
                } else {
                    Message msg = obtainMessage(R.id.action_error, intentInfo.id, 0, "包名不存在:" + intentInfo.packageName1);
                    sendMessage(msg);
                }
            }
            if (intentInfo.packageName2 != null) {
                Intent intent = IntentUtils.qureyLaunchIntent(getService(), intentInfo.packageName2);
                if (intent != null) {
                    intents.add(intent);
                } else {
                    Message msg = obtainMessage(R.id.action_error, intentInfo.id, 0, "包名不存在:" + intentInfo.packageName1);
                    sendMessage(msg);
                }
            }
            return intents;
        }
    }
}
