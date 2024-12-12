import AmazonIVSBroadcast

public class ViewerStrategy: NSObject, IVSStageStrategy {
    public func stage(_ stage: IVSStage, streamsToPublishForParticipant participant: IVSParticipantInfo) -> [IVSLocalStageStream] {
        return [];
    }
    
    public func stage(_ stage: IVSStage, shouldPublishParticipant participant: IVSParticipantInfo) -> Bool {
        return false
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
