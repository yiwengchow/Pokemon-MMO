package World

import scalafx.Includes._
import scalafx.scene.input.{ KeyCode, KeyEvent, MouseEvent }
import scala.util.Random
import Messages._
import WorldObject._
import Controller._

class ClientMovement(moveType : String, moveNum : Int) extends Movement(Screen.clientPlayer : Player, moveType : String, moveNum : Int) {

  var typeMove = "move"
  var move = "left"
  var previousMapNum = 0
  var previousMapType = "map"
  
  var moveSuccess = false
  
  var previousMovement = "nothing"
  var currentMovement = "nothing"
  
  var oldCoordX = 0
  var oldCoordY = 0
  
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
    if (!Screen.shopOpen && !Screen.battleUIVisibility.value){
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
          
          var moved = false
          
          if (oldCoordX != Screen.clientPlayer.coordsX || oldCoordY != Screen.clientPlayer.coordsY){
            oldCoordX == Screen.clientPlayer.coordsX
            oldCoordY == Screen.clientPlayer.coordsY
            
            moved = true
          }

          if (Random.nextInt(100) < 1 && Screen.currentMapType == "map" && Screen.battleReady && moved){ //1%
            if (Screen.clientPlayer.checkAlive){
              moveKey("stop",movement)
              
              Screen.trainerBattle = 0
              
              val battleStuff = Screen.getBattle
              Screen.game.scene = battleStuff._1
              Screen.battleController = battleStuff._2
              
              Screen.battleController.setOppPokimon(Pokimon.createPokimon(Screen.getMap(Screen.currentMapType, Screen.currentMapNum).battleVal))
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
  
    scene.onKeyPressed = (k : KeyEvent) => 
      k.code match{
        case KeyCode.LEFT => moveKey("move","left")
        case KeyCode.RIGHT => moveKey("move","right")
        case KeyCode.UP => moveKey("move","up")
        case KeyCode.DOWN => moveKey("move","down")
        case KeyCode.E => 
          moveKey("move","e")
        case KeyCode.CONTROL => {
          chatField.requestFocus()
        }
        case KeyCode.ESCAPE => {
          if (!scene.content.contains(Screen.menuUI)){
            scene.content.add(Screen.menuUI)
            Screen.shopOpen = true
          }
          else{
            scene.content.remove(Screen.menuUI)
            Screen.shopOpen = false
          }
        }
        case _ => 
      }
  
    scene.onKeyReleased() = (k : KeyEvent) => 
      k.code match{
      case KeyCode.LEFT => moveKey("stop","left")
      case KeyCode.RIGHT => moveKey("stop","right")
      case KeyCode.UP => moveKey("stop","up")
      case KeyCode.DOWN => moveKey("stop","down")
      case _ => 
      }
    
    scene.onMouseClicked = (k : MouseEvent) =>
      for (x <- Screen.nameList){
        
        try{
          val otherPlayer = Screen.characterList(x)
          
          if (otherPlayer.playerSprite.intersects(k.x,k.y,Characters.width,Characters.height)){
            Screen.oppName.value = otherPlayer.name
            Screen.UIImage.value = otherPlayer.charStandingDown.getImage
          }
        }
        catch{
          case e : NullPointerException =>
          case a : NoSuchElementException =>
        }
      }
}
