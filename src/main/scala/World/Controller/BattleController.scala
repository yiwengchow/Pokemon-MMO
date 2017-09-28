package World.Controller

import scala.util.Random
import scala.util.control._
import scala.collection.mutable._
import scalafx.scene.input._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.image._
import scalafx.scene.layout._
import scalafx.beans.property._
import scalafx.scene.control.cell._
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import javafx.beans.value._
import WorldObject._
import World._
import World.Messages._
import World.OutMessages._
import javafx.application.Platform

@sfxml
class BattleController (
    private val BackButton: Button,
    private val ExitButton: Button,
    private val BallButton: Button,
    private val FightButton: Button,
    private val GreatPokiball: Button,
    private val HyperPotion: Button,
    private val PokemonFive: Button,
    private val PokemonFour: Button,
    private val PokemonOne: Button,
    private val PokemonSix: Button,
    private val PokemonThree: Button,
    private val PokemonTwo: Button,
    private val Pokiball: Button,
    private val PokiButton: Button,
    private val PotButton: Button,
    private val Potion: Button,
    private val RunButton: Button,
    private val SkillFour: Button,
    private val SkillOne: Button,
    private val SkillThree: Button,
    private val SkillTwo: Button,
    private val SuperPotion: Button,
    private val UltraPokiball: Button,
    private val CancelButton: Button,
    private val BattleLog: ListView[String],
    private val BallPane: GridPane,
    private val ControlPane: GridPane,
    private val PokemonPane: GridPane,
    private val PotPane: GridPane,
    private val SkillPane: GridPane,
    private val InfoText: TextArea,
    private val OppTextArr: TextArea,
    private val PlayerTextArr: TextArea,
    private val PlayerImg: ImageView,
    private val OppImg: ImageView) {
  
  Screen.syncCanceller.cancel
  ExitButton.visible = false
  CancelButton.visible = false
  var oppPot = 0
  var oppSupPot = 0
  var oppHypPot = 0
  var oppAvgLevel = 0
  val client = Screen.clientPlayer
  var currentElement = ""
  val pokimon = client.trainerPoki
  val breakableLoop = new Breaks
  BattleStatics.currentPokimon.value_=(pokimonStarterChoser)
  PlayerImg.image.value_=(Pokimon.getImage(BattleStatics.currentPokimon.value.imagePath))
  OppTextArr.text.bind(BattleStatics.oppPokiStats)
  PlayerTextArr.text.bind(BattleStatics.playerPokiInfo)
  val pokiButtonArray = ArrayBuffer[Button](PokemonOne,PokemonTwo,PokemonThree,PokemonFour,PokemonFive,PokemonSix)
  val ballButtonArray = ArrayBuffer[Button](Pokiball,GreatPokiball,UltraPokiball)
  val potionButtonArray = ArrayBuffer[Button](Potion,SuperPotion,HyperPotion)
  val skillButtonArray = ArrayBuffer[Button](SkillOne,SkillTwo,SkillThree,SkillFour)
  val index = ArrayBuffer[Int]()
  var skillOverflow = false
  var skillOverride: Skill = null
  var levelUpBool = false
  
  BattleLog.cellFactory = (chatList) =>
    new TextFieldListCell[String](){
      prefWidth = 452
      wrapText = true
    }
    
  BattleLog.items = BattleStatics.battleLog
  
  val listener = new ChangeListener[String] {
    override def changed(obVal: ObservableValue[_ <: String], oldVal: String, newVal: String) {
      println("ass" + BattleStatics.oppPokimon.value.imagePath)
      OppImg.image = (Pokimon.getImage(BattleStatics.oppPokimon.value.imagePath))
      BattleStatics.playerPokiInfo.value_=(BattleStatics.currentPokimon.value.toStringBattle)
      BattleLog.scrollTo(BattleStatics.battleLog.length)
      if(!newVal.equals("")){
        Platform.runLater(new Thread{
          override def run(){
            Checker
          }
        })
      }
    }
  }
  BattleStatics.oppPokiStats.addListener(listener)
  BattleStatics.placeholder.synchronized{
    BattleStatics.placeholder.notify
  }
  
  val listenerEnd = new ChangeListener[java.lang.Boolean] {
    override def changed(obVal: ObservableValue[_ <: java.lang.Boolean], oldVal: java.lang.Boolean, newVal: java.lang.Boolean) {
      OppTextArr.text.unbind
      PlayerTextArr.text.unbind
      FightButton.disable.unbind
      PokiButton.disable.unbind
      BallButton.disable.unbind
      PotButton.disable.unbind
      RunButton.disable.unbind
      BattleStatics.amountFight = 0
      Screen.oppName.value = ""
      BattleStatics.oppPokimon.value = new Pokimon("a","water",2)
      BattleStatics.oppPokimons.clear
      BattleStatics.battleLog.clear
      removeListener
    }
  }
  BattleStatics.endBattle.addListener(listenerEnd)
  
  def removeListener{
    BattleStatics.oppPokiStats.removeListener(listener)
    BattleStatics.oppPokiStats.value = ""
    BattleStatics.endBattle.removeListener(listenerEnd)
    BattleStatics.endBattle.value = false
    Screen.battleReady = true
  }
  
  val opponentName = Screen.oppName.value
    
  BattleStatics.playerPokiInfo.value = BattleStatics.currentPokimon.value.toStringBattle
  InfoText.text = client.name + " Choose one"
  FightButton.disable.bind(BattleStatics.dead)
  PokiButton.disable.bind(BattleStatics.poki)
  BallButton.disable.bind(BattleStatics.finished)
  PotButton.disable.bind(BattleStatics.finished)
  RunButton.disable.bind(BattleStatics.finished)
  var potting = false
  var potpower = 0
    
  Initialize
  
  if(BattleStatics.trainerBattle == 2)
    Screen.senderActor ! BattleDetails(opponentName, BattleStatics.currentPokimon.value)
  else
    BattleStatics.oppPokiStats.value = BattleStatics.oppPokimon.value.toStringBattle
  
  def pokimonStarterChoser: Pokimon = {
    var chosen: Pokimon = null
    breakableLoop.breakable{
      for(x <- pokimon){
        if(x.health > 0){
          println(x.health)
          chosen = x
          breakableLoop.break
        }
      }
    }
    chosen
  }
  
  def Initialize(){
    PokemonOne.text.value = "-"
    PokemonTwo.text.value = "-"
    PokemonThree.text.value = "-"
    PokemonFour.text.value = "-"
    PokemonFive.text.value = "-"
    PokemonSix.text.value = "-"
    for(x <- 0 to pokimon.size-1){
      pokiButtonArray(x).text = pokimon(x).pokimon
    }
    BackButton.visible = false
    PokemonPane.visible = false
    PotPane.visible = false
    SkillPane.visible = false
    BallPane.visible = false
    BattleStatics.poki.value = false
    Screen.battleReady = false
    if(BattleStatics.trainerBattle == 1){
      BattleStatics.oppPokimon.value = BattleStatics.oppPokimons(0)
      var averageLevel = 0
      var temp = 0
      var tempCount = 0
      for(x <- BattleStatics.oppPokimons){
        temp = temp + x.pokiLevel
        tempCount+=1
      }
      averageLevel = temp/tempCount
      oppAvgLevel = averageLevel
      oppHypPot = averageLevel/7
      averageLevel-=oppPot
      oppSupPot = averageLevel/5
      averageLevel-=oppSupPot
      oppPot = averageLevel
    }
    if(BattleStatics.trainerBattle == 2){
      BattleStatics.finished.value = true
      BallButton.disable.unbind
      PotButton.disable.unbind
      RunButton.disable.unbind
    }
    else if(BattleStatics.trainerBattle == 1){
      BattleStatics.finished.value = true
      RunButton.disable.unbind
      BallButton.disable.unbind
      BattleStatics.finished.value = false
    }
    else
      BattleStatics.finished.value = false
  }
  
  def Checker{
    var index = 0
    for(x <- 0 to pokimon.size-1){
      if(BattleStatics.currentPokimon.value.equals(pokimon(x))){
        index = x
        println("Found you" + pokimon(x).pokimon)
      }
    }
    
    val skill = BattleStatics.currentPokimon.value.skillSet
    for(x <- 0 to skillButtonArray.length-1){
      try{
        skillButtonArray(x).disable = false
        skillButtonArray(x).text = skill(x).name
      }
      catch {
        case e:IndexOutOfBoundsException =>{
          skillButtonArray(x).text = "-"
          skillButtonArray(x).disable = true
        }
      }
    }
    
    try{
      if(BattleStatics.currentPokimon.value.health <= 0|| BattleStatics.oppPokimon.value.health <= 0){
        BattleStatics.finished.value = true
        BattleStatics.dead.value = true
      }
      else{
        BattleStatics.finished.value = false
        BattleStatics.dead.value = false
      }
      if(BattleStatics.oppPokimon.value.health <= 0 && BattleStatics.trainerBattle == 0){
        LevelLoop
      }
    } 
    catch{
      case e: NullPointerException =>
    }
    
    var deathCounter = 0;
    for(x <- 0 to pokiButtonArray.size-1){
      if(!pokiButtonArray(x).text.value.equals("-")){
        if(pokimon(x).health <= 0){
          println(pokimon(x).pokimon+ " is dead")
          pokiButtonArray(x).disable = true
          deathCounter+=1
        }
        else
          pokiButtonArray(x).disable = false
        }
      else{
        pokiButtonArray(x).disable = true
        deathCounter+=1
      }
    }
    
    pokiButtonArray(index).disable = true
    
    if(deathCounter == 6 && !levelUpBool){
      if(BattleStatics.trainerBattle == 2){
        Screen.senderActor ! EndBattle(Screen.clientName, opponentName)
      }
      else{
        client.money.value = (client.money.value.toInt * 0.9).floor.toInt.toString
        println("ONE")
        BattleStatics.amountFight = 0
        LevelLoop
      }
    }
    
    if(BattleStatics.trainerBattle == 1 && !levelUpBool){
      var opponentDeathCounter = 0
      for(x <- BattleStatics.oppPokimons){
        if(x.health <= 0){
          opponentDeathCounter+=1
        }
      }
      if(opponentDeathCounter == BattleStatics.oppPokimons.length){
        client.money.value = (client.money.value.toInt * 1.1).ceil.toInt.toString
        println("TWO")
        LevelLoop
      }
    }
      
    for(x <- 0 to ballButtonArray.size-1){
      if(client.pokiballs(ballButtonArray(x).text.value.toLowerCase) <= 0)
        ballButtonArray(x).disable = true
      else
        ballButtonArray(x).disable = false
    }
    
    for(x <- 0 to potionButtonArray.size-1){
      if(client.pokiPotion(potionButtonArray(x).text.value.toLowerCase) <= 0)
        potionButtonArray(x).disable = true
      else
        potionButtonArray(x).disable = false
    }
  }
  
  
  def OnClick(e: MouseEvent){
    val button = e.getSource.asInstanceOf[javafx.scene.control.Button]
    val name = button.getId
    name match{
      case "FightButton" => {
        Checker
        ControlPane.visible = false
        SkillPane.visible = true
        BackButton.visible = true
      }
      case "RunButton" => {
        if(Random.nextBoolean){
         LevelLoop 
        }
        else{
          BattleAI("pokemon")
        }
      }
      case "BallButton" => {
        Checker
        BallPane.visible = true
        ControlPane.visible = false
        BackButton.visible = true
      }
      case "PotButton" => {
        Checker
        PotPane.visible = true
        ControlPane.visible = false
        BackButton.visible = true
      }
      case "BackButton" => {
        if(potting){
          potting = false
          PotPane.visible = true
          PokemonPane.visible = false
        }
        else{
          ControlPane.visible = true
          PokemonPane.visible = false
          PotPane.visible = false
          SkillPane.visible = false
          BallPane.visible = false
        }
      }
      case "ExitButton" => {
        ChangeScene
      }
      case "CancelButton" => {
        if(skillOverflow){
          LevelLoop
        }
      }
      case "PokiButton" => {
        Checker
        PokemonPane.visible = true
        ControlPane.visible = false
        BackButton.visible = true
      }
      case "PokemonOne" => {
        PokemonButton(0)
      }
      case "PokemonTwo" => {
        PokemonButton(1)
      }
      case "PokemonThree" => {
        PokemonButton(2)
      }
      case "PokemonFour" => {
        PokemonButton(3)
      }
      case "PokemonFive" => {
        PokemonButton(4)
      }
      case "PokemonSix" => {
        PokemonButton(5)
      }
      case "Potion" => {
        PotButton(1, name)
      }
      case "SuperPotion" => {
        PotButton(2, name)
      }
      case "HyperPotion" => {
        PotButton(3, name)
      }
      case "Pokiball" => {
        BallButton(name)
      }
      case "GreatPokiball" => {
        BallButton(name)
      }
      case "UltraPokiball" => {
        BallButton(name)
      }
      case "SkillOne" => {
        SkillButton(0)
      }
      case "SkillTwo" => {
        SkillButton(1)
      }
      case "SkillThree" => {
        SkillButton(2)
      }
      case "SkillFour" => {
        SkillButton(3)
      }
      case _ => 
    }
  }
  
  def BallButton(ballName: String){
    client.modifyPokiball(ballName.toLowerCase, -1)
    var bool = true
    if(Random.nextBoolean){
      bool = client.caughtPoki(BattleStatics.oppPokimon.value)
      if(!bool){
        client.pcPoki.append(BattleStatics.oppPokimon.value)
        Screen.pcBuffer += BattleStatics.oppPokimon.value.pokimon
      }
      LevelLoop
    }
    else{
      BattleAI("pokemon")
    }
    ControlPane.visible = true
    BallPane.visible = false
    BackButton.visible = false
  }
  
  def PotButton(potPower: Int, potName: String){
    potting = true
    potpower = potPower
    client.modifyPokiPotion(potName.toLowerCase, -1)
    PotPane.visible = false
    PokemonPane.visible = true
    for(x <- 0 to pokiButtonArray.size-1){
      if(!pokiButtonArray(x).text.value.equals("-")){
        if(pokimon(x).health <= 0){
          pokiButtonArray(x).disable = true
        }
        else
          pokiButtonArray(x).disable = false
        }
      else{
        pokiButtonArray(x).disable = true
      }
    }
  }
  
  def PokemonButton(pokiIndex: Int){
    if(potting){
      pokimon(pokiIndex).heal(20*potpower)
      potting = false
      BattleAI("pokemon")
    }
    else if(!BattleStatics.dead.value){
      if(BattleStatics.trainerBattle == 2){
        BattleStatics.currentPokimon.value_=(pokimon(pokiIndex))
        PlayerImg.image.value_=(Pokimon.getImage(BattleStatics.currentPokimon.value.imagePath))
        Screen.senderActor ! BattleChoice(Screen.clientName, opponentName, "pokemon", BattleStatics.currentPokimon.value)
        BattleStatics.finished.value = true
        BattleStatics.poki.value = true
        BattleStatics.dead.value = true
      }
      else{
        BattleStatics.currentPokimon.value_=(pokimon(pokiIndex))
        PlayerImg.image.value_=(Pokimon.getImage(BattleStatics.currentPokimon.value.imagePath))
        BattleAI("pokemon")
      }
    }
    else{
      if(BattleStatics.trainerBattle == 2){
        BattleStatics.currentPokimon.value_=(pokimon(pokiIndex))
        PlayerImg.image.value_=(Pokimon.getImage(BattleStatics.currentPokimon.value.imagePath))
        Screen.senderActor ! ChangePokemon(opponentName, BattleStatics.currentPokimon.value)
        BattleStatics.playerPokiInfo.value = BattleStatics.currentPokimon.value.toStringBattle
        BattleStatics.dead.value = false
      }
      else{
        BattleStatics.currentPokimon.value_=(pokimon(pokiIndex))
        PlayerImg.image.value_=(Pokimon.getImage(BattleStatics.currentPokimon.value.imagePath))
        BattleStatics.dead.value = false
        BattleStatics.finished.value = false
        BattleStatics.playerPokiInfo.value = BattleStatics.currentPokimon.value.toStringBattle
      }
    }
    ControlPane.visible = true
    PokemonPane.visible = false
    BackButton.visible = false
  }
  
  def SkillButton(skillIndex: Int){
    ControlPane.visible = true
    SkillPane.visible = false
    if(BattleStatics.trainerBattle == 2){
      Screen.senderActor ! BattleChoice(Screen.clientName, opponentName, s"fight $skillIndex", BattleStatics.currentPokimon.value)
      BattleStatics.finished.value = true
      BattleStatics.poki.value = true
      BattleStatics.dead.value = true
    }
    else if(skillOverflow){
      val skills = BattleStatics.currentPokimon.value.skillSet
      var index = 0
      for(x <- 0 to skills.length-1){
        if(skills(x).name.equals(skillButtonArray(skillIndex).text.value))
          index = x
      }
      BattleStatics.amountFight-=1
      skills.remove(index)
      skills.append(skillOverride)
      BattleStatics.battleLog.append(f"${BattleStatics.currentPokimon.value.pokimon} forgot " +
          f"${skillButtonArray(skillIndex).text.value} and learns ${skillOverride.name}")
      skillOverride = null
      skillOverflow = false
      println(BattleStatics.amountFight)
      LevelLoop
    }
    else if(BattleStatics.trainerBattle == 1 || BattleStatics.trainerBattle == 0){
      BattleAI(s"fight $skillIndex")
    }
    BackButton.visible = false
  }
  
  def LevelUp: Boolean = {
    println("leveled")
    ControlPane.visible = false
    PokemonPane.visible = false
    PotPane.visible = false
    BallPane.visible = false
    SkillPane.visible = true
    var randVal = 0
    println(BattleStatics.amountFight + " " + pokimon.length)
    if(BattleStatics.amountFight >= pokimon.length){
      println("big or equal")
      randVal = Random.nextInt(pokimon.size)
    }
    else{
      breakableLoop.breakable{
        while(true){ 
          randVal = Random.nextInt(pokimon.size)
          if(!index.contains(randVal)){
            breakableLoop.break
          }
        }
      }
    }
    index.append(randVal)
    BattleStatics.currentPokimon.value = pokimon(randVal)
    OppImg.image = null
    val skill = BattleStatics.currentPokimon.value.skillSet
    for(x <- 0 to skillButtonArray.length-1){
      try{
        skillButtonArray(x).disable = false
        skillButtonArray(x).text = skill(x).name
      }
      catch {
        case e:IndexOutOfBoundsException =>{
          skillButtonArray(x).text = "-"
          skillButtonArray(x).disable = true
        }
      }
    }
    val overflowSkill = BattleStatics.currentPokimon.value.levelUp
    PlayerImg.image = Pokimon.getImage(BattleStatics.currentPokimon.value.imagePath)
    BattleStatics.playerPokiInfo.value_=(BattleStatics.currentPokimon.value.toStringBattle)
    println(overflowSkill._2.name + " " + overflowSkill._1)
    if(!overflowSkill._2.name.equals("-") && overflowSkill._1 == 4){
      InfoText.text = s"Skill overflow. Choose one of the skill to be override by ${overflowSkill._2.name} or press cancel to not learn the new skill"
      CancelButton.visible = true
      skillOverride = overflowSkill._2
      true
    }
    else{
      false
    }
  }
  
  def LevelLoop{
    println("scene")
    BattleStatics.oppPokiStats.removeListener(listener)
    BattleStatics.oppPokiStats.value = ""
    var overflow = false
    breakableLoop.breakable{
      println("IM IN")
      while(BattleStatics.amountFight != 0){
        if(BattleStatics.amountFight != 0){
          println("got levels")
          levelUpBool = true
          overflow = LevelUp
          println("IM OUT")
          if(overflow){
            skillOverflow = true
            breakableLoop.break
          }
          BattleStatics.amountFight-=1
        }
      }
    }
    if(BattleStatics.amountFight == 0 && !overflow){
      println("amountFight")
      SkillPane.visible = false
      PokemonPane.visible = false
      BallPane.visible = false
      ControlPane.visible = true
      BattleStatics.dead.value = true
      BattleStatics.poki.value = true
      BattleStatics.finished.value = true
      ExitButton.visible = true
    }
  }
  
  def ChangeScene {
    Screen.currentMapType = Screen.clientPlayer.previousMapType
    Screen.currentMapNum = Screen.clientPlayer.previousMap
    
    Screen.clientPlayer.previousMapType = "battle"
    Screen.clientPlayer.previousMap = 0
    
    Screen.senderActor ! MapChange(false, Screen.clientName, Screen.clientPlayer.coordsX, Screen.clientPlayer.coordsY, 
        Screen.currentMapNum, Screen.currentMapType, Screen.clientPlayer.previousMap, Screen.clientPlayer.previousMapType)
    
    Platform.runLater(new Thread{
      override def run(){
        Screen.game.scene = Screen.getScene(Screen.currentMapType, Screen.currentMapNum)
        Screen.oppName.value_=("")
        Screen.scheduleSync
      }
    })
    BattleStatics.endBattle.value = true
  }
  
  def EnteredMouse(e: MouseEvent){
    val button = e.getSource.asInstanceOf[javafx.scene.control.Button]
    val name = button.getId
    name match{
      case "FightButton" => {
        InfoText.text = "FIGHTO"
      }
      case "RunButton" => {
        InfoText.text = "CHICKEN!"
      }
      case "BallButton" => {
        InfoText.text = "BALLLLSSS!!"
      }
      case "PotButton" => {
        InfoText.text = "POTS ARE FOR COWARDS!!!"
      }
      case "PokemonOne" => {
        InfoText.text = pokimon(0).toString
      }
      case "PokemonTwo" => {
        InfoText.text = pokimon(1).toString
      }
      case "PokemonThree" => {
        InfoText.text = pokimon(2).toString
      }
      case "PokemonFour" => {
        InfoText.text = pokimon(3).toString
      }
      case "PokemonFive" => {
        InfoText.text = pokimon(4).toString
      }
      case "PokemonSix" => {
        InfoText.text = pokimon(5).toString
      }
      case "Potion" => {
        InfoText.text = s"${Description.description(name)}\nCount: ${client.pokiPotion(name.toLowerCase)}"
      }
      case "SuperPotion" => {
        InfoText.text = s"${Description.description(name)}\nCount: ${client.pokiPotion(name.toLowerCase)}"
      }
      case "HyperPotion" => {
        InfoText.text = s"${Description.description(name)}\nCount: ${client.pokiPotion(name.toLowerCase)}"
      }
      case "Pokiball" => {
        InfoText.text = s"${Description.description(name)}\nCount: ${client.pokiballs(name.toLowerCase)}"
      }
      case "GreatPokiball" => {
        InfoText.text = s"${Description.description(name)}\nCount: ${client.pokiballs(name.toLowerCase)}"
      }
      case "UltraPokiball" => {
        InfoText.text = s"${Description.description(name)}\nCount: ${client.pokiballs(name.toLowerCase)}"
      }
      case "SkillOne" => {
        InfoText.text = BattleStatics.currentPokimon.value.skillSet(0).description
      }
      case "SkillTwo" => {
        InfoText.text = BattleStatics.currentPokimon.value.skillSet(1).description
      }
      case "SkillThree" => {
        InfoText.text = BattleStatics.currentPokimon.value.skillSet(2).description
      }
      case "SkillFour" => {
        InfoText.text = BattleStatics.currentPokimon.value.skillSet(3).description
      }
      case _ => 
    }
  }
  
  def BattleAI(playerChoice: String){
    println("AI")
    if(BattleStatics.trainerBattle == 0){//WILD POKIMON AI
      processBattle(BattleStatics.currentPokimon.value, BattleStatics.oppPokimon.value,
          playerChoice, s"fight ${Random.nextInt(BattleStatics.oppPokimon.value.skillSet.length)}")
    }
    else{//PLAYER AI
      val poki = BattleStatics.oppPokimon.value
      var potted = false
      if(poki.health < poki.health*0.30 && poki.pokiLevel >= oppAvgLevel){
        if(poki.maxHealth - poki.health < 20){
          if(oppPot != 0){
            oppPot-=1
            poki.heal(100*1)
            potted = true
          }
        }
        else if(poki.maxHealth - poki.health < 40){
          if(oppSupPot != 0){
            oppSupPot-=1
            poki.heal(100*2)
            potted = true
          }
        }
        else{
          if(oppHypPot != 0){
            oppHypPot-=1
            poki.heal(100*3)
            potted = true
          }
        }
      }
      var result = ("","")
      if(!potted){
        result = processBattle(BattleStatics.currentPokimon.value, BattleStatics.oppPokimon.value,
          playerChoice, s"fight ${Random.nextInt(BattleStatics.oppPokimon.value.skillSet.length)}")
      }
      else{
        result = processBattle(BattleStatics.currentPokimon.value, BattleStatics.oppPokimon.value,
          playerChoice, s"pokemon")
      }
      if(result._2.equals("AI")){
        var chosen = false
        for(x <- BattleStatics.oppPokimons){
          if(x.health > 0){
            if(Element.getMultiplier(Element.getIntFromElem(x.element), Element.getIntFromElem(BattleStatics.currentPokimon.value.element)) >= 1.9){
              BattleStatics.oppPokimon.value = x
              println("I FOUND YOUR COUNTER!")
            }
            else{
              if(!chosen){
                println("chosen")
                BattleStatics.oppPokimon.value = x
                chosen = true
              }
            }
          }
        }
      }
    }
    println(playerChoice)
    BattleStatics.oppPokiStats.value = ""
    BattleStatics.oppPokiStats.value = BattleStatics.oppPokimon.value.toStringBattle
  }
  
  def errorProneStatement(dmgTakePk: Pokimon, dmgDealPk: Pokimon, dmgDealSkill: Int){
    try{
      val damage = dmgTakePk.takeDmg(dmgDealPk.doDmg(dmgTakePk.element, dmgDealSkill))
      BattleStatics.battleLog.append(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
      BattleStatics.battleLog.append(f"${dmgTakePk.pokimon} suffers $damage%.2f")
    }
    catch{
      case e: NoSuchElementException =>{
        val nullskill = dmgDealPk.skillSet(dmgDealSkill)
        if(nullskill.power < 0 && nullskill.description.contains("defense")){
          dmgTakePk.debuffSkill(true, nullskill.power)
          BattleStatics.battleLog.append(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
          BattleStatics.battleLog.append(f"${dmgTakePk.pokimon} defense is reduced by ${nullskill.power}")
        }
        else if(nullskill.power > 0 && nullskill.description.contains("defense")){
          dmgDealPk.buffSkill(true, nullskill.power)
          BattleStatics.battleLog.append(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
          BattleStatics.battleLog.append(f"${dmgDealPk.pokimon} defense is increased by ${nullskill.power}")
        }
        else if(nullskill.power < 0 && nullskill.description.contains("attack")){
          dmgTakePk.debuffSkill(false, nullskill.power)
          BattleStatics.battleLog.append(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
          BattleStatics.battleLog.append(f"${dmgTakePk.pokimon} attack is reduced by ${nullskill.power}")
        }
        else if(nullskill.power > 0 && nullskill.description.contains("attack")){
          dmgDealPk.buffSkill(false, nullskill.power)
          BattleStatics.battleLog.append(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
          BattleStatics.battleLog.append(f"${dmgDealPk.pokimon} attack is increased by ${nullskill.power}")
        }
      }
      case e:Exception =>{
        e.printStackTrace
      }
    }
  }
  
  def processBattle(player: Pokimon, AI: Pokimon, playerChoice: String, AIChoice: String): (String,String) ={
    if(playerChoice.equals("pokemon")^AIChoice.equals("pokemon")){
      if(playerChoice.equals("pokemon")&&AIChoice.contains("fight")){
        errorProneStatement(player, AI, AIChoice.split(" ")(1).toInt)
        if(player.health <= 0){
          BattleStatics.battleLog.append(f"${Screen.clientName}'s ${player.pokimon} has fainted")
          ("dead","player")
        }
        else{
          ("alive","")
        }
      }
      else{
        errorProneStatement(AI, player, playerChoice.split(" ")(1).toInt)
        if(AI.health <= 0){
          BattleStatics.battleLog.append(f"AI's ${AI.pokimon} has fainted")
          BattleStatics.amountFight+=1
          ("dead","AI")
        }
        else{
          ("alive","")
        }
      }
    }
    else{
      if(playerChoice.contains("fight")&&AIChoice.contains("fight")){
        if(player.speed > AI.speed){
          errorProneStatement(AI, player, playerChoice.split(" ")(1).toInt)
          if(AI.health <= 0){
            BattleStatics.battleLog.append(f"AI's ${AI.pokimon} has fainted")
            BattleStatics.amountFight+=1
            ("dead","AI")
          }
          else{
            errorProneStatement(player, AI, AIChoice.split(" ")(1).toInt)
            if(player.health <= 0){
              BattleStatics.battleLog.append(f"${Screen.clientName}'s ${player.pokimon} has fainted")
              ("dead","player")
            }
            else{
              ("alive","")
            }
          }
        }
        else if(player.speed < AI.speed){
          errorProneStatement(player, AI, AIChoice.split(" ")(1).toInt)
          if(player.health <= 0){
            BattleStatics.battleLog.append(f"${Screen.clientName}'s ${player.pokimon} has fainted")
            ("dead","player")
          }
          else{
            errorProneStatement(AI, player, playerChoice.split(" ")(1).toInt)
            if(AI.health <= 0){
              BattleStatics.battleLog.append(f"AI's ${AI.pokimon} has fainted")
              BattleStatics.amountFight+=1
              ("dead","AI")
            }
            else{
              ("alive","")
            }
          }
        }
        else{
          if(Random.nextBoolean){
            errorProneStatement(AI, player, playerChoice.split(" ")(1).toInt)
            if(AI.health <= 0){
              BattleStatics.battleLog.append(f"AI's ${AI.pokimon} has fainted")
              BattleStatics.amountFight+=1
              ("dead","AI")
            }
            else{
              errorProneStatement(player, AI, AIChoice.split(" ")(1).toInt)
              if(player.health <= 0){
                BattleStatics.battleLog.append(f"${Screen.clientName}'s ${player.pokimon} has fainted")
                ("dead","player")
              }
              else{
                ("alive","")
              }
            }
          }
          else{
            errorProneStatement(player, AI, AIChoice.split(" ")(1).toInt)
            if(player.health <= 0){
              BattleStatics.battleLog.append(f"${Screen.clientName}'s ${player.pokimon} has fainted")
              ("dead","player")
            }
            else{
              errorProneStatement(AI, player, playerChoice.split(" ")(1).toInt)
              if(AI.health <= 0){
                BattleStatics.battleLog.append(f"AI's ${AI.pokimon} has fainted")
                BattleStatics.amountFight+=1
                ("dead","AI")
              }
              else{
                ("alive","")
              }
            }
          }
        }
      }
      else{
        ("alive","")
      }
    }
  }
  
  def ExitMouse(e: MouseEvent){
    if(!skillOverflow){
      InfoText.text = client.name + " Choose one"
    }
    else{
      InfoText.text = "Choose one of the skill to be overwritten"
    }
  }
}

object BattleStatics {
  val placeholder = new AnyRef
  val endBattle = BooleanProperty(false)
  var trainerBattle = 0  //0 for wild, 1 for gym, 2 for player
  val finished = BooleanProperty(true)
  val poki = BooleanProperty(true)
  val dead = BooleanProperty(false)
  var opponentName = ""
  val oppPokimons = ArrayBuffer[Pokimon]()
  val oppPokimon = new ObjectProperty[Pokimon]
  oppPokimon.value = new Pokimon("a","water",2)
  val oppPokiStats = StringProperty("")
  val playerPokiInfo = new StringProperty("")
  val currentPokimon = new ObjectProperty[Pokimon]
  val battleLog = new scalafx.collections.ObservableBuffer[String]()
  var amountFight = 0
}