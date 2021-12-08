import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:tjaudio_flutter/TJAudioPlayView.dart';
import 'package:tjaudio_flutter/TJAudioPlayViewManager.dart';
import 'package:tjaudio_flutter/TJMediaBackGroundModel.dart';
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
        body:Stack(
          children: [

            Center(
              child:GestureDetector(
                child: Container(
                  width: 100,
                  height: 100,
                  color: Colors.orange,
                  alignment: Alignment.center,
                  child: Text("play",),
                ),
                onTap: _tapClick,
              ),
            ),
            Container(
              child: TJAudioPlayView(),
            ),
          ],
        ),
      ),
    );
  }

  void _tapClick(){
    // Future<String> loadString = DefaultAssetBundle.of(context).loadString("assets/data/video.json");
    //
    // loadString.then((String value){
    //   List videolist = json.decode(value); // 解码
    //   List<TJMediaBackGroundModel> resList = [];
    //   for(int i=0;i<videolist.length;i++){
    //     TJMediaBackGroundModel model = TJMediaBackGroundModel.mapToModel(videolist[i]);
    //     resList.add(model);
    //   }
    //   TJAudioPlayViewManager.audioSourceData(resList);
    //   TJAudioPlayViewManager.playWithModel(resList.first);
    // });
    TJAudioPlayViewManager.openBackGround(true);
    TJAudioPlayViewManager.show();

  }
}



