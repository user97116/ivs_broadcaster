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
  StreamSubscription? renderSubscription;

  bool isLoading = true;

  @override
  void dispose() {
    renderSubscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (Platform.isAndroid) {
      return Stack(
        children: [
          AndroidView(
            viewType: 'ivs_stage_player',
            onPlatformViewCreated: (id) async {
              await mainChannel.invokeMethod("join", widget.token);
              renderSubscription =
                  renderEventChannel.receiveBroadcastStream().listen(
                (event) {
                  print("ivs_stage_event: " + event.toString());
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
      return Stack(
        children: [
          UiKitView(
            viewType: 'ios_ivs_stage_player',
            onPlatformViewCreated: (id) async {
              await mainChannel.invokeMethod("join", widget.token);
              renderSubscription =
                  renderEventChannel.receiveBroadcastStream().listen(
                (event) {
                  print("ivs_stage_event: " + event.toString());
                },
              );
              print("Stage something went wrong");
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

class IvsStagePlayerController {
  final MethodChannel mainChannel = const MethodChannel("ivs_stage_method");
}
