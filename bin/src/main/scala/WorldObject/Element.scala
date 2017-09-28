package WorldObject

import scala.collection.immutable.ListMap

object Element extends Serializable{
  val relationship =  Array.ofDim[Double](6,6)
  val elemRep = ListMap[String, Int]("fire" -> 0, "water" -> 1, "grass" -> 2, "rock" -> 3, "air" -> 4, "electric" -> 5)
  
  relationship(0)(0) = 0.5
  relationship(0)(1) = 2
  relationship(0)(2) = 0.5
  relationship(0)(3) = 1
  relationship(0)(4) = 0.5
  relationship(0)(5) = 1
  relationship(1)(0) = 0.5
  relationship(1)(1) = 0.5
  relationship(1)(2) = 2
  relationship(1)(3) = 1
  relationship(1)(4) = 1
  relationship(1)(5) = 2
  relationship(2)(0) = 2
  relationship(2)(1) = 0.5
  relationship(2)(2) = 0.5
  relationship(2)(3) = 0.5
  relationship(2)(4) = 2
  relationship(2)(5) = 0.5
  relationship(3)(0) = 0.5
  relationship(3)(1) = 2
  relationship(3)(2) = 2
  relationship(3)(3) = 0.5
  relationship(3)(4) = 0.5
  relationship(3)(5) = 1
  relationship(4)(0) = 1
  relationship(4)(1) = 1
  relationship(4)(2) = 0.5
  relationship(4)(3) = 2
  relationship(4)(4) = 1
  relationship(4)(5) = 2
  relationship(5)(0) = 2
  relationship(5)(1) = 0.5
  relationship(5)(2) = 1
  relationship(5)(3) = 2
  relationship(5)(4) = 1
  relationship(5)(5) = 0.5
  
  def getIntFromElem (elem: String) : Int = {
    elemRep(elem)
  }
  
  def getMultiplier (atk: Int, defe: Int): Double = {
    relationship(defe)(atk)
  }
}
