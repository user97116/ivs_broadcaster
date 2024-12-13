import Flutter
import UIKit
import AmazonIVSBroadcast

public class IvsStagePlayerView: NSObject, FlutterPlatformView {
    private var surfaceView: UIView!
    private var eventChannel: FlutterEventChannel!
    private var statusSink: FlutterEventSink?
    private var hashMap: [String: Any] = [:]
    private var stage: IVSStage?

    
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
            case "join":
                join(call, result: result)
                break
            case "leave":
                leave(call, result: result)
            default:
                result(FlutterMethodNotImplemented)
                print("IVSStage received unimplemented method call: \(call.method)")
        }
    }
    
    private func join(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        do {
            let token = call.arguments as? String;
            stage = try IVSStage(token: token!, strategy: self)
            stage?.addRenderer(self);
            try stage?.join()
            hashMap["is_joined"] = true;
            print("Stage is joined")
        } catch let error {
            print("IVSStage Can't joined with stage ")
            hashMap["is_joined"] = false;
            print("Stage is not joining")
        }
        statusSink?(hashMap)
        result("joined")
    }
    
    private func onClose() {
        print("IVSStage onClose...")
        surfaceView?.removeFromSuperview()
        stage?.leave()
    }
    
    private func leave(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        surfaceView?.removeFromSuperview()
        stage?.leave()
//        stage?.remove(self)
        result("leaved")
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
extension IvsStagePlayerView: IVSStageStrategy {
    public func stage(_ stage: IVSStage, subscribeConfigurationForParticipant participant: IVSParticipantInfo) -> IVSSubscribeConfiguration {
        print("IVSStage subscribeConfigurationForParticipant")
        let config = IVSSubscribeConfiguration()
        try! config.jitterBuffer.setMinDelay(.medium())
        return config
        
    }
    public func stage(_ stage: IVSStage, shouldSubscribeToParticipant participant: IVSParticipantInfo) -> IVSStageSubscribeType {
        print("IVSStage shouldSubscribeToParticipant")
        return IVSStageSubscribeType.audioVideo
    }
    public func stage(_ stage: IVSStage, shouldPublishParticipant participant: IVSParticipantInfo) -> Bool {
        print("IVSStage shouldPublishParticipant")
        return true
    }
    public func stage(_ stage: IVSStage, streamsToPublishForParticipant participant: IVSParticipantInfo) -> [IVSLocalStageStream] {
        print("IVSStage streamsToPublishForParticipant")
        let devices = IVSDeviceDiscovery().listLocalDevices()

        // Find the camera virtual device, choose the front source, and create a stream
        let camera = devices.compactMap({ $0 as? IVSCamera }).first!
        let frontSource = camera.listAvailableInputSources().first(where: { $0.position == .front })!
        camera.setPreferredInputSource(frontSource)
        let cameraStream = IVSLocalStageStream(device: camera)

        // Find the microphone virtual device and create a stream
        let microphone = devices.compactMap({ $0 as? IVSMicrophone }).first!
        let microphoneStream = IVSLocalStageStream(device: microphone)

        // Configure the audio manager to use the videoChat preset, which is optimized for bi-directional communication, including echo cancellation.
        IVSStageAudioManager.sharedInstance().setPreset(.videoChat)

        return [cameraStream, microphoneStream]
    }
}
extension IvsStagePlayerView: IVSStageRenderer {
    public func stage(_ stage: IVSStage, participantDidJoin participant: IVSParticipantInfo) {
        print("Stage IvsStagePlayerView")
        hashMap["joined"] = participant.participantId;
        if(statusSink != nil) {
            statusSink?(hashMap)
        }
    }
    public func stage(_ stage: IVSStage, participantDidLeave participant: IVSParticipantInfo) {
        hashMap["left"] = participant.participantId;
        if(statusSink != nil) {
            statusSink?(hashMap)
        }
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didAdd streams: [IVSStageStream]) {
        hashMap["stream_added"] = streams.count
        print("Stage amar \(streams)")
        do {
            for stream in streams {
                if let imageDevice = stream.device as? IVSImageDevice {
                    surfaceView.isHidden = true
                    surfaceView.clipsToBounds = false

                    let previewView = try imageDevice.previewView();
                    previewView.frame = surfaceView.bounds
                    previewView.backgroundColor = .red
                    surfaceView.insertSubview(previewView, at: 0)
                    surfaceView.setNeedsLayout()
                    surfaceView.layoutIfNeeded()
                    
                    surfaceView.isHidden = false
                    surfaceView.clipsToBounds = false

                }
            }
        }catch let error {
            print("stage error \(error)")
        }
        if(statusSink != nil) {
            statusSink?(hashMap)
        }
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChange publishState: IVSParticipantPublishState) {
        hashMap["publish_changed"] = participant.participantId;
        if(statusSink != nil) {
            statusSink?(hashMap)
        }
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChange subscribeState: IVSParticipantSubscribeState) {
        hashMap["subscribe_changed"] = participant.participantId;
        if(statusSink != nil) {
            statusSink?(hashMap)
        }
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didRemove streams: [IVSStageStream]) {
        hashMap["stream_removed"] = streams.count
        if(statusSink != nil) {
            statusSink?(hashMap)
        }
    }
    public func stage(_ stage: IVSStage, didChange connectionState: IVSStageConnectionState, withError error: (any Error)?) {
        hashMap["error"] = error?.localizedDescription;
        if(statusSink != nil) {
            statusSink?(hashMap)
        }
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChangeMutedStreams streams: [IVSStageStream]) {
        hashMap["stream_mute"] = streams.count;
        if(statusSink != nil) {
            statusSink?(hashMap)
        }
    }
}
