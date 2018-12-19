package cn.muse.facetrack.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.muse.facetrack.R;
import cn.muse.facetrack.view.FaceBeautyView;

public class MainActivity extends Activity {

    private static final String TAG = "FaceTrack";
    private static final int IMG_QUALITY = 100;

    private ImageView ivSwitchBtn;
    private ImageView ivTakePhotoBtn;
    private FaceBeautyView fbvSurfaceView;
    private boolean hasInit;//是否初始化过拍照监听
    private boolean doTaken;//拍照标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initOpenCV();
    }

    private void initOpenCV() {
        if (OpenCVLoader.initDebug()) {
            fbvSurfaceView.initDetector(getApplicationContext());
            fbvSurfaceView.setLoadSuccess(true);
            fbvSurfaceView.enableView();
            fbvSurfaceView.selectBeauty(1);
        } else {
            Log.e(TAG, "OpenCV初始化失败");
        }
    }

    private void initView() {
        fbvSurfaceView = (FaceBeautyView) findViewById(R.id.fbv_surface_view);
        ivSwitchBtn = (ImageView) findViewById(R.id.iv_switch_btn);
        ivTakePhotoBtn = (ImageView) findViewById(R.id.iv_take_photo_btn);
        ivSwitchBtn.setOnClickListener(v -> {
            fbvSurfaceView.switchCamera();
        });
        ivTakePhotoBtn.setOnClickListener(v -> {
            takePhoto();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fbvSurfaceView.disableView();
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        if (!hasInit) {
            initPhotoListener();
            hasInit = true;
        }
        doTaken = true;
    }

    private void initPhotoListener() {
        fbvSurfaceView.setOnPhotoTakenListener(frameData -> {
            // 为了造成不必要的资源损耗
            if (doTaken && frameData != null) {
                doTaken = false;
                savePicture(frameData);
            }
        });
    }

    /**
     * 保存图片
     *
     * @param frameData 帧数据
     */
    private void savePicture(Mat frameData) {
        Bitmap bitmap = Bitmap.createBitmap(frameData.width(), frameData.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameData, bitmap);
        String fileName = System.currentTimeMillis() + ".jpg";
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + fileName;
        File file = new File(filePath);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMG_QUALITY, outputStream);
            if (file.exists()) {
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), filePath, fileName, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
