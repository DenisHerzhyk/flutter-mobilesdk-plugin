//
//  SdkPlatoformFactory.h
//  mobilesdk_plugin
//
//  Created by Priyanka Somisetti on 12/03/25.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>
#import "SdkPlatoformView.h"

@interface SdkPlatformFactory : NSObject<FlutterPlatformViewFactory>

@property (nonatomic, strong) FlutterMethodChannel *channel;
@property (nonatomic, strong, nullable) SdkPlatoformView *platformView; // Store the view reference
- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar;
@end

