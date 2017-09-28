package World

import akka.actor._
import WorldObject._

object Messages {
  case class MovementMsg(name: String, move: String, coordsX: Int, coordsY: Int, mapNum: Int, mapType: String)
  case class Message(name: String, mapNum: Int, mapType: String, msg: String)
  case class MapChange(initialize : Boolean, name: String, coordsX : Int, coordsY: Int, mapNum: Int, mapType: String, pMapNum: Int, pMapType: String)
  case class BattleReq(toReqName: String, name: String, mapNum: Int, mapType: String)
  case class BattleReqAccepted(name: String, receipientName: String, mapNum: Int, mapType: String)
  case class BattleDetails(receipientName: String, poki: Pokimon)
  case class BattleChoice(name: String, oppName: String, choice: String, poki: Pokimon)
  case class ChangePokemon(oppName: String, poki: Pokimon)
  case class EndBattle(name: String, oppName: String)
  case class Synchronizer(player: Player)
  case class Login(name: String, password: String)
  case class Register(name: String, password: String)
  case class InitializeGame(player : Player)
}

object OutMessages{
  case class OutMessage(name: String, mapNum: Int, mapType: String, msg: String)
  case class OutMovement(initialize : Boolean, player: String, move: String, coordsX: Int, coordsY: Int)
  case class OutUpdate(player: String)
  case class OutBattleReq(player: String)
  case class OutBattleReqAccepted(battleServer: ActorRef)
  case class OutBattleDetails(poki: Pokimon)
  class OutBattleResult
  case class OutBattleResultDead(deadPokimonOwnerName: String, deadPoki: Pokimon, alivePoki: Pokimon,
      deadPokiChoice: String, alivePokiChoice: String, skillDeadPoki: Int, skillAlivePoki: Int, dmgTakenDeadPoki: Double,
      dmgTakenAlivePoki: Double) extends OutBattleResult
  case class OutBattleResultAlive(poki1Owner: String, poki1: Pokimon, poki2: Pokimon,
      poki1Choice: String, poki2Choice: String, skillPoki1: Int, skillPoki2: Int, dmgTakemPoki1: Double,
      dmgTakenPoki2: Double) extends OutBattleResult
  case class OutChangePokimon(poki: Pokimon)
  case object OutEndBattle
  case object FailLogin
  case class Registered(name: String)
  case object FailRegister
  case class OutSynchronizer(player: Player)
}