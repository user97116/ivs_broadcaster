import Flutter
import AmazonIVSBroadcast

public class StageController: NSObject {
    public var stage: IVSStage?;
    public var stageSink: FlutterEventSink?
    private var stageChat: StageChat?

    init(stageSink: FlutterEventSink? = nil, stageChat: StageChat? = nil) {
        self.stageSink = stageSink
        self.stageChat = stageChat
    }
    
    public func joinStage(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if let arguments = call.arguments as? [String: Any] {
            let token = arguments["token"] as? String
            let shouldPublish = arguments["shouldPublish"] as? Bool ?? false
            
            print("StageController: token \(token!)")
            print("StageController: shouldPublish \(shouldPublish)")
            
            do {
                if shouldPublish {
                    let publish =  PublishStrategy();
                    stage = try IVSStage(token: token!, strategy: publish);
                    print("StageController: published")
                }else {
                    let sub = ViewerStrategy();
                    stage = try IVSStage(token: token!, strategy: sub);
                    print("StageController: viewer")
                }
                stage?.addRenderer(StageListener(stageSink: stageSink))
                
                do {
                    try stage?.join()
                    print("StageController: stage is joined")
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
