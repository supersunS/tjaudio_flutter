import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'dart:math' as Math;

class _TJAudioPlayStateView extends State<TJAudioPlayStateView> {
  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return new Center(
      child: new Container(
        width: 36,
        height: 36,
        decoration: BoxDecoration(
          color: Color.fromRGBO(48, 114, 246, 1),
          borderRadius: BorderRadius.circular(18),
        ),
        child: Stack(
          children: [
            TJAudioPlayLineWidget(
              key: Key('layerLeft_1'),
              top: 12,
              left: 9,
              width: 2,
              height: 12,
              animatedBeginValue: 2,
              animatedEndValue: Math.sin(Math.pi / 2.0) * 12.0,
              animationsStart: this.widget.animationsStart,
            ),
            TJAudioPlayLineWidget(
              key: Key('layerright_1'),
              top: 12,
              left: 25,
              width: 2,
              height: 12,
              animatedBeginValue: 2,
              animatedEndValue: Math.sin(Math.pi / 2.0) * 12.0,
              animationsStart: this.widget.animationsStart,
            ),
            TJAudioPlayLineWidget(
              key: Key('layerLeft_2'),
              top: 12,
              left: 13,
              width: 2,
              height: 12,
              animatedBeginValue: Math.sin(Math.pi / 4.0) * 12.0,
              animatedEndValue: Math.sin(Math.pi / 2.0) * 12.0,
              animationsStart: this.widget.animationsStart,
            ),
            TJAudioPlayLineWidget(
              key: Key('layerright_2'),
              top: 12,
              left: 21,
              width: 2,
              height: 12,
              animatedBeginValue: Math.sin(Math.pi / 4.0) * 12.0,
              animatedEndValue: Math.sin(Math.pi / 2.0) * 12.0,
              animationsStart: this.widget.animationsStart,
            ),
            TJAudioPlayLineWidget(
              key: Key('layer_center'),
              top: 12,
              left: 17,
              width: 2,
              height: 12,
              animatedBeginValue: Math.sin(Math.pi / 2.0) * 12.0,
              animatedEndValue: Math.sin(Math.pi / 8.0) * 12.0,
              animationsStart: this.widget.animationsStart,
            ),
          ],
        ),
      ),
    );
  }
}

class TJAudioPlayStateView extends StatefulWidget {
  final bool animationsStart;

  const TJAudioPlayStateView({
    required this.animationsStart,
  }) : super();

  @override
  _TJAudioPlayStateView createState() => _TJAudioPlayStateView();
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class _TJAudioPlayLineWidget extends State<TJAudioPlayLineWidget>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  late final Animation<double> _animation;

  _changeVisibility(bool v) {
    setState(() {});
  }

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    _controller = AnimationController(
        duration: const Duration(milliseconds: 300), vsync: this);
    _controller.addListener(() {
      setState(() {});
    });
    _controller.addStatusListener((status) {

    });

    _animation = Tween(
            begin: this.widget.animatedBeginValue,
            end: this.widget.animatedEndValue)
        .animate(_controller);
    _controller.forward();
    _controller.repeat(reverse: true);
  }

  @override
  void dispose() {
    // TODO: implement dispose
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    if(this.widget.animationsStart == true && _controller.isAnimating != true){
      _controller.repeat(reverse: true);
    }else if(this.widget.animationsStart == false && _controller.isAnimating == true){
      _controller.stop();
    }
    return Positioned(
      top: this.widget.top + (this.widget.top - _animation.value) / 2.0,
      left: this.widget.left,
      child: Container(
        width: this.widget.width,
        height: _animation.value,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(1),
        ),
      ),
    );
  }
}

class TJAudioPlayLineWidget extends StatefulWidget {
  final double top;
  final double left;
  final double width;
  final double height;
  final double animatedBeginValue;
  final double animatedEndValue;
  final bool animationsStart;

  const TJAudioPlayLineWidget({
    required Key key,
    required this.top,
    required this.left,
    required this.width,
    required this.height,
    required this.animatedBeginValue,
    required this.animatedEndValue,
    required this.animationsStart,
  }) : super(key: key);

  @override
  _TJAudioPlayLineWidget createState() => _TJAudioPlayLineWidget();
}
