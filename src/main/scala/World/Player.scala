package World

import WorldObject._
import scalafx.scene.image.{ImageView, Image}
import scalafx.beans.property.StringProperty
import scala.collection.mutable.{ArrayBuffer, ListMap}

class Player(val name : String, val charType : Int) extends Serializable{
  
  @transient var charLeftDown : ImageView = null
  @transient var charRightDown : ImageView = null
  @transient var charStandingDown : ImageView = null
  
  @transient var charLeftLeft : ImageView = null
  @transient var charRightLeft : ImageView = null
  @transient var charStandingLeft : ImageView = null
  
  @transient var charLeftRight : ImageView = null
  @transient var charRightRight : ImageView = null
  @transient var charStandingRight : ImageView = null
  
  @transient var charLeftUp : ImageView = null
  @transient var charRightUp : ImageView = null
  @transient var charStandingUp : ImageView = null
  
  @transient var charUp  : Array[ImageView] = null
  @transient var charDown : Array[ImageView] = null
  @transient var charLeft : Array[ImageView] = null
  @transient var charRight : Array[ImageView] = null
  
  @transient var char : Array[Array[ImageView]] = null
  
  var moneyInt = 644
  
  @transient var playerSprite = charStandingDown
  
  var pokiballBuffer = ArrayBuffer("pokiball","greatpokiball","ultrapokiball")
  var pokiPotionBuffer = ArrayBuffer("potion","superpotion","hyperpotion")
  
  var pokiballs = ListMap[String, Int]("pokiball" -> 10, "greatpokiball" -> 0, "ultrapokiball" -> 0)
  var pokiPotion = ListMap[String, Int]("potion" -> 5, "superpotion" -> 0, "hyperpotion" -> 0)
  
  var trainerPoki = ArrayBuffer[Pokimon]()
  var pcPoki = ArrayBuffer[Pokimon]()
  
  var coordsX = 100
  var coordsY = 300
  
  var previousCoordsX = 0
  var previousCoordsY = 0
  
  var mapType : String = "map"
  var mapNum : Integer = 0
  
  var previousMapType = "map"
  var previousMap = 0
  
  pcPoki.append(Pokimon.createPokimon(9), Pokimon.createPokimon(9))
  
  def caughtPoki(caughtPoki: Pokimon): Boolean = {
    if(trainerPoki.length < 6){
      trainerPoki.append(caughtPoki)
      Screen.pokemonBuffer += caughtPoki.pokimon
      true
    }
    else
      false
  }
  
  def freePoki(index : Int){
    trainerPoki.remove(index)
  }
  
  def modifyPokiball(name: String, value: Int){
    if(pokiballs(name) + value <= 99)
      pokiballs(name) = pokiballs(name) + value
    else{
      pokiballs(name) = 99
      //Inform player
    }
  }
  
  def modifyPokiPotion(name: String, value: Int){
    if(pokiPotion(name) + value <= 99)
      pokiPotion(name) = pokiPotion(name) + value
    else{
      pokiPotion(name) = 99
      //Inform player
    }
  }
  
  def setCharacter(){
    charUp = char(0)
    charDown = char(1)
    charLeft = char(2)
    charRight = char(3)
    
    charLeftDown = charDown(0)
    charRightDown = charDown(1)
    charStandingDown = charDown(2)
    
    charLeftLeft = charLeft(0)
    charRightLeft = charLeft(1)
    charStandingLeft = charLeft(2)
    
    charLeftRight = charRight(0)
    charRightRight = charRight(1)
    charStandingRight = charRight(2)
    
    charLeftUp = charUp(0)
    charRightUp = charUp(1)
    charStandingUp = charUp(2)
  }
  
  def setPosition(positionX : Int, positionY : Int){
    for (x <- char){
      for(y <- x){
        y.setX(positionX)
        y.setY(positionY)
      }
    }
    setXY(positionX, positionY)
    charDown(2).visible = true
  }
  
  def setXY(x : Int, y : Int) = synchronized{
    coordsX = x
    coordsY = y
  }
  
  def setDirection(direction : String){
    direction match{
      case "up" => playerSprite = charStandingUp
      case "down" => playerSprite = charStandingDown
      case "left" => playerSprite = charStandingLeft
      case "right" => playerSprite = charStandingRight
    }
  }
  
  def checkAlive : Boolean = {
    var pokemonAlive = false
            
    for (x <- trainerPoki){
      if (x.health > 0){
        pokemonAlive = true
      }
    }
    
    pokemonAlive
  }
}