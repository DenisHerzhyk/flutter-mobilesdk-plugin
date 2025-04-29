import 'package:flutter_test/flutter_test.dart';
import 'package:mobilesdk_plugin/mobilesdk_plugin.dart';
import 'package:mobilesdk_plugin/mobilesdk_plugin_platform_interface.dart';
import 'package:mobilesdk_plugin/mobilesdk_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockMobilesdkPluginPlatform
    with MockPlatformInterfaceMixin
    implements MobilesdkPluginPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final MobilesdkPluginPlatform initialPlatform =
      MobilesdkPluginPlatform.instance;

  test('$MethodChannelMobilesdkPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelMobilesdkPlugin>());
  });
}
