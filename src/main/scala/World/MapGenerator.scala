package World

class MapGenerator(baseNumber : Int, number : Int, mapType : String, battle : Int) extends Maps {
     
    var countX = 0
    var countY = 0
    var mapCounter = 0
    var battleVal = battle
    
    var stuff = Array[Stuff]()
    
    map = Maps.baseMapContainer(baseNumber)
    generate()
    
    if (mapType.equalsIgnoreCase("map")){
      map = Maps.mapContainer(number)
      generate()
    }
    
    else if (mapType.equalsIgnoreCase("house")){
      map = Maps.houseMapContainer(number)
      generate()
    }
    
    def generate(){
          
    countX = 0
    countY = 0
    mapCounter = 0
    
      for (y <- 0 to 19){
        for (x <- 0 to 36){
          
          map(mapCounter) match{
            case 0 => //stuff :+= new OverworldMovable(countX,countY,"empty")
            case 1 => stuff :+= new OverworldMovable(countX,countY,"field")
            case 2 => stuff :+= new OverworldMovable(countX,countY,"houseTiles")
            case 3 => stuff :+= new OverworldMovable(countX,countY,"desert")
            case 4 => stuff :+= new OverworldMovable(countX,countY,"path")
            case 5 => stuff :+= new OverworldMovable(countX,countY,"ladder")
            case 6 => stuff :+= new OverworldMovable(countX,countY,"stairs")
            case 10 => stuff :+= new OverworldImmovable(countX,countY,"water")
            case 11 => stuff :+= new OverworldImmovable(countX,countY,"fence")
            case 12 => stuff :+= new OverworldImmovable(countX,countY,"fire")
            case 14 => stuff :+= new OverworldImmovable(countX,countY,"hole")
            case 15 => stuff :+= new OverworldImmovable(countX,countY,"bed")
            case 16 => stuff :+= new OverworldImmovable(countX,countY,"tree")
            case 20 => stuff :+= new OverworldImmovable(countX,countY,"hillLeft")
            case 21 => stuff :+= new OverworldImmovable(countX,countY+15,"hillTop")
            case 22 => stuff :+= new OverworldImmovable(countX+15,countY,"hillRight")
            case 23 => stuff :+= new OverworldImmovable(countX,countY,"hill")
            case 100 => stuff :+= new Houses(countX,countY,0, 100, 0)
            case 101 => stuff :+= new Houses(countX,countY,1, 100, 100)
            case 102 => stuff :+= new Houses(countX,countY,2, 100, 100)
            case 103 => stuff :+= new Houses(countX,countY,3, 100, 100)
            case 104 => stuff :+= new Houses(countX,countY,4, 100, 100)
            case 105 => stuff :+= new Houses(countX,countY,5, 100, 100)
            case 106 => stuff :+= new Houses(countX,countY,6, 100, 100)
            case 107 => stuff :+= new Houses(countX,countY,7, 100, 100)
            case 50 => stuff :+= new MapPortal(countX,countY, 400, 400, 0)
            case 51 => stuff :+= new MapPortal(countX,countY, 200, 200, 1)
            case 52 => stuff :+= new MapPortal(countX,countY, 400, 200, 2)
            case 99 => stuff :+= new ExitPortal(countX,countY)
            case 40 => stuff :+= new NPC(countX, countY, "desertNPC")
            case 41 => stuff :+= new NPC(countX, countY, "beachNPC")
            case 42 => stuff :+= new NPC(countX, countY, "coderNPC")
            case 43 => stuff :+= new NPC(countX, countY, "scientistNPC")
            case 44 => stuff :+= new NPC(countX, countY, "villageGirlNPC")
            case 45 => stuff :+= new NPC(countX, countY, "villageGuyNPC")
          }
          countX += 25
          mapCounter+=1
        }
        countX = 0
        countY += 25
      }
    }
}