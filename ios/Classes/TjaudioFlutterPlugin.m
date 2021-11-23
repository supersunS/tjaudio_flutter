//
//  TJAudioPlayPlugin.m
//  tjaudio_flutter
//
//  Created by SuperSun on 2021/11/23.
//

#import "TjaudioFlutterPlugin.h"
#import "TJAudioPlayManager.h"
#import "TJAudioPlayViewManger.h"

static NSString *const CHANNEL_NAME = @"com.flutterplugin.tj/flutter_audioPlay";

@implementation TjaudioFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel =
        [FlutterMethodChannel methodChannelWithName:CHANNEL_NAME
                                    binaryMessenger:[registrar messenger]];
    TjaudioFlutterPlugin *instance = [TjaudioFlutterPlugin new];
    [registrar addMethodCallDelegate:instance channel:channel];
}

-(void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result{
    NSString *method = [call method];
    id arguments = [call arguments];
    NSLog(@"~~~~method~~~~%@",method);
    NSLog(@"~~~~arguments~~~~~%@",arguments);
    
    if([method isEqualToString:@"openBackGround"]){
        [TJAudioPlayViewManger openBackGround:[[call arguments]boolValue]];
    }else if ([method isEqualToString:@"show"]){
        [TJAudioPlayViewManger show];
    }else if ([method isEqualToString:@"audioSourceData"]){
        NSMutableArray<TJMediaBackGroundModel *> *res = [[NSMutableArray alloc]init];
        if([arguments isKindOfClass:[NSArray class]]){
            for(int i =0;i<[((NSArray *)arguments) count];i++){
                NSDictionary *dict = [((NSArray *)arguments) objectAtIndex:i];
                TJMediaBackGroundModel *model = [[TJMediaBackGroundModel alloc]init];
                model.mediaId = [dict objectForKey:@"mediaId"];
                model.coverUrl = [dict objectForKey:@"coverUrl"];
                model.auther = [dict objectForKey:@"auther"];
                model.mediaUrl = [dict objectForKey:@"mediaUrl"];
                [res addObject:model];
            }
        }
        [TJAudioPlayViewManger audioSourceData:res];
    }else if ([method isEqualToString:@"playWithModel"]){
        if([arguments isKindOfClass:[NSDictionary class]]){
            NSDictionary *dict = (NSDictionary *)arguments;
            TJMediaBackGroundModel *model = [[TJMediaBackGroundModel alloc]init];
            model.mediaId = [dict objectForKey:@"mediaId"];
            model.coverUrl = [dict objectForKey:@"coverUrl"];
            model.auther = [dict objectForKey:@"auther"];
            model.mediaUrl = [dict objectForKey:@"mediaUrl"];
            [TJAudioPlayViewManger playWithModel:model];
        }
    }
    
}

@end
