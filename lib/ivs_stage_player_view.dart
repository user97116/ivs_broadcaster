import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class IvsStagePlayerView extends StatefulWidget {
  const IvsStagePlayerView({
    Key? key,
    required this.token,
  }) : super(key: key);
  final String token;

  @override
  State<IvsStagePlayerView> createState() => _IvsStagePlayerViewState();
}

class _IvsStagePlayerViewState extends State<IvsStagePlayerView> {
  final MethodChannel mainChannel = const MethodChannel("ivs_stage_method");
  final EventChannel renderEventChannel = const EventChannel("ivs_stage_event");

  bool isLoading = true;

  @override
  Widget build(BuildContext context) {
    if (Platform.isAndroid) {
      return Stack(
        children: [
          AndroidView(
            viewType: 'ivs_stage_player',
            onPlatformViewCreated: (id) async {
              await mainChannel.invokeMethod("join", widget.token);
              renderEventChannel.receiveBroadcastStream().listen(
                (event) {
                  print("ivs_stage_event: " + event);
                },
              );
              setState(() {
                isLoading = false;
              });
            },
          ),
          if (isLoading)
            Container(
              color: Colors.black38,
              alignment: Alignment.center,
              child: const CircularProgressIndicator(),
            )
        ],
      );
    } else if (Platform.isIOS) {
      return UiKitView(
        viewType: 'ivs_player',
        onPlatformViewCreated: (id) async {},
      );
    }
    return const Center(
      child: Text(
        'Platform not supported',
        style: TextStyle(
          color: Colors.red,
          fontSize: 20,
        ),
      ),
    );
  }
}
