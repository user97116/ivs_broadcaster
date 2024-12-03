package com.example.ivs_broadcaster.player;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import static io.flutter.plugin.common.MethodChannel.MethodCallHandler;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.platform.PlatformView;

import com.amazonaws.ivs.broadcast.BroadcastException;
import com.amazonaws.ivs.broadcast.ImagePreviewView;
import com.amazonaws.ivs.broadcast.JitterBufferConfiguration;
import com.amazonaws.ivs.broadcast.LocalStageStream;
import com.amazonaws.ivs.broadcast.ParticipantInfo;
import com.amazonaws.ivs.broadcast.Stage;
import com.amazonaws.ivs.broadcast.StageRenderer;
import com.amazonaws.ivs.broadcast.StageStream;
import com.amazonaws.ivs.broadcast.SubscribeConfiguration;
import com.amazonaws.ivs.player.Cue;
import com.amazonaws.ivs.player.Player;
import com.amazonaws.ivs.player.PlayerException;
import com.amazonaws.ivs.player.PlayerView;
import com.amazonaws.ivs.player.Quality;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public class IVSPlayerView extends Player.Listener implements PlatformView, MethodCallHandler, SurfaceHolder.Callback {
    private static final String TAG = "StreamView";

    private final Player player;
    private final SurfaceView surfaceView;
    private Surface surface;
    private final EventChannel statusChannel;
    private EventChannel.EventSink statusSink;
    private ImagePreviewView previewView;

    IVSPlayerView(Context context, BinaryMessenger messenger) {
        PlayerView playerView = new PlayerView(context);
        player = playerView.getPlayer();
        player.addListener(this);

        surfaceView = new SurfaceView(context);
        MethodChannel methodChannel = new MethodChannel(messenger, "ivs_player_channel");
        methodChannel.setMethodCallHandler(this);

//        player.setLogLevel(Player.LogLevel.DEBUG);

        statusChannel = new EventChannel(messenger, "ivs_player_status_stream");

        // Broadcaster lister
//        BroadcastSession.Listener broadcastListener =
//                new BroadcastSession.Listener() {
//                    @Override
//                    public void onStateChanged(@NonNull BroadcastSession.State state) {
//                        Log.d("Broadcaster", state.toString());
//                    }
//
//                    @Override
//                    public void onError(@NonNull BroadcastException e) {
//                        Log.d("Broadcaster", e.toString());
//                    }
//                };
        // Broadcaster session
//        BroadcastSession broadcastSession = new BroadcastSession(context.getApplicationContext(),
//                broadcastListener,
//                Presets.Configuration.STANDARD_PORTRAIT,
//                Presets.Devices.FRONT_CAMERA(context.getApplicationContext()));

//        broadcastSession.start(IVS_RTMPS_URL, IVS_STREAMKEY);

        // Stage
        Stage stage = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Stage.Strategy strategy = new Stage.Strategy() {
                @Override
                public SubscribeConfiguration subscribeConfigrationForParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
                    Log.d("Stage", "subscribeConfigrationForParticipant");
                    SubscribeConfiguration config = new SubscribeConfiguration();
                    config.jitterBuffer.setMinDelay(JitterBufferConfiguration.JitterBufferDelay.MEDIUM());
                    return config;
//                    return Stage.Strategy.super.subscribeConfigrationForParticipant(stage, participantInfo);
                }

                @NonNull
                @Override
                public List<LocalStageStream> stageStreamsToPublishForParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
                    Log.d("Stage", "stageStreamsToPublishForParticipant");
                    return Collections.emptyList();
                }

                @Override
                public boolean shouldPublishFromParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
                    Log.d("Stage", "shouldPublishFromParticipant");
                    return true;
                }

                @Override
                public Stage.SubscribeType shouldSubscribeToParticipant(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
                    Log.d("Stage", "shouldSubscribeToParticipant");
                    return Stage.SubscribeType.AUDIO_VIDEO;
                }
            };
            String token = "eyJhbGciOiJLTVMiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjE3MzE3MjEzOTcsImlhdCI6MTczMTY3ODE5NywianRpIjoiQzhRYWV2Q2pKTUd6IiwicmVzb3VyY2UiOiJhcm46YXdzOml2czphcC1zb3V0aC0xOjI5ODYzOTcxMjAzMjpzdGFnZS80b1d0WnZRTU9KRGwiLCJ0b3BpYyI6IjRvV3RadlFNT0pEbCIsImV2ZW50c191cmwiOiJ3c3M6Ly9nbG9iYWwuZXZlbnRzLmxpdmUtdmlkZW8ubmV0Iiwid2hpcF91cmwiOiJodHRwczovLzdkNzdlNDI1NDVkYy5nbG9iYWwtYm0ud2hpcC5saXZlLXZpZGVvLm5ldCIsInVzZXJfaWQiOiJhbWFyIiwiY2FwYWJpbGl0aWVzIjp7ImFsbG93X3N1YnNjcmliZSI6dHJ1ZX0sInZlcnNpb24iOiIwLjAifQ.MGUCMGT-5eudd7OvzzfryDGKrIfPSwMEtjFuO0Y9GVjOiJggdrrqqpE0yP6iBJJkxqRggwIxAMFsTf89kXgUraDkyjX7kscfAufBQrUkeZaFJdLwWehzQP0Wzah3G-egwaPo1OzxiQ";
            stage = new Stage(context, token, strategy);
            stage.addRenderer(new StageRenderer() {
                @Override
                public void onError(@NonNull BroadcastException exception) {
                    StageRenderer.super.onError(exception);
                    Log.d("Stage onError", exception.toString());
                }

                @Override
                public void onConnectionStateChanged(@NonNull Stage stage, @NonNull Stage.ConnectionState state, @Nullable BroadcastException exception) {
                    StageRenderer.super.onConnectionStateChanged(stage, state, exception);
                    Log.d("Stage onCtateChanged", state.toString());
                }

                @Override
                public void onParticipantJoined(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
                    StageRenderer.super.onParticipantJoined(stage, participantInfo);
                    Log.d("Stage onPaJoined", participantInfo.participantId);
                }

                @Override
                public void onParticipantLeft(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
                    StageRenderer.super.onParticipantLeft(stage, participantInfo);
                    Log.d("Stage onPartLeft", participantInfo.toString());
                }

                @Override
                public void onParticipantPublishStateChanged(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull Stage.PublishState publishState) {
                    StageRenderer.super.onParticipantPublishStateChanged(stage, participantInfo, publishState);
                    Log.d("Stage publish status c", publishState.name());
                }

                @Override
                public void onParticipantSubscribeStateChanged(@NonNull Stage stage, @NonNull ParticipantInfo publishingParticipantInfo, @NonNull Stage.SubscribeState subscribeState) {
                    StageRenderer.super.onParticipantSubscribeStateChanged(stage, publishingParticipantInfo, subscribeState);
                    Log.d("Stage subscribe statte", subscribeState.name());
                }

                @Override
                public void onStreamsAdded(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
                    StageRenderer.super.onStreamsAdded(stage, participantInfo, streams);
                    Log.d("Stage onStreamsAdded", streams.toString());
                    if (!streams.isEmpty()) {
                        previewView = streams.get(1).getPreview();
                        if (previewView != null) {
                            surfaceView.setVisibility(View.GONE);
                            ((ViewGroup) surfaceView.getParent()).addView(previewView);
                            surfaceView.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onStreamsRemoved(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
                    StageRenderer.super.onStreamsRemoved(stage, participantInfo, streams);
                    Log.d("Stage onStreamsRemoved", participantInfo.toString());
                }

                @Override
                public void onStreamsMutedChanged(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
                    StageRenderer.super.onStreamsMutedChanged(stage, participantInfo, streams);
                    Log.d("Stage onSdChanged", participantInfo.toString());

                }
            });
            try {
                stage.join();
                // Other Stage implementation code
                stage.refreshStrategy();
                Log.d("Stage", "Joibed");
            } catch (Exception e) {
                Log.d("Stage joined failed", e.getMessage());
            }

        }

    }

    @Override
    public View getView() {
        return surfaceView;
    }

    @Override
    public void onFlutterViewAttached(@NonNull View flutterView) {
        PlatformView.super.onFlutterViewAttached(flutterView);
        surfaceView.getHolder().addCallback(this);

        statusChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink event) {
                statusSink = event;
            }

            @Override
            public void onCancel(Object o) {
                statusSink = null;
            }
        });
    }

    @Override
    public void onFlutterViewDetached() {
        PlatformView.super.onFlutterViewDetached();
        if (surfaceView != null) {
            surfaceView.getHolder().removeCallback(this);
        }
    }

    @Override
    public void dispose() {
        onClose();
    }

    private void onClose() {
        // Remove listener and release player resources if player is not null
        if (player != null) {
            player.removeListener(this);
            player.release();
            Log.d(TAG, "Player disposed.");
        }

        // Ensure surface is cleaned up properly
        if (surface != null) {
            surface.release();  // Optional: depending on how the surface is managed.
        }
    }


    @Override
    public void onMethodCall(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        switch (methodCall.method) {
            case "load":
                String uriString = (String) methodCall.arguments;
                if (uriString != null && !uriString.isEmpty()) {
                    player.load(Uri.parse(uriString));
                    result.success("Loading");
                } else {
                    result.error("INVALID_URL", "Provided URL is invalid", null);
                }
                break;

            case "play":
                player.play();
                result.success("Playing");
                break;

            case "close":
                onClose();
                result.success("Closed");
                break;

            case "pause":
                player.pause();
                result.success("Paused");
                break;

            default:
                result.notImplemented();
        }
    }

    @Override
    public void onCue(@NonNull Cue cue) {
        // Handle cue events if necessary
        Log.d(TAG, "Cue received: " + cue.toString());
    }

    @Override
    public void onDurationChanged(long duration) {
        Log.d(TAG, "Duration changed: " + duration);
    }

    @Override
    public void onStateChanged(@NonNull Player.State state) {
        Log.d(TAG, "Player state changed: " + state.name());

        switch (state) {
            case BUFFERING:
                statusSink.success("BUFFERING");
                break;

            case READY:
                player.setLooping(true);
                player.play();
                statusSink.success("READY");
                break;

            case IDLE:
                statusSink.success("IDLE");
                break;

            case PLAYING:
                statusSink.success("PLAYING");
                break;

            case ENDED:
                statusSink.success("ENDED");
                break;
        }
    }

    @Override
    public void onError(@NonNull PlayerException e) {
        Log.e(TAG, "Player error: " + e.getMessage());
        Log.e(TAG, "Error details: " + e.getCause());
        if (statusSink != null) {
            statusSink.success("ERROR: " + e.getMessage());
        }
    }


    @Override
    public void onRebuffering() {
        Log.d(TAG, "Rebuffering...");
    }

    @Override
    public void onSeekCompleted(long position) {
        Log.d(TAG, "Seek completed at position: " + position);
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        Log.d(TAG, "Video size changed: " + width + "x" + height);
    }

    @Override
    public void onQualityChanged(@NonNull Quality quality) {
        Log.d(TAG, "Quality changed: " + quality.toString());
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        this.surface = surfaceHolder.getSurface();
        if (player != null) {
            player.setSurface(this.surface);
            Log.d(TAG, "Surface created and player surface set.");
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
        // Handle surface changes if necessary
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        this.surface = null;
        if (player != null) {
            player.setSurface(null);
            Log.d(TAG, "Surface destroyed and player surface cleared.");
        }
    }
}