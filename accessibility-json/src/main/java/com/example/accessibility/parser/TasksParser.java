package com.example.accessibility.parser;

import android.content.Context;

import com.example.accessibility.bean.TaskInfo;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xingxiaogang on 2016/5/23.
 * 根据romId解析出对应的所有任务
 */
public class TasksParser extends JsonParser<TasksParser.TasksResult> {

    private static final String TASK_INFO_JSON_PATH = "permission/tasks_config.json";
    private int mRomIdToFind;

    public TasksParser(Context context, int romId) {
        super(context);
        this.mRomIdToFind = romId;
    }

    @Override
    protected InputStream decodeJsonStream() {
        try {
            return mContext.getAssets().open(TASK_INFO_JSON_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected TasksResult parse(InputStream jsonStream) {
        TasksResult rulesResult = new TasksResult();
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
                    case "task_items": {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            TaskInfo info = GSON.fromJson(reader, TaskInfo.class);
                            if (info != null && info.romId == mRomIdToFind) {
                                rulesResult.rulsInfo.add(info);
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

    public class TasksResult {
        public int version;
        //任务集合
        public List<TaskInfo> rulsInfo = new ArrayList<>();
    }
}
