import 'dart:typed_data';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:tjaudio_flutter/tjaudio_flutter.dart';
import 'package:tjaudio_flutter/TJMediaBackGroundModel.dart';

class TJAudioPlayViewManager {
  List<TJMediaBackGroundModel> _currentAudioSourceData = [];
  List<TJMediaBackGroundModel> _cacheNewAudioSourceData = [];
  bool isShow = false;
  int sourceCount = 0;

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
    if (this._currentAudioSourceData.length != 0) {
      this._cacheNewAudioSourceData = dataArray;
      return false;
    }
    this.sourceCount = dataArray.length;
    this._currentAudioSourceData = dataArray;
    _autoNextAudio(true);
    return TjaudioFlutter.audioSourceData(dataArray);
  }

  void _show() {
    this.isShow = true;
  }

  void _showNative(){
    TjaudioFlutter.showNative();
  }

  Future<bool> _playWithModel(TJMediaBackGroundModel model) async {
    if (this._cacheNewAudioSourceData != null) {
      if (this._cacheNewAudioSourceData.length != 0) {
        this._currentAudioSourceData = [];
        this._audioSourceData(this._cacheNewAudioSourceData);
        this._cacheNewAudioSourceData = [];
      } else {
        List<TJMediaBackGroundModel> dataArray = [];
        dataArray.addAll(this._currentAudioSourceData);
        this._currentAudioSourceData = [];
        this._audioSourceData(dataArray);
        this._cacheNewAudioSourceData = [];
      }
    } else {
      this._currentAudioSourceData = [];
      this._audioSourceData([]);
      this._cacheNewAudioSourceData = [];
    }
    return TjaudioFlutter.playWithModel(model);
  }

  Future<bool> _playWithIndex(int index) async {
    return TjaudioFlutter.playWithIndex(index);
  }

  Future<List<TJMediaBackGroundModel>> _getAudioSourceData() async {
    return TjaudioFlutter.getAudioSourceData();
  }

  Future<Uint8List> _imageName(String imageName) async {
    return TjaudioFlutter.imageName(imageName);
  }

  void _setAudioPlayStateChangeListener(ValueChanged audioPlayStateChangeBlock,
      ValueChanged progressBlock) async {
    TjaudioFlutter.setAudioPlayStateChangeListener(
        audioPlayStateChangeBlock, progressBlock);
  }

  void _setMessageHandler() {
    TjaudioFlutter.setMessageHandler();
  }

  void _cancelMessageHandler() {
    TjaudioFlutter.cancelMessageHandler();
  }

  void _pause() {
    TjaudioFlutter.pause();
  }

  void _resume() {
    TjaudioFlutter.resume();
  }

  Future<bool> _getAudioIsPlaying() async {
    return TjaudioFlutter.getAudioIsPlaying();
  }

  void _autoNextAudio(bool autoNextAudio) {
    TjaudioFlutter.autoNextAudio(autoNextAudio);
  }

  void _destoryView() {
    this.isShow = false;
    TjaudioFlutter.destoryView();
  }

  void _nextAudio() {
    TjaudioFlutter.nextAudio();
  }


  //=====================================================================================

  /// default NO
  static void openBackGround(bool openBackGround) {
    TJAudioPlayViewManager._instance._openBackGround(openBackGround);
  }

  ///必须先设置资源再执行 show ，不然 会造成监听错误
  static Future<bool> audioSourceData(List<TJMediaBackGroundModel> dataArray) async {
    return TJAudioPlayViewManager._instance._audioSourceData(dataArray);
  }

  static void show() {
    TJAudioPlayViewManager._instance._show();
  }

  static void showNative(){
    TJAudioPlayViewManager._instance._showNative();
  }

  static Future<bool> playWithModel(TJMediaBackGroundModel model) async {
    return TJAudioPlayViewManager._instance._playWithModel(model);
  }

  static Future<bool> playWithIndex(int index) async {
    return TJAudioPlayViewManager._instance._playWithIndex(index);
  }

  static Future<List<TJMediaBackGroundModel>> getAudioSourceData() async {
    return TJAudioPlayViewManager._instance._getAudioSourceData();
  }

  static Future<Uint8List> imageName(String imageName) async {
    return TJAudioPlayViewManager._instance._imageName(imageName);
  }

  static void pause() {
    TJAudioPlayViewManager._instance._pause();
  }

  static void resume() {
    TJAudioPlayViewManager._instance._resume();
  }

  static void autoNextAudio(bool autoNextAudio) {
    TJAudioPlayViewManager._instance._autoNextAudio(autoNextAudio);
  }

  static void destoryView() {
    TJAudioPlayViewManager._instance._cacheNewAudioSourceData = [];
    TJAudioPlayViewManager._instance._currentAudioSourceData = [];
    TJAudioPlayViewManager._instance._destoryView();
  }

  static void nextAudio() {
    TJAudioPlayViewManager._instance._nextAudio();
  }

  static Future<bool> getAudioIsPlaying() async {
    return TJAudioPlayViewManager._instance._getAudioIsPlaying();
  }

  static void setAudioPlayStateChangeListener(
      ValueChanged audioPlayStateChangeBlock,
      ValueChanged progressBlock) async {
    TJAudioPlayViewManager._instance._setAudioPlayStateChangeListener(
        audioPlayStateChangeBlock, progressBlock);
  }

  static void setMessageHandler() {
    TJAudioPlayViewManager._instance._setMessageHandler();
  }

  static void cancelMessageHandler() {
    TJAudioPlayViewManager._instance._cancelMessageHandler();
  }

}
