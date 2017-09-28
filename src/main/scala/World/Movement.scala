package World

import scalafx.Includes._
import scalafx.scene.{Scene}
import javafx.event.{EventHandler, ActionEvent}
import scalafx.scene.text.Text
import scalafx.scene.layout.{StackPane,Pane}
import scalafx.scene.shape.{Rectangle}
import scalafx.scene.paint.Color
import scalafx.scene.image.{ImageView, Image}
import javafx.animation.AnimationTimer
import scalafx.application.JFXApp.PrimaryStage
import java.awt.Robot

class Movement(val player : Player, mapType : String, mapNum : Int) {
  
  val maxX = Screen.sceneX-25
  val maxY = Screen.sceneY-25
  
  val map = Screen.getMap(mapType,mapNum)
  val scene = Screen.getScene(mapType,mapNum)
  
  val stuff = map.stuff
  
  val charDown = player.charDown
  val charUp = player.charUp
  val charLeft = player.charLeft
  val charRight = player.charRight
  
  var goUp = false
  var goDown = false
  var goLeft = false
  var goRight = false     
  
  var movingUp = false
  var movingDown = false
  var movingLeft = false
  var movingRight = false
  
  var direction = "down"
  var sprite = 2
  
  var oldSprite = charDown(sprite)
  var currentKanon = charDown
  
  var movementCounter = 0
  
  def move(moveType : String){
    if (oldSprite != currentKanon(sprite)){
      oldSprite.visible = false
      movementCounter = 0
    }
    
    if (movementCounter >= 20){
      movementCounter = 0
    }
    
    if (movementCounter == 0){
      currentKanon(sprite).visible = false
      sprite = 0
      currentKanon(sprite).visible = true
    }
    
    else if (movementCounter == 5){
      currentKanon(sprite).visible = false
      sprite = 2
      currentKanon(sprite).visible = true
    }
    
    else if (movementCounter == 10){
      currentKanon(sprite).visible = false
      sprite = 1
      currentKanon(sprite).visible = true
    }
    
    else if (movementCounter == 15){
      currentKanon(sprite).visible = false
      sprite = 2
      currentKanon(sprite).visible = true
    }
    
    moveType match{
      case "up" => player.coordsY -= 3
      case "down" => player.coordsY += 3
      case "left" => player.coordsX -= 3
      case "right" => player.coordsX += 3
    }
    
    direction = moveType
    
    player.setDirection(direction)
    
    currentKanon(sprite).setX(player.coordsX)
    currentKanon(sprite).setY(player.coordsY)
    
    movementCounter+=1
    
    oldSprite = currentKanon(sprite)
  }
  
  def checkMoving() : Boolean = {
    if (movingUp){
      return false
    }
    else if (movingDown){
      return false
    }
    else if (movingLeft){
      return false
    }
    else if (movingRight){
      return false
    }
    else{
      return true
    }
  }
  
  def pressMove(move : String){
    if (move.equals("left")){
      goLeft = true; movingLeft = true
      goRight = false; movingRight = false
      goUp = false; movingUp = false
      goDown = false; movingDown = false
    }
    else if (move.equals("right")){
      goRight = true; movingRight = true
      goLeft = false; movingLeft = false
      goUp = false; movingUp = false
      goDown = false; movingDown = false
    }
    else if (move.equals("up")){
      goUp = true; movingUp = true
      goRight = false; movingRight = false
      goLeft = false; movingLeft = false
      goDown = false; movingDown = false
    }
    else if (move.equals("down")){
      goDown = true; movingDown = true
      goRight = false; movingRight = false
      goUp = false; movingUp = false
      goLeft = false; movingLeft = false
    }
    else if (move.equals("e")){
      direction match{
        case "up" =>
          if (checkMoving){
            interact(player.coordsX,player.coordsY-3)
          }
        case "down" =>
          if (checkMoving){
            interact(player.coordsX,player.coordsY+3)
          }
        case "left" =>
          if (checkMoving){
            interact(player.coordsX-3,player.coordsY)
          }
        case "right" =>
          if (checkMoving){
            interact(player.coordsX+3,player.coordsY)
          }
      }
    }
    
  }
  
  def releaseMove(move : String){
    if (move.equals("left")){
       if (goLeft){
        goLeft = false 
        movingLeft = false
        oldSprite.visible = false
        oldSprite = charLeft(2)
        direction = "left"
        release()
      }
    }
    else if (move.equals("right")){
      if (goRight){
        goRight = false 
        movingRight = false
        oldSprite.visible = false 
        oldSprite = charRight(2)
        direction = "right"
        release()
      }
    }
    else if (move.equals("up")){
      if (goUp){
        goUp = false
        movingUp = false
        oldSprite.visible = false
        oldSprite = charUp(2)
        direction = "up"
        release()
      }
    }
    else if (move.equals("down")){
      if (goDown){
        goDown = false
        movingDown = false
        oldSprite.visible = false
        oldSprite = charDown(2)
        direction = "down"
        release()
      }
    }
    player.setDirection(direction)
  }
  
  def interact(coordsX : Double, coordsY : Double){
    
    this.asInstanceOf[ClientMovement].moveSuccess = false
    
    for (x <- stuff){
      if (x.isInstanceOf[Interactable]){
        if (x.resultImage.intersects(coordsX,coordsY+20,Characters.width,Characters.height-20)){
          if (x.asInstanceOf[Interactable].interact){
            oldSprite = charDown(2)
            this.asInstanceOf[ClientMovement].moveSuccess = true
          }
        }
      }
    }
  }
  
  def release(){
    oldSprite.setX(player.coordsX)
    oldSprite.setY(player.coordsY)
    oldSprite.visible = true
  }
    
  def moveable(positionX : Double, positionY : Double) : Boolean = {
    var move = true
    
    if (positionX >= 0 && positionX <= maxX && positionY >= 0 && positionY <= maxY){
      for (x <- stuff){
        if (x.isInstanceOf[OverworldImmovable]){
          if (x.asInstanceOf[OverworldImmovable].resultImage.intersects(positionX+10, positionY+20, Characters.width-20, Characters.height-20)){
            move = false
          }
        }
      } 
      return move
    }
    else{
      return false
    }
  }
  
  Animation.addMovement(this)
}
