package World.Controller

import scala.util.Random
import scala.util.control.Breaks
import scala.collection.mutable.{ArrayBuffer}
import scalafx.scene.input.{MouseEvent}
import scalafx.scene.control.{Button, ListView, TextArea}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.GridPane
import scalafx.beans.property.{IntegerProperty, BooleanProperty, ObjectProperty, StringProperty}
import scalafx.scene.control.cell.TextFieldListCell
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import javafx.beans.value.{ChangeListener, ObservableValue}
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
  val mapNum = Screen.currentMapNum
  val mapType = Screen.currentMapType
  val breakableLoop = new Breaks
  ExitButton.visible = false
  CancelButton.visible = false
  val client = Screen.clientPlayer
  val pokimon = client.trainerPoki
  var trainerBattle = Screen.trainerBattle  //0 for wild, 1 for gym, 2 for player
  val finished = BooleanProperty(true)
  val poki = BooleanProperty(true)
  val dead = BooleanProperty(false)
  var opponentName = Screen.oppName.value
  val oppPokimons = ArrayBuffer[Pokimon]()
  var oppPokimon = new Pokimon("a","water",2)
  var currentPokimon = pokimonStarterChoser
  val battleLog = new scalafx.collections.ObservableBuffer[String]()
  var amountFight = 0
  
  if(trainerBattle == 2)
    Screen.senderActor ! BattleDetails(opponentName, currentPokimon, mapNum, mapType)
    
  val countdown = IntegerProperty(60)
  var oppPot = 0
  var oppSupPot = 0
  var oppHypPot = 0
  var oppAvgLevel = 0
  PlayerImg.image.value_=(Pokimon.getImage(currentPokimon.imagePath))
  val pokiButtonArray = ArrayBuffer[Button](PokemonOne,PokemonTwo,PokemonThree,PokemonFour,PokemonFive,PokemonSix)
  val ballButtonArray = ArrayBuffer[Button](Pokiball,GreatPokiball,UltraPokiball)
  val potionButtonArray = ArrayBuffer[Button](Potion,SuperPotion,HyperPotion)
  val skillButtonArray = ArrayBuffer[Button](SkillOne,SkillTwo,SkillThree,SkillFour)
  val index = ArrayBuffer[Int]()
  var skillOverflow = false
  var skillOverride: Skill = null
  var levelUpBool = false
  
  PlayerTextArr.text = currentPokimon.toStringBattle
  InfoText.text = client.name + " please choose one"
  FightButton.disable.bind(dead)
  PokiButton.disable.bind(poki)
  BallButton.disable.bind(finished)
  PotButton.disable.bind(finished)
  RunButton.disable.bind(finished)
  var potting = false
  var potpower = 0
    
  Initialize
  
  
  BattleLog.cellFactory = (chatList) =>
    new TextFieldListCell[String](){
      prefWidth = 254
      wrapText = true
    }
    
  BattleLog.items = battleLog
  
  val listenerCountdown = new ChangeListener[java.lang.Number] {
    override def changed(obVal: ObservableValue[_ <: java.lang.Number], oldVal: java.lang.Number, newVal: java.lang.Number) {
      if(newVal.intValue%10 == 0 && newVal.intValue != 60){
        Platform.runLater(new Runnable{
          def run{
            if(finished.value){
              logResult(s"The opponent have ${newVal.intValue} seconds left to choose an option")
              BattleLog.scrollTo(battleLog.size)
            }
            else{
              logResult(s"You have ${newVal.intValue} seconds left to choose an option")
              BattleLog.scrollTo(battleLog.size)
            }
          }
        })
      }
      if(newVal == 0){
        Screen.senderActor ! EndBattle(client.name, opponentName, mapNum, mapType)
        LevelLoop
      }
    }
  }
  
  if(trainerBattle == 2){
    countdown.addListener(listenerCountdown)
    
    new Thread(new Runnable{
      override def run {
        while(countdown.value != 0){
          Thread.sleep(1000)
          countdown.value = countdown.value - 1
        }
      }
    }).start
  }
  
  def debuffOrBuffSkill(debuff: Boolean, defense: Boolean, amount: Int){
    if(debuff)
      currentPokimon.debuffSkill(defense, amount)
    else
      currentPokimon.buffSkill(defense, amount)
  }
  
  def setPokimons(pokis: ArrayBuffer[Pokimon]){
    oppPokimons.appendAll(pokis)
    if(trainerBattle == 1){
      setOppPokimon(oppPokimons(0))
      var averageLevel = 0
      var temp = 0
      var tempCount = 0
      for(x <- oppPokimons){
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
  }
  
  def setEndBattlePlayer(loser: String){
    if(loser.equals(client.name)){
      PlayerTextArr.text = "Player has lost".toLowerCase
    }
    else{
      PlayerTextArr.text = "Player has won!!".toUpperCase
    }
    LevelLoop
  }
  
  def logResult (result: String){
    battleLog.append(result)
  }
  
  def reEnableButtons {
    finished.value = false
    poki.value = false
    dead.value = false
  }
  
  def disableButtons {
    finished.value = true
    poki.value = true
    dead.value = true
  }
  
  def updateStats{
    PlayerTextArr.text.value = (currentPokimon.toStringBattle)
    OppTextArr.text.value = oppPokimon.toStringBattle
    Checker
  }
  
  def setPlayerPokimon(poki: Pokimon){
    currentPokimon.copy(poki)
    BattleLog.scrollTo(battleLog.length)
    updateStats
    Checker
  }
  
  def setOppPokimon(poki: Pokimon){
    oppPokimon = poki
    countdown.value = 60
    OppImg.image = (Pokimon.getImage(oppPokimon.imagePath))
    BattleLog.scrollTo(battleLog.length)
    updateStats
    Checker
  }
    
  
  
  def pokimonStarterChoser: Pokimon = {
    var chosen: Pokimon = null
    breakableLoop.breakable{
      for(x <- pokimon){
        if(x.health > 0){
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
    poki.value = false
    Screen.battleReady = false
    
    if(trainerBattle == 2){
      finished.value = true
      BallButton.disable.unbind
      PotButton.disable.unbind
      RunButton.disable.unbind
    }
    else if(trainerBattle == 1){
      finished.value = true
      RunButton.disable.unbind
      BallButton.disable.unbind
      finished.value = false
    }
    else
      finished.value = false
  }
  
  def Checker{
    var index = 0
    for(x <- 0 to pokimon.size-1){
      if(currentPokimon.equals(pokimon(x))){
        index = x
      }
    }
    
    val skill = currentPokimon.skillSet
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
      if(currentPokimon.health <= 0|| oppPokimon.health <= 0){
        if(currentPokimon.health <= 0 && trainerBattle == 2){
          finished.value = false
        }
        else{
          finished.value = true
        }
        dead.value = true
      }
      else{
        finished.value = false
        dead.value = false
      }
      if(oppPokimon.health <= 0 && trainerBattle == 0){
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
          BattleLog.scrollTo(battleLog.size)
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
      if(trainerBattle == 2){
        Screen.senderActor ! EndBattle(client.name, opponentName, mapNum, mapType)
      }
      else{
        Screen.money.value = (Screen.money.value.toInt * 0.9).floor.toInt.toString
        if(trainerBattle != 2){
          logResult("Player has lost")
          BattleLog.scrollTo(battleLog.size)
        }
        amountFight = 0
        LevelLoop
      }
    }
    
    if(trainerBattle == 1 && !levelUpBool){
      var opponentDeathCounter = 0
      for(x <- oppPokimons){
        if(x.health <= 0){
          opponentDeathCounter+=1
        }
      }
      if(opponentDeathCounter == oppPokimons.length){
        Screen.money.value = (Screen.money.value.toInt * 1.1).ceil.toInt.toString
        logResult("AI has lost")
        BattleLog.scrollTo(battleLog.size)
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
          BackButton.visible = false
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
    var caught = false
    if (ballName.toLowerCase == "pokiball"){ // 20%
      if (Random.nextInt(100) < 20){
        caught = true
      }
    }
    else if (ballName.toLowerCase == "greatpokiball"){ //35%
      if (Random.nextInt(100) < 35){
        caught = true
      }
    }
    else if (ballName.toLowerCase == "ultrapokiball"){ //50%
      if (Random.nextInt(100) < 50){
        caught = true
      }
    }
    
    if(caught){
      bool = client.caughtPoki(oppPokimon)
      logResult(s"${client.name} has caught ${oppPokimon.pokimon}")
      BattleLog.scrollTo(battleLog.size)
      OppTextArr.text.value = "CAUGHT!"
      if(!bool){
        logResult(s"${oppPokimon.pokimon} has been sent to the PC as the " + 
            s"player's pokemon inventory is full")
        client.pcPoki.append(oppPokimon)
        Screen.pcBuffer += oppPokimon.pokimon
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
      logResult(s"${client.name} heals ${pokimon(pokiIndex).health} for ${20*potpower}")
      potting = false
      BattleAI("pokemon")
    }
    else if(!dead.value){
      if(trainerBattle == 2){
        currentPokimon_=(pokimon(pokiIndex))
        PlayerImg.image.value_=(Pokimon.getImage(currentPokimon.imagePath))
        Screen.senderActor ! BattleChoice(client.name, opponentName, "pokemon", currentPokimon, mapNum, mapType)
        finished.value = true
        poki.value = true
        dead.value = true
      }
      else{
        currentPokimon_=(pokimon(pokiIndex))
        PlayerImg.image.value_=(Pokimon.getImage(currentPokimon.imagePath))
        BattleAI("pokemon")
      }
    }
    else{
      if(trainerBattle == 2){
        currentPokimon_=(pokimon(pokiIndex))
        PlayerImg.image.value_=(Pokimon.getImage(currentPokimon.imagePath))
        Screen.senderActor ! ChangePokemon(opponentName, currentPokimon, mapNum, mapType)
        PlayerTextArr.text = currentPokimon.toStringBattle
        dead.value = false
      }
      else{
        currentPokimon_=(pokimon(pokiIndex))
        PlayerImg.image.value_=(Pokimon.getImage(currentPokimon.imagePath))
        dead.value = false
        finished.value = false
        PlayerTextArr.text = currentPokimon.toStringBattle
      }
    }
    ControlPane.visible = true
    PokemonPane.visible = false
    BackButton.visible = false
  }
  
  def SkillButton(skillIndex: Int){
    ControlPane.visible = true
    SkillPane.visible = false
    if(trainerBattle == 2){
      Screen.senderActor ! BattleChoice(client.name, opponentName, s"fight $skillIndex", currentPokimon, mapNum, mapType)
      finished.value = true
      poki.value = true
      dead.value = true
    }
    else if(skillOverflow){
      val skills = currentPokimon.skillSet
      var index = 0
      for(x <- 0 to skills.length-1){
        if(skills(x).name.equals(skillButtonArray(skillIndex).text.value))
          index = x
      }
      amountFight-=1
      skills.remove(index)
      skills.append(skillOverride)
      logResult(f"${currentPokimon.pokimon} forgot " +
          f"${skillButtonArray(skillIndex).text.value} and learns ${skillOverride.name}")
      BattleLog.scrollTo(battleLog.size)
      skillOverride = null
      skillOverflow = false
      LevelLoop
    }
    else if(trainerBattle == 1 || trainerBattle == 0){
      BattleAI(s"fight $skillIndex")
    }
    BackButton.visible = false
  }
  
  def LevelUp: Boolean = {
    ControlPane.visible = false
    PokemonPane.visible = false
    PotPane.visible = false
    BallPane.visible = false
    SkillPane.visible = true
    
    currentPokimon = pokimon(index.remove(0))
    OppImg.image = null
    val skill = currentPokimon.skillSet
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
    if(currentPokimon.pokiLevel != 50){
      logResult(f"${currentPokimon.pokimon} has leveled up to ${currentPokimon.pokiLevel + 1}")
      BattleLog.scrollTo(battleLog.size)
    }
    val overflowSkill = currentPokimon.levelUp
    PlayerImg.image = Pokimon.getImage(currentPokimon.imagePath)
    PlayerTextArr.text.value = (currentPokimon.toStringBattle)
    if(!overflowSkill._2.name.equals("-")){
      logResult(f"${currentPokimon.pokimon} learned ${overflowSkill._2.name}")
      BattleLog.scrollTo(battleLog.size)
    }
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
    countdown.value = 60
    if(trainerBattle == 2){
      finished.value = false
    }
    countdown.removeListener(listenerCountdown)
    countdown.value = 1
      
    OppTextArr.text.value = ""
    var overflow = false
    breakableLoop.breakable{
      while(amountFight != 0){
        if(amountFight != 0){
          levelUpBool = true
          overflow = LevelUp
          if(overflow){
            skillOverflow = true
            breakableLoop.break
          }
          amountFight-=1
        }
      }
    }
    if(amountFight == 0 && !overflow){
      SkillPane.visible = false
      PokemonPane.visible = false
      BallPane.visible = false
      ControlPane.visible = true
      dead.value = true
      poki.value = true
      finished.value = true
      ExitButton.visible = true
    }
  }
  
  def ChangeScene {
    for(x <- pokimon){
      x.reset
    }
    Screen.oppName.value = ""
    
    Screen.uiDescription.value = ""

    Platform.runLater(new Thread{
      override def run(){
        Screen.game.scene = Screen.getScene(Screen.currentMapType, Screen.currentMapNum)
        Screen.oppName.value_=("")
        Screen.scheduleSync
      }
    })
    Screen.battleReady = true
  }
  
  def EnteredMouse(e: MouseEvent){
    val button = e.getSource.asInstanceOf[javafx.scene.control.Button]
    val name = button.getId
    name match{
      case "FightButton" => {
        InfoText.text = "For sparta!"
      }
      case "RunButton" => {
        InfoText.text = "Exactly what it said. Run"
      }
      case "BallButton" => {
        InfoText.text = "The balls you need to catch pokimons"
      }
      case "PotButton" => {
        InfoText.text = "Potion that heals your pokimons."
      }
      case "PokemonOne" => {
        InfoText.text = pokimon(0).toStringBattle
      }
      case "PokemonTwo" => {
        InfoText.text = pokimon(1).toStringBattle
      }
      case "PokemonThree" => {
        InfoText.text = pokimon(2).toStringBattle
      }
      case "PokemonFour" => {
        InfoText.text = pokimon(3).toStringBattle
      }
      case "PokemonFive" => {
        InfoText.text = pokimon(4).toStringBattle
      }
      case "PokemonSix" => {
        InfoText.text = pokimon(5).toStringBattle
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
        InfoText.text = currentPokimon.skillSet(0).description
      }
      case "SkillTwo" => {
        InfoText.text = currentPokimon.skillSet(1).description
      }
      case "SkillThree" => {
        InfoText.text = currentPokimon.skillSet(2).description
      }
      case "SkillFour" => {
        InfoText.text = currentPokimon.skillSet(3).description
      }
      case _ => 
    }
  }
  
  def BattleAI(playerChoice: String){
    if(trainerBattle == 0){//WILD POKIMON AI
      processBattle(currentPokimon, oppPokimon,
          playerChoice, s"fight ${Random.nextInt(oppPokimon.skillSet.length)}")
    }
    else{//PLAYER AI
      val poki = oppPokimon
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
        result = processBattle(currentPokimon, oppPokimon,
          playerChoice, s"fight ${Random.nextInt(oppPokimon.skillSet.length)}")
      }
      else{
        result = processBattle(currentPokimon, oppPokimon,
          playerChoice, s"pokemon")
      }
      if(result._2.equals("AI")){
        var chosen = false
        for(x <- oppPokimons){
          if(x.health > 0){
            if(Element.getMultiplier(Element.getIntFromElem(x.element), Element.getIntFromElem(currentPokimon.element)) >= 1.9){
              setOppPokimon(x)
            }
            else{
              if(!chosen){
                setOppPokimon(x)
                chosen = true
              }
            }
          }
        }
      }
    }
    updateStats
  }
  
  def errorProneStatement(dmgTakePk: Pokimon, dmgDealPk: Pokimon, dmgDealSkill: Int){
    try{
      val damage = dmgTakePk.takeDmg(dmgDealPk.doDmg(dmgTakePk.element, dmgDealSkill))
      logResult(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
      logResult(f"${dmgTakePk.pokimon} suffers $damage%.2f")
      BattleLog.scrollTo(battleLog.size)
    }
    catch{
      case e: NoSuchElementException =>{
        val nullskill = dmgDealPk.skillSet(dmgDealSkill)
        if(nullskill.power < 0 && nullskill.description.contains("defense")){
          dmgTakePk.debuffSkill(true, nullskill.power)
          logResult(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
          logResult(f"${dmgTakePk.pokimon} defense is reduced by ${nullskill.power}")
        }
        else if(nullskill.power > 0 && nullskill.description.contains("defense")){
          dmgDealPk.buffSkill(true, nullskill.power)
          logResult(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
          logResult(f"${dmgDealPk.pokimon} defense is increased by ${nullskill.power}")
        }
        else if(nullskill.power < 0 && nullskill.description.contains("attack")){
          dmgTakePk.debuffSkill(false, nullskill.power)
          logResult(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
          logResult(f"${dmgTakePk.pokimon} attack is reduced by ${nullskill.power}")
        }
        else if(nullskill.power > 0 && nullskill.description.contains("attack")){
          dmgDealPk.buffSkill(false, nullskill.power)
          logResult(f"${dmgDealPk.pokimon} use ${dmgDealPk.skillSet(dmgDealSkill).name}")
          logResult(f"${dmgDealPk.pokimon} attack is increased by ${nullskill.power}")
        }
        BattleLog.scrollTo(battleLog.size)
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
          logResult(f"${client.name}'s ${player.pokimon} has fainted")
          BattleLog.scrollTo(battleLog.size)
          ("dead","player")
        }
        else{
          ("alive","")
        }
      }
      else{
        errorProneStatement(AI, player, playerChoice.split(" ")(1).toInt)
        if(AI.health <= 0){
          logResult(f"AI's ${AI.pokimon} has fainted")
          BattleLog.scrollTo(battleLog.size)
          amountFight+=1
          breakableLoop.breakable{
            for(x <- 0 to pokimon.length-1){
              if(pokimon(x).equals(player)){
                index.append(x)
                breakableLoop.break
              }
            }
          }
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
            logResult(f"AI's ${AI.pokimon} has fainted")
            BattleLog.scrollTo(battleLog.size)
            amountFight+=1
            breakableLoop.breakable{
              for(x <- 0 to pokimon.length-1){
                if(pokimon(x).equals(player)){
                  index.append(x)
                  breakableLoop.break
                }
              }
            }
            ("dead","AI")
          }
          else{
            errorProneStatement(player, AI, AIChoice.split(" ")(1).toInt)
            if(player.health <= 0){
              logResult(f"${client.name}'s ${player.pokimon} has fainted")
              BattleLog.scrollTo(battleLog.size)
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
            logResult(f"${client.name}'s ${player.pokimon} has fainted")
            BattleLog.scrollTo(battleLog.size)
            ("dead","player")
          }
          else{
            errorProneStatement(AI, player, playerChoice.split(" ")(1).toInt)
            if(AI.health <= 0){
              logResult(f"AI's ${AI.pokimon} has fainted")
              BattleLog.scrollTo(battleLog.size)
              amountFight+=1
              breakableLoop.breakable{
                for(x <- 0 to pokimon.length-1){
                  if(pokimon(x).equals(player)){
                    index.append(x)
                    breakableLoop.break
                  }
                }
              }
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
              logResult(f"AI's ${AI.pokimon} has fainted")
              BattleLog.scrollTo(battleLog.size)
              amountFight+=1
              breakableLoop.breakable{
                for(x <- 0 to pokimon.length-1){
                  if(pokimon(x).equals(player)){
                    index.append(x)
                    breakableLoop.break
                  }
                }
              }
              ("dead","AI")
            }
            else{
              errorProneStatement(player, AI, AIChoice.split(" ")(1).toInt)
              if(player.health <= 0){
                logResult(f"${client.name}'s ${player.pokimon} has fainted")
                BattleLog.scrollTo(battleLog.size)
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
              logResult(f"${client.name}'s ${player.pokimon} has fainted")
              BattleLog.scrollTo(battleLog.size)
              ("dead","player")
            }
            else{
              errorProneStatement(AI, player, playerChoice.split(" ")(1).toInt)
              if(AI.health <= 0){
                logResult(f"AI's ${AI.pokimon} has fainted")
                BattleLog.scrollTo(battleLog.size)
                amountFight+=1
                breakableLoop.breakable{
                  for(x <- 0 to pokimon.length-1){
                    if(pokimon(x).equals(player)){
                      index.append(x)
                      breakableLoop.break
                    }
                  }
                }
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