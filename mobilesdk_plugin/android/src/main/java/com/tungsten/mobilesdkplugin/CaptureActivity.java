package com.tungsten.mobilesdkplugin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;

//import com.tungsten.mobilesdkplugin.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.graphics.Color;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kofax.kmc.kui.uicontrols.CameraInitializationEvent;
import com.kofax.kmc.kui.uicontrols.CameraInitializationListener;
import com.kofax.kmc.kui.uicontrols.ImageCaptureView;
import com.kofax.kmc.kui.uicontrols.IImagesCaptured;
import com.kofax.kmc.kui.uicontrols.ImageCapturedEvent;
import com.kofax.kmc.kui.uicontrols.ImageCapturedListener;
import com.kofax.kmc.kui.uicontrols.ImageCapturedEventListener;
import com.kofax.kmc.kui.uicontrols.captureanimations.FixedAspectRatioCaptureExperience;
import com.kofax.kmc.kui.uicontrols.captureanimations.FixedAspectRatioExperienceCriteriaHolder;
import com.kofax.kmc.kui.uicontrols.captureanimations.PassportCaptureExperience;
import com.kofax.kmc.kui.uicontrols.captureanimations.PassportCaptureExperienceCriteriaHolder;
import com.kofax.kmc.kui.uicontrols.captureanimations.DocumentBaseCaptureExperience;
import com.kofax.kmc.kui.uicontrols.data.Flash;
import com.kofax.kmc.kut.utilities.AppContextProvider;
import com.kofax.kmc.kut.utilities.Licensing;
import com.kofax.kmc.kut.utilities.SdkVersion;
import com.kofax.kmc.kut.utilities.error.ErrorInfo;
import com.kofax.kmc.ken.engines.data.Image;

import io.flutter.embedding.android.FlutterActivity;

public class CaptureActivity extends FlutterActivity {
    private static final String TAG = "CaptureActivity";
    private String captureType;
    private FrameLayout imageCaptureContainer;
    private ImageCaptureView imageCaptureView;
    private ProgressDialog mProgressDialog;
    private DocumentBaseCaptureExperience captureExperience;
    private FixedAspectRatioCaptureExperience fixedcaptureExperience;
    private PassportCaptureExperience passportCaptureExperience;

    private final CameraInitializationListener cameraInitializationListener = new CameraInitializationListener() {
        @Override
        public void onCameraInitialized(@NonNull CameraInitializationEvent event) {
            runOnUiThread(() -> {
                imageCaptureView.setFlash(Flash.OFF);
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(CaptureActivity.this);
        if (checkCameraPermission()){
            SetUp();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        onRequestPermission(new String[]{android.Manifest.permission.CAMERA}, 1);
    }

    private void onRequestPermission(String[] permission, int requestType) {
        ActivityCompat.requestPermissions(CaptureActivity.this, permission, requestType);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void SetUp() {
        imageCaptureContainer = new FrameLayout(this);
        Toolbar toolbar = new Toolbar(this);
        TextView title = new TextView(this);
        title.setText("Capture");
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        toolbar.addView(title);
        toolbar.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        toolbar.setBackgroundColor(Color.parseColor("#00558C"));

        // Set layout parameters (optional - MATCH_PARENT fills the screen).
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        imageCaptureContainer.setLayoutParams(params);

        // Optionally, set a background color (here transparent).
        //container.setBackgroundColor(Color.TRANSPARENT);
        setContentView(imageCaptureContainer);
        imageCaptureContainer.addView(toolbar);
        //setContentView(R.layout.activity_capture);
        //imageCaptureContainer = findViewById(R.id.capture_container);
        createImageCapture();
        createImageCaptureExperience();
        //createQuickExtractor();
    }

    private void createImageCapture() {
        //mProgressDialog.setMessage("loading");
        //mProgressDialog.show();
        imageCaptureView = new ImageCaptureView(this);
        imageCaptureView.addCameraInitializationListener(cameraInitializationListener);
        imageCaptureContainer.addView(imageCaptureView, 0);
    }

    private void createImageCaptureExperience() {
        Intent intent = getIntent();
        captureType = intent.getStringExtra("captureType");
        Log.d(TAG, "CaptureActivity launched with captureType: " + captureType);
        if(captureType.equals("ID")) {
            captureExperience = new FixedAspectRatioCaptureExperience(imageCaptureView, new FixedAspectRatioExperienceCriteriaHolder());
            captureExperience.takePicture();
        } else {
            captureExperience = new PassportCaptureExperience(imageCaptureView, new PassportCaptureExperienceCriteriaHolder());
            captureExperience.takePicture();
        }
        attachImageCapturedListener();
    }

    private void attachImageCapturedListener() {
        captureExperience.addOnImageCapturedEventListener(new ImageCapturedEventListener() {
            @Override
            public void onImageCapturedEvent(IImagesCaptured iImagesCaptured) {
                try {
                    Image image = iImagesCaptured.getPrimaryImage();
                    MobilesdkPlugin.setCapturedImageBitmap(image);
                    if (image != null) {
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_CANCELED, resultIntent);
                        finish();
                        Log.e("CaptureActivity", "Bitmap is null");
                    }
                } catch (Exception e) {
                    Log.e("CaptureActivity", "Error processing captured image", e);
                    //  finish();
                }
            }
        });
    }

    @Override
    public void finish() {
        Log.d(TAG, "Custom finish called");
        // Add custom logic here if needed
        super.finish();  // Call the parent implementation to actually close the activity
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        destroyImageCapture();
        destroyImageCaptureExperience();
        super.onDestroy();
    }

    private void destroyImageCaptureExperience() {
        captureExperience.stopCapture();
        captureExperience.destroy();
    }

    private void destroyImageCapture() {
        imageCaptureView.removeAllViews();
        imageCaptureView.removeCameraInitializationListener(cameraInitializationListener);
        imageCaptureContainer.removeView(imageCaptureView);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back button pressed in CaptureActivity");
        Intent resultIntent = new Intent();
        resultIntent.putExtra("onBackPressed","onBackPressedValue" );
        setResult(Activity.RESULT_CANCELED, resultIntent); // Indicate cancellation
        finish(); // Close the activity
    }


}