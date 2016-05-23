package com.example.accessibility2.bean;

import com.example.accessibility2.bean.node.Node;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by xingxiaogang on 2016/5/23.
 * 执行时的具体操作
 */
public class ActionInfo {

    @Expose
    @SerializedName("id")
    public int id;

    @Expose
    @SerializedName("describe")
    public String describe;

    @Expose
    @SerializedName("need_wait_window")
    public boolean needWaitWindow;

    @Expose
    @SerializedName("locate_node")
    public Node locateNode;

    @Expose
    @SerializedName("scroll_node")
    public Node scrollNode;

    @Expose
    @SerializedName("check_node")
    public Node checkNode;

    @Expose
    @SerializedName("operation_node")
    public Node operationNode;

    @Expose
    @SerializedName("click_node")
    public Node clickNode;

    @Expose
    @SerializedName("identify_node")
    public Node identifyNode;

}
