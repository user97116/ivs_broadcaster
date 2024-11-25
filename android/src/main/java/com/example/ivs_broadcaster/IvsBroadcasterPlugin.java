package com.example.ivs_broadcaster;

import android.os.Build;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;

public class IvsBroadcasterPlugin implements FlutterPlugin {


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        binding.getPlatformViewRegistry().registerViewFactory("ivs_player", new IVSPlayerFactory(binding.getBinaryMessenger()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            binding.getPlatformViewRegistry().registerViewFactory("ivs_stage_player", new IVSStageFactory(binding.getPlatformViewRegistry(), binding.getBinaryMessenger()));
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

    }
}
