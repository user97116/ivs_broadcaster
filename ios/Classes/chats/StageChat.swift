import Flutter
import AmazonIVSChatMessaging

public class StageChat: NSObject, ChatRoomDelegate {
    public var roomSink: FlutterEventSink?
    public var chatRoom: ChatRoom?
    
    init(roomSink: FlutterEventSink? = nil) {
        self.roomSink = roomSink
    }

    public func join(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        print("StageChat: joined chat")
        if let arguments = call.arguments as? [String: Any] {
            let token = arguments["token"] as? String;
            let sessionExpiryIso = arguments["sessionExpiryIso"] as? String;
            let expiryIso = arguments["expiryIso"] as? String
            let isoFormat = ISO8601DateFormatter()
            isoFormat.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
            
            print("StageChat: \(token)")
            print("StageChat: \(sessionExpiryIso)")
            print("StageChat: \(isoFormat)")
            
            let sessionExpiryDate = isoFormat.date(from: sessionExpiryIso!)
            let tokenExpiryDate = isoFormat.date(from: expiryIso!)
            
            chatRoom = ChatRoom(awsRegion: "ap-south-1") {
                return ChatToken(token: token!, tokenExpirationTime: tokenExpiryDate, sessionExpirationTime: sessionExpiryDate)
            }
            print("StageChat: chat is ready")
            Task {
                do {
                    try await chatRoom?.connect()
                    print("StageChat: chat is connected")
                    chatRoom?.delegate = self
                } catch {
                    print("StageChat: Chat is not connected")
                }
            }

        }
        result("Chat is joined...")
    }
    
    public func leave(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        print("StageChat: leaving chat")
        result("Chat is leaving...")
    }
    
    public func roomDidConnect(_ room: ChatRoom) {
        print("StageChat: connected \(room.description)")
    }
    
    public func roomIsConnecting(_ room: ChatRoom) {
        print("StageChat: connecting \(room.description)")
    }
    
    public func room(_ room: ChatRoom, didDelete message: DeletedMessage) {
        print("StageChat: delete message \(message.description)")

    }
    public func roomDidDisconnect(_ room: ChatRoom) {
        print("StageChat: diconnected \(room.description)")

    }
    public func room(_ room: ChatRoom, didReceive message: ChatMessage) {
        print("StageChat: recive message \(message.description)")
    }
    public func room(_ room: ChatRoom, didReceive event: ChatEvent) {
        roomSink?(event.eventName)
        print("StageChat: event message \(event.eventName)")
    }
    public func room(_ room: ChatRoom, didDisconnect user: DisconnectedUser) {
        print("StageChat: diconnected user\(user.userId)")

    }
}
