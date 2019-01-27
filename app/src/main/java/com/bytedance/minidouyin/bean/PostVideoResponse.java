package com.bytedance.minidouyin.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.18 17:53
 */
public class PostVideoResponse {

    // TODO-C2 (3) Implement your PostVideoResponse Bean here according to the response json
    @SerializedName("success")
    private boolean result;

    @SerializedName("item")
    private Feed feed;

    public boolean isResult() {
        return result;
    }

    public Feed getFeed() {
        return feed;
    }
}
