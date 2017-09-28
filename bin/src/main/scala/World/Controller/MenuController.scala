package World.Controller

import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.AnchorPane
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import World.Screen
import World.Messages._

@sfxml
class MenuController(
    anchorPane : AnchorPane,
    guideButton : Button,
    logoutButton : Button){
  
  def onClickGuide{
    
  }
  
  def onClickLogout{
    Screen.syncCanceller.cancel
    Screen.senderActor ! Synchronizer(Screen.clientPlayer)
    Screen.senderActor ! Logout(Screen.clientName, Screen.currentMapType, Screen.currentMapNum)
  }
  
}