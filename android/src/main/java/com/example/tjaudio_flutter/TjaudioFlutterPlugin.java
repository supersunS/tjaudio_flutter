package com.example.tjaudio_flutter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.StandardMessageCodec;


/**
 * TjaudioFlutterPlugin
 */
public class TjaudioFlutterPlugin implements FlutterPlugin, MethodCallHandler {

  final static String  CHANNEL_NAME = "com.flutterplugin.tj/flutter_audioPlay";
  final static String  CHANNEL_NAME_message = "com.flutterplugin.tj/flutter_audioPlay_message";
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private BasicMessageChannel _messagechannel;
  private EventChannel _eventChannel;
  private EventChannel.EventSink _eventSink;
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    _messagechannel = new BasicMessageChannel(flutterPluginBinding.getBinaryMessenger(), CHANNEL_NAME_message, StandardMessageCodec.INSTANCE);
    _messagechannel.setMessageHandler(new BasicMessageChannel.MessageHandler() {
      @Override
      public void onMessage(@Nullable Object message, @NonNull BasicMessageChannel.Reply reply) {
        if(!(message instanceof Map)){
          return;
        }
        Log.d("message",message.toString());
        Map<String, String> map = (Map<String , String >) message;
        String methode = "";
        Object arguments = "";
        if(map.containsKey("methode")){
          methode = map.get("methode");
        }
        if(map.containsKey("arguments")){
          arguments = map.get("arguments");
        }
        Log.d("Method Name",methode);
        Log.d("arguments value",arguments.toString());
        if(methode.equals("")){

        }else if(methode.equals("")){

        }
      }
    });

    _eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(),CHANNEL_NAME);
    _eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
      @Override
      public void onListen(Object arguments, EventChannel.EventSink events) {
          _eventSink = events;
      }
      @Override
      public void onCancel(Object arguments) {

      }
    });
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    _messagechannel.setMessageHandler(null);
    _eventChannel.setStreamHandler(null);
  }
}
