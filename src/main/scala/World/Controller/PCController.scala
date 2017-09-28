package World.Controller

import scalafx.Includes._
import scalafxml.core.macros.sfxml
import scalafx.scene.control.{ListView, TextArea}
import scalafx.scene.image.{ImageView, Image}
import scalafx.scene.layout.AnchorPane
import scalafx.scene.input.MouseEvent
import scalafx.collections.ObservableBuffer
import World.Screen
import WorldObject._

@sfxml
class PCController(
    pcPokimon : ListView[String],
    trainerPokimon : ListView[String],
    pokimonDetails : TextArea,
    pokimonImage : ImageView,
    exitImage : ImageView,
    anchorPane : AnchorPane){
  
  exitImage.image = new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Overworld/x.png"))
  
  val pcPoki = Screen.clientPlayer.pcPoki
  val trainerPoki = Screen.clientPlayer.trainerPoki
  
  var selected = pcPokimon.getSelectionModel.getSelectedItem
  var selectedType = "pcPokimon" 
  var index = 0
  
  trainerPokimon.items = Screen.pokemonBuffer
  pcPokimon.items = Screen.pcBuffer
  
  def onClicked(e : MouseEvent){
    val listView = e.getSource.asInstanceOf[javafx.scene.control.ListView[String]]
    val name = listView.getId
    
    name match{
      case "pcPokimon" => {
        
        if (pcPokimon.getSelectionModel.getSelectedItem != null){
          selected = pcPokimon.getSelectionModel.getSelectedItem
          selectedType = "pcPokimon"
          index = pcPokimon.getSelectionModel.selectedIndex.value
          pokimonDetails.text = pcPoki(index).toString
          
          pokimonImage.image = Pokimon.getImage(pcPoki(index).imagePath)
          trainerPokimon.getSelectionModel.clearSelection
          pokimonImage.visible = true
        }
      }
      case "trainerPokimon" => {
        
        if (trainerPokimon.getSelectionModel.getSelectedItem != null){
          selected = trainerPokimon.getSelectionModel.getSelectedItem
          selectedType = "trainerPokimon"
          index = trainerPokimon.getSelectionModel.selectedIndex.value
          pokimonDetails.text = trainerPoki(index).toString
          
          pokimonImage.image = Pokimon.getImage(trainerPoki(index).imagePath)
          pcPokimon.getSelectionModel.clearSelection
          pokimonImage.visible = true
        }
      }
      case _ =>
    }
  }
  
  def onImageClicked(){
    if (selectedType.equals("pcPokimon") && trainerPoki.size > 5){
      
    }
    else{
      if (selectedType.equals("pcPokimon") && selected != null){
        trainerPoki.append(pcPoki(index))
        pcPoki.remove(index)
        
        selectedType = "trainerPokimon"
        
      }
      else if (selectedType.equals("trainerPokimon") && selected != null){
        pcPoki.append(trainerPoki(index))
        trainerPoki.remove(index)
        
        selectedType = "pcPokimon"
      }
      
      Screen.pokemonBuffer.clear
      Screen.pcBuffer.clear
      
      for (x <- pcPoki){
        Screen.pcBuffer += x.pokimon
      }
      
      for (x <- trainerPoki){
        Screen.pokemonBuffer += x.pokimon
      }
      
      if (selectedType == "pcPokimon"){
        pcPokimon.getSelectionModel.selectLast
        index = pcPoki.size-1
        trainerPokimon.getSelectionModel.clearSelection
      }
      else{
        trainerPokimon.getSelectionModel.selectLast
        index = trainerPoki.size-1
        pcPokimon.getSelectionModel.clearSelection
      }
      
      Screen.clientPlayer.trainerPoki = trainerPoki
      Screen.clientPlayer.pcPoki = pcPoki
    }
  }
  
  def onExitClicked(){
    Screen.getCurrentScene.content.remove(Screen.pcUI)
    Screen.shopOpen = false
  }
  
}