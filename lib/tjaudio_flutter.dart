
import 'dart:async';
import 'dart:ffi';

import 'package:flutter/services.dart';
import 'package:tjaudio_flutter/TJMediaBackGroundModel.dart';


class TjaudioFlutter {
  static const MethodChannel _channel = MethodChannel('com.flutterplugin.tj/flutter_audioPlay');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// default NO
  static void openBackGround(bool openBackGround){
    _channel.invokeMethod('openBackGround',openBackGround);
  }

  ///必须先设置资源再执行 show ，不然 会造成监听错误
  static Future<bool> audioSourceData(List<TJMediaBackGroundModel> dataArray) async{
    var result = await _channel.invokeListMethod('audioSourceData',TJMediaBackGroundModel.toMapList(dataArray));
    return result as bool;
  }

  static void show(){
    _channel.invokeListMethod("show");
  }

  static Future<bool> playWithModel(TJMediaBackGroundModel model) async{
    var result = await _channel.invokeListMethod('playWithModel',model.toMap());
    return result as bool;
  }

  static Future<List<TJMediaBackGroundModel>> getAudioSourceData() async{
    List res = await _channel.invokeListMethod('getAudioSourceData') as List;
    List <TJMediaBackGroundModel> result = <TJMediaBackGroundModel>[];
    for(var i =0;i<res.length;i++){
      result.add(TJMediaBackGroundModel.mapToModel(res[i]));
    }
    return result;
  }
}

