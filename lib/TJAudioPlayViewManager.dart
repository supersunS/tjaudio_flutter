import 'dart:typed_data';
import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:tjaudio_flutter/tjaudio_flutter.dart';
import 'package:tjaudio_flutter/TJMediaBackGroundModel.dart';

class TJAudioPlayViewManager {
  TJAudioPlayViewManager._privateConstructor();

  static final TJAudioPlayViewManager _instance =
      TJAudioPlayViewManager._privateConstructor();

  factory TJAudioPlayViewManager() {
    return _instance;
  }

  void _openBackGround(bool openBackGround) {
    TjaudioFlutter.openBackGround(openBackGround);
  }

  ///必须先设置资源再执行 show ，不然 会造成监听错误
  Future<bool> _audioSourceData(List<TJMediaBackGroundModel> dataArray) async {
    return TjaudioFlutter.audioSourceData(dataArray);
  }

  void _show() {
    TjaudioFlutter.show();
  }

  Future<bool> _playWithModel(TJMediaBackGroundModel model) async {
    return TjaudioFlutter.playWithModel(model);
  }

  Future<List<TJMediaBackGroundModel>> _getAudioSourceData() async {
    return TjaudioFlutter.getAudioSourceData();
  }

  Future<Uint8List> _imageName(String imageName) async {
    return TjaudioFlutter.imageName(imageName);
  }

  //=====================================================================================

  /// default NO
  static void openBackGround(bool openBackGround) {
    TJAudioPlayViewManager._instance._openBackGround(openBackGround);
  }

  ///必须先设置资源再执行 show ，不然 会造成监听错误
  static Future<bool> audioSourceData(
      List<TJMediaBackGroundModel> dataArray) async {
    return TJAudioPlayViewManager._instance._audioSourceData(dataArray);
  }

  static void show() {
    TJAudioPlayViewManager._instance._show();
  }

  static Future<bool> playWithModel(TJMediaBackGroundModel model) async {
    return TJAudioPlayViewManager._instance._playWithModel(model);
  }

  static Future<List<TJMediaBackGroundModel>> getAudioSourceData() async {
    return TJAudioPlayViewManager._instance._getAudioSourceData();
  }

  static Future<Uint8List> imageName(String imageName) async {
    return TJAudioPlayViewManager._instance._imageName(imageName);
  }
}

