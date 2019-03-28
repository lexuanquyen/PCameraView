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

import android.hardware.Camera;
import android.view.View;

import com.readsense.cameraview.modle.CameraParameters;

import java.util.List;

abstract class CameraViewImpl {

    protected final Callback mCallback;

    protected final PreviewImpl mPreview;

    CameraViewImpl(Callback callback, PreviewImpl preview) {
        mCallback = callback;
        mPreview = preview;
    }

    View getView() {
        return mPreview.getView();
    }

    public PreviewImpl getPreview() {
        return mPreview;
    }


    /**
     * @return {@code true} if the implementation was able to start the camera session.
     */
    abstract boolean start();

    abstract void stop();

    abstract boolean isCameraOpened();


    abstract List<Camera.Size> getSupportedPreviewSize();

    abstract void setDisplayOrientation(int displayOrientation);

    interface Callback extends Camera.PreviewCallback {

        void onCameraOpened();

        void onCameraClosed();

        void onPreviewFrame(byte[] data, Camera camera);

    }

    abstract void setCameraParameters(CameraParameters cameraParameters);

    abstract CameraParameters getCameraParameters();
}
