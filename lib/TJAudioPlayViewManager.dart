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
}

class _TJAudioPlayView extends State<TJAudioPlayView>
    with SingleTickerProviderStateMixin {

  final double kBottomPadding = 88.0; //距顶部的偏移
  final double kDefaultWidth = 48.0; //距底部
  final double kDefaultMAXWidth = 200.0; //距底部

  double _ScreenWidth = 0.0;
  double _ScreenHeight = 0.0;
  double _screenTopPadding = 0.0;

  double _top = 0.0;
  double _left = 20.0;

  bool isOpen = false;

  @override
  Widget build(BuildContext context) {
    _ScreenWidth = MediaQuery.of(context).size.width;
    _ScreenHeight = MediaQuery.of(context).size.height;
    _screenTopPadding = MediaQuery.of(context).padding.top+20;

    double defauleTop = _ScreenHeight-kDefaultWidth - kBottomPadding-_screenTopPadding ;
    double currentWidth = isOpen?kDefaultMAXWidth:kDefaultWidth;

    if(_left < 20) {
      _left = 20;
    }
    if(_left > _ScreenWidth - 20 - currentWidth) {
      _left = _ScreenWidth - currentWidth - 20;
    }
    if(_top < _screenTopPadding) {
      _top = _screenTopPadding;
    }
    if(_top > defauleTop-kBottomPadding) {
      _top =  defauleTop-kBottomPadding;
    }
    return Stack(
      fit: StackFit.loose,
      children: [
        Positioned(
          left:_left,
          top:_top,
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
            child: Row(
              children: [
                GestureDetector(
                  child: Container(
                    width: kDefaultWidth,
                    height: kDefaultWidth,
                    decoration: BoxDecoration(
                      color: Colors.orange,
                      borderRadius: BorderRadius.circular(24),
                      border: Border.all(
                          color: Color.fromRGBO(243, 245, 246, 1), width: 2),
                    ),
                  ),
                  onTap: () {
                    this.setState(() {
                      isOpen = !isOpen;
                    });
                  },
                  // onVerticalDragUpdate:(DragUpdateDetails detail){
                  //   this.setState(() {
                  //
                  //   });
                  //   print(_top.toString());
                  // },
                  onPanUpdate:(DragUpdateDetails detail){
                    this.setState(() {
                      _left += detail.delta.dx;
                      _top += detail.delta.dy;
                    });
                  },
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class TJAudioPlayView extends StatefulWidget {
  @override
  _TJAudioPlayView createState() => _TJAudioPlayView();
}
