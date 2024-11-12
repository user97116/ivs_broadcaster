import Flutter
import UIKit
import AmazonIVSPlayer

public class StreamView: NSObject, FlutterPlatformView, IVSPlayer.Delegate {
    private var player: IVSPlayer!
    private var playerView: IVSPlayerView!
    private var surfaceView: UIView!
    private var eventChannel: FlutterEventChannel!
    private var statusSink: FlutterEventSink?
    
    init(frame: CGRect, messenger: FlutterBinaryMessenger) {
        super.init()
        
        // Initialize the player
        self.player = IVSPlayer()
        self.playerView = IVSPlayerView(frame: frame)
        self.surfaceView = UIView(frame: frame)
        self.surfaceView.addSubview(self.playerView)

        //
        self.playerView.player = player
        
        // Self must conform to IVSPlayer.Delegate
        player.delegate = self

        
        // Setup Flutter method channel
        let methodChannel = FlutterMethodChannel(name: "ivs_player_channel", binaryMessenger: messenger)
        methodChannel.setMethodCallHandler(self.onMethodCall)
        
        // Set up event channel for status updates
        self.eventChannel = FlutterEventChannel(name: "ivs_player_status_stream", binaryMessenger: messenger)
        self.eventChannel.setStreamHandler(self)
    }
    
    public func view() -> UIView {
        self.playerView
    }
    
    
    public func onMethodCall(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        print("StreamView method calling")
        switch call.method {
            case "load":
                if let urlString = call.arguments as? String, let url = URL(string: urlString) {
                    self.player.load(url)
                    result("Loading")
                } else {
                    result(FlutterError(code: "INVALID_URL", message: "Invalid URL", details: nil))
                }
                
            case "play":
                self.player.looping = true;
                self.player.play()
                result("Playing")
                
            case "pause":
                self.player.pause()
                result("Paused")
                
            case "close":
                self.onClose()
                result("Closed")
                
            default:
                result(FlutterMethodNotImplemented)
        }
    }
    
    private func onClose() {
        self.playerView.removeFromSuperview()
    }
}

extension StreamView: FlutterStreamHandler {
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.statusSink = events
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        self.statusSink = nil
        return nil
    }
}

extension StreamView {
    public func player(_ player: IVSPlayer, didChangeState state: IVSPlayer.State) {
        print("StreamView status")
        switch state {
            case .buffering:
                statusSink?("BUFFERING")
            
            case .ready:
                self.player.play()
                statusSink?("READY")
                
            case .idle:
                statusSink?("IDLE")
                
            case .playing:
                statusSink?("PLAYING")
                
            case .ended:
                statusSink?("ENDED")
                
            default:
                break
        }
    }
}
