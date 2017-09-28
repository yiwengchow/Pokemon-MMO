package World

import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}

class Stuff {
  var resultImage : ImageView = null
  
  def setXY(x : Int, y : Int){
    resultImage.setX(x)
    resultImage.setY(y)
  }
  
  def getImageView(path : String, width : Int, height : Int) : ImageView = {
    return new ImageView(new Image(getClass.getClassLoader.getResourceAsStream(path))){
      fitWidth = width
      fitHeight = height
    }
  }
}