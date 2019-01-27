package com.bytedance.minidouyin.activity;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.minidouyin.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.bytedance.minidouyin.utils.RecordUtils.MEDIA_TYPE_VIDEO;
import static com.bytedance.minidouyin.utils.RecordUtils.getOutputMediaFile;

/**
 * 录制视频的页面
 */
public class RecordActivity extends AppCompatActivity implements View.OnClickListener,SurfaceHolder.Callback{

    private static final String TAG = "CustomCameraActivity";

    /**
     * 单次变焦变化量
     */
    public static final int ZOOM_CHANGE_SCALE = 2;
    /**
     * 初始化焦距
     */
    public static final int ZOOM_INIT_VALUE = 0;

    private SurfaceView mSurfaceView;
    private Camera mCamera;



    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;
    private SurfaceHolder mSurfaceHolder;
    private File outputMediaFile;
    private ImageView iv_record_tag;
    private TextView tv_time;
    private Thread task_auto_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_record);

        mSurfaceView = findViewById(R.id.img);
        Button btn_record = findViewById(R.id.btn_record);
        Button btn_facing = findViewById(R.id.btn_facing);
        Button btn_zoom = findViewById(R.id.btn_zoom);
        iv_record_tag = findViewById(R.id.iv_record_tag);
        tv_time = findViewById(R.id.tv_time);

        btn_record.setOnClickListener(this);
        btn_facing.setOnClickListener(this);
        btn_zoom.setOnClickListener(this);

        //todo 给SurfaceHolder添加Callback

        //初始化摄像头对象
        mCamera = getCamera(CAMERA_TYPE);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);


    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        //旋转
        cam.setDisplayOrientation(getCameraDisplayOrientation(CAMERA_TYPE));
        //设置自动对焦
        Camera.Parameters parameters = cam.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        cam.setParameters(parameters);


        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //todo 释放camera资源
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        //将摄像头数据提供预览
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(getCameraDisplayOrientation(CAMERA_TYPE));

        outputMediaFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);

        try{
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            releaseMediaRecorder();
        }
        return true;
    }


    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder

        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();

        //录制完成后，刷新媒体库，且退回到主页
        if(outputMediaFile != null){
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(outputMediaFile));
            this.sendBroadcast(mediaScanIntent);

            this.finish();
        }


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //todo 释放Camera和MediaRecorder资源
        if(mCamera != null)
            releaseCameraAndPreview();
        if(mMediaRecorder != null)
            releaseMediaRecorder();
    }



    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btn_record:

                if (isRecording) {
                    //主动停止录制，隐藏录制按钮
                    releaseMediaRecorder();
                    isRecording = false;
                    iv_record_tag.setVisibility(View.GONE);
                    if(!task_auto_stop.isInterrupted())
                        task_auto_stop.interrupt();
                } else {
                    //开启录制
                    isRecording = true;
                    prepareVideoRecorder();

                    //显示录制时标记
                    iv_record_tag.setVisibility(View.VISIBLE);

                    //另起线程记录录制时间，10s后自动停止录制，如果没到10s主动停止了，摧毁自动停止线程
                    long start_time = System.currentTimeMillis();

                    //每秒钟更新一次屏幕上的秒数
                    task_auto_stop = new Thread(() -> {
                        try {
                            int i;
                            for (i = 0; i < 10; ++i) {
                                Thread.sleep(1000);
                                String tag = "录制时间：" + (i + 1)+"s";
                                //每秒钟更新一次屏幕上的秒数
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_time.setText(tag);
                                    }
                                });
                            }
                            //结束计时后
                            runOnUiThread(() -> {
                                releaseMediaRecorder();
                                isRecording = false;
                                iv_record_tag.setVisibility(View.GONE);

                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    task_auto_stop.start();
                }
                break;
            case R.id.btn_facing:
                //todo 切换前后摄像头
                if(CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_BACK)
                    mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                else
                    mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                //切换了摄像头需要重新配置一遍surface与camera之间的联系
                mSurfaceHolder.addCallback(this);
                startPreview(mSurfaceHolder);
                break;
            case R.id.btn_zoom:
                //todo 调焦，需要判断手机是否支持
                //判断是否支持调整焦距
                if (mCamera.getParameters().isSmoothZoomSupported())
                {
                    Camera.Parameters params = mCamera.getParameters();
                    final int MAX = params.getMaxZoom();
                    if(MAX==0)
                        return;

                    int zoomValue = params.getZoom();
                    //未达到变焦上限，则放大焦距
                    if(zoomValue <= MAX-ZOOM_CHANGE_SCALE){
                        zoomValue += ZOOM_CHANGE_SCALE;
                        params.setZoom(zoomValue);

                    }else{
                        //达到最大后回到原始焦距
                        params.setZoom(ZOOM_INIT_VALUE);
                    }

                    mCamera.setParameters(params);

                }
                break;

            default:
                break;

        }
    }
}
