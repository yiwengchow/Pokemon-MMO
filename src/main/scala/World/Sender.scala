package World

import akka.actor._
import java.net._
import java.io._
import WorldObject._
import Messages._
import OutMessages._
import scalafx.scene.Scene
import javafx.application.Platform
import scala.collection.mutable.ArrayBuffer
import World.Controller._
  
class Sender extends Actor{
  val loginServer = context.actorSelection("akka.tcp://PukimanServer@127.0.0.1:5150/user/LoginServer")
  val movementServer = context.actorSelection("akka.tcp://PukimanServer@127.0.0.1:5150/user/MovementServer")
  val battleSystem = context.actorSelection("akka.tcp://PukimanServer@127.0.0.1:5150/user/BattleServer")
  
  var nameID : String = null
  var move : String = null
  var coordsX : Integer = null
  var coordsY : Integer = null
  
  var character : Player = null
  var movement : Movement = null
  
  def initialize(){
    nameID = null
    move = null
    coordsX = null
    coordsY = null
    character = null
    movement = null
  }
  
  override def receive ={
    case MovementMsg(name, move, coordsX, coordsY, mapNum, mapType) =>{
      movementServer ! MovementMsg(name, move, coordsX, coordsY, mapNum, mapType)
    }
    
    case Message(name, mapNum, mapType, msg) =>{
      movementServer ! Message(name, mapNum, mapType, msg)
    }
    
    case MapChange(initialize, name, coordsX, coordsY, mapNum, mapType, pMapNum, pMapType) =>{
      movementServer ! MapChange(initialize, name, coordsX, coordsY, mapNum, mapType, pMapNum, pMapType)
    }
    
    case BattleReq(toReqName, name, mapNum, mapType) => {
      movementServer ! BattleReq(toReqName, name, mapNum, mapType)
    }
    
    case BattleReqAccepted(name, receipientName, mapNum, mapType) => {
      movementServer ! BattleReqAccepted(name, receipientName, mapNum, mapType)
    }
    
    case Login(name: String, password: String) => {
      loginServer ! Login(name, password)
    }
    
    case Register(name: String, password: String) => {
      loginServer ! Register(name, password)
    }
    
    case Synchronizer(player: Player) => {
      player.moneyInt = player.money.value.toInt
      player.mapType = Screen.currentMapType
      player.mapNum = Screen.currentMapNum
      loginServer ! Synchronizer(player)
    }
    
    case BattleDetails(receipientName: String, poki: Pokimon) => {
      battleSystem ! BattleDetails(receipientName, poki)
    }
    
    case BattleChoice(name: String, oppName: String, choice: String, poki: Pokimon) => {
      battleSystem ! BattleChoice(name, oppName, choice, poki)
    }
    
    case ChangePokemon(oppName: String, poki: Pokimon) => {
      battleSystem ! ChangePokemon(oppName, poki)
    }
    
    case EndBattle(name: String, oppName: String) => {
      battleSystem ! EndBattle(name, oppName)
    }
    
    case OutUpdate(player) =>{
      println("update")
      var scene : Scene = null
      
      Screen.synchronized{
        scene = Screen.getScene(Screen.currentMapType, Screen.currentMapNum)
        character = Screen.characterList(player)
        movement = Screen.movementList(character)
      }
        
      Platform.runLater(new Thread{
        override def run(){
          if (!Screen.currentMapType.equals("battle")){
            Screen.deleteACharacter(scene, character, movement)
          }
        }
      })
    }
    
    case OutMessage(name, mapNum, mapType, msg) => {
      println("message")
      nameID = name
      
      val chatList = Screen.messageList(Screen.getCurrentScene)._1
        
      Platform.runLater(new Thread{
        override def run(){
          val message = nameID + ": " + msg
          Screen.chatMessage += message
          chatList.scrollTo(Screen.chatMessage.size)
        }
      })
    }
    
    case OutMovement(initialize, player, playerMove, playerCoordsX, playerCoordsY) => {
      if (Screen.readyActor || initialize){
        println(s"Movement $player $playerMove $playerCoordsX $playerCoordsY")
        nameID = player
        move = playerMove
        coordsX = playerCoordsX
        coordsY = playerCoordsY
        
        findCharacter
        moveCharacter
      }
    }
    
    case OutBattleReq(opponent: String) => {
      if(!Screen.battleUIVisibility.value){
          if (Screen.clientPlayer.checkAlive){
            Screen.battleUIVisibility.value = true
            
            Platform.runLater(new Thread{
            override def run(){
              Screen.oppName.value = f"${opponent}"
            }
          })
        }
      }
    }
    
    case OutBattleReqAccepted(actor: ActorRef) =>{
      Screen.clientPlayer.previousMapType = Screen.currentMapType
      Screen.clientPlayer.previousMap = Screen.currentMapNum
      
      Screen.currentMapType = "battle"
      Screen.currentMapNum = 0
      
      Screen.senderActor ! MapChange(false, Screen.clientName, Screen.clientPlayer.coordsX, Screen.clientPlayer.coordsY, 
          Screen.currentMapNum, Screen.currentMapType, Screen.clientPlayer.previousMap, Screen.clientPlayer.previousMapType)
      
      Platform.runLater(new Thread{
        override def run(){
          Screen.game.scene = Screen.getBattle
        }
      })
      BattleStatics.trainerBattle = 2
    }
    
    case OutBattleResultDead(ownerName: String, deadPoki: Pokimon, poki: Pokimon,
      deadPokiChoice: String, alivePokiChoice: String, skillDeadPoki: Int, skillAlivePoki: Int, dmgTakenDeadPoki: Double,
      dmgTakenAlivePoki: Double) => {
      Platform.runLater(new Runnable{
        override def run {
          println("dead")
          BattleStatics.finished.value = false
          BattleStatics.poki.value = false
          if(Screen.clientName.equals(ownerName)){
            println("me dead")
            if(deadPokiChoice.equals("pokemon")){
              dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, true)
            }
            else{
              if(deadPoki.speed > poki.speed){
                dmgBuffDebuffDisp("dmg", poki, deadPoki, skillDeadPoki, dmgTakenAlivePoki, false, false, false)
                //Thread.sleep(1000)
                BattleStatics.oppPokimon.value_=(poki)
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(poki.toStringBattle)
                dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, true)
              }
              else{
                dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, true)
              }
            }
          }
          else{
            println("you dead")
            if(deadPokiChoice.equals("pokemon")){
              println("pokemon")
              BattleStatics.battleLog.append(
                  f"$ownerName chooses ${deadPoki.pokimon}")
              BattleStatics.oppPokimon.value_=(deadPoki)
              BattleStatics.oppPokimon.value.health+=dmgTakenDeadPoki
              BattleStatics.oppPokiStats.value_=("")
              BattleStatics.oppPokiStats.value_=(deadPoki.toStringBattle)
              dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, false)
            }
            else{
              if(deadPoki.speed > poki.speed){
                println("fight")
                dmgBuffDebuffDisp("dmg", poki, deadPoki, skillDeadPoki, dmgTakenAlivePoki, false, false, true)
                //Thread.sleep(1000)
                BattleStatics.currentPokimon.value.health_=(poki.health)
                val temp = BattleStatics.oppPokiStats.value
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(temp)
                dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, false)
              }
              else{
                dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, false)
              }
            }
            BattleStatics.finished.value = true
            BattleStatics.poki.value = true
          }
        }
      })
    }
      
    case OutBattleResultAlive(poki1Owner: String, poki1: Pokimon, poki2: Pokimon,
      poki1Choice: String, poki2Choice: String, skillPoki1: Int, skillPoki2: Int, dmgTakenPoki1: Double,
      dmgTakenPoki2: Double) => {
      Platform.runLater(new Runnable{
        override def run{
          println("alive")
          BattleStatics.oppPokiStats.value_=("")
          BattleStatics.finished.value = false
          BattleStatics.poki.value = false
          if(Screen.clientName.equals(poki1Owner)){
            if(poki1Choice.equals("pokemon") ^ poki2Choice.equals("pokemon")){
              if(poki1Choice.equals("pokemon")){
                AliveDebuffBuffIfDmg(1, poki2, poki1, skillPoki2, dmgTakenPoki1)
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(poki2.toStringBattle)
              }
              else{
                BattleStatics.battleLog.append(
                    f"${Screen.oppName.value} chooses ${poki2.pokimon}")
                BattleStatics.oppPokimon.value_=(poki2)
                BattleStatics.oppPokimon.value.health = poki2.health+dmgTakenPoki2
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(BattleStatics.oppPokimon.value.toStringBattle)
                AliveDebuffBuffIfDmg(2, poki1, poki2, skillPoki1, dmgTakenPoki2)
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(BattleStatics.oppPokimon.value.toStringBattle)
              }
            }
            else{
              if(poki1Choice.equals("pokemon")){
                BattleStatics.battleLog.append(
                    f"${Screen.oppName.value} chooses ${poki2.pokimon}")
                BattleStatics.oppPokimon.value_=(poki2)
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(poki2.toStringBattle)
              }
              else{
                if(poki1.speed > poki2.speed){//debuff/buff
                  
                  AliveDebuffBuffIfDmg(2, poki1, poki2, skillPoki1, dmgTakenPoki2)
                    
                  BattleStatics.oppPokiStats.value_=("")
                  BattleStatics.oppPokiStats.value_=(poki2.toStringBattle)
                    
                    
                  AliveDebuffBuffIfDmg(1, poki2, poki1, skillPoki2, dmgTakenPoki1)
                  BattleStatics.oppPokiStats.value_=("")
                  BattleStatics.oppPokiStats.value_=(poki2.toStringBattle)
                }
                else{   
                  AliveDebuffBuffIfDmg(1, poki2, poki1, skillPoki2, dmgTakenPoki1)
                    
                  BattleStatics.oppPokiStats.value_=("")
                  BattleStatics.oppPokiStats.value_=(poki2.toStringBattle)
                  
                  AliveDebuffBuffIfDmg(2, poki1, poki2, skillPoki1, dmgTakenPoki2)
                  
                  BattleStatics.oppPokiStats.value_=("")
                  BattleStatics.oppPokiStats.value_=(poki2.toStringBattle)
                }
              }
            }
          }
          else{
            if(poki1Choice.equals("pokemon") ^ poki2Choice.equals("pokemon")){
              if(poki1Choice.equals("pokemon")){
                BattleStatics.battleLog.append(
                    f"${Screen.oppName.value} chooses ${poki1.pokimon}")
                BattleStatics.oppPokimon.value_=(poki1)
                BattleStatics.oppPokimon.value.health = poki1.health+dmgTakenPoki1
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(BattleStatics.oppPokimon.value.toStringBattle)
                
                AliveDebuffBuffIfDmg(2, poki2, poki1, skillPoki2, dmgTakenPoki1)
                
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(poki1.toStringBattle)
              }
              else{
                AliveDebuffBuffIfDmg(1, poki1, poki2, skillPoki1, dmgTakenPoki2)
                
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(BattleStatics.oppPokimon.value.toStringBattle)
              }
            }
            else{
              if(poki1Choice.equals("pokemon")){
                BattleStatics.battleLog.append(
                    f"${Screen.oppName.value} chooses ${poki1.pokimon}")
                BattleStatics.oppPokimon.value_=(poki1)
                BattleStatics.oppPokiStats.value_=("")
                BattleStatics.oppPokiStats.value_=(poki1.toStringBattle)
              }
              else{
                if(poki1.speed > poki2.speed){//debuff/buff
                  AliveDebuffBuffIfDmg(1, poki1, poki2, skillPoki1, dmgTakenPoki2)
                    
                    
                  BattleStatics.oppPokiStats.value_=("")
                  BattleStatics.oppPokiStats.value_=(poki1.toStringBattle)
                    
                  AliveDebuffBuffIfDmg(2, poki2, poki1, skillPoki2, dmgTakenPoki1)
                    
                  BattleStatics.oppPokiStats.value_=("")
                  BattleStatics.oppPokiStats.value_=(poki1.toStringBattle)
                }
                else{
                  
                  AliveDebuffBuffIfDmg(2, poki2, poki1, skillPoki2, dmgTakenPoki1)
                    
                  BattleStatics.oppPokiStats.value_=("")
                  BattleStatics.oppPokiStats.value_=(poki1.toStringBattle)
                    
                    
                  AliveDebuffBuffIfDmg(1, poki1, poki2, skillPoki1, dmgTakenPoki2)
                    
                  BattleStatics.oppPokiStats.value_=("")
                  BattleStatics.oppPokiStats.value_=(poki1.toStringBattle)
                }
              }
            }
          }
          }
      })
    }
    case OutBattleDetails(oppPoki: Pokimon) => {
      BattleStatics.oppPokimon.value_=(oppPoki)
      BattleStatics.placeholder.synchronized{
        BattleStatics.placeholder.wait(1000)
      }
      BattleStatics.oppPokiStats.value_=(BattleStatics.oppPokimon.value.toStringBattle)
    }
    case OutChangePokimon(poki: Pokimon) => {
      Platform.runLater(new Runnable{
        override def run{
          BattleStatics.battleLog.append(
              f"${Screen.oppName.value} chooses ${poki.pokimon}")
          BattleStatics.oppPokimon.value = poki
          BattleStatics.oppPokiStats.value_=(poki.toStringBattle)
          BattleStatics.finished.value = false
          BattleStatics.poki.value = false
        }
      })
    }
    case OutEndBattle => {
      Screen.currentMapType = Screen.clientPlayer.previousMapType
      Screen.currentMapNum = Screen.clientPlayer.previousMap
      
      Screen.clientPlayer.previousMapType = "battle"
      Screen.clientPlayer.previousMap = 0
      
      Screen.uiDescription.value = ""
      
      Platform.runLater(new Thread{
        override def run(){
          BattleStatics.endBattle.value = true
          
          Screen.senderActor ! MapChange(false, Screen.clientName, Screen.clientPlayer.coordsX, Screen.clientPlayer.coordsY, 
              Screen.currentMapNum, Screen.currentMapType, Screen.clientPlayer.previousMap, Screen.clientPlayer.previousMapType)
              
          Screen.game.scene = Screen.getScene(Screen.currentMapType, Screen.currentMapNum)
          Screen.oppName.value_=("")
          Screen.scheduleSync
        }
      })
    }
    case OutSynchronizer(player: Player) => {
      LoginStatics.loginRegFailed.value_=(false)
      Platform.runLater(new Runnable(){
        override def run(){
          val tempPlayer = new Player(player.name, player.charType)
          tempPlayer.money.value = player.moneyInt.toString
          tempPlayer.pokiballBuffer = player.pokiballBuffer
          tempPlayer.pokiPotionBuffer = player.pokiPotionBuffer
          tempPlayer.pokiballs = player.pokiballs
          tempPlayer.pokiPotion = player.pokiPotion
          tempPlayer.trainerPoki = player.trainerPoki
          tempPlayer.pcPoki = player.pcPoki
          tempPlayer.coordsX = player.coordsX
          tempPlayer.coordsY = player.coordsY
          tempPlayer.previousCoordsX = player.previousCoordsX
          tempPlayer.previousCoordsY = player.previousCoordsY
          
          if (player.mapType == "battle"){
            tempPlayer.mapType = player.previousMapType
            tempPlayer.mapNum = player.previousMap
          }
          
          tempPlayer.mapType = player.mapType
          tempPlayer.mapNum = player.mapNum
          tempPlayer.previousMapType = player.previousMapType
          tempPlayer.previousMap = player.previousMap
          self ! InitializeGame(tempPlayer)
        }
      })
    }
    
    case FailLogin | FailRegister => {
      println("failed")
      LoginStatics.failBoolean.value_=(true)
    }
    
    case Registered (name: String)=> {
      LoginStatics.loginRegFailed.value_=(false)
      Screen.clientPlayer = new Player(name,1)
      self ! InitializeGame(Screen.clientPlayer)
    }
    
    case InitializeGame(player : Player) =>{
      Screen.clientPlayer = player
      Screen.clientName = player.name
      Screen.name = player.name
      
      Screen.initializeMap()
      
      Screen.currentMapNum = player.mapNum
      Screen.currentMapType = player.mapType 
      
      Screen.senderActor ! MapChange(true, Screen.clientName, player.coordsX, player.coordsY,
        Screen.currentMapNum, Screen.currentMapType, Screen.currentMapNum, Screen.currentMapType)
        
      Screen.reloadItems()
      
      Screen.pcUI = Screen.getPCUI
      Screen.shopUI = Screen.getShopUI
      
      Platform.runLater(new Runnable{
        def run{
          Screen.initializePlayer()
          Screen.game.scene = Screen.getCurrentScene
          Screen.readyActor = true
          Screen.scheduleSync
        }
      })
    }
    
    case meh: Any => 
      println(meh.getClass)
  }
  
  def AliveDebuffBuffIfDmg(typeMethod: Int, attacker: Pokimon, receiver: Pokimon, attackerMove: Int, dmgDealt: Double){
    if(attacker.skillSet(attackerMove).element.equals("null")){
      if(attacker.skillSet(attackerMove).power < 0 && attacker.skillSet(attackerMove).description.contains("defense")){
        dmgBuffDebuffDisp("debuff", receiver, attacker, attackerMove, dmgDealt, true, false, true)
        if(typeMethod == 1){
          BattleStatics.currentPokimon.value.debuffSkill(true, dmgDealt.ceil.toInt)
        }
        else{
          BattleStatics.oppPokimon.value_=(receiver)
        }
      }
      else if(attacker.skillSet(attackerMove).power > 0 && attacker.skillSet(attackerMove).description.contains("defense")){
        dmgBuffDebuffDisp("buff", attacker, attacker, attackerMove, dmgDealt, true, false, false)
        if(typeMethod == 1){
          BattleStatics.oppPokimon.value_=(attacker)
        }
        else{
          BattleStatics.currentPokimon.value.buffSkill(true, dmgDealt.ceil.toInt)
        }
      }
      else if(attacker.skillSet(attackerMove).power < 0 && attacker.skillSet(attackerMove).description.contains("attack")){
        dmgBuffDebuffDisp("debuff", receiver, attacker, attackerMove, dmgDealt, false, false, true)
        if(typeMethod == 1){
          BattleStatics.currentPokimon.value.debuffSkill(false, dmgDealt.ceil.toInt)
        }
        else{
          BattleStatics.oppPokimon.value_=(receiver)
        }
      }
      else if(attacker.skillSet(attackerMove).power > 0 && attacker.skillSet(attackerMove).description.contains("attack")){
        dmgBuffDebuffDisp("buff", attacker, attacker, attackerMove, dmgDealt, false, false, false)
        if(typeMethod == 1){
          BattleStatics.oppPokimon.value_=(attacker)
        }
        else{
          BattleStatics.currentPokimon.value.buffSkill(false, dmgDealt.ceil.toInt)
        }
      }
    }
    else{//skill that attacks people
      dmgBuffDebuffDisp("dmg", receiver, attacker, attackerMove, dmgDealt, false, false, false)
      if(typeMethod == 1){
        BattleStatics.currentPokimon.value.health_=(receiver.health)
      }
      else{
        BattleStatics.oppPokimon.value_=(receiver)
      }
    }
  }
  
  def dmgBuffDebuffDisp(typeString: String, receiving: Pokimon, doing: Pokimon, skill: Int, dmg: Double,
      defense: Boolean, isDead: Boolean, owner: Boolean){
    typeString match {
      case "dmg" =>{
        BattleStatics.battleLog.append(
            f"${doing.pokimon} use ${doing.skillSet(skill).name}")
        
        BattleStatics.battleLog.append(
            f"${receiving.pokimon} suffers ${dmg}%.2f")
      }
      case "buff" =>{
        if(defense){
          BattleStatics.battleLog.append(
              f"${doing.pokimon} use ${doing.skillSet(skill).name}")
          
          BattleStatics.battleLog.append(
              f"${doing.pokimon} defense is increased by ${dmg}%.2f")
        }
        else{
          BattleStatics.battleLog.append(
              f"${doing.pokimon} use ${doing.skillSet(skill).name}")
          
          BattleStatics.battleLog.append(
              f"${doing.pokimon} attack is increased by ${dmg}%.2f")
        }
      }
      case "debuff" => {
        if(defense){
          BattleStatics.battleLog.append(
              f"${doing.pokimon} use ${doing.skillSet(skill).name}")
          
          BattleStatics.battleLog.append(
              f"${receiving.pokimon} defense is reduced by ${dmg}%.2f")
        }
        else{
          BattleStatics.battleLog.append(
              f"${doing.pokimon} use ${doing.skillSet(skill).name}")
          
          BattleStatics.battleLog.append(
              f"${receiving.pokimon} attack is reduced by ${dmg}%.2f")
        }
      }
    }
    if(isDead && owner){
      BattleStatics.currentPokimon.value.health_=(receiving.health)
      BattleStatics.oppPokiStats.value_=("")
      BattleStatics.oppPokiStats.value_=(doing.toStringBattle)
      BattleStatics.battleLog.append(
          f"${Screen.clientName}'s ${receiving.pokimon} has fainted")
    }
    else if(isDead && !owner){
      BattleStatics.oppPokimon.value_=(receiving)
      BattleStatics.oppPokiStats.value_=("")
      BattleStatics.oppPokiStats.value_=(receiving.toStringBattle)
      BattleStatics.battleLog.append(
          f"${Screen.oppName}'s ${receiving.pokimon} has fainted")
    }
  }
  
  def findCharacter{
    
    if (!Screen.characterList.contains(nameID)){
      
      val localScene = Screen.getScene(Screen.currentMapType, Screen.currentMapNum)
      val player = new Player(nameID,1)
      val characterX = coordsX
      val characterY = coordsY
      
      player.char = new Characters().getCharacter(player.charType)
      player.setCharacter
     
      Screen.characterList.synchronized{
        Screen.characterList.put(nameID, player)
      }
      Screen.movementList.synchronized{
        Screen.movementList.put(player, new Movement(player,Screen.currentMapType, Screen.currentMapNum))
      }
      Screen.nameList.synchronized{
        Screen.nameList.append(nameID)
      }
      
      Platform.runLater(new Thread(){
        override def run(){
          Screen.addACharacter(localScene,player,characterX,characterY)
        }
      })
    }
    
    character = Screen.characterList.getOrElse(nameID, null)
    movement = Screen.movementList.getOrElse(character, null)
  }
  
  def moveCharacter(){
    if (move != null){
      move match {
        case "movedown" => movement.pressMove("down"); checkSync
        case "moveup" => movement.pressMove("up"); checkSync
        case "moveleft" => movement.pressMove("left"); checkSync
        case "moveright" => movement.pressMove("right"); checkSync
        case "movee" => 
//          if (!(Screen.currentMapNum.equals(mapNum) && Screen.currentMapType.equals(mapType))){
//            Platform.runLater(new Runnable(){
//              def run(){
//                Screen.deleteACharacter(Screen.getScene(Screen.currentMapType, Screen.currentMapNum), character, movement)
//              }
//            })
//          }
        
        case "stopdown" => movement.releaseMove("down")
        case "stopup" => movement.releaseMove("up")
        case "stopleft" => movement.releaseMove("left")
        case "stopright" => movement.releaseMove("right")
        case _ => 
      }
    }
  }
  
  def checkSync() {
    var compareX = 0
    var compareY = 0
    val localX = Math.abs(character.coordsX)
    val x = Math.abs(coordsX)
    val localY = Math.abs(character.coordsY)
    val y = Math.abs(coordsY)
    
    if (localX > x){
      compareX = localX - x
    }
    else {
      compareX = x - localX
    }
    
    if (localY > y){
      compareY = localY - y
    }
    else{
      compareY = y - localY
    }
    
    if (compareX > 50 || compareY > 50){
      character.setXY(x, y)
    }
  }
}

