package com.example.ivs_broadcaster;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import com.amazonaws.ivs.broadcast.Stage;
import com.example.ivs_broadcaster.chats.StageChat;
import com.example.ivs_broadcaster.player.IVSPlayerFactory;
import com.example.ivs_broadcaster.stage.IVSStageFactory;
import com.example.ivs_broadcaster.streams.StageController;
import com.example.ivs_broadcaster.streams.StageListener;
import com.example.ivs_broadcaster.streams.strategy.PublishStrategy;
import com.example.ivs_broadcaster.streams.strategy.ViewerStrategy;

@SuppressLint("NewApi")
public class IvsBroadcasterPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler {
    StageChat stageChat;
    StageController stageController;
    MethodChannel ivsStageMethod;
    FlutterPluginBinding binding;
    //    Stage stage;
    EventChannel stageEventChannel;
    EventChannel roomEventChannel;
    EventChannel.EventSink stageSink;
    EventChannel.EventSink roomSink;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        this.binding = binding;
        binding.getPlatformViewRegistry().registerViewFactory("ivs_player", new IVSPlayerFactory(binding.getBinaryMessenger()));
        ivsStageMethod = new MethodChannel(binding.getBinaryMessenger(), "gb_ivs_stage_method");
        ivsStageMethod.setMethodCallHandler(this);
        stageEventChannel = new EventChannel(binding.getBinaryMessenger(), "gb_ivs_stage_event");
        roomEventChannel = new EventChannel(binding.getBinaryMessenger(), "gb_ivs_stage_event_room");
        stageEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink event) {
                stageSink = event;
            }

            @Override
            public void onCancel(Object o) {
                stageSink = null;
            }
        });
        roomEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink event) {
                roomSink = event;
            }

            @Override
            public void onCancel(Object o) {
                roomSink = null;
            }
        });
        binding.getPlatformViewRegistry().registerViewFactory("ivs_stage_player", new IVSStageFactory(binding.getPlatformViewRegistry(), binding.getBinaryMessenger()));
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        Log.d("Stage", "onDetachedFromEngine");
        roomEventChannel.setStreamHandler(null);
        stageEventChannel.setStreamHandler(null);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "init":
                stageChat = new StageChat(roomSink);
                stageController = new StageController(binding, stageSink, stageChat);
                result.success(null);
                break;
            case "join":
                stageController.joinStage(call, result);
                break;
            case "leave":
                stageController.leaveStage(call, result);
                break;
            case "joinChat":
                stageChat.join(call, result);
                break;
            case "leaveChat":
                stageChat.leave(call, result);
                break;
            default:
                result.notImplemented();
        }
    }

