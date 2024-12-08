
import Foundation
import Flutter
import UIKit

public class RemoteView: NSObject, FlutterPlatformView {
    private var myView: UIView;
    
    init(testView: UIView) {
        self.myView = testView
    }
    
    public func view() -> UIView {
        myView
    }
}
