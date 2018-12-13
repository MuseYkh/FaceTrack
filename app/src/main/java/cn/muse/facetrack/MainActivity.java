package cn.muse.facetrack;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "FaceTrack";

    //    static {
//        System.loadLibrary("native-lib");
//    }

    private JavaCameraView jcvSurfaceView;

    private CascadeClassifier cascadeClassifier;
    private Mat rgb;
    private boolean isFrontCamera = false;

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV加载成功");
                    try {
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(cascadeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();
                        cascadeClassifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
                        if (cascadeClassifier.empty()) {
                            Log.e(TAG, "级联分类器加载失败");
                            cascadeClassifier = null;
                        } else {
                            Log.i(TAG, "级联分类器加载成功");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "未找到级联分类器");
                    }
                    jcvSurfaceView.enableView();
                }
                break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 初始化OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (jcvSurfaceView != null) {
            jcvSurfaceView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (jcvSurfaceView != null) {
            jcvSurfaceView.disableView();
        }
    }

    private void initView() {
        jcvSurfaceView = (JavaCameraView) findViewById(R.id.jcv_surface_view);
        jcvSurfaceView.setCvCameraViewListener(this);
        if (isFrontCamera) {
            jcvSurfaceView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        } else {
            jcvSurfaceView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        rgb = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat gray = inputFrame.gray();
        rgb = inputFrame.rgba();
        // 旋转输入帧
        if (isFrontCamera) {
            Core.rotate(rgb, rgb, Core.ROTATE_90_COUNTERCLOCKWISE);
            Core.rotate(gray, gray, Core.ROTATE_90_COUNTERCLOCKWISE);
            Core.flip(rgb, rgb, 1);
            Core.flip(gray, gray, 1);
        } else {
            Core.rotate(rgb, rgb, Core.ROTATE_90_CLOCKWISE);
            Core.rotate(gray, gray, Core.ROTATE_90_CLOCKWISE);
        }

        // 在帧中检测人脸
        MatOfRect faces = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(gray, faces, 1.1, 2, 2,
                    new Size(200, 200), new Size());
        }

        Rect[] faceArray = faces.toArray();
        for (int i = 0; i < faceArray.length; i++) {
            Imgproc.rectangle(rgb, faceArray[i].tl(), faceArray[i].br(), new Scalar(100), 3);
        }
        return rgb;
    }

    public void onClick(View view) {

    }
}
