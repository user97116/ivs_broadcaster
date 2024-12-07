package com.example.ivs_broadcaster.chats;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazonaws.ivs.chat.messaging.ChatRoom;
import com.amazonaws.ivs.chat.messaging.ChatRoomListener;
import com.amazonaws.ivs.chat.messaging.ChatToken;
import com.amazonaws.ivs.chat.messaging.DisconnectReason;
import com.amazonaws.ivs.chat.messaging.entities.ChatEvent;
import com.amazonaws.ivs.chat.messaging.entities.ChatMessage;
import com.amazonaws.ivs.chat.messaging.entities.DeleteMessageEvent;
import com.amazonaws.ivs.chat.messaging.entities.DisconnectUserEvent;
import com.amazonaws.ivs.chat.messaging.logger.ChatLogger;
import com.amazonaws.ivs.chat.messaging.requests.DisconnectUserRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class StageChat {
    public ChatRoom room;
    EventChannel.EventSink sink;

    public StageChat(EventChannel.EventSink sink) {
        this.sink = sink;
    }

    public void join(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        if (room != null) {
            room.setListener(null);
            room.setReceiveEventListener$ivs_chat_messaging_release(null);
            room.setReceiveMessageListener$ivs_chat_messaging_release(null);
            room.disconnect();
            room = null;
        }
        Log.d("StageChat", "Chat room is destroying...");
        //
        String token = (String) methodCall.argument("token");
        String sessionExpiryIso = (String) methodCall.argument("sessionExpiryIso");
        String expiryIso = (String) methodCall.argument("expiryIso");
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date sessionExpiryDate = null;
        Date tokenExpiryDate = null;

        try {
            sessionExpiryDate = isoFormat.parse(sessionExpiryIso);
            tokenExpiryDate = isoFormat.parse(expiryIso);
            Date finalTokenExpiryDate = tokenExpiryDate;
            Date finalSessionExpiryDate = sessionExpiryDate;
            room = new ChatRoom("ap-south-1", chatTokenCallback -> {
                chatTokenCallback.onSuccess(new ChatToken(token, finalSessionExpiryDate, finalTokenExpiryDate));
                return null;
            });
            Log.d("StageChat", "Token: " + token + ", Session Expiry: " + finalSessionExpiryDate + ", Token Expiry: " + finalTokenExpiryDate);
            Log.d("StageChat", "Chat is ready");
            room.setLogger(new ChatLogger() {
                @Override
                public void debug(@NonNull String s) {
                    Log.d("StageChat", s);
                }

                @Override
                public void info(@NonNull String s) {
                    Log.d("StageChat", s);
                }

                @Override
                public void error(@NonNull String s, @Nullable Throwable throwable) {
                    Log.d("StageChat", s);
                }
            });
            room.connect();
            room.setListener(new ChatRoomListener() {
                @Override
                public void onConnecting(@NonNull ChatRoom chatRoom) {
                    Log.d("StageChat", "onConnecting");
                }

                @Override
                public void onConnected(@NonNull ChatRoom chatRoom) {
                    Log.d("StageChat", "onConnected");
                    if (room != null) {
                        room.setReceiveMessageListener$ivs_chat_messaging_release(chatMessage -> {
                            Log.d("StageChat", "onMessageReceived " + chatMessage.component1());
                            Log.d("StageChat", "onMessageReceived " + chatMessage.getContent());
                            return null;
                        });
                        room.setReceiveEventListener$ivs_chat_messaging_release(chatEvent -> {
                            Log.d("StageChat", "onEventReceived " + chatEvent.component1());
                            Log.d("StageChat", "onEventReceived " + chatEvent.getEventName());
                            return null;
                        });
                    }
                }

                @Override
                public void onDisconnected(@NonNull ChatRoom chatRoom, @NonNull DisconnectReason disconnectReason) {
                    Log.d("StageChat", "onDisconnected");
                }

                @Override
                public void onMessageReceived(@NonNull ChatRoom chatRoom, @NonNull ChatMessage chatMessage) {
                    Log.d("StageChat", "onMessageReceived");
                }

                @Override
                public void onEventReceived(@NonNull ChatRoom chatRoom, @NonNull ChatEvent chatEvent) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (sink != null) sink.success(chatEvent.getEventName());
                    });
                    Log.d("StageChat", "onEventReceived 2 - " + chatEvent.getEventName());
                }

                @Override
                public void onMessageDeleted(@NonNull ChatRoom chatRoom, @NonNull DeleteMessageEvent deleteMessageEvent) {
                    Log.d("StageChat", "onMessageDeleted");
                }

                @Override
                public void onUserDisconnected(@NonNull ChatRoom chatRoom, @NonNull DisconnectUserEvent disconnectUserEvent) {
                    Log.d("StageChat", "onUserDisconnected");
                }
            });

        } catch (ParseException e) {
            Log.d("StageChat", "this is error " + e.getMessage());
            Log.d("StageChat", "Chat is not ready");
        }
        result.success(null);
    }

    public void leave(MethodCall methodCall, @NonNull MethodChannel.Result result) {
        if (room == null) {
            result.success("Chat is leaving...");
            return;
        }
        room.setListener(null);
        room.setReceiveEventListener$ivs_chat_messaging_release(null);
        room.setReceiveMessageListener$ivs_chat_messaging_release(null);
        room.disconnect();
        room = null;
        Log.d("StageChat", "Chat leave room is destroying...");
        result.success("Chat is leaving...");
    }
}
