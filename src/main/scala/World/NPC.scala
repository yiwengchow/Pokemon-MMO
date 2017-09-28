package World

import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}
import scala.util.Random
import scala.collection.mutable._
import WorldObject._
import World.Controller._
import World.Messages._

/**
 * @author 12079059
 */
class NPC (coordsX : Int, coordsY : Int, name : String) extends OverworldImmovable(coordsX,coordsY,name) with Interactable{
  
  override def interact() : Boolean = {
    name match{
      case "desertNPC" => {
        Screen.getCurrentScene.content.add(Screen.shopUI)
        Screen.shopOpen = true
      }
      
      case "beachNPC" => {
        Screen.getCurrentScene.content.add(Screen.pcUI)
        Screen.shopOpen = true
      }
      
      case "coderNPC" => {
        if (Screen.battleReady){
          
          if (Screen.clientPlayer.checkAlive){
            BattleStatics.trainerBattle = 1
            val numberOfPokemon = Random.nextInt(1) + 1
            
            for (x <- 0 to numberOfPokemon){
              BattleStatics.oppPokimons.append(Pokimon.createPokimon(Screen.getMap(Screen.currentMapType, Screen.currentMapNum).battleVal))
            }
            
            Screen.clientPlayer.previousMap = Screen.currentMapNum
            Screen.clientPlayer.previousMapType = Screen.currentMapType
            
            Screen.currentMapNum = 0
            Screen.currentMapType = "battle"
            
             Screen.senderActor ! MapChange(false, Screen.clientName, Screen.clientPlayer.coordsX, Screen.clientPlayer.coordsY, 
              Screen.currentMapNum, Screen.currentMapType, Screen.clientPlayer.previousMap, Screen.clientPlayer.previousMapType)
              
            Screen.game.scene = Screen.getBattle()
          }
        }
      }
    }
    
    return false
  }
}