import Flutter
import AmazonIVSChatMessaging

public class StageChat: NSObject {
    public var roomSink: FlutterEventSink?
    
    init(roomSink: FlutterEventSink? = nil) {
        self.roomSink = roomSink
    }

    public func join(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        print("StageChat: joined chat")
        result("Chat is joined...")
    }
    
    public func leave(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        print("StageChat: leaving chat")
        result("Chat is leaving...")
    }
}
