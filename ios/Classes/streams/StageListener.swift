import AmazonIVSBroadcast

public class StageListener: NSObject, IVSStageRenderer {
    public func stage(_ stage: IVSStage, participantDidJoin participant: IVSParticipantInfo) {
    }
    public func stage(_ stage: IVSStage, participantDidLeave participant: IVSParticipantInfo) {
        
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didAdd streams: [IVSStageStream]) {
        
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChange publishState: IVSParticipantPublishState) {
        
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChange subscribeState: IVSParticipantSubscribeState) {
        
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didRemove streams: [IVSStageStream]) {
        
    }
    public func stage(_ stage: IVSStage, didChange connectionState: IVSStageConnectionState, withError error: (any Error)?) {
        
    }
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChangeMutedStreams streams: [IVSStageStream]) {
        
    }
}
