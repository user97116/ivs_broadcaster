import Flutter
import UIKit
import AmazonIVSPlayer

public class StreamView: NSObject, FlutterPlatformView, IVSPlayer.Delegate {
    internal init(player: IVSPlayer? = nil, playerView: IVSPlayerView? = nil, playerViewController: UIViewController? = nil, eventChannel: FlutterEventChannel? = nil, statusSink: FlutterEventSink? = nil) {
        self.player = player
        self.playerView = playerView
        self.playerViewController = playerViewController
        self.eventChannel = eventChannel
        self.statusSink = statusSink
    }
    
    
    public func view() -> UIView {
        playerView
    }
    
    
    private var player: IVSPlayer!
    private var playerView: IVSPlayerView!
    private var playerViewController: UIViewController!
    private var eventChannel: FlutterEventChannel!
    private var statusSink: FlutterEventSink?
    
    init(frame: CGRect, messenger: FlutterBinaryMessenger) {
        super.init()
        
        // Initialize the player
        self.player = IVSPlayer()
        self.playerView = IVSPlayerView()
        
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
    

    
    public func onMethodCall(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "load":
            if let urlString = call.arguments as? String, let url = URL(string: urlString) {
                self.player.load(url)
                result("Loading")
            } else {
                result(FlutterError(code: "INVALID_URL", message: "Invalid URL", details: nil))
            }
            
        case "play":
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
 
    public func onStateChanged(_ state: IVSPlayer.State) {
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
