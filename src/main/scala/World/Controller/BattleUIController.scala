package World.Controller

import scalafx.scene.input.{MouseEvent}
import scalafx.scene.control.{Label, Button}
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import World.Screen

@sfxml
class BattleUIController (
    private val oppTextArr: Label,
    private val acceptButton: Button,
    private val rejectButton: Button){
  
  import World.Messages._
  
  oppTextArr.text.bind(Screen.oppName)
  
  def onClick(e: MouseEvent){
    val button = new Button(e.source.asInstanceOf[javafx.scene.control.Button])
    val name = button.id.value
     name match {
      case "acceptButton" => 
          Screen.senderActor ! BattleReqAccepted(Screen.oppName.value, Screen.clientName, Screen.currentMapNum, Screen.currentMapType)
          Screen.battleUIVisibility.value = false
          
      case "rejectButton" => 
        Screen.senderActor ! RejectBattleReq(Screen.oppName.value, Screen.currentMapNum, Screen.currentMapType, "REJECTED!")
        Screen.battleUIVisibility.value = false
    }
  }
}