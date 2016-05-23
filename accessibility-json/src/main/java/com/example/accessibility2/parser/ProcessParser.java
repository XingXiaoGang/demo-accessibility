package com.example.accessibility2.parser;

import android.content.Context;

import com.example.accessibility2.bean.ProcessInfo;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xingxiaogang on 2016/5/23.
 * 根据需要解析出具体的任务
 */
public class ProcessParser extends JsonParser<ProcessParser.ProcessInfoResult> {

    private static final String PROCESS_INFO_JSON_PATH = "permission/process_info_data.json";

    private int[] mProcessIds;

    /**
     * @param processId TaskInfo.processId
     **/
    public ProcessParser(Context context, int[] processId) {
        super(context);
        this.mProcessIds = processId;
    }

    @Override
    InputStream decodeJsonStream() {
        try {
            return mContext.getAssets().open(PROCESS_INFO_JSON_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    ProcessInfoResult parse(InputStream jsonStream) {
        ProcessInfoResult rulesResult = new ProcessInfoResult();
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
                    case "process_items": {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            ProcessInfo info = GSON.fromJson(reader, ProcessInfo.class);
                            if (info != null && contains(info.id)) {
                                rulesResult.processInfos.put(info.id, info);
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
        for (int i : mProcessIds) {
            if (i == id) {
                return true;
            }
        }
        return false;
    }

    public class ProcessInfoResult {
        public int version;
        public Map<Integer, ProcessInfo> processInfos = new HashMap<>();
    }

}
