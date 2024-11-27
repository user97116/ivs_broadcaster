package com.example.ivs_broadcaster.views;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.platform.PlatformView;

@RequiresApi(api = Build.VERSION_CODES.P)
public class RemoteView implements PlatformView {
    private final View view;

    RemoteView(View view) {
        this.view = view;
        Log.d("remove_views", "RemoteView");
    }

    @Override
    public View getView() {
        Log.d("remove_views", "getView called");
        return view;
    }

    @Override
    public void onFlutterViewAttached(@NonNull View flutterView) {
        PlatformView.super.onFlutterViewAttached(flutterView);
        Log.d("remove_views", "onFlutterViewAttached");

    }

    @Override
    public void onFlutterViewDetached() {
        PlatformView.super.onFlutterViewDetached();
        Log.d("remove_views", "onFlutterViewDetached");
    }

    @Override
    public void dispose() {
        Log.d("remove_views", "dispose");
        if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
            Log.d("remove_views", "View detached from parent");
        }
    }

    @Override
    public void onInputConnectionLocked() {
        PlatformView.super.onInputConnectionLocked();
    }

    @Override
    public void onInputConnectionUnlocked() {
        PlatformView.super.onInputConnectionUnlocked();
    }
}