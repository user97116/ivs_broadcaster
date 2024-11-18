import Flutter
import UIKit

public class IvsStagePlayerView: NSObject, FlutterPlatformView {
    private var surfaceView: UIView!
    private var eventChannel: FlutterEventChannel!
    private var statusSink: FlutterEventSink?
    
    init(frame: CGRect, messenger: FlutterBinaryMessenger) {
        super.init()
        print("IVSStage init.")
        self.surfaceView = UIView(frame: frame)
        
        // Setup Flutter method channel
        let methodChannel = FlutterMethodChannel(name: "ivs_stage_method", binaryMessenger: messenger)
        methodChannel.setMethodCallHandler(self.onMethodCall)
        print("Method channel IVSStage set up.")

        // Set up event channel for status updates
        self.eventChannel = FlutterEventChannel(name: "ivs_stage_event", binaryMessenger: messenger)
        self.eventChannel.setStreamHandler(self)
        print("Event channel IVSStage set up.")
    }
    
    public func view() -> UIView {
        return surfaceView
    }
    
    public func onMethodCall(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        print("IVSStage received method call: \(call.method) with arguments: \(String(describing: call.arguments))")
        switch call.method {
            case "load":
                result("Laoding")
            
            case "play":
                result("Playing")
                print("IVSStage started playback.")
                
            case "pause":
                result("Paused")
                print("IVSStage paused playback.")
                
            case "close":
                self.onClose()
                result("Closed")
                print("IVSStage closed.")
                
            default:
                result(FlutterMethodNotImplemented)
                print("IVSStage received unimplemented method call: \(call.method)")
        }
    }
    
    private func onClose() {
        print("IVSStage onClose...")
    }
    
    deinit {
        self.onClose()
        print("IVSStage deinitialized and resources disposed.")
    }
}

extension IvsStagePlayerView: FlutterStreamHandler {
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.statusSink = events
        print("IVSStage listening for status updates.")
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        self.statusSink = nil
        print("IVSStage status updates cancelled.")
        return nil
    }
}
