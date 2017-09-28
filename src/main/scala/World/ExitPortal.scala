package World

import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.{Scene,Group,SubScene}

class ExitPortal(x : Int, y : Int) extends OverworldImmovable(x,y,"exitPortal") with Interactable{
  
  def checkClient(){
    if (Screen.name.equalsIgnoreCase(Screen.clientName)){
      Screen.deleteAllCharacters(Screen.houseScene(Screen.currentMapNum))
      
      Screen.currentMapType = "map"
      Screen.currentMapNum = Screen.clientPlayer.previousMap
      
      Screen.movementList.put(Screen.clientPlayer, new ClientMovement(Screen.currentMapType,Screen.currentMapNum))
      Screen.clientMovement = Screen.movementList.getOrElse(Screen.clientPlayer,null).asInstanceOf[ClientMovement]
      
      Screen.addACharacter(Screen.mapScene(Screen.clientPlayer.previousMap),Screen.clientPlayer,Screen.clientPlayer.previousCoordsX,Screen.clientPlayer.previousCoordsY)
    }
  }
  
  override def interact() : Boolean = {
    checkClient
    Screen.game.scene = Screen.mapScene(Screen.clientPlayer.previousMap)
    return true
  }
}