import Flutter
import UIKit
import AmazonIVSPlayer

public class IVSPlayerFlutterView: NSObject, FlutterPlatformView, IVSPlayer.Delegate {
    private var player: IVSPlayer!
    private var playerView: IVSPlayerView!
    private var surfaceView: UIView!
    private var eventChannel: FlutterEventChannel!
    private var statusSink: FlutterEventSink?
    
    init(frame: CGRect, messenger: FlutterBinaryMessenger) {
        super.init()
        print("StreamView initializing...")
        

        // Initialize the player
        self.player = IVSPlayer()
        print("IVSPlayer initialized.")
        
        self.playerView = IVSPlayerView(frame: frame)
        self.surfaceView = UIView(frame: frame)
        self.surfaceView.addSubview(self.playerView)
        
        print("IVSPlayerView and surfaceView set up.")

        self.playerView.player = player
        player.delegate = self

        // Setup Flutter method channel
        let methodChannel = FlutterMethodChannel(name: "ivs_player_channel", binaryMessenger: messenger)
        methodChannel.setMethodCallHandler(self.onMethodCall)
        print("Method channel ivs_player_channel set up.")

        // Set up event channel for status updates
        self.eventChannel = FlutterEventChannel(name: "ivs_player_status_stream", binaryMessenger: messenger)
        self.eventChannel.setStreamHandler(self)
        print("Event channel ivs_player_status_stream set up.")
    }
    
    public func view() -> UIView {
        print("StreamView providing playerView as the view.")
        return self.playerView
    }
    
    public func onMethodCall(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        print("StreamView received method call: \(call.method) with arguments: \(String(describing: call.arguments))")
        switch call.method {
            case "load":
                if let urlString = call.arguments as? String, let url = URL(string: urlString) {
                    self.player.load(url)
                    result("Loading")
                    print("StreamView loading URL: \(urlString)")
                } else {
                    result(FlutterError(code: "INVALID_URL", message: "Invalid URL", details: nil))
                    print("StreamView received invalid URL.")
                }
                
            case "play":
                self.playerView.player?.looping = true
                self.player.play()
                result("Playing")
                print("StreamView started playback.")
                
            case "pause":
                self.player.pause()
                result("Paused")
                print("StreamView paused playback.")
                
            case "close":
                self.onClose()
                result("Closed")
                print("StreamView closed.")
                
            default:
                result(FlutterMethodNotImplemented)
                print("StreamView received unimplemented method call: \(call.method)")
        }
    }
    
    private func onClose() {
        print("StreamView onClose triggered.")
        if self.player != nil {
            self.player.pause()
            self.playerView.removeFromSuperview()
            self.player.delegate = nil
            self.player = nil
            print("StreamView player and view resources cleaned up.")
        }
        print("StreamView onClose completed.")
    }
    
    deinit {
        self.onClose()
        print("StreamView deinitialized and resources disposed.")
    }
}

extension IVSPlayerFlutterView: FlutterStreamHandler {
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.statusSink = events
        print("StreamView listening for status updates.")
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        self.statusSink = nil
        print("StreamView status updates cancelled.")
        return nil
    }
}

extension IVSPlayerFlutterView {
    public func player(_ player: IVSPlayer, didChangeState state: IVSPlayer.State) {
        print("StreamView player state changed: \(state.rawValue)")
        switch state {
            case .buffering:
                statusSink?("BUFFERING")
                print("StreamView BUFFERING")

            case .ready:
                player.looping = true
                self.player.play()
                statusSink?("READY")
                print("StreamView READY and playing")

            case .idle:
                statusSink?("IDLE")
                print("StreamView IDLE")
                
            case .playing:
                statusSink?("PLAYING")
                print("StreamView PLAYING")

            case .ended:
                statusSink?("ENDED")
                print("StreamView ENDED")

            default:
                print("StreamView encountered unknown state: \(state.rawValue)")
                break
        }
    }
}
