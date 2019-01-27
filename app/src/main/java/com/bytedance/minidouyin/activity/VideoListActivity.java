package com.bytedance.minidouyin.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bytedance.minidouyin.R;
import com.bytedance.minidouyin.adapter.VideoListAdapter;
import com.bytedance.minidouyin.adapter.VideoListAdapter.FeedViewHolder;
import com.bytedance.minidouyin.bean.Feed;
import com.bytedance.minidouyin.bean.FeedResponse;
import com.bytedance.minidouyin.bean.PostVideoResponse;
import com.bytedance.minidouyin.newtork.IMiniDouyinService;
import com.bytedance.minidouyin.newtork.RetrofitManager;
import com.bytedance.minidouyin.utils.ResourceUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class VideoListActivity extends AppCompatActivity implements View.OnClickListener,VideoListAdapter.OnFeedItemClickListener{

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "VideoListActivity";

    /**
     * 指定host
     */
    private static final String BASE_URL = "http://10.108.10.39:8080/";
    private static final String STU_NAME = "liuYang";
    private static final String STU_ID = "3220180830";

    private RecyclerView mRv;
    private List<Feed> mFeeds = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;
    private Button mBtnRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution2_c2);
        initRecyclerView();
        initBtns();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //首次进入页面后主动刷新一次数据
        fetchFeed();
    }

    private void initBtns() {
        mBtn = findViewById(R.id.btn);
        mBtnRefresh = findViewById(R.id.btn_refresh);
        mBtnRecord = findViewById(R.id.btn_record);

        mBtn.setOnClickListener(this);
        mBtnRefresh.setOnClickListener(this);
        mBtnRecord.setOnClickListener(this);
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        //设置滑动监听器，播放第一个完整可见的player
        mRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /**
                 * 自定义的自动播放策略：列表填充数据完成后，若存在第一个视频，则自动播放
                 * 当滑动列表静止后，自动播放第一个完整可见的视频
                 */

                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    autoPlay();
                }

            }
        });
        mRv.setAdapter(new VideoListAdapter(mFeeds,this));
    }

    /**
     * 自动播放列表第一个完整可见的视频逻辑
     */
    private void autoPlay() {
        RecyclerView.LayoutManager layoutManager = mRv.getLayoutManager();
        if(layoutManager instanceof LinearLayoutManager){
            LinearLayoutManager manager = (LinearLayoutManager) layoutManager;

            int firstPosition;
            FeedViewHolder holder = null;

            firstPosition = manager.findFirstCompletelyVisibleItemPosition();

            holder = (FeedViewHolder) mRv.findViewHolderForLayoutPosition(firstPosition);

            if(holder!=null){
                Log.i(TAG, "onScrollStateChanged: 自动播放,视频位于列表位置为："+firstPosition);
                holder.player.startPlayLogic();
            }
        }
    }
    public void chooseImage() {
        // TODO-C2 (4) Start Activity to select an image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE);
    }

    public void chooseVideo() {
        // TODO-C2 (5) Start Activity to select a video

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent,"Select Video"), PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(VideoListActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);

        // TODO-C2 (6) Send Request to post a video with its cover image
        // if success, make a text Toast and show
        Retrofit retrofit = RetrofitManager.get(BASE_URL);

        MultipartBody.Part img = getMultipartFromUri("cover_image", mSelectedImage);
        MultipartBody.Part video = getMultipartFromUri("video", mSelectedVideo);


        Call<PostVideoResponse> call = retrofit.create(IMiniDouyinService.class)
                .createVideo(STU_ID, STU_NAME, img, video);

        call.enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                PostVideoResponse body= response.body();

                if(body!=null && body.isResult()){
                    Toast.makeText(VideoListActivity.this,"success",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                Toast.makeText(VideoListActivity.this,"failure",Toast.LENGTH_SHORT).show();
                resetRefreshBtn();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();

    }

    public void fetchFeed() {
        mBtnRefresh.setText("requesting...");
        mBtnRefresh.setEnabled(false);

        // if success, assign data to mFeeds and call mRv.getAdapter().notifyDataSetChanged()
        // don't forget to call resetRefreshBtn() after response received
        Retrofit retrofit = RetrofitManager.get(BASE_URL);
        Call<FeedResponse> call = retrofit.create(IMiniDouyinService.class).fetchFeeds();
        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                mFeeds = response.body().getFeeds();

                //刷新列表数据
                ((VideoListAdapter)mRv.getAdapter()).refreshData(mFeeds);

                //每次刷新完成后重置列表位置
                mRv.scrollToPosition(0);

                Log.i(TAG, "onResponse: 视频个数----"+mFeeds.size());
                resetRefreshBtn();
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                resetRefreshBtn();
            }
        });

    }

    private void resetRefreshBtn() {
        mBtnRefresh.setText(R.string.refresh_feed);
        mBtnRefresh.setEnabled(true);
    }

    /**
     * 点击item时触发点击事件
     * @param index 点击的item位置
     */
    @Override
    public void onFeedItemClick(int index) {

        //点击cover时跳转视频详情页面
        Intent intent = new Intent(this, DetailPlayerActivity.class);
        Feed feed = mFeeds.get(index);
        intent.putExtra(DetailPlayerActivity.FEED_VIDEO_URL,feed.getVideoUrl());
        intent.putExtra(DetailPlayerActivity.FEED_COVER_URL,feed.getImgUrl());
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn:
                String s = mBtn.getText().toString();
                if (getString(R.string.select_an_image).equals(s)) {
                    chooseImage();
                } else if (getString(R.string.select_a_video).equals(s)) {
                    chooseVideo();
                } else if (getString(R.string.post_it).equals(s)) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                    }
                } else if ((getString(R.string.success_try_refresh).equals(s))) {
                    mBtn.setText(R.string.select_an_image);
                }

                break;

            case R.id.btn_refresh:
                //刷新数据
                fetchFeed();
                break;

            case R.id.btn_record:
                //跳转到视频录制
                jump2Record();
                break;
            default:
                break;
        }
    }

    private void jump2Record() {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }
}