//    private void joinChat(MethodCall methodCall, @NonNull MethodChannel.Result result) {
//        String token = (String) methodCall.argument("token");
//        String sessionExpiryIso = (String) methodCall.argument("sessionExpiryIso");
//        String expiryIso = (String) methodCall.argument("expiryIso");
//        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//
//        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        Date sessionExpiryDate = null;
//        Date tokenExpiryDate = null;
//
//        try {
//            sessionExpiryDate = isoFormat.parse(sessionExpiryIso);
//            tokenExpiryDate = isoFormat.parse(expiryIso);
//            // Use the token with expiry dates
//            Date finalTokenExpiryDate = tokenExpiryDate;
//            Date finalSessionExpiryDate = sessionExpiryDate;
//            chatRoom = new ChatRoom("ap-south-1", chatTokenCallback -> {
//                chatTokenCallback.onSuccess(new ChatToken(token, finalSessionExpiryDate, finalTokenExpiryDate));
//                return null;
//            });
//            Log.d("ChatToken", "Token: " + token + ", Session Expiry: " + finalSessionExpiryDate + ", Token Expiry: " + finalTokenExpiryDate);
//        } catch (ParseException e) {
//            Log.d("ChatRoom", e.getMessage());
//        }
//
//        chatRoom.connect();
//        chatRoom.setReceiveMessageListener$ivs_chat_messaging_release(chatMessage -> {
//            Log.d("ChatRoom", "onMessageReceived " + chatMessage.component1());
//            Log.d("ChatRoom", "onMessageReceived " + chatMessage.getContent());
//            return null;
//        });
//        chatRoom.setReceiveEventListener$ivs_chat_messaging_release(chatEvent -> {
//            Log.d("ChatRoom", "onEventReceived " + chatEvent.component1());
//            Log.d("ChatRoom", "onEventReceived " + chatEvent.getEventName());
//            return null;
//        });
//        chatRoom.setListener(new ChatRoomListener() {
//            @Override
//            public void onConnecting(@NonNull ChatRoom chatRoom) {
//                Log.d("ChatRoom", "onConnecting");
//            }
//
//            @Override
//            public void onConnected(@NonNull ChatRoom chatRoom) {
//                Log.d("ChatRoom", "onConnected");
//            }
//
//            @Override
//            public void onDisconnected(@NonNull ChatRoom chatRoom, @NonNull DisconnectReason disconnectReason) {
//                Log.d("ChatRoom", "onDisconnected");
//            }
//
//            @Override
//            public void onMessageReceived(@NonNull ChatRoom chatRoom, @NonNull ChatMessage chatMessage) {
//                Log.d("ChatRoom", "onMessageReceived");
//            }
//
//            @Override
//            public void onEventReceived(@NonNull ChatRoom chatRoom, @NonNull ChatEvent chatEvent) {
//                new Handler(Looper.getMainLooper()).post(() -> {
//                    if (roomSink != null) roomSink.success(chatEvent.getEventName());
//                });
//                Log.d("ChatRoom", "onEventReceived 2 - " + chatEvent.getEventName());
//            }
//
//            @Override
//            public void onMessageDeleted(@NonNull ChatRoom chatRoom, @NonNull DeleteMessageEvent deleteMessageEvent) {
//                Log.d("ChatRoom", "onMessageDeleted");
//            }
//
//            @Override
//            public void onUserDisconnected(@NonNull ChatRoom chatRoom, @NonNull DisconnectUserEvent disconnectUserEvent) {
//                Log.d("ChatRoom", "onUserDisconnected");
//            }
//        });
//        result.success(null);
//    }
//
//
//    private void joinStage(MethodCall methodCall, @NonNull MethodChannel.Result result) {
//        String token = (String) methodCall.argument("token");
//        boolean shouldPublish = (boolean) methodCall.argument("shouldPublish");
//        Log.d("Stage", token);
//        Log.d("Stage", String.valueOf(shouldPublish));
//        if (shouldPublish) {
//            stage = new Stage(binding.getApplicationContext(), token, new PublishStrategy(binding.getApplicationContext()));
//        } else {
//            stage = new Stage(binding.getApplicationContext(), token, new ViewerStrategy());
//        }
//        stage.addRenderer(new StageListener(binding, stageSink, stageChat));
//        try {
//            stage.join();
//            Log.d("Stage joined", "Success");
//        } catch (Exception e) {
//            Log.d("Stage joined failed", e.getMessage());
//        }
//        result.success("Success");
//    }

//    private void leaveStage(MethodCall methodCall, @NonNull MethodChannel.Result result) {
//        stage.leave();
//        result.success("Stage is leaving...");
//    }

    // Render

//    @Override
//    public void onParticipantJoined(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
//        StageRenderer.super.onParticipantJoined(stage, participantInfo);
//        Log.d("Stage onPaJoined", participantInfo.participantId);
//        Log.d("Stage onPaJoined", participantInfo.attributes.toString());
//        Log.d("Stage onPaJoined", participantInfo.capabilities.toString());
//        Log.d("Stage onPaJoined", participantInfo.userInfo.toString());
//        Log.d("Stage onPaJoined", participantInfo.userId.toString());
//        joined.add(participantInfo.participantId);
//        if (!lefts.isEmpty()) {
//            lefts.remove(participantInfo.participantId);
//        }
//        stageMap.put("joined", joined);
//        String message = String.format("<>^S^E^R^V^E^R<>::dev::{\"type\":\"participantJoined\",\"category\":\"liveRoom\",\"data\":{\"participantId\":\"%s\"}}", participantInfo.participantId);
//        try {
//            chatRoom.sendMessage(new SendMessageRequest(message));
//        } catch (Exception e) {
//            Log.d("ChatRoom", e.getMessage());
//        }
//
//        if (stageSink != null) stageSink.success(stageMap);
////        participantInfo.capabilities.add(ParticipantInfo.Capabilities.SUBSCRIBE);
//    }

