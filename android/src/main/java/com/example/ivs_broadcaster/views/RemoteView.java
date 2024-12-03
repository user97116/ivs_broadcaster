package com.example.ivs_broadcaster.views;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.amazonaws.ivs.broadcast.ImagePreviewSurfaceView;
import com.amazonaws.ivs.broadcast.ImagePreviewView;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.platform.PlatformView;

@SuppressLint("NewApi")
public class RemoteView implements PlatformView {

    private static final String TAG = "RemoteView"; // Consistent log tag
    private final View view;

    // Constructor to initialize the RemoteView with a given View
    public RemoteView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("View cannot be null");
        }
        this.view = view;
        Log.d(TAG, "RemoteView created");
    }

    @Override
    public View getView() {
        Log.d(TAG, "getView called");
        return view;
    }


    @Override
    public void onFlutterViewAttached(@NonNull View flutterView) {
        // Calling the default implementation using PlatformView.super
        PlatformView.super.onFlutterViewAttached(flutterView);
        Log.d(TAG, "onFlutterViewAttached: Flutter view attached");
    }

    @Override
    public void onFlutterViewDetached() {
        // Calling the default implementation using PlatformView.super
        PlatformView.super.onFlutterViewDetached();
        Log.d(TAG, "onFlutterViewDetached: Flutter view detached");
    }

    @Override
    public void dispose() {
        Log.d(TAG, "dispose: Cleaning up resources");
    }


    @Override
    public void onInputConnectionLocked() {
        // Calling the default implementation using PlatformView.super
        PlatformView.super.onInputConnectionLocked();
        Log.d(TAG, "onInputConnectionLocked: Input connection locked");
    }

    @Override
    public void onInputConnectionUnlocked() {
        // Calling the default implementation using PlatformView.super
        PlatformView.super.onInputConnectionUnlocked();
        Log.d(TAG, "onInputConnectionUnlocked: Input connection unlocked");
    }
}