package com.bytedance.minidouyin.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.20 14:18
 */
public class Feed {

    // TODO-C2 (1) Implement your Feed Bean here according to the response json
    @SerializedName("user_name")
    private String stuName;

    @SerializedName("student_id")
    private String stuId;

    @SerializedName("image_url")
    private String imgUrl;

    @SerializedName("video_url")
    private String videoUrl;


    public String getStuName() {
        return stuName;
    }

    public String getStuId() {
        return stuId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}
