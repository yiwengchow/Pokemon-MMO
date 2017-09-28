package World

import scalafx.Includes._
import scalafx.scene.{Scene}
import scalafx.scene.input.{ KeyCode, KeyEvent, MouseEvent }
import javafx.event.{EventHandler, ActionEvent}
import scalafx.scene.text.Text
import scalafx.scene.layout.{StackPane,Pane}
import scalafx.scene.shape.{Rectangle}
import scalafx.scene.paint.Color
import scalafx.scene.image.{ImageView, Image}
import javafx.animation.AnimationTimer
import scalafx.application.JFXApp.PrimaryStage
import java.awt.Robot
import Screen._
import Controller._
import scalafx.scene.control._
import scalafx.beans.property._
import scala.util.Random
import World.Messages._
import WorldObject._
import World.Controller._

class ClientMovement(moveType : String, moveNum : Int) extends Movement(Screen.clientPlayer : Player, moveType : String, moveNum : Int) {

  var typeMove = "move"
  var move = "left"
  var previousMapNum = 0
  var previousMapType = "map"
  
  var moveSuccess = false
  
  var previousMovement = "nothing"
  var currentMovement = "nothing"
  
  val chatField = Screen.messageList(scene)._2
  
  def send(moveType : String, move : String){
    val movement = moveType+move
    
    if (movement.equals("movee")){
      Screen.senderActor ! MapChange(false, Screen.name, Screen.clientPlayer.coordsX, Screen.clientPlayer.coordsY, Screen.currentMapNum, Screen.currentMapType, previousMapNum, previousMapType)
    }
    else{
      Screen.senderActor ! MovementMsg(Screen.name, movement, Screen.clientPlayer.coordsX, Screen.clientPlayer.coordsY, Screen.currentMapNum, Screen.currentMapType)
    }
  }
  
  def moveKey(moveType : String, movement : String){
    currentMovement = moveType+movement
    
    if (!Screen.shopOpen){
      if (moveType.equals("move")){
        if (movement.equals("e")){
          previousMapNum = Screen.currentMapNum
          previousMapType = Screen.currentMapType
          
          Screen.name.synchronized{
            Screen.name = Screen.clientName
            pressMove(movement)
            if (moveSuccess){
              send(moveType, movement)
              val chatList = Screen.messageList(Screen.getCurrentScene)._1
              chatList.scrollTo(Screen.chatMessage.size)
            }
          }
        }
        else{
          pressMove(movement)
          if (Random.nextInt(100) < 1 && Screen.currentMapType == "map" && Screen.battleReady){ //1%
            
            if (Screen.clientPlayer.checkAlive){
              releaseMove(movement)
              
              BattleStatics.trainerBattle = 0
              BattleStatics.oppPokimon.value = Pokimon.createPokimon(Screen.getMap(Screen.currentMapType, Screen.currentMapNum).battleVal)
              
              Screen.clientPlayer.previousMap = Screen.currentMapNum
              Screen.clientPlayer.previousMapType = Screen.currentMapType
              
              Screen.currentMapNum = 0
              Screen.currentMapType = "battle"
              println("fight")
                
              Screen.senderActor ! MapChange(false, Screen.clientName, Screen.clientPlayer.coordsX, Screen.clientPlayer.coordsY, 
              Screen.currentMapNum, Screen.currentMapType, Screen.clientPlayer.previousMap, Screen.clientPlayer.previousMapType)
                  
              Screen.game.scene = Screen.getBattle()
            }
          }
          
          if (!currentMovement.equals(previousMovement)){
            send(moveType, movement)
          }
        }
      }
      else if (moveType.equals("stop")){
        releaseMove(movement)
        send(moveType, movement)
      }
      previousMovement = s"$moveType$movement"
    }
      moveSuccess = false
  }
  
    scene.onKeyPressed = (k : KeyEvent) => k.code match{
      case KeyCode.LEFT => moveKey("move","left")
      case KeyCode.RIGHT => moveKey("move","right")
      case KeyCode.UP => moveKey("move","up")
      case KeyCode.DOWN => moveKey("move","down")
      case KeyCode.E => 
        moveKey("move","e")
      case KeyCode.CONTROL => {
        chatField.requestFocus()
      }
      case _ => 
    }
  
    scene.onKeyReleased() = (k : KeyEvent) => k.code match{
      case KeyCode.LEFT => moveKey("stop","left")
      case KeyCode.RIGHT => moveKey("stop","right")
      case KeyCode.UP => moveKey("stop","up")
      case KeyCode.DOWN => moveKey("stop","down")
      case _ => 
    }
    
    scene.onMouseClicked = (k : MouseEvent) => //World.playerName.value = "shit"; println("dogshit")
      for (x <- Screen.nameList){
        val otherPlayer = Screen.characterList(x)
        
        if (otherPlayer != null){
          if (otherPlayer.playerSprite.intersects(k.x,k.y,Characters.width,Characters.height)){
            Screen.oppName.value = otherPlayer.name
            Screen.UIImage.value = otherPlayer.charStandingDown.getImage
          }
        }
      }
}
