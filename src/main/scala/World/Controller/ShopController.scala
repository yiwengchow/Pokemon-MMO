package World.Controller

import scalafx.scene.input._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.image._
import scalafx.scene.layout._
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import java.util.Date
import java.text.SimpleDateFormat
import World._
import scala.collection.immutable.ListMap

@sfxml
class ShopController(
    private val shopList : ListView[String],
    private val purchaseLog : ListView[String],
    private val itemImage : ImageView,
    private var itemText : TextArea,
    private val buyButton : Button,
    private val exitButton : Button,
    private val shopAnchor : AnchorPane,
    private val amountAnchor : AnchorPane,
    private val amountTextArea : TextArea,
    private val amountText : TextField,
    private val upImage : ImageView,
    private val downImage : ImageView,
    private val purchaseButton : Button,
    private val amountExitButton : Button,
    private val totalText : TextField){
  
  private var counter = 0
  private var shopBuffer = new ObservableBuffer[String]
  private var shopLog = new ObservableBuffer[String]
  private var selected : String = null
  
  val itemPrice = ListMap[String,Int]("potion" -> 10, "superpotion" -> 25, "hyperpotion" -> 50,
      "pokiball" -> 25, "greatpokiball" -> 100, "ultrapokiball" -> 200)
  
  shopBuffer += ("potion","superpotion","hyperpotion","pokiball","greatpokiball","ultrapokiball")
  
  itemImage.image = new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Overworld/shop.png"))
  upImage.image = new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Overworld/up.png"),25,25,false,false)
  downImage.image = new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Overworld/down.png"),25,25,false,false)
  shopList.items = shopBuffer
  purchaseLog.items = shopLog
      
  itemText.text.value = "Welcome to PokeShop!"
  amountAnchor.visible = false
  
  def onListClicked(){
    selected = shopList.getSelectionModel.getSelectedItem
    
    if (selected != null){
      itemText.text.value = s"Description: ${Description.description(selected)}"
    }
  }
  
  def onBuyClicked(){
    if (selected != null){
      counter = 1
      amountText.text.value = counter.toString
      amountTextArea.text.value = s"Item: $selected"
      totalText.text.value = s"${itemPrice(selected) * counter}"
      
      amountAnchor.visible = true
      
      itemText.disable = true
      shopList.disable = true
      buyButton.disable = true
      exitButton.disable = true
    }
    else{
      shopLog += "Select an item first!"
    }
  }
  
  def onExitClicked(){
    shopList.getSelectionModel.clearSelection
    selected = null
    itemText.text.value = "Welcome to PokeShop!"
    
    shopLog.clear
    Screen.getCurrentScene.content.remove(Screen.shopUI)
    Screen.shopOpen = false
  }
  
  def onUpClicked(){
    counter += 1
    amountText.text.value = counter.toString
    totalText.text.value = s"${itemPrice(selected) * counter}"
  }
  
  def onDownClicked(){
    if (counter > 1){
      counter -= 1
    }
    
    amountText.text.value = counter.toString
    totalText.text.value = s"${itemPrice(selected) * counter}"
  }
  
  def onPurchaseClicked(){
    
    var purchaseAmount = 0
    var price = 0
    var purchase = true
    
    try{
      purchaseAmount = Integer.parseInt(amountText.text.value)
      
      price = itemPrice(selected) * purchaseAmount
      var money = Integer.parseInt(Screen.clientPlayer.money.value)
      
      if (price <= money){
        purchase = true
        money = money - price
        Screen.clientPlayer.money.value = s"$money"
      }
      else{
        shopLog += s"Not enough funds!"
        purchaseLog.scrollTo(shopLog.size)
        purchase = false
      }
    }
    catch{
      case e : NumberFormatException => {
        shopLog += s"Only numbers are allowed!"
        purchase = false
      }
    }
    
    if (purchase){
      val date = new Date()
      val dateFormat = new SimpleDateFormat("HH:mm:ss")
      
      shopLog += s"Time: ${dateFormat.format(date)}, Purchased ${amountText.text.value} of ${selected}"
      purchaseLog.scrollTo(shopLog.size)
      
      if (Screen.clientPlayer.pokiPotion.contains(selected)){
        val temp = Screen.clientPlayer.pokiPotion(selected)
        
        Screen.clientPlayer.pokiPotion.remove(selected)
        Screen.clientPlayer.pokiPotion.put(selected,temp + purchaseAmount)
      }
      
      else if (Screen.clientPlayer.pokiballs.contains(selected)){
        val temp = Screen.clientPlayer.pokiballs(selected)
        
        Screen.clientPlayer.pokiballs.remove(selected)
        Screen.clientPlayer.pokiballs.put(selected,temp + purchaseAmount)
      }
      
      Screen.reloadItems()
      
      
      amountAnchor.visible = false
      itemText.disable = false
      shopList.disable = false
      buyButton.disable = false
      exitButton.disable = false
    }
  }
  
  def onAmountExitClicked(){
    amountAnchor.visible = false
    itemText.disable = false
    shopList.disable = false
    buyButton.disable = false
    exitButton.disable = false
  }
}