import Foundation
import Flutter
import UIKit

class RemoveViewFactory:  NSObject, FlutterPlatformViewFactory {
    private var view: UIView;
    
    init(view: UIView) {
        self.view = view
    }
    
    func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?) -> any FlutterPlatformView {
        return RemoteView(testView: view)
    }
    
}
