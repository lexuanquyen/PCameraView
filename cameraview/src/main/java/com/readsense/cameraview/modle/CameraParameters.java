package com.readsense.cameraview.modle;

import android.view.Gravity;

import com.readsense.cameraview.camera.Size;

public class CameraParameters {
    public static final int FACING_BACK = 0;  //后摄像头
    public static final int FACING_FRONT = 1; //前摄像头

    public float mScale = -1; //预览缩放比例
    public Size mCameraSize;//摄像头分辨率
    public int CameraId = 0;
    public boolean mAdjustViewBounds = true;//是否设配屏幕
    public Size mPreviewSize;//预览分辨率
    public boolean mAdjustVertical = false;
    public int mDisplayOrientation = 0;//预览方向
    public int gravity = Gravity.CENTER;//位置

    public void init(CameraParameters cameraParameters) {
        this.setmScale(cameraParameters.mScale);
        this.setCameraId(cameraParameters.CameraId);
        this.setDisplayOrientation(cameraParameters.mDisplayOrientation);
        this.setmAdjustVertical(cameraParameters.mAdjustVertical);
        this.setmAdjustViewBounds(cameraParameters.mAdjustViewBounds);
        this.setmPreviewSize(cameraParameters.mPreviewSize);
        this.setmCameraSize(cameraParameters.mCameraSize);
    }

    public CameraParameters setmScale(float mScale) {
        if (mScale != -1)
            this.mScale = mScale;
        return this;
    }

    public CameraParameters setmCameraSize(Size mCameraSize) {
        if (null != mCameraSize)
            this.mCameraSize = mCameraSize;
        return this;
    }

    public CameraParameters setmCameraSize(int w, int h) {
        Size size = new Size(w, h);
        if (null != size)
            this.mCameraSize = size;
        return this;
    }

    public CameraParameters setCameraId(int cameraId) {
        if (cameraId != -1)
            CameraId = cameraId;
        return this;
    }

    public CameraParameters setmAdjustViewBounds(boolean mAdjustViewBounds) {
        this.mAdjustViewBounds = mAdjustViewBounds;
        return this;
    }

    public CameraParameters setmPreviewSize(int w, int h) {
        Size size = new Size(w, h);
        if (null != size)
            this.mPreviewSize = size;
        return this;
    }

    public CameraParameters setmPreviewSize(Size mPreviewSize) {
        if (null != mPreviewSize)
            this.mPreviewSize = mPreviewSize;
        return this;
    }

    public CameraParameters setmAdjustVertical(boolean mAdjustVertical) {
        this.mAdjustVertical = mAdjustVertical;
        return this;
    }

    public CameraParameters setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        return this;
    }
}