//    @Override
//    public void onParticipantLeft(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo) {
//        StageRenderer.super.onParticipantLeft(stage, participantInfo);
//        Log.d("Stage onPartLeft", participantInfo.toString());
//        if (!joined.isEmpty()) {
//            joined.remove(participantInfo.participantId);
//        }
//        lefts.add(participantInfo.participantId);
//        stageMap.put("left", lefts);
//        String message = String.format("<>^S^E^R^V^E^R<>::dev::{\"type\":\"participantLeft\",\"category\":\"liveRoom\",\"data\":{\"participantId\":\"%s\"}}", participantInfo.participantId);
//        try {
//            chatRoom.sendMessage(new SendMessageRequest(message));
//        } catch (Exception e) {
//            Log.d("ChatRoom", e.getMessage());
//        }
//        if (stageSink != null) stageSink.success(stageMap);
//    }


//    int totalViews = 0;

//    @Override
//    public void onStreamsAdded(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
//        StageRenderer.super.onStreamsAdded(stage, participantInfo, streams);
//        Log.d("Stage onStreamsAdded", streams.toString());
//        views.clear();
//        for (int i = 0; i < streams.size(); i++) {
//            Log.d("Stage onStreamsAddeqd", streams.get(i).getDevice().getDescriptor().urn);
//            if (streams.get(i).getStreamType() == StageStream.Type.VIDEO) {
//                String viewId = totalViews + "_" + streams.get(i).getDevice().getDescriptor().urn;
//                boolean isAlreadPresent = binding.getPlatformViewRegistry().registerViewFactory(viewId, new RemoteViewFactory(streams.get(i).getPreview()));
//                if (isAlreadPresent) {
//                    Log.d("Stage isAlreadPresent", streams.get(i).getDevice().getDescriptor().deviceId);
//                }
//                views.add(viewId);
//                totalViews++;
//                Log.d("Stage", "streamAdded: " + viewId);
//            }
//        }
//        stageMap.put("views", views);
//        stageMap.put("stream_added", streams.size());
//        for (int i = 0; i < streams.size(); i++) {
//            Log.d(TAG, "onStreamsAdded: " + streams.get(i).getDevice().getDescriptor().deviceId);
//        }
//        if (stageSink != null) stageSink.success(stageMap);
//    }

//    @Override
//    public void onStreamsRemoved(@NonNull Stage stage, @NonNull ParticipantInfo participantInfo, @NonNull List<StageStream> streams) {
//        StageRenderer.super.onStreamsRemoved(stage, participantInfo, streams);
//        Log.d("Stage onStreamsRemoved", participantInfo.toString());
//        Log.d("Stage onStreamsRemoved", participantInfo.userInfo.toString());
//        Log.d("Stage onStreamsRemoved", String.valueOf(participantInfo.isLocal));
//        stageMap.put("stream_removed", streams.size());
//        for (int i = 0; i < streams.size(); i++) {
//            StageStream stream = streams.get(i);
//            if (stream.getStreamType() == StageStream.Type.VIDEO) {
//                String deviceId = streams.get(i).getDevice().getDescriptor().deviceId;
//                views.removeIf(item -> item.contains(deviceId));
//                Log.d("Stage", "stream removed " + deviceId);
//            }
//        }
//        for (int i = 0; i < streams.size(); i++) {
//            Log.d(TAG, "onStreamsRemoved: " + streams.get(i).getDevice().getDescriptor().deviceId);
//        }
//        if (stageSink != null) stageSink.success(stageMap);
//    }

}
