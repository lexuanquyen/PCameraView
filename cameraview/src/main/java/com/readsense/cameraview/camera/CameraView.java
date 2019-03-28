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

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.os.Build;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.readsense.cameraview.R;
import com.readsense.cameraview.modle.CameraParameters;


public class CameraView extends FrameLayout {
    private CameraViewImpl mImpl;
    private CallbackBridge mCallbacks;
    private final DisplayOrientationDetector mDisplayOrientationDetector;
    private CameraParameters mCameraParameters;


    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            mCallbacks = null;
            mDisplayOrientationDetector = null;
            return;
        }
        final PreviewImpl preview = createPreviewImpl(context);
        mCallbacks = new CallbackBridge();
//        if (Build.VERSION.SDK_INT < 21) {
//            mImpl = new Camera1(mCallbacks, preview);
//        } else if (Build.VERSION.SDK_INT > 21) {
//            mImpl = new Camera2(mCallbacks, preview, context);
//        }
        mImpl = new Camera1(mCallbacks, preview);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraView, defStyleAttr,
                R.style.Widget_CameraView);
        mCameraParameters = mImpl.getCameraParameters();
        mCameraParameters.mAdjustViewBounds = a.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false);
        a.recycle();
        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {        // Display orientation detector
            @Override
            public void onDisplayOrientationChanged(int displayOrientation) {
                Log.e("pan", "旋转角度");
                mImpl.setDisplayOrientation(displayOrientation);
            }
        };
    }

    @NonNull
    private PreviewImpl createPreviewImpl(Context context) {
        PreviewImpl preview;
        if (Build.VERSION.SDK_INT < 14) {
            preview = new SurfaceViewPreview(context, this);
        } else {
            preview = new TextureViewPreview(context, this);
        }
        return preview;
    }


    public CameraParameters getCameraParameters() {
        mCameraParameters = mImpl.getCameraParameters();
        return mCameraParameters;
    }

    public void setCameraParameters(CameraParameters mCameraParameters) {
        if (null == mCameraParameters) return;
        this.mCameraParameters = mCameraParameters;
        mImpl.setCameraParameters(mCameraParameters);
        requestLayout();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mDisplayOrientationDetector.enable(ViewCompat.getDisplay(this));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            mDisplayOrientationDetector.disable();
        }
        super.onDetachedFromWindow();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
        if (isInEditMode()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
            mImpl.getView().measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
        } else {
            if (mCameraParameters.mPreviewSize != null) {
                int w = mCameraParameters.mPreviewSize.getWidth(),
                        h = mCameraParameters.mPreviewSize.getHeight();
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(w), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(h), MeasureSpec.EXACTLY));
                mImpl.getView().measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(w), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(h), MeasureSpec.EXACTLY));
            } else {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
                mImpl.getView().measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
            }
        }
    }

    public void start(CameraParameters mCameraParameters) {
        mImpl.setCameraParameters(mCameraParameters);
        if (!mImpl.start()) {
            Parcelable state = onSaveInstanceState();//store the state ,and restore this state after fall back o Camera1
            mImpl = new Camera1(mCallbacks, createPreviewImpl(getContext()));// Camera2 uses legacy hardware layer; fall back to Camera1
            onRestoreInstanceState(state);
            mImpl.start();
        }
        this.mCameraParameters = mCameraParameters;
        requestLayout();
    }

    public void reStart(CameraParameters mCameraParameters) {
        if (mImpl.isCameraOpened()) {
            mImpl.stop();
        }
        this.start(mCameraParameters);
    }

    /**
     * 停止摄像头
     */
    public void stop() {
        mCallbacks = null;
        if (mImpl.isCameraOpened()) {
            mImpl.stop();
        }
    }

    /**
     * 是否开启
     *
     * @return
     */
    public boolean isCameraOpened() {
        return mImpl.isCameraOpened();
    }

    public void addCallback(@NonNull Callback callback) {
        if (mCallbacks == null) mCallbacks = new CallbackBridge();
        mCallbacks.setmCallback(callback);
    }

    public void swichCamera() {
        CameraParameters cameraParameters = mImpl.getCameraParameters();
        int mCameraId = cameraParameters.CameraId == CameraParameters.FACING_BACK ? CameraParameters.FACING_FRONT : CameraParameters.FACING_BACK;
        cameraParameters.setCameraId(mCameraId);
        reStart(cameraParameters);
    }


    public void initCameraParameters(CameraParameters cameraParameters) {
        if (null == mCameraParameters) {
            this.mCameraParameters = cameraParameters;
            return;
        }
        this.mCameraParameters.init(cameraParameters);
    }


    private class CallbackBridge implements CameraViewImpl.Callback {
        private CameraView.Callback mCallback = null;

        @Override
        public void onCameraOpened() {
            if (mCallback != null) {
                mCallback.onCameraOpened(CameraView.this);
            }

        }

        @Override
        public void onCameraClosed() {
            if (mCallback != null) {
                mCallback.onCameraClosed(CameraView.this);
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mCallback != null) {
                mCallback.onPreviewFrame(data, camera);
            }
        }

        public void setmCallback(@NonNull CameraView.Callback callback) {
            mCallback = callback;
        }
    }

    public abstract static class Callback {

        public void onCameraOpened(CameraView cameraView) {
        }

        public void onCameraClosed(CameraView cameraView) {
        }

        public void onPreviewFrame(byte[] data, Camera camera) {
        }

    }
}
