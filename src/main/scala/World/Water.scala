package World

import scalafx.scene.{Scene,Group}
import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}

class Water(x : Double, y : Double) extends Immovable {
    
  val water = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/water.png"))){
    fitWidth = 50
    fitHeight = 50
  }
  
  water.setX(x)
  water.setY(y)
}