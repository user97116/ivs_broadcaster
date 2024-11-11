import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ivs_broadcaster/Player/Widget/ivs_player_view.dart';
import 'package:ivs_broadcaster/Player/ivs_player.dart';

class PlayerPage extends StatefulWidget {
  const PlayerPage({super.key});

  @override
  State<PlayerPage> createState() => _PlayerPageState();
}

class _PlayerPageState extends State<PlayerPage> {
  late IvsPlayer _player;

  @override
  void initState() {
    super.initState();
    _player = IvsPlayer.instance;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          SizedBox(
            width: 300,
            height: 200,
            child: IvsPlayerView(
              controller: _player,
            ),
          ),
          ElevatedButton(
            onPressed: () {
              MethodChannel("ivs_player_channel").invokeMethod("play");
            },
            child: Text("Play"),
          ),
        ],
      ),
    );
  }
}
