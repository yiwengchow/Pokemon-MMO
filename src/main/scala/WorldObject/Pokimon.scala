package WorldObject

import scala.util.Random
import scala.util.control.Breaks
import scalafx.scene.image.Image

class Pokimon (pokiName: String, elem : String, level: Int) extends Serializable{
  
  var pokiLevel = level
  private var baseDmg = (Random.nextInt(3) + 3) + (10)*level
  private var baseDef = (Random.nextInt(3) + 3) + (10)*level
  var _battleDefense = baseDef
  var _battleDamage = baseDmg
  var _health = (40 * Random.nextDouble() + 80) + (10)*level
  var maxHealth = _health
  val pokimon = pokiName
  val element = elem
  var speed = (Random.nextInt(4)+1)*level
  val imagePath = Pokimon.getImagePath(pokimon, element)
  
  def battleDamage_=(value: Int){
    if(value > _battleDamage)
      _battleDamage = baseDmg
    else if(value < 0)
      _battleDamage = 0
    else
      _battleDamage = value
  }
  
  def battleDamage : Int = _battleDamage
  def battleDefense : Int = _battleDefense
  
  def battleDefense_=(value: Int){
    if(value > _battleDefense)
      _battleDefense = baseDef
    else if(value < 0)
      _battleDefense = 0
    else
      _battleDefense = value
  }
  
  def health_= (value:Double) : Unit ={
    if(value <= 0)
      _health = 0
    else if(value >= maxHealth)
      _health = maxHealth
    else
      _health = value
  }
  
  def health = _health
  
  val skillSet = Skill.SkillDataInit(level.toString, element)
  
  def doDmg(oppElem: String, skill: Int) : Double = {
      (1+ baseDmg) * skillSet(skill).power * 
      Element.getMultiplier(Element.getIntFromElem(skillSet(skill).element), Element.getIntFromElem(oppElem))
  }
  
  def heal(value: Double){
    health = health + value
  }
  
  def takeDmg(dmg: Double): Double = {
    val damage = dmg/(baseDef/2)
    health_=(health - (damage))
    damage
  }
  
  def buffSkill(buffDef : Boolean, value: Int){
    if(buffDef)
      battleDefense+=value
    else
      battleDamage+=value
  }
  
  def debuffSkill(debuffDef : Boolean, value: Int){
    if(debuffDef)
      battleDefense+=value
    else
      battleDamage+=value
  }
  
  def levelUp : (Int,Skill) = {
    var fullSkillCounter = 0
    var skill = Skill.SkillData(pokiLevel.toString(),elem)
    var message = ""
    if(pokiLevel != 50){
      pokiLevel+=1
      baseDmg += 10
      baseDef += 10
      maxHealth += 200
      health = maxHealth
      if(!skill.name.equals("-")){
        if(skillSet.length == 4){
          fullSkillCounter=4
        }
        else{
          skillSet.append(skill)
        }
      }
    }
    (fullSkillCounter, skill)
  }
  
  override def equals(o: Any): Boolean = {
    val obj = o.asInstanceOf[Pokimon]
    var correctness = 0;
    if(maxHealth == obj.maxHealth) correctness+=1
    if(pokiLevel == obj.pokiLevel) correctness+=1
    if(pokimon.equals(obj.pokimon)) correctness+=1
    if(element.equals(obj.element)) correctness+=1
    if(speed == obj.speed) correctness+=1
    if(correctness == 5) true else false
  }
  
  def copy(poki: Pokimon){
    health = poki.health
    battleDamage = poki.battleDamage
    battleDefense = poki.battleDefense
  }
  
  def reset{
    battleDamage = baseDmg
    battleDefense = baseDef
  }
  
  def toStringBattle : String ={
    f"${pokimon.toUpperCase}\nElement: $element\nLv: $pokiLevel\nHealth: $health%.3f\nDef: $battleDefense\nAtk: $battleDamage"
  }
  
  override def toString : String = {
    f"Name: $pokimon\nLevel: $pokiLevel\nHealth: $health%.0f\nElemenet: $element\nDef: $baseDef\nAtk: $baseDmg"
  }
}

object Pokimon{
  val pukimanDirectory = "World/Pukimans"
  val pukiman = Array.ofDim[String](10,2)
  
  pukiman(0)(0) = "dragonite"
  pukiman(0)(1) = "air"
  pukiman(1)(0) = "rayquaza"
  pukiman(1)(1) = "air"
  pukiman(2)(0) = "arceus"
  pukiman(2)(1) = "electric"
  pukiman(3)(0) = "charizard"
  pukiman(3)(1) = "fire"
  pukiman(4)(0) = "meganium"
  pukiman(4)(1) = "grass"
  pukiman(5)(0) = "shaymin"
  pukiman(5)(1) = "grass"
  pukiman(6)(0) = "giratina-origin"
  pukiman(6)(1) = "rock"
  pukiman(7)(0) = "tyranitar-mega"
  pukiman(7)(1) = "rock"
  pukiman(8)(0) = "empoleon"
  pukiman(8)(1) = "water"
  pukiman(9)(0) = "gardevoir"
  pukiman(9)(1) = "water"
  
  def createPokimon(mapValue: Int): Pokimon = {
    val level = (Random.nextInt(5)+1)*(mapValue+1)
    val randomed = Random.nextInt(10)
    val pokimon = pukiman(randomed)(0)
    val element = pukiman(randomed)(1)
    new Pokimon(pokimon,element,level)
  }
  
  def getImagePath(pokimon: String, element: String): String = {
    s"$pukimanDirectory/$element/${pokimon.toLowerCase}.gif"
  }
  
  def getImage(path: String): Image = {
    new Image(getClass.getClassLoader.getResourceAsStream(path))
  }
  
//  def getImage(pokimon: String): Image = {
//    var element : String = null
//    
//    for (x <- 0 to pukiman.size-1){
//      if (pokimon.equals(pukiman(x)(0))){
//        element = pukiman(x)(1)
//      }
//    }
//    
//    new Image(getClass.getClassLoader.getResourceAsStream(s"$pukimanDirectory/$element/$pokimon.gif"))
//  }
}