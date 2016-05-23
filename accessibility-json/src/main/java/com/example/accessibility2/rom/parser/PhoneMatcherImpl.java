package com.example.accessibility2.rom.parser;

import android.content.Context;
import android.os.Build;

import com.example.accessibility2.rom.PhoneMatcher;
import com.example.accessibility2.rom.bean.Feature;
import com.example.accessibility2.rom.bean.RomInfo;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xingxiaogang on 2016/5/23.
 */
/*package*/ class PhoneMatcherImpl extends PhoneMatcher {

    private static final String PHONE_INFO_JSON_URL = "permission/rom_info_data.json";

    private Gson GSON;
    private RomInfoResult mRomInfoResault;

    public PhoneMatcherImpl(Context context) {
        super(context);
        GSON = new Gson();
        mRomInfoResault = new RomInfoResult();
    }

    @Override
    protected List<RomInfo> parseJson() {
        final Context context = mContext;
        List<RomInfo> romInfos = new ArrayList<>();
        InputStream inputStream = null;
        JsonReader reader = null;
        try {
            inputStream = context.getAssets().open(PHONE_INFO_JSON_URL);
            reader = new JsonReader(new InputStreamReader(inputStream));
            reader.beginObject();
            while (reader.hasNext()) {
                String tag = reader.nextName();
                switch (tag) {
                    case "version": {
                        mRomInfoResault.version = reader.nextInt();
                        break;
                    }
                    case "case rom_items": {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            RomInfo info = GSON.fromJson(reader, RomInfo.class);
                            romInfos.add(info);
                        }
                        reader.endArray();
                        break;
                    }
                    default: {
                        reader.skipValue();
                        break;
                    }
                }
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return romInfos;
    }

    @Override
    protected RomInfoResult match(List<RomInfo> romInfos) {
        if (romInfos != null && romInfos.size() > 0) {
            out:
            for (RomInfo romInfo : romInfos) {
                final List<Feature> features = romInfo.features;
                //规则都匹配成功则rom匹配成功
                if (features != null && features.size() > 0) {
                    boolean allOk;
                    boolean hasMatch = false;
                    for (Feature feature : features) {
                        hasMatch = true;
                        final String key = feature.key;
                        final String condition = feature.condition;
                        String value = getKeyValue(key);
                        allOk = matchValue(condition, value, feature.value);
                        //如果有一个不成则不成功
                        if (!allOk) {
                            continue out;
                        }
                    }
                    //匹配成功
                    if (hasMatch) {
                        mRomInfoResault.romInfo = romInfo;
                        break;
                    }
                }
                break;
            }
        }
        return mRomInfoResault;
    }

    //获取值
    private String getKeyValue(String key) {
        final Context context = mContext;
        String res = null;
        if (key.startsWith("ro.")) {
            try {
                Class localClass = Class.forName("android.os.SystemProperties");
                try {
                    res = (String) localClass.getDeclaredMethod("get", new Class[]{String.class}).invoke(localClass.newInstance(), new Object[]{key});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException localClassNotFoundException) {
                localClassNotFoundException.printStackTrace();
                return "";
            }
        } else if ("SDK_INT".equals(key)) {
            res = String.valueOf(Build.VERSION.SDK_INT);
        } else if ("BRAND".equals(key)) {
            res = Build.BRAND;
        } else if ("DEVICE".equals(key)) {
            res = Build.DEVICE;
        } else if ("DISPLAY".equals(key)) {
            res = Build.DISPLAY;
        } else if ("ID".equals(key)) {
            res = Build.ID;
        } else if ("MANUFACTURER".equals(key)) {
            res = Build.MANUFACTURER;
        } else if ("RELEASE".equals(key)) {
            res = Build.VERSION.RELEASE;
        } else if ("PRODUCT".equals(key)) {
            res = Build.PRODUCT;
        }
        return res;
    }

    //比较值
    private boolean matchValue(String condition, String value, String valueToMatch) {
        boolean success = false;
        if (value != null && condition != null) {
            int valueInt = -1;
            int valueMatch = -1;
            try {
                valueInt = Integer.valueOf(value);
                valueMatch = Integer.valueOf(valueToMatch);
            } catch (Exception ignored) {
            }

            switch (condition) {
                case "equal": {
                    success = valueToMatch.equals(value);
                    break;
                }
                case "greater": {
                    success = valueInt != -1 && valueMatch > valueInt;
                    break;
                }
                case "less": {
                    success = valueInt != -1 && valueMatch < valueInt;
                    break;
                }
                case "ge": {
                    success = valueInt != -1 && valueMatch >= valueInt;
                    break;
                }
                case "le": {
                    success = valueInt != -1 && valueMatch <= valueInt;
                    break;
                }
                case "ne": {
                    success = valueInt != -1 && valueMatch != valueInt;
                    break;
                }
                default: {
                    throw new RuntimeException("un support condition");
                }
            }
        }
        return success;
    }
}
