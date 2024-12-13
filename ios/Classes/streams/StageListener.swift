import Flutter
import AmazonIVSBroadcast
import AmazonIVSChatMessaging

public class StageListener: NSObject, IVSStageRenderer {
    private var map: [String: Any] = [:]
    public var stageSink: FlutterEventSink?
    var joinedParticipants: [String] = []
    var leftParticipants: [String] = []
    var viewsParticipants: [String] = []
    private var stageChat: StageChat
    private var binding: FlutterPluginRegistrar

    init(stageSink: FlutterEventSink? = nil, stageChat: StageChat, binding: FlutterPluginRegistrar) {
        self.binding = binding
        self.stageSink = stageSink
        self.stageChat = stageChat
        print("StageListener init");
    }
    
    public func stage(_ stage: IVSStage, participantDidJoin participant: IVSParticipantInfo) {
        let participantId = participant.participantId
        let message = "<>^S^E^R^V^E^R<>::dev::{\"type\":\"participantJoined\",\"category\":\"liveRoom\",\"data\":{\"participantId\":\"\(participantId)\"}}"
        stageChat.chatRoom?.sendMessage(with: SendMessageRequest(content: message))
        print("StageListener message sent \(message)")
        joinedParticipants.removeAll { $0 == participant.participantId }
        joinedParticipants.append(participant.participantId)
        map["joined_participants"] = joinedParticipants
        stageSink?(map)
        print("StageListener joined")
    }
    
    public func stage(_ stage: IVSStage, participantDidLeave participant: IVSParticipantInfo) {
        let participantId = participant.participantId
        let message = "<>^S^E^R^V^E^R<>::dev::{\"type\":\"participantLeft\",\"category\":\"liveRoom\",\"data\":{\"participantId\":\"\(participantId)\"}}"
        stageChat.chatRoom?.sendMessage(with: SendMessageRequest(content: message))
        print("StageListener message sent \(message)")
        leftParticipants.removeAll { $0 == participant.participantId }
        leftParticipants.append(participant.participantId)
        map["left_participants"] = leftParticipants
        stageSink?(map)
        print("StageListener leaved")
    }
    
 
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChange publishState: IVSParticipantPublishState) {
        map["participant_publish_state_changed"] =  publishState.rawValue
        stageSink?(map)
        print("StageListener publish state")
    }
    
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChange subscribeState: IVSParticipantSubscribeState) {
        map["participant_subscribe_state_changed"] =  subscribeState.rawValue
        stageSink?(map)
        print("StageListener subscribe state")
    }
    
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didAdd streams: [IVSStageStream]) {
        for stream in streams {
            if let imageDevice = stream.device as? IVSImageDevice {
                let viewId = UUID().uuidString + "_" + stream.device.descriptor().urn;
                do {
                    binding.register(RemoveViewFactory(view: try imageDevice.previewView()), withId: viewId)
                    print("StageListener view is registered")

                }catch {
                    print("StageListener view not register")
                }
                viewsParticipants.removeAll { $0 == viewId }
                viewsParticipants.append(viewId)
            }
        }
        map["stream_added"] = viewsParticipants;
        stageSink?(map)
        print("StageListener stream added")

    }
    
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didRemove streams: [IVSStageStream]) {
        for stream in streams {
            if let imageDevice = stream.device as? IVSImageDevice {
                let viewId = stream.device.descriptor().urn;
                viewsParticipants.removeAll { $0.contains(viewId)}
            }
        }
        map["stream_removed"] = viewsParticipants;
        stageSink?(map)
        print("StageListener stream removed")
    }
    
    public func stage(_ stage: IVSStage, didChange connectionState: IVSStageConnectionState, withError error: (any Error)?) {
        print("StageListener: \(error) \(connectionState) \(stage)")
    }
    
    public func stage(_ stage: IVSStage, participant: IVSParticipantInfo, didChangeMutedStreams streams: [IVSStageStream]) {
        var streamsMutedChanged: [String] = []
        for stream in streams {
            let urn = stream.device.descriptor().urn
            streamsMutedChanged.append(urn)
        }
        map["streams_muted_changed"] = streamsMutedChanged
        stageSink?(map)
        print("StageListener stream muted")

    }
}
