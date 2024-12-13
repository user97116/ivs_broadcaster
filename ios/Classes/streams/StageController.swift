import Flutter
import AmazonIVSBroadcast

public class StageController: NSObject {
    public var stage: IVSStage?;
    public var stageSink: FlutterEventSink?
    private var stageChat: StageChat?
    private var binding: FlutterPluginRegistrar
    private var viewer: ViewerStrategy
    private var publiser: PublishStrategy
    private var listener: StageListener

    init(stageSink: FlutterEventSink? = nil, stageChat: StageChat? = nil, binding:FlutterPluginRegistrar) {
        self.binding = binding
        self.stageSink = stageSink
        self.stageChat = stageChat
        self.viewer = ViewerStrategy()
        self.publiser = PublishStrategy()
        self.listener = StageListener(stageSink: stageSink, stageChat: stageChat!, binding: binding)
    }
    
    public func joinStage(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if let arguments = call.arguments as? [String: Any] {
            let token = arguments["token"] as? String
            let shouldPublish = arguments["shouldPublish"] as? Bool ?? false
            
            print("StageController: token \(token!)")
            print("StageController: shouldPublish \(shouldPublish)")
            
            do {
                if shouldPublish {
                    stage = try IVSStage(token: token!, strategy: publiser);
                    print("StageController: published")
                }else {
                    stage = try IVSStage(token: token!, strategy: viewer);
                    print("StageController: viewer")
                }
                do {
                    stage?.leave()
                    stage!.addRenderer(listener)
                    let x = try stage?.join()
                    print("StageController: stage is joined \(x)")
                } catch {
                    print("StageController: stage is not joined")
                }
                print("StageController: Set stage")
            } catch {
                print("StageController: Can't connect to stage")
            }
        }
        result("Success")
    }
    
    public func leaveStage(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        stage?.leave()
        print("StageController: Stage is leaving")
        result("StageController: StageController is leaving...")
    }
}

