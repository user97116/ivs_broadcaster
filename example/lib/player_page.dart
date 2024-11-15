import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ivs_broadcaster/ivs_stage_player_view.dart';

class PlayerPage extends StatefulWidget {
  const PlayerPage({super.key});

  @override
  State<PlayerPage> createState() => _PlayerPageState();
}

class _PlayerPageState extends State<PlayerPage> {
  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: RotatedBox(
          quarterTurns: 3,
          child: AspectRatio(
            aspectRatio: 16 / 9,
            child: IvsStagePlayerView(token: "eyJhbGciOiJLTVMiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjE3MzE3MjEzOTcsImlhdCI6MTczMTY3ODE5NywianRpIjoiQzhRYWV2Q2pKTUd6IiwicmVzb3VyY2UiOiJhcm46YXdzOml2czphcC1zb3V0aC0xOjI5ODYzOTcxMjAzMjpzdGFnZS80b1d0WnZRTU9KRGwiLCJ0b3BpYyI6IjRvV3RadlFNT0pEbCIsImV2ZW50c191cmwiOiJ3c3M6Ly9nbG9iYWwuZXZlbnRzLmxpdmUtdmlkZW8ubmV0Iiwid2hpcF91cmwiOiJodHRwczovLzdkNzdlNDI1NDVkYy5nbG9iYWwtYm0ud2hpcC5saXZlLXZpZGVvLm5ldCIsInVzZXJfaWQiOiJhbWFyIiwiY2FwYWJpbGl0aWVzIjp7ImFsbG93X3N1YnNjcmliZSI6dHJ1ZX0sInZlcnNpb24iOiIwLjAifQ.MGUCMGT-5eudd7OvzzfryDGKrIfPSwMEtjFuO0Y9GVjOiJggdrrqqpE0yP6iBJJkxqRggwIxAMFsTf89kXgUraDkyjX7kscfAufBQrUkeZaFJdLwWehzQP0Wzah3G-egwaPo1OzxiQ"),
          ),
        ),
      ),
    );
  }
}
