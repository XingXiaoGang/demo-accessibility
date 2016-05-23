package com.example.accessibility2.bean.node;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by xingxiaogang on 2016/5/23.
 */
public abstract class Node {

    //要匹配的文本
    @Expose
    @SerializedName("find_texts")
    public String[] findTexts;

    //要匹配的id
    @Expose
    @SerializedName("id_name")
    public String idName;

    //className
    @Expose
    @SerializedName("class_name")
    public String className;

}