package World

import scala.util.Random
import scala.collection.mutable._
import WorldObject._
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
            Screen.trainerBattle = 1
            val numberOfPokemon = Random.nextInt(1) + 1
            
            val oppPokimons = ArrayBuffer[Pokimon]()
            for (x <- 0 to numberOfPokemon){
              oppPokimons.append(Pokimon.createPokimon(Screen.getMap(Screen.currentMapType, Screen.currentMapNum).battleVal))
            }
            
            val battleStuff = Screen.getBattle
            Screen.game.scene = battleStuff._1
            Screen.battleController = battleStuff._2
             
            Screen.battleController.setPokimons(oppPokimons) 
          }
        }
      }
      
      
      case "scientistNPC" => {
        var healed = false
        
        for (x <- Screen.clientPlayer.trainerPoki){
          if (x.health != x.maxHealth){
            x.health = x.maxHealth
            healed = true
          }
        }
        
        if (healed){
          Screen.chatMessage += "Healer: Healed your pokemon!"
          Screen.uiDescription.value = ""
        }
        else{
          Screen.chatMessage += "Healer: Your pokemons are at full health!"
        }
        
        val chatField = Screen.messageList(Screen.getCurrentScene)._1
        chatField.scrollTo(Screen.chatMessage.size)
      }
      
      case _ => 
    }
    
    return false

  }
}