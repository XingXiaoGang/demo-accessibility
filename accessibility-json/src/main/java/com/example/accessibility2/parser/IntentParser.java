package com.example.accessibility2.parser;

import android.content.Context;

import com.example.accessibility2.bean.IntentInfo;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xingxiaogang on 2016/5/23.
 */
public class IntentParser extends JsonParser<IntentParser.IntentResult> {

    private static final String INTENT_INFO_JSON_PATH = "permission/intent_info_data.json";
    private int[] mIntentIds;

    public IntentParser(Context context, int[] intentIds) {
        super(context);
        this.mIntentIds = intentIds;
    }

    @Override
    InputStream decodeJsonStream() {
        try {
            return mContext.getAssets().open(INTENT_INFO_JSON_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    IntentResult parse(InputStream jsonStream) {
        IntentResult rulesResult = new IntentResult();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(jsonStream));
            reader.beginObject();
            while (reader.hasNext()) {
                String tag = reader.nextName();
                switch (tag) {
                    case "version": {
                        rulesResult.version = reader.nextInt();
                        break;
                    }
                    case "intent_items": {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            IntentInfo info = GSON.fromJson(reader, IntentInfo.class);
                            if (info != null && contains(info.id)) {
                                rulesResult.intentInfo.put(info.id, info);
                                break;
                            }
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
                if (jsonStream != null) {
                    jsonStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rulesResult;
    }

    private boolean contains(int id) {
        for (int i : mIntentIds) {
            if (i == id) {
                return true;
            }
        }
        return false;
    }


    public class IntentResult {
        public int version;
        public Map<Integer, IntentInfo> intentInfo = new HashMap<>();
    }

}
