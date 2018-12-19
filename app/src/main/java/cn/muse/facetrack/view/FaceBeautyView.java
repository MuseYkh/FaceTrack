package cn.muse.facetrack.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import cn.muse.facetrack.R;
import cn.muse.facetrack.detector.ObjectDetector;

/**
 * author: muse
 * created on: 2018/12/18 下午2:57
 * description:
 */
public class FaceBeautyView extends BaseCameraView {

    private static final String TAG = "FaceDetectingView";
    private ObjectDetector mFaceDetector;
    private MatOfRect mFaces;
    private Mat beauty;
    private List<Mat> beautyList = new ArrayList<>();

    public FaceBeautyView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public FaceBeautyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 初始化人脸检测器
     *
     * @param context context
     */
    public void initDetector(Context context) {
        mFaces = new MatOfRect();
        mFaceDetector = new ObjectDetector(context, R.raw.lbpcascade_frontalface, 6, 0.2F, 0.2F, new Scalar(255, 255, 255, 0));
        setCameraIndex(CAMERA_ID_BACK);
    }

    @Override
    public void onOpenCVLoadSuccess() {

    }

    @Override
    public void onOpenCVLoadFail() {

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        // 旋转输入帧
        if (mCameraIndex == CAMERA_ID_FRONT) {
            Core.rotate(mRgba, mRgba, Core.ROTATE_90_COUNTERCLOCKWISE);
            Core.rotate(mGray, mGray, Core.ROTATE_90_COUNTERCLOCKWISE);
            Core.flip(mRgba, mRgba, 1);
            Core.flip(mGray, mGray, 1);
        } else {
            Core.rotate(mRgba, mRgba, Core.ROTATE_90_CLOCKWISE);
            Core.rotate(mGray, mGray, Core.ROTATE_90_CLOCKWISE);
        }

        // 检测人脸
        Rect[] faceArray = mFaceDetector.detectObject(mGray, mFaces);
        if (faceArray != null && faceArray.length > 0) {
            for (Rect rect : faceArray) {
            //矩形标识
            Imgproc.rectangle(mRgba, rect.tl(), rect.br(), mFaceDetector.getRectColor(), 3);
//                if (beauty != null) {
//                    //添加宠萌妆饰
//                    addBeauty((int) rect.tl().y, (int) (rect.tl().x + rect.br().x - beauty.cols()) / 2);
//                }
            }
        }
        //拍照一帧数据回调
        if (onPhotoTakenListener != null) {
            onPhotoTakenListener.onPhotoTaken(mRgba);
        }
        return mRgba;
    }

    /**
     * 添加宠萌效果
     *
     * @param offsetX x坐标偏移量
     * @param offsetY y坐标偏移量
     */
    private void addBeauty(int offsetX, int offsetY) {
        offsetX -= 200;//高度校正
        if (offsetX < 0) {
            offsetX = 0;
        }
        for (int x = 0; x < beauty.rows(); x++) {
            for (int y = 0; y < beauty.cols(); y++) {
                double[] array = beauty.get(x, y);
                if (array[0] != 0) {//过滤全黑像素
                    mRgba.put(x + offsetX, y + offsetY, array);
                }
            }
        }
    }

    /**
     * 选择妆饰
     *
     * @param index index
     */
    public void selectBeauty(int index) {
        if (beautyList.size() == 0) {
            getBeauty();
        }
        beauty = beautyList.get(index);
    }

    /**
     * 获取妆饰list集合
     */
    private void getBeauty() {
        Drawable drawable1 = getResources().getDrawable(R.drawable.bg_decoration_cat, null);
        Bitmap bitmap1 = ((BitmapDrawable) drawable1).getBitmap();
        bitmap1 = Bitmap.createScaledBitmap(bitmap1, 320, 320, true);
        Mat beauty1 = new Mat();
        Utils.bitmapToMat(bitmap1, beauty1);
        beautyList.add(beauty1);
        Drawable drawable2 = getResources().getDrawable(R.drawable.bg_decoration_rabbit, null);
        Bitmap bitmap2 = ((BitmapDrawable) drawable2).getBitmap();
        bitmap2 = Bitmap.createScaledBitmap(bitmap2, 320, 320, true);
        Mat beauty2 = new Mat();
        Utils.bitmapToMat(bitmap2, beauty2);
        beautyList.add(beauty2);
    }

}
