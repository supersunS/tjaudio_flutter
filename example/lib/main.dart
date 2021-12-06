import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:tjaudio_flutter/TJAudioPlayView.dart';
import 'package:tjaudio_flutter/tjaudio_flutter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await TjaudioFlutter.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('audioPlay Demo'),
        ),
        body:Center(
          child: Container(
            child: TJAudioPlayView(),
          ),
        ),
      ),
    );
  }
}


/*
var model = TJMediaBackGroundModel.mapToModel(
      { "mediaId":"1",
      "coverUrl":"https://tianjiutest.oss-cn-beijing.aliyuncs.com/tojoy/tojoyClould/backstageSystem/image/1631168736433.jpg",
      "auther":"AudioPlayDemo",
      "mediaUrl":"https://tianjiutest.oss-cn-beijing.aliyuncs.com/tojoy/tojoyClould/serverUpload/202109/01/image/1630459636944.mp3"}
);
TJAudioPlayViewManager.openBackGround(true);
TJAudioPlayViewManager.audioSourceData([model]);
TJAudioPlayViewManager.show();
TJAudioPlayViewManager.playWithModel(model);
 */
