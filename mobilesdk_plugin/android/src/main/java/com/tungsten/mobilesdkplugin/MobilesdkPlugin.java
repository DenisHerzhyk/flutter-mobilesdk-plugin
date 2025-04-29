package com.tungsten.mobilesdkplugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.graphics.Bitmap;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;

import com.kofax.kmc.kut.utilities.AppContextProvider;
import com.kofax.kmc.kut.utilities.Licensing;
import com.kofax.kmc.kut.utilities.error.ErrorInfo;
import com.kofax.kmc.kut.utilities.SdkVersion;

import com.kofax.kmc.ken.engines.data.Image;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** MobilesdkPlugin */
public class MobilesdkPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private static final String TAG = "MobilesdkPlugin";
  private MethodChannel channel;
  private Activity activity;
  private Context applicationContext;

  // Track license validity.
  private boolean isLicenseValid = false;
  private static final int CAPTURE_REQUEST_CODE = 1001;
  private Result pendingResult;
  private static Image captureImage;
  public static void setCapturedImageBitmap(Image img) {
    if(captureImage != null){
      if((captureImage.getImageBitmap()!=null)&&(!captureImage.getImageBitmap().isRecycled())){
        captureImage.imageClearBitmap();
        captureImage = null;
      }
    }
    captureImage = img;
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    applicationContext = binding.getApplicationContext();
    channel = new MethodChannel(binding.getBinaryMessenger(), "tungstenMobileSdkPlugin");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    binding.addActivityResultListener(this::handleActivityResult);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }

  public void setActivity(Activity activity) {
    this.activity = activity;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("setSDKLicense")) {
      String license = call.argument("license");
      setSDKLicense(license, result);
    } else if (call.method.equals("startCapture")) {
      // Only allow capture if the license is valid.
      if (!isLicenseValid) {
        Toast.makeText(applicationContext, "SDK license is not valid. Please set a valid license", Toast.LENGTH_LONG).show();
        result.error("INVALID_LICENSE", "SDK license is not valid. Please set a valid license first.", null);
        return;
      }
      String captureType = call.argument("captureType");
      startCaptureActivity(result, captureType);
    } else if (call.method.equals("getSDKVersion")) {
      String sdkVersion = SdkVersion .getSdkVersion(); // Adjust class name as needed
      Log.d("MobilesdkPlugin", "SDK Version: " + sdkVersion);
      result.success(sdkVersion);
    } else {
      result.notImplemented();
    }
  }

  private void setSDKLicense(String license, Result result) {
    try {
      // Optionally, set the context required by your SDK.
      AppContextProvider.setContext(applicationContext);
      ErrorInfo licenseInfo = Licensing.setMobileSDKLicense(license);
      if (licenseInfo == ErrorInfo.KMC_EV_LICENSE_EXPIRED) {
        Log.i(TAG, "License expired. Days left: " + Licensing.getDaysRemaining());
        Toast.makeText(applicationContext, licenseInfo.getErrMsg(), Toast.LENGTH_LONG).show();
        isLicenseValid = false;
        result.error("LICENSE_EXPIRED", licenseInfo.getErrMsg(),Licensing.getDaysRemaining());
      } else if (licenseInfo == ErrorInfo.KMC_EV_LICENSING) {
        Toast.makeText(applicationContext, licenseInfo.getErrMsg(), Toast.LENGTH_LONG).show();
        isLicenseValid = false;
        result.error("LICENSING_ERROR", licenseInfo.getErrMsg(), null);
      } else {
        isLicenseValid = true;
        result.success(true);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error setting license", e);
      isLicenseValid = false;
      result.error("ERROR_SETTING_LICENSE", e.getMessage(), null);
    }
  }

  private void startCaptureActivity(Result result, String captureType) {
    if (activity != null) {
      Intent intent = new Intent();
      // Ensure that the CaptureActivity class exists in the package below.
      intent.setClassName(activity.getPackageName(), "com.tungsten.mobilesdkplugin.CaptureActivity");
      intent.putExtra("captureType", captureType);
      try {
        //activity.startActivity(intent);
        pendingResult = result;
        activity.startActivityForResult(intent, CAPTURE_REQUEST_CODE);
        Log.d("MobilesdkPlugin", "Started CaptureActivity with requestCode=" + CAPTURE_REQUEST_CODE);
        result.success("CaptureActivity Started with captureType: " + captureType);
      } catch (Exception e) {
        result.error("ACTIVITY_NOT_FOUND", "Could not start CaptureActivity: " + e.getMessage(), null);
      }
    } else {
      result.error("NO_ACTIVITY", "Activity is not attached", null);
    }
  }

  private String convertBitmapToBase64(Bitmap bitmap) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
    byte[] byteArray = byteArrayOutputStream .toByteArray();
    // Convert to Base64
    return Base64.encodeToString(byteArray, Base64.NO_WRAP);
  }

  private boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CAPTURE_REQUEST_CODE && pendingResult != null) {
      if (resultCode == Activity.RESULT_OK) {
        if(captureImage != null && captureImage.getImageBitmap() !=null && !captureImage.getImageBitmap().isRecycled()) {
          String base64String = convertBitmapToBase64(captureImage.getImageBitmap());
          Log.d("MobilesdkPlugin", "Received in plugin: base64String = " + base64String);
          Map<String, Object> resultMap = new HashMap<>();
          resultMap.put("base64String", base64String);
          captureImage.imageClearBitmap();
          captureImage = null;
          channel.invokeMethod("onImageCaptured", resultMap);
        } else {
          Map<String, Object> resultMap = new HashMap<>();
          resultMap.put("base64String", "");
          channel.invokeMethod("onImageCaptured", resultMap);
        }

        Log.d("MobilesdkPlugin", "sending to flutter : base64String = ");
        //pendingResult.success("Capture completed");
      } else {
        Log.d("MobilesdkPlugin", "Capture failed or canceled: resultCode=" + resultCode);
        String receivedValue = data.getStringExtra("onBackPressed");
        //pendingResult.error("CAPTURE_FAILED", "Capture failed or canceled", null);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("base64String", "");
        resultMap.put("onBackPressed",receivedValue);
        channel.invokeMethod("onImageCaptured", resultMap); // Notify Flutter
      }
      pendingResult = null;
      return true;
    }
    return false;
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}