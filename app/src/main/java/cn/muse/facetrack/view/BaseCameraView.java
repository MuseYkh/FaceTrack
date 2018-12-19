package cn.muse.facetrack.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import cn.muse.facetrack.listener.OnPhotoTakenListener;

/**
 * author: muse
 * created on: 2018/12/18 下午2:37
 * description:
 */
public abstract class BaseCameraView extends JavaCameraView implements LoaderCallbackInterface, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "BaseCameraView";

    public abstract void onOpenCVLoadSuccess();
    public abstract void onOpenCVLoadFail();

    // 标记当前OpenCV加载状态
    private boolean isLoadSuccess;
    protected Mat mRgba;
    protected Mat mGray;
    protected OnPhotoTakenListener onPhotoTakenListener;

    public BaseCameraView(Context context, int cameraId) {
        super(context, cameraId);
        setCvCameraViewListener(this);
    }

    public BaseCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCvCameraViewListener(this);
    }

    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS:
                Log.i(TAG, "onOpenCVLoadSuccess");
                isLoadSuccess = true;
                // 加载成功
                onOpenCVLoadSuccess();
                enableView();
                break;
            default:
                isLoadSuccess = false;
                // 加载失败
                onOpenCVLoadFail();
                Log.i(TAG, "onOpenCVLoadFail");
                break;
        }
    }

    @Override
    public void onPackageInstall(int operation, InstallCallbackInterface callback) {
        Log.i(TAG, "onPackageInstall: " + operation);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat(height, width, CvType.CV_8UC4);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        Log.i(TAG, "onWindowVisibilityChanged: " + visibility);
        switch (visibility) {
            case VISIBLE:
                Log.i(TAG, "onWindowVisibilityChanged: VISIBLE");
                enableView();
                break;
//            case INVISIBLE:
                // Log.i(TAG, "onWindowVisibilityChanged: INVISIBLE");
                // disableView();
                // break;
//            case GONE:
                // Log.i(TAG, "onWindowVisibilityChanged: GONE");
                // disableView();
                // break;
            default:
                // Log.i(TAG, "onWindowVisibilityChanged: default");
                disableView();
                break;
        }
    }

    @Override
    public void enableView() {
        // OpenCV 已经加载成功并且当前Camera关闭
        if (isLoadSuccess && !mEnabled) {
            super.enableView();
        }
    }

    @Override
    public void disableView() {
        if (mEnabled) {
            super.disableView();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "onDetachedFromWindow: ");
        disableView();
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        disableView();
        setCameraIndex(mCameraIndex == CAMERA_ID_BACK ? CAMERA_ID_FRONT : CAMERA_ID_BACK);
        enableView();
    }

    public void setLoadSuccess(boolean loadSuccess){
        this.isLoadSuccess = loadSuccess;
    }

    public void setOnPhotoTakenListener(OnPhotoTakenListener onPhotoTakenListener) {
        this.onPhotoTakenListener = onPhotoTakenListener;
    }
}
