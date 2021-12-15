import 'dart:typed_data';
import 'dart:ui';
import 'dart:io';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:tjaudio_flutter/TJAudioPlayStateView.dart';
import 'package:tjaudio_flutter/TJMediaBackGroundModel.dart';
import 'TJAudioPlayViewManager.dart';

class _TJAudioPlayView extends State<TJAudioPlayView>
    with SingleTickerProviderStateMixin {
  final double _kBottomPadding = 88.0; //距顶部的偏移
  final double _kDefaultWidth = 48.0; //距底部
  final double _kDefaultMAXWidth = 200.0; //距底部
  bool _isOpen = false;
  int _sourceCount = 0;
  double _ScreenWidth = 0.0;
  double _ScreenHeight = 0.0;
  double _screenTopPadding = 0.0;

  double _top = 0.0;
  double _left = 20.0;

  Uint8List? _audio_icon_close;
  Uint8List? _audio_icon_next;
  Uint8List? _audio_icon_pause;
  Uint8List? _audio_icon_play;
  Uint8List? _audio_icon_unnext;
  Uint8List? _audio_state_icon;

  double _audioPlayProgress = 0.0;

  bool _audioPlayStates = false;



  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    TJAudioPlayViewManager.imageName("audio_state_icon").then((value) {
      setState(() {
        this._audio_state_icon = value;
      });
    });

    TJAudioPlayViewManager.imageName("audio_icon_close").then((value) {
      setState(() {
        this._audio_icon_close = value;
      });
    });

    TJAudioPlayViewManager.imageName("audio_icon_next").then((value) {
      setState(() {
        this._audio_icon_next = value;
      });
    });

    TJAudioPlayViewManager.imageName("audio_icon_play").then((value) {
      setState(() {
        this._audio_icon_play = value;
      });
    });

    TJAudioPlayViewManager.imageName("audio_icon_pause").then((value) {
      setState(() {
        this._audio_icon_pause = value;
      });
    });

    TJAudioPlayViewManager.imageName("audio_icon_unnext").then((value) {
      setState(() {
        this._audio_icon_unnext = value;
      });
    });

    TJAudioPlayViewManager.setMessageHandler();
    TJAudioPlayViewManager.setAudioPlayStateChangeListener((audioState) {
      TJAudioPlayViewManager.getAudioIsPlaying().then((value) {
        setState(() {
          this._audioPlayStates = !value;
        });
      });
    }, (progress) {
      setState(() {
        this._audioPlayProgress = progress;
      });
    });

  }

  @override
  void dispose() {
    // TODO: implement dispose
    TJAudioPlayViewManager.cancelMessageHandler();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    _ScreenWidth = MediaQuery.of(context).size.width;
    _ScreenHeight = MediaQuery.of(context).size.height;
    _screenTopPadding = MediaQuery.of(context).padding.top + 20;

    double defauleTop =
        _ScreenHeight - _kDefaultWidth - _kBottomPadding - _screenTopPadding;
    double currentWidth = _isOpen ? _kDefaultMAXWidth : _kDefaultWidth;

    if (_left < 20) {
      _left = 20;
    }
    if (_left > _ScreenWidth - 20 - currentWidth) {
      _left = _ScreenWidth - currentWidth - 20;
    }
    if (_top < _screenTopPadding) {
      _top = _screenTopPadding;
    }
    if (_top > defauleTop - _kBottomPadding) {
      _top = defauleTop - _kBottomPadding;
    }
    return Offstage(
      offstage: !TJAudioPlayViewManager().isShow,
      child: Stack(
        fit: StackFit.loose,
        children: [
          Positioned(
            left: _left,
            top: _top,
            child: AnimatedContainer(
              width: this._isOpen == false ? _kDefaultWidth : _kDefaultMAXWidth,
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
                    child: Stack(
                      clipBehavior: Clip.hardEdge,
                      children: [
                        Container(
                          width: _kDefaultWidth,
                          height: _kDefaultWidth,
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
                            child:Offstage(
                              offstage: this._audioPlayStates == false,
                              child: (_audio_state_icon != null
                                  ? Image.memory(
                                  _audio_state_icon! )
                                  : Image.network("")),

                            ),
                          ),
                        ),
                        Positioned(
                          left: 6,
                          top: 6,
                          child:
                          Offstage(
                              offstage: this._audioPlayStates != false,
                              child:TJAudioPlayStateView(animationsStart: !this._audioPlayStates,),
                          ),
                        ),
                      ],
                    ),
                  ),
                  Positioned(
                    top: 2,
                    left: 45,
                    child: Offstage(
                        offstage: !_isOpen,
                        child: Row(
                          children: [
                            Container(
                              margin: EdgeInsets.fromLTRB(10, 0, 0, 0),
                              width: 1,
                              height: 18,
                              color: Color.fromRGBO(243, 245, 246, 1),
                            ),
                            new GestureDetector(
                              child: Container(
                                padding: EdgeInsets.fromLTRB(0, 12, 0, 12),
                                width: 45,
                                height: 45,
                                child: this._audioPlayStates == true
                                    ? (_audio_icon_play != null
                                        ? Image.memory(_audio_icon_play!)
                                        : Text('play'))
                                    : (_audio_icon_pause != null
                                        ? Image.memory(_audio_icon_pause!)
                                        : Text('pause')),
                              ),
                              onTap: () {
                                TJAudioPlayViewManager.getAudioIsPlaying()
                                    .then((value) {
                                  if (value == true) {
                                    TJAudioPlayViewManager.pause();
                                  } else {
                                    TJAudioPlayViewManager.resume();
                                  }
                                });
                              },
                            ),
                            Container(
                              width: 1,
                              height: 18,
                              color: Color.fromRGBO(243, 245, 246, 1),
                            ),
                            GestureDetector(
                              child: Container(
                                padding: EdgeInsets.fromLTRB(0, 12, 0, 12),
                                width: 45,
                                height: 45,
                                child: _sourceCount>1 ?
                                (_audio_icon_next != null ? Image.memory(_audio_icon_next!) : Text('next'))
                                    : (_audio_icon_unnext != null ? Image.memory(_audio_icon_unnext!) : Text('unnext')),
                              ),
                              onTap:_sourceCount>1? _onTapNextAudio:null,
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
                                child: _audio_icon_close != null
                                    ? Image.memory(_audio_icon_close!)
                                    : Text('close'),
                              ),
                              onTap: () {
                                TJAudioPlayViewManager.destoryView();
                                setState(() {
                                  this._audioPlayProgress = 0.0;
                                });
                              },
                            )
                          ],
                        )),
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
                          _isOpen = !_isOpen;
                          if(_isOpen){
                            _sourceCount = TJAudioPlayViewManager().sourceCount;
                          }
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

  _onTapNextAudio(){
    TJAudioPlayViewManager.nextAudio();
  }

  CircularProgressIndicator _circularProgressIndicator() {
    return CircularProgressIndicator(
      value: this._audioPlayProgress, // 当前进度
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
