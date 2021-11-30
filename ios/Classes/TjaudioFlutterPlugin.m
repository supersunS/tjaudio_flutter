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
static NSString *const CHANNEL_NAME_message = @"com.flutterplugin.tj/flutter_audioPlay_message";

@implementation TjaudioFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel =
    [FlutterMethodChannel methodChannelWithName:CHANNEL_NAME
                                binaryMessenger:[registrar messenger]];
    TjaudioFlutterPlugin *instance = [TjaudioFlutterPlugin new];
    [registrar addMethodCallDelegate:instance channel:channel];
    
    FlutterBasicMessageChannel *messagechannel = [FlutterBasicMessageChannel messageChannelWithName:CHANNEL_NAME_message binaryMessenger:[registrar messenger]];
    [messagechannel setMessageHandler:^(NSDictionary * _Nullable message, FlutterReply  _Nonnull callback) {
        NSString *method = [message objectForKey:@"methode"];
        id arguments = [message objectForKey:@"arguments"];
        NSLog(@"~~~~method~~~~%@",method);
        NSLog(@"~~~~arguments~~~~~%@",arguments);
        
        
        if ([method isEqualToString:@"imageName"]){
            if([arguments isKindOfClass:[NSString class]]){
                UIImage *image = [TJAudioPlayManager tj_imageNamed:arguments];
                if(image){
                    NSData *imageData=UIImagePNGRepresentation(image);
                    callback(@{@"image":imageData});
                }
                
            }
        }
        
    }];
    
    
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
