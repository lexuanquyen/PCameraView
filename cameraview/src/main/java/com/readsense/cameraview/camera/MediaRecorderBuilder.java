package com.readsense.cameraview.camera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.Surface;

import java.io.IOException;

public class MediaRecorderBuilder {
    private MediaRecorder mediarecorder;
    private Camera mCamera = null;
    private int mAudioSource = MediaRecorder.AudioSource.CAMCORDER;//defalut，camcorder，mic，voice_call，voice_communication,voice_downlink, voice_recognition,  voice_uplink;
    private int mVideoSource = MediaRecorder.VideoSource.CAMERA; //default，CAMERA
    private int mOutputFormat = MediaRecorder.OutputFormat.MPEG_4; //输出格式MP4 amr_nb，amr_wb,default,mpeg_4,raw_amr,three_gpp.
    private int mAudioEncoder = MediaRecorder.AudioEncoder.DEFAULT;//声音编码 default，AAC，AMR_NB，AMR_WB
    private int mVideoEncoder = MediaRecorder.VideoEncoder.H264;//视频编码  default，H263，H264，MPEG_4_SP
    private String mOutputFile = "/sdcard/myVideo.3gp";//视频位置
    private int mVideoSizeWidth = 640;
    private int mVideoSizeHeight = 480;
    private int mVideoEncodingBitRate = 10 * 1024 * 1024;//提高帧频率，录像模糊，花屏，绿屏可写上调试
    private Surface mPreviewDisplay = null;
    private int mVideoFrameRate = 24;//帧
    private CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
    private MediaRecorder.OnErrorListener errorListener = null;
    private MediaRecorder.OnInfoListener infoListener = null;
    private int mOrientationHint = 0;

    public MediaRecorderBuilder MediaRecorderBuild() {
        mediarecorder = new MediaRecorder();
        return this;
    }

    /**
     * 设置摄像头
     *
     * @param camera
     * @return
     */
    public MediaRecorderBuilder setCamera(Camera camera) {
        mCamera = camera;
        return this;
    }

    public MediaRecorderBuilder setOrientationHint(int o) {
        mOrientationHint = o;
        return this;
    }

    /**
     * 设置音频资源
     *
     * @param audioSource
     * @return
     */
    public MediaRecorderBuilder setAudioSource(int audioSource) {
        mAudioSource = audioSource;
        return this;
    }

    /**
     * 设置视频资源
     *
     * @param videoSource
     * @return
     */
    public MediaRecorderBuilder setVideoSource(int videoSource) {
        mVideoSource = videoSource;
        return this;
    }

    /**
     * 输出格式
     *
     * @param outputFormat
     * @return
     */
    public MediaRecorderBuilder setOutputFormat(int outputFormat) {
        mOutputFormat = outputFormat;
        return this;
    }

    /**
     * 设置音频编码
     *
     * @param audioEncoder
     * @return
     */
    public MediaRecorderBuilder setAudioEncoder(int audioEncoder) {
        mAudioEncoder = audioEncoder;
        return this;
    }

    /**
     * 设置视频编码
     *
     * @param videoEncoder
     * @return
     */
    public MediaRecorderBuilder setVideoEncoder(int videoEncoder) {
        mVideoEncoder = videoEncoder;
        return this;
    }

    /**
     * 设置输出文件路径与文件名
     *
     * @param path
     * @return
     */
    public MediaRecorderBuilder setOutputFile(String path, String name) {
        mOutputFile = path + name;
        return this;
    }

    /**
     * 设置视频分辨率
     *
     * @param w
     * @param h
     * @return
     */
    public MediaRecorderBuilder setVideoSize(int w, int h) {
        mVideoSizeWidth = w;
        mVideoSizeHeight = h;
        return this;
    }

    /**
     * 设置视频帧率
     *
     * @param rate
     * @return
     */
    public MediaRecorderBuilder setVideoFrameRate(int rate) {
        mVideoFrameRate = rate;
        return this;
    }

    /**
     * 提高帧频率
     *
     * @param rate
     * @return
     */
    public MediaRecorderBuilder setVideoEncodingBitRate(int rate) {
        mVideoEncodingBitRate = rate;
        return this;
    }

    /**
     * 设置视频预览
     *
     * @param sf
     * @return
     */
    public MediaRecorderBuilder setPreviewDisplay(Surface sf) {
        mPreviewDisplay = sf;
        return this;
    }

    public MediaRecorderBuilder setProfile(CamcorderProfile pf) {
        mProfile = pf;
        return this;
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public MediaRecorderBuilder setOnErrorListener(MediaRecorder.OnErrorListener listener) {
        errorListener = listener;
        return this;
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public MediaRecorderBuilder setInfoListener(MediaRecorder.OnInfoListener listener) {
        infoListener = listener;
        return this;
    }


    /**
     * build
     */
    public MediaRecorderBuilder Build() {
        if (mVideoSource == MediaRecorder.VideoSource.CAMERA) {
            mediarecorder.setCamera(mCamera);//设置调用的摄像头
        }
        mediarecorder.setAudioSource(mAudioSource); //指定Audio，video来源
        mediarecorder.setVideoSource(mVideoSource);
        // 指定CamcorderProfile(需要API Level 8以上版本)
        mediarecorder.setProfile(mProfile);
        //使用CamcorderProfile做配置的话，输出格式，音频编码，视频编码 不要写
        // 设置输出格式和编码格式(针对低于API Level 8版本)
        mediarecorder.setOutputFormat(mOutputFormat); //设置输出格式，.THREE_GPP为3gp，.MPEG_4为mp4
        mediarecorder.setAudioEncoder(mAudioEncoder);//设置声音编码类型 mic
        mediarecorder.setVideoEncoder(mVideoEncoder);//设置视频编码类型，一般h263，h264
        mediarecorder.setOutputFile(mOutputFile);
        mediarecorder.setVideoSize(mVideoSizeWidth, mVideoSizeHeight);//设置视频分辨率，设置错误调用start()时会报错，可注释掉在运行程序测试，有时注释掉可以运行
        mediarecorder.setVideoFrameRate(mVideoFrameRate);//设置视频帧率，可省略
        mediarecorder.setVideoEncodingBitRate(mVideoEncodingBitRate);//提高帧频率，录像模糊，花屏，绿屏可写上调试
        mediarecorder.setPreviewDisplay(mPreviewDisplay); //设置视频预览
        if (errorListener != null) mediarecorder.setOnErrorListener(errorListener);
        if (infoListener != null) mediarecorder.setOnInfoListener(infoListener);
        mediarecorder.setOrientationHint(mOrientationHint);
        return this;
    }

    /**
     * 开始录制
     */
    public void start() throws IOException {
        mediarecorder.prepare();
        mediarecorder.start();
    }

    /**
     * 停止录制
     */
    public void stop() {
        mediarecorder.stop();// 停止录制
        mediarecorder.reset(); // 在重置mediarecorder
        mediarecorder.release();// 释放资源
    }
}
