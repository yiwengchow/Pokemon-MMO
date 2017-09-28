package World

import scalafx.application.JFXApp
import scalafxml.core.{NoDependencyResolver, FXMLLoader}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.{Scene, Group}
import scalafx.scene.media.{MediaPlayer, Media}
import scalafx.Includes._
import scala.collection.mutable.{ListMap, ArrayBuffer}
import scalafx.beans.property.{StringProperty, ObjectProperty, BooleanProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ListView, TextField}
import akka.actor.{Actor, ActorSystem, ActorRef, Cancellable, Props}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import Messages._
import Controller.BattleController

object Screen extends JFXApp{
  
  var clientName : String = null
  var name : String = null
  var clientProperty = new StringProperty("")
  
  var mapArray = Array[MapGenerator]()
  var houseArray = Array[MapGenerator]()
  
  var mapScene = Array[Scene]()
  var houseScene = Array[Scene]()
  
  var movementList = ListMap[Player, Movement]()
  var characterList = ListMap[String, Player]()
  
  var messageList = ListMap[Scene, (ListView[String],TextField)]()
  
  var money = new StringProperty("644")
  var nameClick = new StringProperty("No Person")
  var oppName = new StringProperty("No Person")
  var uiDescription = new StringProperty("")
  
  var UIImage = new ObjectProperty[javafx.scene.image.Image]
  
  var pcBuffer = new ObservableBuffer[String]
  var pokemonBuffer = new ObservableBuffer[String]
  var itemBuffer = new ObservableBuffer[String]
  
  var readyActor = false
  
  var battleUIVisibility = BooleanProperty(false)
  battleUIVisibility.value = false
  
  var nameList = ArrayBuffer[String]()
  
  val sceneX = 925
  val sceneY = 500
  
  var chatMessage = new ObservableBuffer[String]
  
  var gameBoard = new Scene (sceneX,sceneY)
  
  @volatile var currentMapNum = 0
  @volatile var currentMapType = "map"
  
  var movement : Movement = null
  var visibility = BooleanProperty(false)
  
  var clientPlayer : Player = null
  
  var clientMovement : ClientMovement = null
  
  var pcUI : javafx.scene.layout.AnchorPane = null
  var shopUI : javafx.scene.layout.AnchorPane = null
  var menuUI : javafx.scene.layout.AnchorPane = null
  
  var shopOpen = false
  var battleReady = true
  
  val system = ActorSystem(s"Client")
  var senderActor : ActorRef = null
  val bgmPlayer = system.actorOf(Props[BGM], "BGMPlayer")
  val synchronizer = system.actorOf(Props[SynchronizeData], "Synchronizer")
  var syncCanceller : Cancellable = null
  var battleController: BattleController#Controller = null
  var trainerBattle: Int = 0
  val placeholder = new AnyRef
  var ipAddress = "127.0.0.1"
  
  class SynchronizeData extends Actor{
    override def receive = {
      case "START" => 
        senderActor ! Synchronizer(clientPlayer)
    }
  }
  
  class BGM extends Actor{
    val media = new MediaPlayer(new Media(getClass.getClassLoader.getResource("World/Items/bgm/bgm.mp3").toString))
    media.cycleCount = MediaPlayer.Indefinite
    override def receive = {
      case "START" =>
        media.play
      case "STOP" =>
        media.stop
    }
  }
  
  def scheduleSync{
    syncCanceller = system.scheduler.schedule(Duration.Zero, Duration(1, MINUTES), synchronizer, "START")
  }
  
  var game = new PrimaryStage{
    resizable = false
    scene = new Scene{
      root = getLoginUI
    }
  }
  
  game.show()
  
  override def stopApp{
    system.terminate
  }
  
