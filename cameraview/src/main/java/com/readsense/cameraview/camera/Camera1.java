/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readsense.cameraview.camera;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;

import com.readsense.cameraview.modle.CameraParameters;

import java.io.IOException;
import java.util.List;


@SuppressWarnings("deprecation")
class Camera1 extends CameraViewImpl {

    private static final int INVALID_CAMERA_ID = 0;
    private int mCameraId;
    private Camera mCamera;
    private Camera.Parameters mCameraParameters;
    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private boolean mShowingPreview; //是否显示预览
    private boolean mAutoFocus;  //是否自动对焦
    private int LANDSCAPE_90 = 90;
    private int LANDSCAPE_270 = 270;
    private CameraParameters mParameters;


    Camera1(Callback callback, PreviewImpl preview) {
        super(callback, preview);
        preview.setCallback(new PreviewImpl.Callback() {
            @Override
            public void onSurfaceChanged() {
                if (mCamera != null) {
                    setUpPreview();
                    adjustCameraParameters();
                }
            }
        });
    }

    @Override
    public void setCameraParameters(CameraParameters ps) {
        if (null == mParameters) return;
        this.mParameters = ps;
        if (isCameraOpened()) {
            stop();
            start();
        }
    }

    public CameraParameters getCameraParameters() {
        if (null == mParameters) {
            mParameters = new CameraParameters();
        }
        return mParameters;
    }

    /**
     * 打开相机与显示预览
     *
     * @return
     */
    @Override
    boolean start() {
        chooseCamera();
        stop();
        openCamera();
        if (mPreview.isReady()) {
            setUpPreview();
        }
        mShowingPreview = true;
        startPreview();
        return true;
    }

    /**
     * 开始显示预览
     */
    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.setPreviewCallback(mCallback);
        }
    }

    /**
     * 关闭相机与预览
     */
    @Override
    void stop() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
        mShowingPreview = false;
        releaseCamera();
    }

    @SuppressLint("NewApi")
    void setUpPreview() {
        try {
            if (mPreview.getOutputClass() == SurfaceHolder.class) {
                final boolean needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14;
                if (needsToStopPreview) {
                    mCamera.stopPreview();
                }
                mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
                if (needsToStopPreview) {
                    startPreview();
                }
            } else {
                mCamera.setPreviewTexture((SurfaceTexture) mPreview.getSurfaceTexture());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    boolean isCameraOpened() {
        return mCamera != null;
    }


    @Override
    List<Camera.Size> getSupportedPreviewSize() {
        if (mCamera != null) {
            List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            return sizes;
        }
        return null;
    }


    @Override
    void setDisplayOrientation(int displayOrientation) {
        if (mParameters == null) mParameters = new CameraParameters();
        if (mParameters.mDisplayOrientation == displayOrientation) {
            return;
        }
        mParameters.mDisplayOrientation = displayOrientation;
        if (isCameraOpened()) {
            mCameraParameters.setRotation(calcCameraRotation(displayOrientation));
            mCamera.setParameters(mCameraParameters);
            final boolean needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14;
            if (needsToStopPreview) {
                mCamera.stopPreview();
            }
            mCamera.setDisplayOrientation(calcDisplayOrientation(displayOrientation));
            if (needsToStopPreview) {
                startPreview();
            }
        }
    }


    /**
     * This rewrites {@link #} and {@link #mCameraInfo}.
     */
    private void chooseCamera() {
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            if (i == mParameters.CameraId) {
                mCameraId = i;
                return;
            }
        }
        mCameraId = INVALID_CAMERA_ID;
    }

    private void openCamera() {
        if (mCamera != null) {
            releaseCamera();
        }
        try {
            mCamera = Camera.open(mCameraId);
        } catch (Exception e) {
            mCameraId = mCameraId == 0 ? 1 : 0;
            mCamera = Camera.open(mCameraId);
        }
        mCameraParameters = mCamera.getParameters();
        adjustCameraParameters();
        mCamera.setDisplayOrientation(calcDisplayOrientation(mParameters.mDisplayOrientation));
        mCallback.onCameraOpened();
    }

    void adjustCameraParameters() {
        if (mParameters.mCameraSize == null) {
            Camera.Size size = mCameraParameters.getSupportedPictureSizes().get(0);//设置摄像头支持该比例下最高分辨率
            mParameters.mCameraSize = new Size(size.width, size.height);
        }

        if (mShowingPreview) {
            mCamera.stopPreview();
        }
        if (mCameraParameters == null || mCamera == null) return;
        mCameraParameters.setPreviewSize(mParameters.mCameraSize.getWidth(), mParameters.mCameraSize.getHeight());
        mCameraParameters.setRotation(calcCameraRotation(mParameters.mDisplayOrientation));
        setAutoFocusInternal(mAutoFocus);
        try {
            mCamera.setParameters(mCameraParameters);
        } catch (Exception e) {
            mParameters.mPreviewSize = new Size(640, 480);
            mCameraParameters.setPreviewSize(640, 480);
            mCamera.setParameters(mCameraParameters);
        }
        if (mShowingPreview) {
            startPreview();
        }
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            mCallback.onCameraClosed();
        }
    }

    private int calcDisplayOrientation(int screenOrientationDegrees) {
        int degrees;
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            degrees = (360 - (mCameraInfo.orientation + screenOrientationDegrees) % 360) % 360;
        } else {  // back-facing
            degrees = (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360;
        }
        if (degrees % 90 == 0) {
            return degrees;
        }
        if (screenOrientationDegrees % 90 == 0) {
            return screenOrientationDegrees;
        }
        return 0;
    }

    private int calcCameraRotation(int screenOrientationDegrees) {
        int degrees;
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            degrees = (mCameraInfo.orientation + screenOrientationDegrees) % 360;
        } else {  // back-facing
            final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
            degrees = (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360;
        }
        if (degrees % 90 == 0) {
            return degrees;
        }
        if (screenOrientationDegrees % 90 == 0) {
            return screenOrientationDegrees;
        }
        return 0;
    }

    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == LANDSCAPE_90 ||
                orientationDegrees == LANDSCAPE_270);
    }

    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    private boolean setAutoFocusInternal(boolean autoFocus) {
        mAutoFocus = autoFocus;
        if (isCameraOpened()) {
            final List<String> modes = mCameraParameters.getSupportedFocusModes();
            if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            } else {
                mCameraParameters.setFocusMode(modes.get(0));
            }
            return true;
        } else {
            return false;
        }
    }
}
