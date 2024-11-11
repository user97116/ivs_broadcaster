import 'package:flutter/material.dart';
import 'package:ivs_broadcaster/Player/Widget/ivs_player_view.dart';

class PlayerPage extends StatefulWidget {
  const PlayerPage({super.key});

  @override
  State<PlayerPage> createState() => _PlayerPageState();
}

class _PlayerPageState extends State<PlayerPage> {
  @override
  Widget build(BuildContext context) {
    return  Scaffold(
      body: Column(
        children: [
          AspectRatio(
            aspectRatio: 16/9,
            child: SizedBox(
              height: 200,
              child: IvsPlayerView(
                onStatusChanged: (status) {
                  print("amar $status");
                },
                url:
                    "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
              ),
            ),
          ),
        ],
      ),
    );
  }
}
