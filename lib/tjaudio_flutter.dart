
import 'dart:async';
import 'dart:ffi';

import 'package:flutter/services.dart';

class TjaudioFlutter {
  static const MethodChannel _channel = MethodChannel('com.flutterplugin.tj/flutter_audioPlay');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');

    return version;
  }

  static void openBackGround(bool openBackGround){
    _channel.invokeMethod('openBackGround',openBackGround);
  }
  static void show(){
    _channel.invokeListMethod('show');
  }
  static void audioSourceData(List audioSourceData){
    _channel.invokeListMethod('audioSourceData',audioSourceData);
  }
  static void playWithModel(Map map){
    _channel.invokeListMethod('playWithModel',map);
  }
}

class TJMediaBackGroundModel{
  //资源图片地址
  String coverUrl = '';

//资源作者
  String auther = '';

//资源标题
  String title = '';

//资源媒体介绍
  String memo = '';

  double playbackTime = 0.0;
  double playbackDuration = 0.0;

//网络资源地址
  String mediaUrl = '';

//本地资源路径
  String mediaLocalPath = '';

// 是否为无效资源
  bool invalidMedia = true;

//媒体资源id 对应相关文章或者短视频Id
  String mediaId  = '';

}
