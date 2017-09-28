package WorldObject

import scala.collection.mutable._
import scala.util.control.Breaks._

final class Skill(data: Array[String]) extends Serializable{
  private val _name = data(0)
  private val _level = Integer.valueOf(data(1))
  private val _power = Integer.valueOf(data(2))
  private val _description = data(3)
  private val _element = data(4)
  
  def name = _name
  def level = _level
  def description = _description
  def power = _power
  def element = _element
  
  override def equals(o: Any): Boolean = {
    val obj = o.asInstanceOf[Skill]
    var correctness = 0
    if(name.equals(obj.name)) correctness+=1
    if(level == obj.level) correctness+=1
    if(power == obj.power) correctness+=1
    if(description.equals(obj.description)) correctness+=1
    if(element.equals(obj.element)) correctness+=1
    if(correctness == 5) true else false
  }
}

object Skill {
  val ElementLevelPower = Array.ofDim[String](22,5)
  
  ElementLevelPower(0)(0) = "ember"
  ElementLevelPower(0)(1) = "1"
  ElementLevelPower(0)(2) = "40"
  ElementLevelPower(0)(3) = "Need to start a fire? No worries!"
  ElementLevelPower(0)(4) = "fire"
  ElementLevelPower(1)(0) = "flamethrower"
  ElementLevelPower(1)(1) = "25"
  ElementLevelPower(1)(2) = "80"
  ElementLevelPower(1)(3) = "A freakin' flamethrower! Now that's what I'm talking about!"
  ElementLevelPower(1)(4) = "fire"
  ElementLevelPower(2)(0) = "sun flare"
  ElementLevelPower(2)(1) = "50"
  ElementLevelPower(2)(2) = "120"
  ElementLevelPower(2)(3) = "Yeah, summon the power of the sun, sure why not."
  ElementLevelPower(2)(4) = "fire"
  
  ElementLevelPower(3)(0) = "water gun"
  ElementLevelPower(3)(1) = "1"
  ElementLevelPower(3)(2) = "40"
  ElementLevelPower(3)(3) = "A pitiful squirt of water, but it gets the job done."
  ElementLevelPower(3)(4) = "water"
  ElementLevelPower(4)(0) = "surf"
  ElementLevelPower(4)(1) = "25"
  ElementLevelPower(4)(2) = "80"
  ElementLevelPower(4)(3) = "Surf's up, dude."
  ElementLevelPower(4)(4) = "water"
  ElementLevelPower(5)(0) = "tsunami"
  ElementLevelPower(5)(1) = "50"
  ElementLevelPower(5)(2) = "120"
  ElementLevelPower(5)(3) = "Okay, I think it's time to seek shelter now."
  ElementLevelPower(5)(4) = "water"
  
  ElementLevelPower(6)(0) = "grass whip"
  ElementLevelPower(6)(1) = "1"
  ElementLevelPower(6)(2) = "40"
  ElementLevelPower(6)(3) = "Kinky."
  ElementLevelPower(6)(4) = "grass"
  ElementLevelPower(7)(0) = "leaf blade"
  ElementLevelPower(7)(1) = "25"
  ElementLevelPower(7)(2) = "80"
  ElementLevelPower(7)(3) = "A sword that is also a leaf. Very sharp."
  ElementLevelPower(7)(4) = "grass"
  ElementLevelPower(8)(0) = "flower storm"
  ElementLevelPower(8)(1) = "50"
  ElementLevelPower(8)(2) = "120"
  ElementLevelPower(8)(3) = "Think flowers are just for wimps? Think again!"
  ElementLevelPower(8)(4) = "grass"
  
  ElementLevelPower(9)(0) = "rock throw"
  ElementLevelPower(9)(1) = "1"
  ElementLevelPower(9)(2) = "40"
  ElementLevelPower(9)(3) = "A stone's throwaway."
  ElementLevelPower(9)(4) = "rock"
  ElementLevelPower(10)(0) = "stone edge"
  ElementLevelPower(10)(1) = "25"
  ElementLevelPower(10)(2) = "80"
  ElementLevelPower(10)(3) = "Launch a pointy rock at your opponents."
  ElementLevelPower(10)(4) = "rock"
  ElementLevelPower(11)(0) = "earthquake"
  ElementLevelPower(11)(1) = "50"
  ElementLevelPower(11)(2) = "120"
  ElementLevelPower(11)(3) = "Off the Richter scale!"
  ElementLevelPower(11)(4) = "rock"
  
