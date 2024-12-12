import Flutter
import AmazonIVSBroadcast

public class StageListener: NSObject, IVSStageRenderer {
    private var map: [String: Any] = [:]
    public var stageSink: FlutterEventSink?
    var joinedParticipants: [String] = []
    var leftParticipants: [String] = []

    init(stageSink: FlutterEventSink? = nil) {
        self.stageSink = stageSink
    }

    public func stage(_ stage: IVSStage, participantDidJoin participant: IVSParticipantInfo) {
        joinedParticipants.removeAll { $0 == participant.participantId }
        joinedParticipants.append(participant.participantId)
        map["joined_participants"] = joinedParticipants
        stageSink?(map)
    }
    
    public func stage(_ stage: IVSStage, participantDidLeave participant: IVSParticipantInfo) {
        leftParticipants.removeAll { $0 == participant.participantId }
        leftParticipants.append(participant.participantId)
        map["left_participants"] = leftParticipants
        stageSink?(map)
    }
    
 
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChange publishState: IVSParticipantPublishState) {
        map["participant_publish_state_changed"] =  publishState.rawValue
        stageSink?(map)
    }
    
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChange subscribeState: IVSParticipantSubscribeState) {
        map["participant_subscribe_state_changed"] =  subscribeState.rawValue
        stageSink?(map)
    }
    
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didAdd streams: [IVSStageStream]) {
        
    }
    
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didRemove streams: [IVSStageStream]) {
        
    }
    
    public func stage(_ stage: IVSStage, didChange connectionState: IVSStageConnectionState, withError error: (any Error)?) {
        print("StageListener: \(error!)")
    }
    
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChangeMutedStreams streams: [IVSStageStream]) {
        var streamsMutedChanged: [String] = []
        for stream in streams {
            let urn = stream.device.description
            streamsMutedChanged.append(urn)
        }
        map["streams_muted_changed"] = streamsMutedChanged
        stageSink?(map)
    }
}
