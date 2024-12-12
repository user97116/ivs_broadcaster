import Flutter
import UIKit

public class IvsBroadcasterPlugin: NSObject, FlutterPlugin {
    private static var stageEventChannel: FlutterEventChannel!
    private static var roomEventChannel: FlutterEventChannel!
    private static var stageHandler: StageStreamHandler?
    private static var roomHandler: RoomStreamHandler?
    private static var stageController: StageController?
    
  public static func register(with registrar: FlutterPluginRegistrar) {
      registrar.register(
          IvsPlayerFactory(messenger: registrar.messenger()),
          withId: "ivs_player"
      )
      registrar.register(
        IvsStagePlayerFactory(messenger: registrar.messenger()),
          withId: "ivs_stage_player"
      )
      let methodChannel = FlutterMethodChannel(name: "gb_ivs_stage_method", binaryMessenger: registrar.messenger())
      methodChannel.setMethodCallHandler(self.onMethodCall)
      
      self.stageEventChannel = FlutterEventChannel(name: "gb_ivs_stage_event", binaryMessenger: registrar.messenger())
      self.stageHandler = StageStreamHandler()
      self.stageEventChannel.setStreamHandler(self.stageHandler)
      
      self.roomEventChannel = FlutterEventChannel(name: "gb_ivs_stage_event_room", binaryMessenger: registrar.messenger())
      self.roomHandler = RoomStreamHandler()
      self.roomEventChannel.setStreamHandler(self.roomHandler)
  }
    

    public static func onMethodCall(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        print("amar " + call.method)
        switch (call.method) {
            case "init":
//                stageChat = new StageChat(roomSink);
//                stageController = new StageController(binding, stageSink, stageChat);
                stageController =  StageController();
                result(nil)
                break;
            case "join":
                stageController?.joinStage(call, result: result)
                break;
            case "leave":
                stageController?.leaveStage(call, result: result)
                break;
            case "joinChat":
//                stageChat.join(call, result);
                result(nil)
                break;
            case "leaveChat":
//                stageChat.leave(call, result);
                result(nil)
                break;
            default:
                result(FlutterMethodNotImplemented)
        }
    }
    
}
class StageStreamHandler: NSObject, FlutterStreamHandler {
    public var stageSink: FlutterEventSink?

    func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        stageSink = events
        return nil
    }

    func onCancel(withArguments arguments: Any?) -> FlutterError? {
        stageSink = nil
        return nil
    }
}
class RoomStreamHandler: NSObject, FlutterStreamHandler {
    public var roomSink: FlutterEventSink?

    func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        roomSink = events
        return nil
    }

    func onCancel(withArguments arguments: Any?) -> FlutterError? {
        roomSink = nil
        return nil
    }
}
