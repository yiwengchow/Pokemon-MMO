package World.Controller

import scalafx.scene.input._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.image._
import scalafx.scene.layout._
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import World._

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
        Screen.battleUIVisibility.value = false
    }
  }
}