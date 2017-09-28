package World

import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.{Scene,Group,SubScene}

class MapPortal(x : Int, y : Int, spawnX : Int, spawnY : Int, exitPlace : Int) extends OverworldImmovable(x,y,"mapPortal") with Interactable{
  
  def checkClient(){
    if (Screen.name.equalsIgnoreCase(Screen.clientName)){
      Screen.deleteAllCharacters(Screen.mapScene(Screen.currentMapNum))
      
      Screen.currentMapType = "map"
      Screen.currentMapNum = exitPlace
      Screen.clientPlayer.previousMap = exitPlace
      
      Screen.movementList.put(Screen.clientPlayer, new ClientMovement(Screen.currentMapType,Screen.currentMapNum))
      Screen.clientMovement = Screen.movementList.getOrElse(Screen.clientPlayer,null).asInstanceOf[ClientMovement]
      
      Screen.addACharacter(Screen.mapScene(exitPlace),Screen.clientPlayer,spawnX,spawnY)
    }
  }
  
  override def interact() : Boolean = {
    
    checkClient
    
    Screen.game.scene = Screen.mapScene(exitPlace)
    
    return true
  }
}