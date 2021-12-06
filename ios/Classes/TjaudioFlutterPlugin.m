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

@interface TjaudioFlutterPlugin ()

@property (nonatomic, strong) FlutterEventSink eventSink;
@end

@implementation TjaudioFlutterPlugin
    

+ (instancetype)sharedInstance {
    static TjaudioFlutterPlugin *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
    
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
        }else if([method isEqualToString:@"openBackGround"]){
            [TJAudioPlayManager openBackGround:[arguments boolValue]];
        }else if ([method isEqualToString:@"show"]){
            [TJAudioPlayViewManger show];
        }else if ([method isEqualToString:@"audioSourceData"]){
            NSMutableArray<TJMediaBackGroundModel *> *resArray = [[NSMutableArray alloc]init];
            if([arguments isKindOfClass:[NSArray class]]){
                for(int i =0;i<[((NSArray *)arguments) count];i++){
                    NSDictionary *dict = [((NSArray *)arguments) objectAtIndex:i];
                    TJMediaBackGroundModel *model = [[TJMediaBackGroundModel alloc]init];
                    model.mediaId = [dict objectForKey:@"mediaId"];
                    model.coverUrl = [dict objectForKey:@"coverUrl"];
                    model.auther = [dict objectForKey:@"auther"];
                    model.mediaUrl = [dict objectForKey:@"mediaUrl"];
                    [resArray addObject:model];
                }
            }
            BOOL res = [TJAudioPlayManager audioSourceData:resArray];
            callback(@{@"result":@(res)});
        }else if ([method isEqualToString:@"playWithModel"]){
            if([arguments isKindOfClass:[NSString class]]){
                NSDictionary *dict = [self dictionaryWithJsonString:arguments];
                TJMediaBackGroundModel *model = [[TJMediaBackGroundModel alloc]init];
                model.mediaId = [dict objectForKey:@"mediaId"];
                model.coverUrl = [dict objectForKey:@"coverUrl"];
                model.auther = [dict objectForKey:@"auther"];
                model.mediaUrl = [dict objectForKey:@"mediaUrl"];
                BOOL res =  [TJAudioPlayManager playWithModel:model];
                callback(@{@"result":@(res)});
            }
        }else if([method isEqualToString:@"setAudioPlayStateChangeListener"]){
            [TJAudioPlayManager audioPlayStateChangeListener:^(STKAudioPlayerState audioState) {
                [TjaudioFlutterPlugin sharedInstance].eventSink(@{@"audioState":@(audioState)});
            } audioPlayProgress:^(float progress) {
                [TjaudioFlutterPlugin sharedInstance].eventSink(@{@"progress":@(progress)});
            }];
        }else if ([method isEqualToString:@"getAudioIsPlaying"]){
            BOOL res =  [TJAudioPlayManager getAudioIsPlaying];
            callback(@{@"result":@(res)});
        }else if ([method isEqualToString:@"resume"]){
            [TJAudioPlayManager resume];
        }else if ([method isEqualToString:@"pause"]){
            [TJAudioPlayManager pause];
        }else if([method isEqualToString:@"destoryView"]){
            [TJAudioPlayManager destory];
        }
    }];
    
    FlutterEventChannel *eventChannel = [FlutterEventChannel eventChannelWithName:CHANNEL_NAME binaryMessenger:[registrar messenger]];
    TjaudioFlutterPlugin *instance = [TjaudioFlutterPlugin sharedInstance];
    [eventChannel setStreamHandler:instance];

}

- (FlutterError* _Nullable)onListenWithArguments:(id _Nullable)arguments eventSink:(FlutterEventSink)eventSink {
    self.eventSink = eventSink;
    return nil;
}
 
- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments {
    self.eventSink = nil;
    return nil;
}


+ (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString
{
    if (jsonString == nil) {
        return nil;
    }
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if(err)
    {
        NSLog(@"json解析失败：%@",err);
        return nil;
    }
    return dic;
}

#pragma mark 字典转化字符串
+(NSString*)dictionaryToJson:(NSDictionary *)dic
{
    NSError *parseError = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dic options:NSJSONWritingPrettyPrinted error:&parseError];
    
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}
@end
