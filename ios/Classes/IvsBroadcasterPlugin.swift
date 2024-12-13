import Flutter
import UIKit
import AVFoundation

public class IvsBroadcasterPlugin: NSObject, FlutterPlugin {
    private static var stageEventChannel: FlutterEventChannel!
    private static var roomEventChannel: FlutterEventChannel!
    private static var stageHandler: StageStreamHandler = StageStreamHandler()
    private static var roomHandler: RoomStreamHandler = RoomStreamHandler()
    private static var stageController: StageController?
    private static var stageChat: StageChat?;
    private static var binding: FlutterPluginRegistrar?
    
  public static func register(with registrar: FlutterPluginRegistrar) {
      self.binding = registrar
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
      self.stageEventChannel.setStreamHandler(self.stageHandler)
      
      self.roomEventChannel = FlutterEventChannel(name: "gb_ivs_stage_event_room", binaryMessenger: registrar.messenger())
      self.roomEventChannel.setStreamHandler(self.roomHandler)
      Task {
         await AVCaptureDevice.requestAccess(for: .video)
      }
  }
    

    public static func onMethodCall(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        print("amar " + call.method)
        switch (call.method) {
            case "init":
                stageChat =  StageChat(roomSink: roomHandler.roomSink);
                stageController =  StageController(stageSink: stageHandler.stageSink, stageChat: stageChat, binding: binding!);
                result(nil)
                break;
            case "join":
                stageController?.joinStage(call, result: result)
                break;
            case "leave":
                stageController?.leaveStage(call, result: result)
                break;
            case "joinChat":
               stageChat?.join(call, result: result)
                break;
            case "leaveChat":
                stageChat?.leave(call, result: result)
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
        print("StageStreamHandler on listen")
        return nil
    }

    func onCancel(withArguments arguments: Any?) -> FlutterError? {
        stageSink = nil
        print("StageStreamHandler on oncancel")
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
