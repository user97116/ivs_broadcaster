import AmazonIVSBroadcast

public class ViewerStrategy: NSObject, IVSStageStrategy {
    public func stage(_ stage: IVSStage, streamsToPublishForParticipant participant: IVSParticipantInfo) -> [IVSLocalStageStream] {
        print("Stage streamsToPublishForParticipant")
        return [];
    }
  
    public func stage(_ stage: IVSStage, shouldPublishParticipant participant: IVSParticipantInfo) -> Bool {
        print("Stage shouldPublishParticipant")
        return false
    }
    
    public func stage(_ stage: IVSStage, shouldSubscribeToParticipant participant: IVSParticipantInfo) -> IVSStageSubscribeType {
        print("Stage shouldSubscribeToParticipant")
        return IVSStageSubscribeType.audioVideo
    }
    
    public func stage(_ stage: IVSStage, subscribeConfigurationForParticipant participant: IVSParticipantInfo) -> IVSSubscribeConfiguration {
        print("Stage subscribeConfigurationForParticipant")
        var config = IVSSubscribeConfiguration();
        do {
            try config.jitterBuffer.setMinDelay(IVSJitterBufferDelay.medium())
        } catch {
            print("Error setting jitter buffer delay: \(error)")
        }
        return config
    }
}
