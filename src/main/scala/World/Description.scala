package World

import scala.collection.mutable._

object Description {
  private val masterDescription = ListMap[String, String]("potion" -> "Basic Potion", 
      "superpotion" -> "Better Potion than potion",
      "hyperpotion" -> "Better Potion than the better potion than potion ( ͡° ͜ʖ ͡°)",
      "pokiball" -> "Basic ball",
      "greatpokiball" -> "Better ball than Pokiball",
      "ultrapokiball" -> "THE ONLY BALLS YOU EVER NEED ( ͡° ͜ʖ ͡°)")

  def description(key: String) : String = {
    masterDescription(key.toLowerCase)
  }
}