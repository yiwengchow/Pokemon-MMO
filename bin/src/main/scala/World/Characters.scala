package World

import scalafx.scene.image.{ImageView, Image}

class Characters{
  
  lazy val kanonLeftDown = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonLeftDown.png")))
  lazy val kanonRightDown = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonRightDown.png")))
  lazy val kanonStandingDown = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonStandingDown.png")))
  
  lazy val kanonLeftLeft = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonLeftLeft.png")))
  lazy val kanonRightLeft = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonRightLeft.png")))
  lazy val kanonStandingLeft = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonStandingLeft.png")))
  
  lazy val kanonLeftRight = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonLeftRight.png")))
  lazy val kanonRightRight = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonRightRight.png")))
  lazy val kanonStandingRight = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonStandingRight.png")))
  
  lazy val kanonLeftUp = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonLeftUp.png")))
  lazy val kanonRightUp = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonRightUp.png")))
  lazy val kanonStandingUp = new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Kanon/kanonStandingUp.png")))
  
  lazy val kanonLeft = Array(kanonLeftLeft,kanonRightLeft,kanonStandingLeft)
  lazy val kanonRight = Array(kanonLeftRight,kanonRightRight,kanonStandingRight)
  lazy val kanonDown = Array(kanonLeftDown,kanonRightDown,kanonStandingDown)
  lazy val kanonUp = Array(kanonLeftUp,kanonRightUp,kanonStandingUp)
  
  lazy val kanon = Array(kanonUp,kanonDown,kanonLeft,kanonRight)
  
  def initialize(characters : Array[Array[ImageView]]){
    for (x <- characters){
      for (y <- x){
        y.fitWidth = Characters.width
        y.fitHeight = Characters.height
        y.visible = false
        y.toFront()
      }
    }
  }
  
  def getCharacter(charType : Int) : Array[Array[ImageView]] = {
    
    if (charType == 1){
      return kanon
    }
    else{
      return null
    }
  }
  
  initialize(kanon)
}

object Characters{
    
  val width = 30
  val height = 30
  
}