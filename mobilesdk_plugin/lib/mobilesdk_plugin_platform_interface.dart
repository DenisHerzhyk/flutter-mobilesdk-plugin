import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'mobilesdk_plugin_method_channel.dart';

abstract class MobilesdkPluginPlatform extends PlatformInterface {
  /// Constructs a MobilesdkPluginPlatform.
  MobilesdkPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static MobilesdkPluginPlatform _instance = MethodChannelMobilesdkPlugin();

  /// The default instance of [MobilesdkPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelMobilesdkPlugin].
  static MobilesdkPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [MobilesdkPluginPlatform] when
  /// they register themselves.
  static set instance(MobilesdkPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
