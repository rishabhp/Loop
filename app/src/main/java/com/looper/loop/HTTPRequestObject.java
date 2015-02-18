package com.looper.loop;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by rishabhpugalia on 02/02/15.
 */
public class HTTPRequestObject {
    HashMap<String, Object> mPostData = new HashMap<String, Object>();

    public void put(String key, Object val) {
        mPostData.put(key, val);
    }

    public HashMap<String, Object> getPostData() {
        return mPostData;
    }

    public String getJSON() {
        JSONObject jsonObject = new JSONObject(mPostData);
        return jsonObject.toString();
    }
}
