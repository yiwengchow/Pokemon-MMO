package World


class Houses(x : Int, y : Int, number : Int, spawnX : Int, spawnY : Int) extends OverworldImmovable(x,y,"house" + number) with Interactable{
  
  def checkClient(){
    if (Screen.name.equalsIgnoreCase(Screen.clientName)){
      Screen.deleteAllCharacters(Screen.mapScene(Screen.currentMapNum))
      
      Screen.clientPlayer.previousMapType = Screen.currentMapType
      Screen.clientPlayer.previousMap = Screen.currentMapNum
        
      Screen.currentMapType = "house"
      Screen.currentMapNum = number
      
      Screen.clientPlayer.previousCoordsX = Screen.clientPlayer.coordsX
      Screen.clientPlayer.previousCoordsY = Screen.clientPlayer.coordsY
      
      Screen.movementList.put(Screen.clientPlayer, new ClientMovement(Screen.currentMapType,Screen.currentMapNum))
      Screen.clientMovement = Screen.movementList.getOrElse(Screen.clientPlayer,null).asInstanceOf[ClientMovement]
      
      Screen.clientPlayer.coordsX = spawnX
      Screen.clientPlayer.coordsY = spawnY
      
      Screen.addACharacter(Screen.houseScene(number),Screen.clientPlayer,spawnX,spawnY)
    }
  }
  
  override def interact() : Boolean = {
    
    checkClient
    Screen.game.scene = Screen.houseScene(number)
  
    return true
  }
}