  def initializeMap(){
    
    mapScene = Array[Scene]()
    houseScene = Array[Scene]()
    
    mapArray :+= new MapGenerator(0,0,"map",1)
    mapArray :+= new MapGenerator(0,1,"map",2)
    mapArray :+= new MapGenerator(2,2,"map",3)
    
    houseArray :+= new MapGenerator(1,0,"house",0) 
    houseArray :+= new MapGenerator(1,1,"house",0) 
    houseArray :+= new MapGenerator(1,0,"house",0) 
    houseArray :+= new MapGenerator(1,1,"house",0) 
    houseArray :+= new MapGenerator(1,1,"house",0) 
    houseArray :+= new MapGenerator(1,0,"house",0) 
    houseArray :+= new MapGenerator(1,1,"house",0) 
    houseArray :+= new MapGenerator(1,1,"house",0) 
    
    for (x <- 0 to mapArray.length-1){
       mapScene :+= new Scene
    }
    
    for (x <- 0 to houseArray.length-1){
       houseScene :+= new Scene
    }
    
    initializeScene("map",mapScene,mapArray)
    initializeScene("house",houseScene,houseArray)
  }
  
  def initializeScene(sceneType : String, sceneArray : Array[Scene], map : Array[MapGenerator]) {
    for (x <- 0 to sceneArray.length-1){
      sceneArray(x).root = new Group{
        
        currentMapNum = x
        currentMapType = sceneType
        
        for (y <- map(x).stuff){
          children.add(y.asInstanceOf[Stuff].resultImage)
        }
        
        for (y <- map(x).stuff){
          if (y.isInstanceOf[Houses]){
            y.asInstanceOf[OverworldImmovable].resultImage.toFront()
          }
        }
        children.add(getInterface)
        children.add(getBattleUI)
      }
    }
  }
  
  def initializePlayer(){
    
    clientPlayer.char = new Characters().getCharacter(clientPlayer.charType)
    clientPlayer.setCharacter
    
    addACharacter(getCurrentScene,clientPlayer,clientPlayer.coordsX,clientPlayer.coordsY)
    
    characterList.put(clientName, clientPlayer)
    
    clientMovement = new ClientMovement(currentMapType,currentMapNum)
    
    movementList.put(characterList.getOrElse(clientName, null), clientMovement)
    
    for (x <- clientPlayer.trainerPoki){
      pokemonBuffer += x.pokimon
    }
    
    for (x <- clientPlayer.pcPoki){
      pcBuffer += x.pokimon
    }
    
  }
  
  def getLoginUI : javafx.scene.layout.AnchorPane ={
    val resource = new FXMLLoader(getClass.getClassLoader.getResource("World/View/Login.fxml"),NoDependencyResolver)
    resource.load
    val UIroot = resource.getRoot[javafx.scene.layout.AnchorPane]
    UIroot
  }
  
  def getPCUI : javafx.scene.layout.AnchorPane ={
    val resource = new FXMLLoader(getClass.getClassLoader.getResource("World/View/PCUI.fxml"),NoDependencyResolver)
    resource.load
    val UIroot = resource.getRoot[javafx.scene.layout.AnchorPane]
    UIroot.translateY = sceneY/10
    UIroot.translateX = sceneX/5
    UIroot
  }
  
  def getShopUI : javafx.scene.layout.AnchorPane = {
    val resource = new FXMLLoader(getClass.getClassLoader.getResource("World/View/ShopUI.fxml"),NoDependencyResolver)
    resource.load
    val UIroot = resource.getRoot[javafx.scene.layout.AnchorPane]
    UIroot.translateY = sceneY/10
    UIroot.translateX = sceneX/5
    UIroot
  }
  
  def getBattleUI: javafx.scene.layout.AnchorPane = {
    val resource = new FXMLLoader(getClass.getClassLoader.getResource("World/View/BattleUI.fxml"),NoDependencyResolver)
    resource.load
    val UIroot = resource.getRoot[javafx.scene.layout.AnchorPane]
    UIroot.translateY = sceneY/2
    UIroot.translateX = sceneX/2
    UIroot.visible.bind(battleUIVisibility)
    UIroot
  }
  
  def getInterface: javafx.scene.layout.BorderPane = {
    val Ui = new FXMLLoader(getClass.getClassLoader.getResource("World/View/UI.fxml"),NoDependencyResolver)
    Ui.load()
    val UIroot = Ui.getRoot[javafx.scene.layout.BorderPane]
    UIroot.translateY = sceneY
    return UIroot
  }
  
