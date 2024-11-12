import Flutter
import UIKit

public class IvsBroadcasterPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
      registrar.register(
          IvsPlayerFactory(messenger: registrar.messenger()),
          withId: "ivs_player"
      )
  }
}