  ElementLevelPower(12)(0) = "gust"
  ElementLevelPower(12)(1) = "1"
  ElementLevelPower(12)(2) = "40"
  ElementLevelPower(12)(3) = "A gust that will blow your socks off."
  ElementLevelPower(12)(4) = "air"
  ElementLevelPower(13)(0) = "air slash"
  ElementLevelPower(13)(1) = "25"
  ElementLevelPower(13)(2) = "80"
  ElementLevelPower(13)(3) = "A clean slice."
  ElementLevelPower(13)(4) = "air"
  ElementLevelPower(14)(0) = "hurricane"
  ElementLevelPower(14)(1) = "50"
  ElementLevelPower(14)(2) = "120"
  ElementLevelPower(14)(3) = "Remember to bring your umbrella!"
  ElementLevelPower(14)(4) = "air"
  
  ElementLevelPower(15)(0) = "spark"
  ElementLevelPower(15)(1) = "1"
  ElementLevelPower(15)(2) = "40"
  ElementLevelPower(15)(3) = "Just a spark of electricity."
  ElementLevelPower(15)(4) = "electric"
  ElementLevelPower(16)(0) = "thunderbolt"
  ElementLevelPower(16)(1) = "25"
  ElementLevelPower(16)(2) = "80"
  ElementLevelPower(16)(3) = "Attack your foes with a lightning bolt summoned from above."
  ElementLevelPower(16)(4) = "electric"
  ElementLevelPower(17)(0) = "thunderstorm"
  ElementLevelPower(17)(1) = "50"
  ElementLevelPower(17)(2) = "120"
  ElementLevelPower(17)(3) = "Does it seem concerning that these creatures can alter the weather?"
  ElementLevelPower(17)(4) = "electric"
 
  ElementLevelPower(18)(0) = "roar"
  ElementLevelPower(18)(1) = "12"
  ElementLevelPower(18)(2) = "-2"
  ElementLevelPower(18)(3) = "A roar that scares the opponent, reducing their defense."
  ElementLevelPower(18)(4) = "null"
  ElementLevelPower(19)(0) = "charm"
  ElementLevelPower(19)(1) = "24"
  ElementLevelPower(19)(2) = "-1"
  ElementLevelPower(19)(3) = "The user charms the opponent, lowering their attack"
  ElementLevelPower(19)(4) = "null"
  ElementLevelPower(20)(0) = "defense curl"
  ElementLevelPower(20)(1) = "48"
  ElementLevelPower(20)(2) = "1"
  ElementLevelPower(20)(3) = "The user curls up into a ball, raising their defense"
  ElementLevelPower(20)(4) = "null"
  ElementLevelPower(21)(0) = "work out"
  ElementLevelPower(21)(1) = "30"
  ElementLevelPower(21)(2) = "1"
  ElementLevelPower(21)(3) = "A work out that will increase the user's attack"
  ElementLevelPower(21)(4) = "null"
  
  def SkillData(level: String, element: String): Skill ={
    var resultingArray = Array[String]()
    var something = false
    for(x <- 0 to ElementLevelPower.length-1){
      if((ElementLevelPower(x)(1).equals(level) && ElementLevelPower(x)(4).equals(element)) || (ElementLevelPower(x)(1).equals(level) && ElementLevelPower(x)(4).equals("null"))){
        resultingArray = ElementLevelPower(x)
          something = true
      }
    }
    if(something) new Skill(resultingArray) else new Skill(Array[String]("-","0","0","None","None"))
  }
  
  def SkillDataInit(level: String, element: String): ArrayBuffer[Skill] ={
    var resultingArray = ArrayBuffer[Skill]()
    var something = false
    for(x <- 0 to ElementLevelPower.length-1){
      if((ElementLevelPower(x)(1).toInt <= level.toInt && ElementLevelPower(x)(4).equals(element)) || (ElementLevelPower(x)(1).toInt <= level.toInt && ElementLevelPower(x)(4).equals("null"))){
        resultingArray.append(new Skill(ElementLevelPower(x)))
      }
    }
    print(resultingArray.length + " ")
    if(resultingArray.length > 4){
      print("remove ")
      resultingArray.remove(0, resultingArray.length-4)
    }
    println(resultingArray.length)
    resultingArray
  }
}