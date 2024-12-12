import Flutter
import AmazonIVSBroadcast

public class StageController: NSObject {
    public var stage: IVSStage?;
    
    public func joinStage(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if let arguments = call.arguments as? [String: Any] {
            var token = arguments["token"] as? String
            var shouldPublish = arguments["shouldPublish"] as? Bool ?? false
            do {
                if shouldPublish {
                    stage = try IVSStage(token: token!, strategy:  PublishStrategy());
                }else {
                    stage = try IVSStage(token: token!, strategy:  ViewerStrategy());
                }
//                stage?.addRenderer(<#T##renderer: any IVSStageRenderer##any IVSStageRenderer#>)
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
