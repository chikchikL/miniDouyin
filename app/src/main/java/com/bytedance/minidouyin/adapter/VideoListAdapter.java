package com.bytedance.minidouyin.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bytedance.minidouyin.R;
import com.bytedance.minidouyin.bean.Feed;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.List;

/**
 * Created by liuYang on 2019/1/27.
 */

public class VideoListAdapter extends RecyclerView.Adapter{
    private static final String TAG = "VideoListAdapter";

    private List<Feed> mFeeds;
    private Context mContext;
    private OnFeedItemClickListener mListener;

    public VideoListAdapter(List<Feed> data, Context context) {

        this.mFeeds = data;
        this.mContext = context;
        this.mListener = (OnFeedItemClickListener) context;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //填充一个列表播放器布局供使用
        View inflate = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_list_autoplay, null, false);
        return new FeedViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        Log.i(TAG, "onBindViewHolder: "+i);
        //初始化播放器及封面
        FeedViewHolder holder = (FeedViewHolder) viewHolder;
        initPlayer(holder.player,mFeeds.get(i));

    }

    @Override public int getItemCount() {
        return mFeeds.size();
    }

    /**
     * 拿到数据后初始化播放器
     * @param player 播放器
     * @param feed 单个视频数据
     */
    private void initPlayer(StandardGSYVideoPlayer player, Feed feed) {
        player.setUp(feed.getVideoUrl(), true, "feed");

        //增加封面
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.ic_launcher);
        Glide.with(mContext.getApplicationContext())
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(3000000)
                                .centerCrop()
                                .error(R.mipmap.xxx2))
                .load(feed.getImgUrl())
                .into(imageView);
        player.setThumbImageView(imageView);
        //增加title
        player.getTitleTextView().setVisibility(View.GONE);
        //设置返回键
        player.getBackButton().setVisibility(View.GONE);
        //是否可以滑动调整
        player.setIsTouchWiget(false);

    }




    /**
     * 视频列表的ViewHolder
     */
    public class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public StandardGSYVideoPlayer player;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);

            //设置点击事件
            itemView.setOnClickListener(this);
            player = itemView.findViewById(R.id.list_player);
        }

        @Override
        public void onClick(View v) {

            //每个播放布局预留了简介的位置，当简介位置被点击时跳转视频详情页面
            mListener.onFeedItemClick(getAdapterPosition());
        }
    }

    /**
     * 这个接口负责将数据带回主页，因为后面可能要根据这个数据做跳转
     */
    public interface OnFeedItemClickListener{
        void onFeedItemClick(int position);
    }

    /**
     * 刷新列表数据
     * @param data 最新请求到的数据
     */
    public void refreshData(List<Feed> data){
        this.mFeeds = data;
        this.notifyDataSetChanged();
    }
}
