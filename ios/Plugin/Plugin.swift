import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(ReaderPlugin)
public class ReaderPlugin: CAPPlugin {
    
    @objc func openFile(_ call: CAPPluginCall) {
        let url = call.getString("url") ?? ""
        let title = call.getString("title") ?? ""
        let navbarColor = call.getString("navbarColor") ?? "#1989fa"
        open(url:url,title:title,navbarColor: navbarColor)
        call.success()
    }
    func open (url:String,title:String,navbarColor:String) {
        DispatchQueue.main.async {
            let webController = WebController()
            webController.toolView.isHiddenToolBar=true
            webController.title=title
            webController.navbarColor = navbarColor
            webController.load(url)
            self.bridge.viewController.present(webController, animated: true, completion: nil)
        }

      
    }
    

}
