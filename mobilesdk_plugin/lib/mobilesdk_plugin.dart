import 'dart:async';
import 'dart:io';
import 'package:flutter/services.dart';

import 'mobilesdk_plugin_platform_interface.dart';

class MobilesdkPlugin {
  static const MethodChannel _channel =
      MethodChannel('tungstenMobileSdkPlugin');

  // StreamController for sending Base64 image string
  static final StreamController<String> _imageStreamController =
      StreamController<String>.broadcast();

  // Expose the stream for main.dart to listen
  static Stream<String> get imageStream => _imageStreamController.stream;

  Future<String?> getMobileSDKVersion() async {
    final version = await _channel.invokeMethod<String>('getSDKVersion');
    return version;
  }

 Future<bool> setSDKLicense(String licenseKey) async {
    final bool result =
        await _channel.invokeMethod('setSDKLicense', {'license': licenseKey});
    return result;
  }

  static Future<void> closeStream() async {
    await _imageStreamController.close();
  }

  Future<void> startCapture(String captureType) async {
    try {
      await _channel.invokeMethod('startCapture', {'captureType': captureType});
    } on PlatformException catch (e) {
      // Show error if invalid capture type is passed
      print("Error starting capture: ${e.message}");
      _showError(e.message ?? "An unknown error occurred.");
    }
  }

  void _showError(String message) {
    print("Error: $message");
  }

  MobilesdkPlugin() {
    _channel.setMethodCallHandler(_handleNativeCallback);
  }

  // Handle native callback and send Base64 string
  Future<void> _handleNativeCallback(MethodCall call) async {
    print("entered callback." + call.method) ;
    if (call.method == "onImageCaptured") {
      if(Platform.isAndroid) {
        Map<String, dynamic> result = Map<String, dynamic>.from(call.arguments);
        String? base64String = result['base64String'];
        String? backPressed = result['onBackPressed'];

        print("Base64 string: $base64String");
        print("Back pressed value: '$backPressed'");
        print("Received in Flutter"); // Added log here to confirm receipt

        if (base64String != null) {
          try {
            print("Base64 string length: ${base64String.length}");
            if (base64String.isNotEmpty) {
              _imageStreamController.add(base64String);
              print("Base64 string added to stream");
            } else if (backPressed != null && backPressed == "onBackPressedValue") {
              print("Adding back pressed value to stream");
              _imageStreamController.add(""); // Send empty string for back press
            } else {
              print("Base64 string is empty");
            }
          } catch (e) {
            print("Error reading Base64 file: $e");
          }
        }
      } else {
        // iOS case
        String base64String = call.arguments["image"];
        _imageStreamController.add(base64String);
      }
    }
  }

  //  Add this function to listen for native callbacks
  void setMethodCallHandler(Future<void> Function(MethodCall) handler) {
    _channel.setMethodCallHandler(handler);
  }
}
