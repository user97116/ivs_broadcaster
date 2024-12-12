import AmazonIVSBroadcast

public class PublishStrategy: NSObject, IVSStageStrategy {
    public func stage(_ stage: IVSStage, streamsToPublishForParticipant participant: IVSParticipantInfo) -> [IVSLocalStageStream] {
        let devices = IVSDeviceDiscovery().listLocalDevices()

        let camera = devices.compactMap({ $0 as? IVSCamera }).first!
        let frontSource = camera.listAvailableInputSources().first(where: { $0.position == .front })!
        camera.setPreferredInputSource(frontSource)
        let cameraStream = IVSLocalStageStream(device: camera)

        let microphone = devices.compactMap({ $0 as? IVSMicrophone }).first!
        let microphoneStream = IVSLocalStageStream(device: microphone)

        IVSStageAudioManager.sharedInstance().setPreset(.videoChat)

        return [cameraStream, microphoneStream]
    }
    
    public func stage(_ stage: IVSStage, shouldPublishParticipant participant: IVSParticipantInfo) -> Bool {
        return true
    }
    
    public func stage(_ stage: IVSStage, shouldSubscribeToParticipant participant: IVSParticipantInfo) -> IVSStageSubscribeType {
        return IVSStageSubscribeType.audioVideo
    }
    
    public func stage(_ stage: IVSStage, subscribeConfigurationForParticipant participant: IVSParticipantInfo) -> IVSSubscribeConfiguration {
        var config = IVSSubscribeConfiguration();
        do {
            try config.jitterBuffer.setMinDelay(IVSJitterBufferDelay.medium())
        } catch {
            print("Error setting jitter buffer delay: \(error)")
        }
        return config
    }
}
