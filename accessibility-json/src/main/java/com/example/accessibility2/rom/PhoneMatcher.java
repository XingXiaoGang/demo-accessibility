package com.example.accessibility2.rom;

import android.content.Context;

import com.example.accessibility2.rom.bean.RomInfo;

import java.util.List;

/**
 * Created by xingxiaogang on 2016/5/23.
 * 机型适配
 */
public abstract class PhoneMatcher {

    protected Context mContext;

    public PhoneMatcher(Context context) {
        this.mContext = context;
    }

    //解析待匹配数据
    protected abstract List<RomInfo> parseJson();

    //匹配
    protected abstract RomInfoResult match(List<RomInfo> romInfos);

    public final RomInfoResult match() {
        final List<RomInfo> romInfos = parseJson();
        return match(romInfos);
    }

    public class RomInfoResult {
        public RomInfo romInfo;
        public int version;
    }

}
