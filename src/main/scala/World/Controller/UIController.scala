package World.Controller

import scalafx.scene.input._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.image._
import scalafx.scene.layout._
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import World._
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.ObjectProperty
import World.Messages._
import scalafx.scene.control.cell._

@sfxml
class UIController(
    private val chatText: TextField,
    private val moneyField : TextField,
    private val sendButton: Button,
    private val chatList: ListView[String],
    private val battleButton: Button,
    private val playerImage: ImageView,
    private val tab : TabPane,
    private val pokemonList : ListView[String],
    private val itemList : ListView[String],
    private val tabImage : ImageView,
    private val tabDetails : TextArea,
    private val oppText : Label,
    private val borderPane : BorderPane){
  
  val scene = Screen.getScene(Screen.currentMapType, Screen.currentMapNum)
  
  Screen.messageList.put(scene,(chatList,chatText))
  pokemonList.items = Screen.pokemonBuffer
  itemList.items = Screen.itemBuffer
  tabDetails.text.bind(Screen.uiDescription)
  oppText.text.bind(Screen.oppName)
  moneyField.text.bind(Screen.clientPlayer.money)
  
  chatList.cellFactory = (chatList) =>
    new TextFieldListCell[String](){
      prefWidth = 190
      wrapText = true
    }
    
  chatList.items = Screen.chatMessage
  
  val imageProperty : ObjectProperty[javafx.scene.image.Image] = tabImage.image
  imageProperty.bind(Screen.UIImage)
  
  def onChatKeyPressed(e : KeyEvent){
    e.code match{
      case KeyCode.ENTER => 
        if (chatText.focused()){
          if (!chatText.text.value.contains("-") && !chatText.text.value.isEmpty){
            Screen.senderActor ! Messages.Message(Screen.name, Screen.currentMapNum, Screen.currentMapType, chatText.text.value)
            Screen.chatMessage += s"${Screen.clientName}: ${chatText.text.value}"
            chatList.scrollTo(Screen.chatMessage.size)
          }
          chatText.text = ""
          borderPane.requestFocus
        }
      case _ =>
    }
  }
    
  def onChatListPressed(){
    borderPane.requestFocus
  }
  
  def onChatMousePressed(){
    borderPane.requestFocus
  }
  
  def onTabPressed(){
    borderPane.requestFocus
  }
  
  def onPokemonListClick(){
    val selectedPokemonIndex = pokemonList.getSelectionModel.getSelectedIndex
    val trainerPokemons = Screen.clientPlayer.trainerPoki 
    
    if (pokemonList.getSelectionModel.getSelectedItem != null){
      Screen.uiDescription.value = trainerPokemons(selectedPokemonIndex).toString()
    }
    
    pokemonList.getSelectionModel.clearSelection
    
  }
  
  def onItemListClick(){
    val selectedItemName = itemList.getSelectionModel.getSelectedItem
    
    val trainerPokiballs = Screen.clientPlayer.pokiballBuffer
    val trainerPokiPotions = Screen.clientPlayer.pokiPotionBuffer
    
    itemList.getSelectionModel.clearSelection
    
    if (selectedItemName != null){
      if (trainerPokiballs.contains(selectedItemName)){
        tabDetails.text = String.format("Item Name: %s\nItem Amount: %s\nDescription: %s\n",
        selectedItemName, Screen.clientPlayer.pokiballs(selectedItemName).toString, Description.description(selectedItemName))
      }
      else if (trainerPokiPotions.contains(selectedItemName)){
        tabDetails.text = String.format("Item Name: %s\nItem Amount: %s\nDescription: %s\n",
        selectedItemName, Screen.clientPlayer.pokiPotion(selectedItemName).toString, Description.description(selectedItemName))
      }
    }
  }
  
  def onTabDetailsClicked(){
    borderPane.requestFocus
  }
  
  def onMoneyPressed(){
    borderPane.requestFocus
  }
  
    def onClick(e: MouseEvent){
    val button = new Button(e.source.asInstanceOf[javafx.scene.control.Button])
    val name = button.id.value
   
    name match{
      case "sendButton" => chatText.text = "your mom"
      case "battleButton" => {
        if (Screen.clientPlayer.checkAlive){
          Screen.senderActor ! BattleReq(oppText.text.value, Screen.clientName, Screen.currentMapNum, Screen.currentMapType)
        }
      }
      case _ => chatText.text = "meh"
    }
  }
  
  
}

