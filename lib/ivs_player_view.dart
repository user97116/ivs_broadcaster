import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class IvsPlayerView extends StatefulWidget {
  const IvsPlayerView({
    Key? key,
    required this.url,
    required this.onStatusChanged,
  }) : super(key: key);
  final String url;
  final void Function(String status) onStatusChanged;

  @override
  State<IvsPlayerView> createState() => _IvsPlayerViewState();
}

class _IvsPlayerViewState extends State<IvsPlayerView> {
  final MethodChannel ivsPlayerChannel =
      const MethodChannel("ivs_player_channel");
  final EventChannel statusEventChannel =
      const EventChannel("ivs_player_status_stream");
  StreamSubscription? statusStream;
  bool isLoading = true;

  @override
  void dispose() {
    statusStream?.cancel();
    print("ivs_player disposing....");
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (Platform.isAndroid) {
      return Stack(
        children: [
          AndroidView(
            viewType: 'ivs_player',
            onPlatformViewCreated: (id) async {
              await ivsPlayerChannel.invokeMethod("load", widget.url);
              statusStream = statusEventChannel.receiveBroadcastStream().listen(
                (event) {
                  if (event == "BUFFERING") {
                    isLoading = true;
                    setState(() {});
                  } else {
                    isLoading = false;
                    setState(() {});
                  }
                  widget.onStatusChanged(event);
                },
              );
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
            viewType: 'ivs_player',
            onPlatformViewCreated: (id) async {
              await ivsPlayerChannel.invokeMethod("load", widget.url);
              statusStream = statusEventChannel.receiveBroadcastStream().listen(
                (event) {
                  if (event == "BUFFERING") {
                    isLoading = true;
                    setState(() {});
                  } else {
                    isLoading = false;
                    setState(() {});
                  }
                  widget.onStatusChanged(event);
                },
              );
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