class _TJAudioPlayView extends State<TJAudioPlayView>
    with SingleTickerProviderStateMixin {

  bool isShow = true;

  final double kBottomPadding = 88.0; //距顶部的偏移
  final double kDefaultWidth = 48.0; //距底部
  final double kDefaultMAXWidth = 200.0; //距底部

  double _ScreenWidth = 0.0;
  double _ScreenHeight = 0.0;
  double _screenTopPadding = 0.0;

  double _top = 0.0;
  double _left = 20.0;

  bool isOpen = false;

  Uint8List? audio_icon_close;
  Uint8List? audio_icon_next;
  Uint8List? audio_icon_pause;
  Uint8List? audio_icon_play;
  Uint8List? audio_icon_unnext;
  Uint8List? audio_state_icon;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    TJAudioPlayViewManager.imageName("audio_state_icon").then((value) {
      setState(() {
        this.audio_state_icon = value;
      });
    });

    TJAudioPlayViewManager.imageName("audio_icon_close").then((value) {
      setState(() {
        this.audio_icon_close = value;
      });
    });

    TJAudioPlayViewManager.imageName("audio_icon_next").then((value) {
      setState(() {
        this.audio_icon_next = value;
      });
    });

    TJAudioPlayViewManager.imageName("audio_icon_play").then((value) {
      setState(() {
        this.audio_icon_play = value;
      });
    });

    TJAudioPlayViewManager.imageName("audio_icon_unnext").then((value) {
      setState(() {
        this.audio_icon_unnext = value;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    _ScreenWidth = MediaQuery.of(context).size.width;
    _ScreenHeight = MediaQuery.of(context).size.height;
    _screenTopPadding = MediaQuery.of(context).padding.top + 20;

    double defauleTop =
        _ScreenHeight - kDefaultWidth - kBottomPadding - _screenTopPadding;
    double currentWidth = isOpen ? kDefaultMAXWidth : kDefaultWidth;

    if (_left < 20) {
      _left = 20;
    }
    if (_left > _ScreenWidth - 20 - currentWidth) {
      _left = _ScreenWidth - currentWidth - 20;
    }
    if (_top < _screenTopPadding) {
      _top = _screenTopPadding;
    }
    if (_top > defauleTop - kBottomPadding) {
      _top = defauleTop - kBottomPadding;
    }
    return Offstage(
      offstage: !this.isShow,
      child: Stack(
        fit: StackFit.loose,
        children: [
          Positioned(
            left: _left,
            top: _top,
            child: AnimatedContainer(
              width: this.isOpen == false ? kDefaultWidth : kDefaultMAXWidth,
              height: 48,
              duration: Duration(milliseconds: 250),
              curve: Curves.ease,
              decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [
                    BoxShadow(
                        color: Color.fromRGBO(0, 0, 0, 0.18),
                        offset: Offset(2.0, 2.0),
                        blurRadius: 10.0,
                        spreadRadius: 2.0),
                    BoxShadow(
                        color: Color.fromRGBO(0, 0, 0, 0.18),
                        offset: Offset(-2.0, -2.0),
                        blurRadius: 10.0,
                        spreadRadius: 2.0)
                  ] // 边色与边宽度
              ),
              child: Stack(
                children: [
                  Positioned(
                    child: Row(
                      children: [
                        Stack(
                          clipBehavior: Clip.hardEdge,
                          children: [
                            Container(
                              width: kDefaultWidth,
                              height: kDefaultWidth,
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(24),
                                border: Border.all(
                                    color: Color.fromRGBO(243, 245, 246, 1),
                                    width: 4),
                              ),
                            ),
                            Positioned(
                              left: 15,
                              top: 18,
                              child: Container(
                                width: 18,
                                height: 11,
                                child: audio_state_icon != null
                                    ? Image.memory(audio_state_icon!)
                                    : Image.network(""),
                              ),
                            ),
                          ],
                        ),
                        Offstage(
                            offstage: !isOpen,
                            child: Row(
                              children: [
                                Container(
                                  margin: EdgeInsets.fromLTRB(10, 0, 0, 0),
                                  width: 1,
                                  height: 18,
                                  color: Color.fromRGBO(243, 245, 246, 1),
                                ),
                                Container(
                                  padding: EdgeInsets.fromLTRB(0, 12,0 , 12),
                                  width: 45,
                                  height: 45,
                                  child: audio_icon_play != null
                                      ? Image.memory(audio_icon_play!)
                                      : Text('play'),
                                ),
                                Container(
                                  width: 1,
                                  height: 18,
                                  color: Color.fromRGBO(243, 245, 246, 1),
                                ),
                                Container(
                                  padding: EdgeInsets.fromLTRB(0, 12,0 , 12),
                                  width: 45,
                                  height: 45,
                                  child: audio_icon_unnext != null
                                      ? Image.memory(audio_icon_unnext!)
                                      : Text('unnext'),
                                ),
                                Container(
                                  width: 1,
                                  height: 18,
                                  color: Color.fromRGBO(243, 245, 246, 1),
                                ),
                                GestureDetector(
                                  child: Container(
                                    padding: EdgeInsets.fromLTRB(0, 16, 0, 16),
                                    width: 45,
                                    height: 45,
                                    child: audio_icon_close != null
                                        ? Image.memory(audio_icon_close!)
                                        : Text('close'),
                                  ),
                                  onTap: (){
                                    setState(() {
                                      this.isShow = false;
                                    });
                                  },
                                )
                              ],
                            )),
                      ],
                    ),
                  ),
                  Positioned(
                    left: 2,
                    top: 2,
                    width: 44,
                    height: 44,
                    child: GestureDetector(
                      child: _circularProgressIndicator(),
                      onTap: () {
                        this.setState(() {
                          isOpen = !isOpen;
                        });
                      },
                      onPanUpdate: (DragUpdateDetails detail) {
                        this.setState(() {
                          _left += detail.delta.dx;
                          _top += detail.delta.dy;
                        });
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  CircularProgressIndicator _circularProgressIndicator() {
    return CircularProgressIndicator(
      value: 0.0 / 100.0, // 当前进度
      strokeWidth: 3, // 最小宽度
      valueColor: AlwaysStoppedAnimation<Color>(
          Color.fromRGBO(48, 114, 246, 1)), // 进度条当前进度颜色
    );
  }
}

class TJAudioPlayView extends StatefulWidget {
  @override
  _TJAudioPlayView createState() => _TJAudioPlayView();
}
