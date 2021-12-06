
import 'dart:async';
import 'dart:ffi';
import 'dart:typed_data';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:tjaudio_flutter/TJMediaBackGroundModel.dart';


class TjaudioFlutter {
  static late ValueChanged _audioPlayStateChangeBlock;
  static late ValueChanged _progressBlock;

  static const BasicMessageChannel _messageChannel = BasicMessageChannel('com.flutterplugin.tj/flutter_audioPlay_message',StandardMessageCodec());
  static const EventChannel _eventChannel = EventChannel("com.flutterplugin.tj/flutter_audioPlay");
  static late StreamSubscription _streamSubscription;

  static Future<String?> get platformVersion async {
    final String? version = await _messageChannel.send({"methode":"getPlatformVersion"});

    return version;
  }

  /// default NO
  static void openBackGround(bool openBackGround){
    _messageChannel.send({"methode":"openBackGround","arguments":openBackGround});
  }

  ///必须先设置资源再执行 show ，不然 会造成监听错误
  static Future<bool> audioSourceData(List<TJMediaBackGroundModel> dataArray) async{
    var result = await _messageChannel.send({"methode":"audioSourceData","arguments":TJMediaBackGroundModel.toMapList(dataArray)});

    return result as bool;
  }

  static void show(){
    _messageChannel.send({"methode":"show"});
  }

  static Future<bool> playWithModel(TJMediaBackGroundModel model) async{
    Map result = await _messageChannel.send({"methode":"playWithModel","arguments":model.toJsonString()}) as Map;
    return result["result"] as bool;
  }


  static void pause(){
    var result = _messageChannel.send({"methode":"pause"});
  }

  static void resume(){
    var result = _messageChannel.send({"methode":"resume"});
  }

  static void destoryView(){
    var result = _messageChannel.send({"methode":"destoryView"});
  }



  static Future<List<TJMediaBackGroundModel>> getAudioSourceData() async{
    List res = await _messageChannel.send({"methode":"getAudioSourceData"}) as List;

    List <TJMediaBackGroundModel> result = <TJMediaBackGroundModel>[];
    for(var i =0;i<res.length;i++){
      result.add(TJMediaBackGroundModel.mapToModel(res[i]));
    }
    return result;
  }

  static Future<Uint8List> imageName(String imageName) async{
    Map map = (await  _messageChannel.send({"methode":"imageName","arguments":imageName})) as Map;
    Uint8List bodyBytes = map['image'] as Uint8List;
    return bodyBytes;
  }

  static Future<bool> getAudioIsPlaying() async {
    Map result = await _messageChannel.send({"methode":"getAudioIsPlaying"}) as Map;
    return result["result"] as bool;
  }

  static void setAudioPlayStateChangeListener(ValueChanged audioPlayStateChangeBlock,ValueChanged progressBlock){
     _messageChannel.send({"methode":"setAudioPlayStateChangeListener"});
     _audioPlayStateChangeBlock = audioPlayStateChangeBlock;
     _progressBlock = progressBlock;
  }

  static void cancelMessageHandler() {
    _streamSubscription.cancel();
  }
  static void setMessageHandler(){
    _streamSubscription = _eventChannel.receiveBroadcastStream().
    listen((dynamic event) {
        Map result = event as Map;
        if(result.containsKey("audioState")){
          _audioPlayStateChangeBlock(result["audioState"]);
        }else if(result.containsKey("progress")){
          _progressBlock(result["progress"]);
          }
        },
        onError: (dynamic error) {
          print('Received error: ${error.message}');
        },
        cancelOnError: true);
  }
}

