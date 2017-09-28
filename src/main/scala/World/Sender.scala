package World

import akka.actor.{ActorSystem, Actor, Props, DeadLetter}
import akka.pattern.AskableActorSelection
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import WorldObject._
import Messages._
import OutMessages._
import scalafx.scene.Scene
import javafx.application.Platform
import scala.collection.mutable.ArrayBuffer
import Controller._
import scalafx.Includes._
  
class Sender extends Actor{
  val movementServer = context.actorSelection(s"akka.tcp://PukimanServer@${Screen.ipAddress}:2047/user/MovementServer")
  val battleServer = context.actorSelection(s"akka.tcp://PukimanServer@${Screen.ipAddress}:2047/user/BattleServer")
  val loginServer = context.actorSelection(s"akka.tcp://PukimanServer@${Screen.ipAddress}:2047/user/LoginServer")
  val loginServerAsk = new AskableActorSelection(loginServer)
  
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
      implicit val timeout = Timeout(3.seconds)
      val fut = loginServerAsk ? Login(name, password) 
      fut.foreach{
        case OutSynchronizer(player: Player) => {
//        LoginStatics.loginRegFailed.value_=(false)
          Platform.runLater(new Runnable(){
            override def run(){
              val tempPlayer = new Player(player.name, player.charType)
              tempPlayer.moneyInt = player.moneyInt
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
              
              tempPlayer.mapType = player.mapType
              tempPlayer.mapNum = player.mapNum
              tempPlayer.previousMapType = player.previousMapType
              tempPlayer.previousMap = player.previousMap
              self ! InitializeGame(tempPlayer)
            }
          })
        }
        case FailLogin(reason: String) => {
          Platform.runLater(new Runnable{
            def run{
              LoginStatics.loginFailText.value = reason
              LoginStatics.failBoolean.value_=(true)
              context.stop(self)
            }
          })
        }
      }
      fut.failed.foreach {
        case e:Exception => {
          Platform.runLater(new Runnable{
            def run{
              LoginStatics.loginFailText.value = "Request to server timeout"
              LoginStatics.failBoolean.value_=(true)
              context.stop(self)
            }
          })
        }
      }
    }
    
    case Register(name: String, password: String) => {
      implicit val timeout = Timeout(3.seconds)
      val fut = loginServerAsk ? Register(name, password) 
      fut.foreach {
        case FailRegister => {
          Platform.runLater(new Runnable{
            def run{
              LoginStatics.loginFailText.value = "ID is already in used"
              LoginStatics.failBoolean.value_=(true)
              context.stop(self)
            }
          })
        }
        
        case Registered (name: String)=> {
          Screen.clientPlayer = new Player(name,1)
          self ! InitializeGame(Screen.clientPlayer)
        }
      }
      fut.failed.foreach{
        case e: Exception => {
          Platform.runLater(new Runnable{
            def run{
              LoginStatics.loginFailText.value = "Request to server timeout"
              LoginStatics.failBoolean.value_=(true)
              context.stop(self)
            }
          })
        }
      }
    }
    
    case Synchronizer(player: Player) => {
      player.moneyInt = Screen.money.value.toInt
      player.mapType = Screen.currentMapType
      player.mapNum = Screen.currentMapNum
      loginServer ! Synchronizer(player)
    }
    
    case BattleDetails(receipientName: String, poki: Pokimon, mapNum: Int, mapType: String) => {
      battleServer ! BattleDetails(receipientName, poki, mapNum: Int, mapType: String)
    }
    
    case BattleChoice(name: String, oppName: String, choice: String, poki: Pokimon, mapNum: Int, mapType: String) => {
      battleServer ! BattleChoice(name, oppName, choice, poki, mapNum: Int, mapType: String)
    }
    
    case ChangePokemon(oppName: String, poki: Pokimon, mapNum: Int, mapType: String) => {
      battleServer ! ChangePokemon(oppName, poki, mapNum: Int, mapType: String)
    }
    
    case EndBattle(name: String, oppName: String, mapNum: Int, mapType: String) => {
      battleServer ! EndBattle(name, oppName, mapNum: Int, mapType: String)
    }
    
    case Logout(name : String, mapType : String, mapNum : Int) => {
      loginServer ! Logout(name, mapType, mapNum)
    }
    
    case OutUpdate(player) =>{      
      Screen.synchronized{
        try{
          val character = Screen.characterList(player)
          val movement = Screen.movementList(character)
          
          Platform.runLater(new Thread{
            override def run(){
              Screen.deleteACharacter(Screen.getCurrentScene, character, movement)
            }
          })
        }
        catch{
          case e : NoSuchElementException => 
        }
      }
      
    }
    
    case OutMessage(name, mapNum, mapType, msg) => {
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
      if ((Screen.readyActor || initialize)){
        nameID = player
        move = playerMove
        coordsX = playerCoordsX
        coordsY = playerCoordsY
        
        findCharacter
        moveCharacter
      }
    }
    
    case OutBattleReq(opponent: String) => {
      if (Screen.battleReady == true){
        if(!Screen.battleUIVisibility.value){
          if(Screen.clientPlayer.checkAlive){
            Screen.battleUIVisibility.value = true
            
            Platform.runLater(new Thread{
              override def run(){
                Screen.oppName.value = f"${opponent}"
              }
            })
          }
          else{
            movementServer ! RejectBattleReq(opponent, Screen.currentMapNum, Screen.currentMapType, "The player have no playable pokimon, please try again later.")
          }
        }
        else{
          movementServer ! RejectBattleReq(opponent, Screen.currentMapNum, Screen.currentMapType, "The player is currently having a request, please try again later.")
        }
      }
      else{
        movementServer ! RejectBattleReq(opponent, Screen.currentMapNum, Screen.currentMapType, "The player is currently in battle, please try again later.")
      }
    }
    
    case RejectBattleReq(name: String, mapNum: Int, mapType: String, reason: String) => {
      movementServer ! RejectBattleReq(name, mapNum, mapType, reason)
    }
  
    case OutRejectBattleReq(reason: String) => {
      Platform.runLater(new Runnable{
        def run{
          Screen.chatMessage.append(reason)
          Screen.messageList(Screen.getCurrentScene)._1.scrollTo(Screen.chatMessage.size)
          Screen.oppName.value = "No Person"
          Screen.UIImage.value = null
        }
      })
    }
    
    case OutBattleReqAccepted =>{
      Platform.runLater(new Runnable{
        override def run {
          Screen.trainerBattle = 2
          val battleStuff = Screen.getBattle
          Screen.game.scene = battleStuff._1
          Screen.battleController = battleStuff._2
        }
      })
    }
    
    
    case OutBattleResultDead(ownerName: String, deadPoki: Pokimon, poki: Pokimon,
      deadPokiChoice: String, alivePokiChoice: String, skillDeadPoki: Int, skillAlivePoki: Int, dmgTakenDeadPoki: Double,
      dmgTakenAlivePoki: Double) => {
      Platform.runLater(new Runnable{
        override def run {
          Screen.battleController.reEnableButtons
          if(Screen.clientName.equals(ownerName)){
            if(deadPokiChoice.equals("pokemon")){
              dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, true)
            }
            else{
              if(deadPoki.speed > poki.speed){
                dmgBuffDebuffDisp("dmg", poki, deadPoki, skillDeadPoki, dmgTakenAlivePoki, false, false, false)
                //Thread.sleep(1000)
                dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, true)
              }
              else{
                dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, true)
              }
            }
          }
          else{
            if(deadPokiChoice.equals("pokemon")){
              Screen.battleController.logResult(
                  f"$ownerName chooses ${deadPoki.pokimon}")
              deadPoki.health+=dmgTakenDeadPoki
              dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, false)
            }
            else{
              if(deadPoki.speed > poki.speed){
                dmgBuffDebuffDisp("dmg", poki, deadPoki, skillDeadPoki, dmgTakenAlivePoki, false, false, true)
                //Thread.sleep(1000)
                dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, false)
              }
              else{
                dmgBuffDebuffDisp("dmg", deadPoki, poki, skillAlivePoki, dmgTakenDeadPoki, false, true, false)
              }
            }
            Screen.battleController.disableButtons
          }
        }
      })
    }
      
    case OutBattleResultAlive(poki1Owner: String, poki1: Pokimon, poki2: Pokimon,
      poki1Choice: String, poki2Choice: String, skillPoki1: Int, skillPoki2: Int, dmgTakenPoki1: Double,
      dmgTakenPoki2: Double) => {
      Platform.runLater(new Runnable{
        override def run{
          Screen.battleController.reEnableButtons
          if(Screen.clientName.equals(poki1Owner)){
            if(poki1Choice.equals("pokemon") ^ poki2Choice.equals("pokemon")){
              if(poki1Choice.equals("pokemon")){
                AliveDebuffBuffIfDmg(1, poki2, poki1, skillPoki2, dmgTakenPoki1)
              }
              else{
                Screen.battleController.logResult(
                    f"${Screen.oppName.value} chooses ${poki2.pokimon}")
                AliveDebuffBuffIfDmg(2, poki1, poki2, skillPoki1, dmgTakenPoki2)
              }
            }
            else{
              if(poki1Choice.equals("pokemon")){
                Screen.battleController.logResult(
                    f"${Screen.oppName.value} chooses ${poki2.pokimon}")
                Screen.battleController.setOppPokimon(poki2)
              }
              else{
                if(poki1.speed > poki2.speed){//debuff/buff
                  AliveDebuffBuffIfDmg(2, poki1, poki2, skillPoki1, dmgTakenPoki2)
                  AliveDebuffBuffIfDmg(1, poki2, poki1, skillPoki2, dmgTakenPoki1)
                }
                else{   
                  AliveDebuffBuffIfDmg(1, poki2, poki1, skillPoki2, dmgTakenPoki1)
                  AliveDebuffBuffIfDmg(2, poki1, poki2, skillPoki1, dmgTakenPoki2)
                }
              }
            }
            Screen.battleController.setPlayerPokimon(poki1)
            Screen.battleController.setOppPokimon(poki2)
          }
          else{
            if(poki1Choice.equals("pokemon") ^ poki2Choice.equals("pokemon")){
              if(poki1Choice.equals("pokemon")){
                Screen.battleController.logResult(
                    f"${Screen.oppName.value} chooses ${poki1.pokimon}")
                AliveDebuffBuffIfDmg(2, poki2, poki1, skillPoki2, dmgTakenPoki1)
              }
              else{
                AliveDebuffBuffIfDmg(1, poki1, poki2, skillPoki1, dmgTakenPoki2)
              }
            }
            else{
              if(poki1Choice.equals("pokemon")){
                Screen.battleController.logResult(
                    f"${Screen.oppName.value} chooses ${poki1.pokimon}")
                Screen.battleController.setOppPokimon(poki1)
              }
              else{
                if(poki1.speed > poki2.speed){//debuff/buff
                  AliveDebuffBuffIfDmg(1, poki1, poki2, skillPoki1, dmgTakenPoki2)
                  AliveDebuffBuffIfDmg(2, poki2, poki1, skillPoki2, dmgTakenPoki1)
                }
                else{
                  AliveDebuffBuffIfDmg(2, poki2, poki1, skillPoki2, dmgTakenPoki1)
                  AliveDebuffBuffIfDmg(1, poki1, poki2, skillPoki1, dmgTakenPoki2)
                }
              }
            }
            Screen.battleController.setPlayerPokimon(poki2)
            Screen.battleController.setOppPokimon(poki1)
          }
          }
      })
    }
    case OutBattleDetails(oppPoki: Pokimon) => {
      Platform.runLater(new Runnable{
        override def run{
          while(!(Screen.battleController != null)){}
          Screen.battleController.setOppPokimon(oppPoki)
        }
      })
    }
    case OutChangePokimon(poki: Pokimon) => {
      Platform.runLater(new Runnable{
        override def run{
          Screen.battleController.logResult(
              f"${Screen.oppName.value} chooses ${poki.pokimon}")
          
          Screen.battleController.setOppPokimon(poki)
          
          Screen.battleController.reEnableButtons
        }
      })
    }
    case OutEndBattle(name: String) => {
      Platform.runLater(new Runnable{
        override def run{
          Screen.battleController.setEndBattlePlayer(name)
          if(name.equals(Screen.clientName)){
            Screen.battleController.logResult(s"Player has lost")
          }
          else{
            Screen.battleController.logResult(s"${Screen.oppName.value} has lost")
          }
        }
      })
    }
    
    case InitializeGame(player : Player) =>{
      LoginStatics.failBoolean.removeListener(LoginStatics.changeListener)
      Screen.clientPlayer = player
      Screen.clientName = player.name
      Screen.clientProperty.value = player.name
      Screen.name = player.name
      Screen.money.value = player.moneyInt.toString
      
      if (Screen.mapScene.size.equals(0)){
        Screen.initializeMap()
      }
      
      Screen.currentMapNum = player.mapNum
      Screen.currentMapType = player.mapType 
      
      Screen.senderActor ! MapChange(true, Screen.clientName, player.coordsX, player.coordsY,
        Screen.currentMapNum, Screen.currentMapType, Screen.currentMapNum, Screen.currentMapType)
        
      Screen.reloadItems()
      
      Screen.pcUI = Screen.getPCUI
      Screen.shopUI = Screen.getShopUI
      Screen.menuUI = Screen.getMenuUI
      
      Platform.runLater(new Runnable{
        def run{
          Screen.initializePlayer()
          Screen.game.scene = Screen.getCurrentScene
          Screen.readyActor = true
          Screen.scheduleSync
        }
      })
    }
    
    case LogoutSuccessful => {
      Platform.runLater(new Runnable{
        def run{
          Screen.system.stop(Screen.senderActor)
          Screen.reinitialize
        }
      })
    }
    
    case DeadLetter(msg, from, to) =>{
      Platform.runLater(new Runnable{
        def run{
            Screen.system.stop(Screen.senderActor)
            Screen.reinitialize
            LoginStatics.failBoolean.value = false
            LoginStatics.loginFailText.value = "Disconnected"
        }
      })
      
      LoginStatics.failBoolean.value_=(true)
    }
    
    case meh: Any => 
      println(meh.getClass)
  }
  
  def AliveDebuffBuffIfDmg(typeMethod: Int, attacker: Pokimon, receiver: Pokimon, attackerMove: Int, dmgDealt: Double){
    if(attacker.skillSet(attackerMove).element.equals("null")){
      if(attacker.skillSet(attackerMove).power < 0 && attacker.skillSet(attackerMove).description.contains("defense")){
        dmgBuffDebuffDisp("debuff", receiver, attacker, attackerMove, dmgDealt, true, false, true)
        if(typeMethod == 1){
          Screen.battleController.debuffOrBuffSkill(true, true, dmgDealt.ceil.toInt)
        }
        else{
          Screen.battleController.setOppPokimon(attacker)
        }
      }
      else if(attacker.skillSet(attackerMove).power > 0 && attacker.skillSet(attackerMove).description.contains("defense")){
        dmgBuffDebuffDisp("buff", attacker, attacker, attackerMove, dmgDealt, true, false, false)
        if(typeMethod == 1){
          Screen.battleController.setOppPokimon(attacker)
        }
        else{
          Screen.battleController.debuffOrBuffSkill(false, true, dmgDealt.ceil.toInt)
        }
      }
      else if(attacker.skillSet(attackerMove).power < 0 && attacker.skillSet(attackerMove).description.contains("attack")){
        dmgBuffDebuffDisp("debuff", receiver, attacker, attackerMove, dmgDealt, false, false, true)
        if(typeMethod == 1){
          Screen.battleController.debuffOrBuffSkill(true, false, dmgDealt.ceil.toInt)
        }
        else{
          Screen.battleController.setOppPokimon(attacker)
        }
      }
      else if(attacker.skillSet(attackerMove).power > 0 && attacker.skillSet(attackerMove).description.contains("attack")){
        dmgBuffDebuffDisp("buff", attacker, attacker, attackerMove, dmgDealt, false, false, false)
        if(typeMethod == 1){
          Screen.battleController.setOppPokimon(attacker)
        }
        else{
          Screen.battleController.debuffOrBuffSkill(false, false, dmgDealt.ceil.toInt)
        }
      }
    }
    else{//skill that attacks people
      dmgBuffDebuffDisp("dmg", receiver, attacker, attackerMove, dmgDealt, false, false, false)
      if(typeMethod == 1){
        Screen.battleController.setPlayerPokimon(receiver)
      }
      else{
        Screen.battleController.setOppPokimon(receiver)
      }
    }
  }
  
  def dmgBuffDebuffDisp(typeString: String, receiving: Pokimon, doing: Pokimon, skill: Int, dmg: Double,
      defense: Boolean, isDead: Boolean, owner: Boolean){
    typeString match {
      case "dmg" =>{
        Screen.battleController.logResult(
            f"${doing.pokimon} use ${doing.skillSet(skill).name}")
        
        Screen.battleController.logResult(
            f"${receiving.pokimon} suffers ${dmg}%.2f")
      }
      case "buff" =>{
        if(defense){
          Screen.battleController.logResult(
              f"${doing.pokimon} use ${doing.skillSet(skill).name}")
          
          Screen.battleController.logResult(
              f"${doing.pokimon} defense is increased by ${dmg}%.2f")
        }
        else{
          Screen.battleController.logResult(
              f"${doing.pokimon} use ${doing.skillSet(skill).name}")
          
          Screen.battleController.logResult(
              f"${doing.pokimon} attack is increased by ${dmg}%.2f")
        }
      }
      case "debuff" => {
        if(defense){
          Screen.battleController.logResult(
              f"${doing.pokimon} use ${doing.skillSet(skill).name}")
          
          Screen.battleController.logResult(
              f"${receiving.pokimon} defense is reduced by ${dmg}%.2f")
        }
        else{
          Screen.battleController.logResult(
              f"${doing.pokimon} use ${doing.skillSet(skill).name}")
          
          Screen.battleController.logResult(
              f"${receiving.pokimon} attack is reduced by ${dmg}%.2f")
        }
      }
    }
    if(isDead && owner){
      Screen.battleController.setPlayerPokimon(receiving)
      Screen.battleController.setOppPokimon(doing)
      Screen.battleController.logResult(
          f"${Screen.clientName}'s ${receiving.pokimon} has fainted")
    }
    else if(isDead && !owner){
      Screen.battleController.setOppPokimon(receiving)
      Screen.battleController.logResult(
          f"${Screen.oppName.value}'s ${receiving.pokimon} has fainted")
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

