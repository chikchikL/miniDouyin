package com.bytedance.minidouyin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

/**
 * @author tianye.xy@bytedance.com
 * 2019/1/9
 */
public class DetailPlayerActivity extends GSYBaseActivityDetail<StandardGSYVideoPlayer> {
    //feed item封面地址
    public static final String FEED_COVER_URL = "FEED_COVER_URL";
    //feed item视频地址
    public static final String FEED_VIDEO_URL = "FEED_VIDEO_URL";


    StandardGSYVideoPlayer detailPlayer;



    //视频地址
    private String videoUrl;

    //封面地址
    private String coverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_detail_player);

        detailPlayer = (StandardGSYVideoPlayer) findViewById(R.id.detail_player);
        TextView tv_video_detail = findViewById(R.id.tv_video_detail);

        videoUrl = getIntent().getStringExtra(FEED_VIDEO_URL);
        coverUrl = getIntent().getStringExtra(FEED_COVER_URL);

        tv_video_detail.setText("封面url为："+coverUrl+"\n"+"视频url为："+ videoUrl);

        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);

        initVideoBuilderMode();

    }


    @Override
    public StandardGSYVideoPlayer getGSYVideoPlayer() {
        return detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //内置封面可参考SampleCoverVideo
        ImageView imageView = new ImageView(this);
        //设置封面
        loadCover(imageView, coverUrl);

        return new GSYVideoOptionBuilder()
                .setThumbImageView(imageView)
                .setUrl(videoUrl)
                .setCacheWithPlay(true)
                .setVideoTitle(" ")
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)//打开动画
                .setNeedLockFull(true)
                .setSeekRatio(1);
    }

    @Override
    public void clickForFullScreen() {

    }


    /**
     * 是否启动旋转横屏，true表示启动
     */
    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }

    private void loadCover(ImageView imageView, String url) {
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.ic_launcher);
        Glide.with(this.getApplicationContext())
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(3000000)
                                .centerCrop()
                                .error(R.mipmap.xxx2))
                .load(url)
                .into(imageView);
    }

}
