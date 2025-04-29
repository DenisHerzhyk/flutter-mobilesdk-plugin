//
//  SdkPlatoformView.h
//  mobilesdk_plugin
//
//  Created by Priyanka Somisetti on 12/03/25.
//

#import <Flutter/Flutter.h>
#import <UIKit/UIKit.h>
#import <MobileSDK/MobileSDK.h>
#import "CaptureFactory.h"


@interface SdkPlatoformView : NSObject<FlutterPlatformView,kfxKUIImageCaptureControlDelegate>
@property (nonatomic, strong) FlutterMethodChannel *flutterChannel;
@property (nonatomic, strong) FlutterResult captureResult;
- (instancetype)initWithFrame:(CGRect)frame
               viewIdentifier:(int64_t)viewId
                    arguments:(id _Nullable)args
                    registrar:(NSObject<FlutterPluginRegistrar>*)registrar
                      channel:(FlutterMethodChannel *)channel;
- (void)startCaptureWithType:(NSString *)captureType result:(FlutterResult)result;
+ (NSString*)getSDKVersion;

@end
