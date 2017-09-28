package World

import javafx.animation.AnimationTimer
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListMap

object Animation {
  var movement = ArrayBuffer[Movement]()
  
  def addMovement(Movement : Movement){
    movement +:= Movement
  }
  
  var animationTimer = new AnimationTimer(){
      override def handle(now : Long) {
        for (x <- movement){
          if (x.goUp && x.moveable(x.player.coordsX,x.player.coordsY-3))  {
              x.currentKanon = x.charUp
              x.move("up")
          }
          
          if (x.goDown && x.moveable(x.player.coordsX,x.player.coordsY+3))  {
              x.currentKanon = x.charDown
              x.move("down")
          }
          
          if (x.goLeft && x.moveable(x.player.coordsX-3,x.player.coordsY))  {
              x.currentKanon = x.charLeft
              x.move("left")
          }
          
          if (x.goRight && x.moveable(x.player.coordsX+3,x.player.coordsY))  {
              x.currentKanon = x.charRight
              x.move("right")
          }
        }
        Thread.sleep(15)
    }
  }  
  animationTimer.start()
}