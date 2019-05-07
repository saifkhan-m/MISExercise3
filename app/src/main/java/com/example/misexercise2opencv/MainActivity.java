package com.example.misexercise2opencv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.CheckBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2{


    //NOTE: all information and references about the setup of openCV have been mentioned in the readme.md file in the project as requested


    private CameraBridgeViewBase        mOpenCvCameraView;
    private CascadeClassifier           cascadeClassifier;
    private int                         absoluteFaceSize;
    private CheckBox                    nose;
    private CheckBox                    rectangle;
    private static final String         TAG =   "OCVSample::Activity";



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                    Log.i(TAG, "OpenCV NOt loaded succesfully");
                } break;
            }
        }
    };


    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        // before opening the CameraBridge, we need the Camera Permission on newer Android versions

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x123);
        } else {
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surface_view);
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }
    }



    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    public void onCameraViewStarted(int width, int height) {
        // The faces will be a 20% of the height of the screen
        absoluteFaceSize = (int) (height * 0.2);
    }


    public void onCameraViewStopped() {
    }



    //Inspiration from
    // https://docs.opencv.org/3.4.1/d7/d8b/tutorial_py_face_detection.html
    // https://www.mirkosertic.de/blog/2013/07/realtime-face-detection-on-android-using-opencv/
    // frame colour to RGB from, https://stackoverflow.com/questions/39957955/how-to-convert-the-mat-object-to-a-bitmap-while-perserving-the-color
    // frame corrected to be in portrait, https://stackoverflow.com/questions/14816166/rotate-camera-preview-to-portrait-android-opencv-camera
    // edited class CameraBridgeViewBase.java 's method deliverAndDrawFrame() for the above.
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat gray = inputFrame.gray();
        Mat color = inputFrame.rgba();
        nose = (CheckBox) findViewById(R.id.nose);
        rectangle = (CheckBox) findViewById(R.id.rectangle);

        Imgproc.cvtColor(color, color, Imgproc.COLOR_BGRA2RGBA);

        cascadeClassifier = new CascadeClassifier(initAssetFile("haarcascade_frontalface_default.xml"));
        MatOfRect faces=new MatOfRect();

        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(gray, faces, 1.1, 1, 1,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }

        Rect[] facesArray = faces.toArray();

        for (int i = 0; i < facesArray.length; i++) {
            // Drawing Functions on the IMGPROC
            // https://docs.opencv.org/3.0-beta/modules/core/doc/drawing_functions.html
            if(rectangle.isChecked())
                Imgproc.rectangle(color, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
            if(nose.isChecked())
                Imgproc.circle(color,new Point(facesArray[i].x+(facesArray[i].width/2),facesArray[i].y+(facesArray[i].width/2)),facesArray[i].height/4,new Scalar(255, 0, 0, 0),-1);
        }
        return color;
    }



    public String initAssetFile(String filename)  {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) try {
            InputStream is = getAssets().open(filename);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data); os.write(data); is.close(); os.close();
        } catch (IOException e) { e.printStackTrace(); }
        Log.d(TAG,"prepared local file: "+filename);
        return file.getAbsolutePath();
    }
}