  def getMenuUI : javafx.scene.layout.AnchorPane = {
    val Ui = new FXMLLoader(getClass.getClassLoader.getResource("World/View/MenuUI.fxml"),NoDependencyResolver)
    Ui.load()
    val UIroot = Ui.getRoot[javafx.scene.layout.AnchorPane]
    UIroot.translateY = sceneY/2
    UIroot.translateX = sceneX/2
    return UIroot 
  }
  
  def getBattle() : (Scene, BattleController#Controller) = {
    val battle = new FXMLLoader(getClass.getClassLoader.getResource("World/View/BattleScene.fxml"),NoDependencyResolver)
    battle.load()
    val controller = battle.getController[BattleController#Controller]
    
    val battleRoot = new Scene{
      root = battle.getRoot[javafx.scene.layout.AnchorPane]
    }
    
    (battleRoot,controller)
  }
  
  def getCurrentScene : Scene = {
    if (currentMapType.equalsIgnoreCase("house")){
      return houseScene(currentMapNum)
    }
    else{
      return mapScene(currentMapNum)
    }
  }
  
  def getScene(mapType : String, map : Int) : Scene = {
    if (mapType.equalsIgnoreCase("house")){
      return houseScene(map)
    }
    else{
      return mapScene(map)
    }
  }
  
  def getMap(mapType : String, map : Int) : MapGenerator = {
    if (mapType.equalsIgnoreCase("house")){
      return houseArray(map)
    }
    else{
      return mapArray(map)
    }
  }
  
  def deleteACharacter(scene : Scene, player : Player, movement : Movement) {
    try{
      this.synchronized{
        nameList.remove(nameList.indexOf(player.name))
        characterList.remove(player.name)
        movementList.remove(player)
        Animation.movement.remove(Animation.movement.indexOf(movement))
      }
    }
    catch{
      case e : ArrayIndexOutOfBoundsException => 
    }
    
    for (x <- 0 to 2){
      scene.content.remove(player.charDown(x))
      scene.content.remove(player.charUp(x))
      scene.content.remove(player.charLeft(x))
      scene.content.remove(player.charRight(x))
    }
  }
  
  def deleteAllCharacters(scene : Scene) = synchronized {
    for (x <- nameList){
      val character = characterList.getOrElse(x,null)
      for (y <- 0 to 2){
        scene.content.remove(character.charDown(y))
        scene.content.remove(character.charUp(y))
        scene.content.remove(character.charLeft(y))
        scene.content.remove(character.charRight(y))
      }
    }
    
    movementList.clear()
    nameList.clear()
    characterList.clear()
  }
  
   def addACharacter(scene : Scene, player : Player, coordsX : Int, coordsY : Int){
     
    for (x <- 0 to 2){
      player.charDown(x).visible = false
      player.charUp(x).visible = false
      player.charLeft(x).visible = false
      player.charRight(x).visible = false
    }
    
    for (x <- 0 to 2){
      try{
      scene.content.add(player.charDown(x))
      scene.content.add(player.charUp(x))
      scene.content.add(player.charLeft(x))
      scene.content.add(player.charRight(x))
      }
      catch{
        case e : IllegalArgumentException => {}
      }
    }
    
    player.setPosition(coordsX, coordsY)
  }
   
  def reloadItems(){
    
    itemBuffer.clear()
    
    for (x <- clientPlayer.pokiballBuffer){
      if (clientPlayer.pokiballs(x) > 0){
        itemBuffer += x
      }
    }
    
    for (x <- clientPlayer.pokiPotionBuffer){
      if (clientPlayer.pokiPotion(x) > 0){
        itemBuffer += x
      }
    }
  }
  
  def reinitialize(){
    getCurrentScene.content.remove(menuUI)
    deleteAllCharacters(getCurrentScene)
    deleteACharacter(getCurrentScene, clientPlayer, clientMovement)
    
    game.scene = new Scene{
      root = getLoginUI
    }
    
    nameList.clear
    shopOpen = false
    readyActor = false
    characterList.clear
    movementList.clear
    pokemonBuffer.clear
    pcBuffer.clear
    chatMessage.clear
  }
}

  