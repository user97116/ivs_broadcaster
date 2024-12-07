package com.example.ivs_broadcaster.streams;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.amazonaws.ivs.broadcast.BroadcastException;
import com.amazonaws.ivs.broadcast.ImagePreviewSurfaceView;
import com.amazonaws.ivs.broadcast.ImagePreviewView;
import com.amazonaws.ivs.broadcast.ParticipantInfo;
import com.amazonaws.ivs.broadcast.Stage;
import com.amazonaws.ivs.broadcast.StageRenderer;
import com.amazonaws.ivs.broadcast.StageStream;
import com.amazonaws.ivs.chat.messaging.requests.SendMessageRequest;
import com.example.ivs_broadcaster.chats.StageChat;
import com.example.ivs_broadcaster.views.RemoteViewFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;

@SuppressLint("NewApi")
public class StageListener implements StageRenderer {
    StageChat stageChat;
    FlutterPlugin.FlutterPluginBinding binding;
    HashMap map = new HashMap();
    EventChannel.EventSink sink;
    ArrayList<String> joinedParticipants = new ArrayList<>();
    ArrayList<String> viewsParticipants = new ArrayList<>();
    ArrayList<String> leftParticipants = new ArrayList<>();

    public StageListener(FlutterPlugin.FlutterPluginBinding binding, EventChannel.EventSink sink, StageChat stageChat) {
        this.sink = sink;
        this.binding = binding;
        this.stageChat = stageChat;
        Log.d("StageListener", "Init " + sink + " ");
    }

    @Override
    public void onError(@NonNull BroadcastException exception) {
        StageRenderer.super.onError(exception);
        Log.d("StageListener", exception.toString());
    }

    @Override
    public void onConnectionStateChanged(@NonNull Stage stage, @NonNull Stage.ConnectionState state, @Nullable BroadcastException exception) {
        StageRenderer.super.onConnectionStateChanged(stage, state, exception);
        map.put("connection_state_changed", state.name());
        if (sink != null) sink.success(map);
    }

    @Override
    public void onParticipantJoined(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        StageRenderer.super.onParticipantJoined(stage, participantInfo);
        String message = String.format("<>^S^E^R^V^E^R<>::dev::{\"type\":\"participantJoined\",\"category\":\"liveRoom\",\"data\":{\"participantId\":\"%s\"}}", participantInfo.participantId);
        try {
            if (stageChat.room != null) {
                stageChat.room.sendMessage(new SendMessageRequest(message));
            }
            Log.d("StageChat", "Message sent");
        } catch (Exception e) {
            Log.d("StageChat", e.getMessage());
        }
        joinedParticipants.removeIf(element -> element.equals(participantInfo.participantId));
        joinedParticipants.add(participantInfo.participantId);
        map.put("joined_participants", joinedParticipants);
        if (sink != null) sink.success(map);
    }

    @Override
    public void onParticipantLeft(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
        StageRenderer.super.onParticipantLeft(stage, participantInfo);
        String message = String.format("<>^S^E^R^V^E^R<>::dev::{\"type\":\"participantLeft\",\"category\":\"liveRoom\",\"data\":{\"participantId\":\"%s\"}}", participantInfo.participantId);
        try {
            if (stageChat.room != null) {
                stageChat.room.sendMessage(new SendMessageRequest(message));
            }
            Log.d("StageChat", "Message sent left");
        } catch (Exception e) {
            Log.d("StageChat", e.getMessage());
        }
        leftParticipants.removeIf(element -> element.equals(participantInfo.participantId));
        leftParticipants.add(participantInfo.participantId);
        map.put("left_participants", leftParticipants);
        if (sink != null) sink.success(map);
    }

    @Override
    public void onParticipantPublishStateChanged(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull Stage.PublishState publishState) {
        StageRenderer.super.onParticipantPublishStateChanged(stage, participantInfo, publishState);
        map.put("participant_publish_state_changed", publishState.name());
        if (sink != null) sink.success(map);
    }

    @Override
    public void onParticipantSubscribeStateChanged(@NonNull Stage stage, @NonNull ParticipantInfo publishingParticipantInfo, @NonNull Stage.SubscribeState subscribeState) {
        StageRenderer.super.onParticipantSubscribeStateChanged(stage, publishingParticipantInfo, subscribeState);
        map.put("participant_subscribe_state_changed", subscribeState.name());
        if (sink != null) sink.success(map);
    }

    @Override
    public void onStreamsAdded(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
        StageRenderer.super.onStreamsAdded(stage, participantInfo, streams);

        for (int i = 0; i < streams.size(); i++) {
            if (streams.get(i).getStreamType() == StageStream.Type.VIDEO) {
                String viewId = UUID.randomUUID().toString() + "_" + streams.get(i).getDevice().getDescriptor().deviceId;
//                View view = streams.get(i).getPreviewSurfaceView();
                View view = streams.get(i).getPreviewSurfaceView();
                final boolean result = binding.getPlatformViewRegistry().registerViewFactory(viewId, new RemoteViewFactory(view));
                Log.d("PlatformView", String.valueOf(result));
                Log.d("PlatformView", String.valueOf(streams.get(i).getDevice().isValid()));
                viewsParticipants.removeIf(element -> element.equals(viewId));
                viewsParticipants.add(viewId);
            }
        }
        map.put("stream_added", viewsParticipants);
        if (sink != null) sink.success(map);
    }

    @Override
    public void onStreamsRemoved(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
        StageRenderer.super.onStreamsRemoved(stage, participantInfo, streams);
        for (int i = 0; i < streams.size(); i++) {
            if (streams.get(i).getStreamType() == StageStream.Type.VIDEO) {
                String deviceId = streams.get(i).getDevice().getDescriptor().deviceId;
                viewsParticipants.removeIf(element -> element.contains(deviceId));
            }
        }
        map.put("stream_removed", viewsParticipants);
        if (sink != null) sink.success(map);
    }

    @Override
    public void onStreamsMutedChanged(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
        StageRenderer.super.onStreamsMutedChanged(stage, participantInfo, streams);
        ArrayList<String> streamsMutedChanged = new ArrayList<String>();
        for (int i = 0; i < streams.size(); i++) {
            streamsMutedChanged.add(streams.get(i).getDevice().getDescriptor().urn);
        }
        map.put("streams_muted_changed", streamsMutedChanged);
        if (sink != null) sink.success(map);
    }
}
