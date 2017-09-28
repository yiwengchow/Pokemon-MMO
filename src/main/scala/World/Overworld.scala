package World

import scalafx.Includes._
import scalafx.scene.image.{ImageView, Image}

class OverworldMovable(x : Int, y : Int, item : String) extends Movable {
  
  item match{
    case "empty" => resultImage = getImageView("World/Items/Overworld/empty.png",25,25)
    case "field" => resultImage = getImageView("World/Items/Overworld/field.png",25,25)
    case "houseTiles" => resultImage = getImageView("World/Items/House/houseTiles.png",25,25)
    case "path" => resultImage = getImageView("World/Items/Overworld/path.png",25,25)
    case "desert" => resultImage = getImageView("World/Items/Overworld/desert.png",25,25)
    case "ladder" => resultImage = getImageView("World/Items/Overworld/ladder.png",25,25)
    case "stairs" => resultImage = getImageView("World/Items/Overworld/stairs.png",25,25)
  }
  
  setXY(x,y)
}

class OverworldImmovable(x : Int, y : Int, item : String) extends Immovable {
  item match{
    case "water" => resultImage = getImageView("World/Items/Overworld/water.png",25,25)
    case "fence" => resultImage = getImageView("World/Items/Overworld/fence.png",25,25)
    case "exitPortal" => resultImage = getImageView("World/Items/House/exit.png",25,25)
    case "mapPortal" => resultImage = getImageView("World/Items/House/exit.png",25,25)
    case "fire" => resultImage = getImageView("World/Items/Overworld/fire.png",25,25)
    case "hole" => resultImage = getImageView("World/Items/Overworld/hole.png",25,25)
    case "tree" => resultImage = getImageView("World/Items/Overworld/tree.png",25,25)
    
    case "hillTop" => resultImage = getImageView("World/Items/Overworld/hillTop.png",25,10)
    case "hillLeft" => resultImage = getImageView("World/Items/Overworld/hillLeft.png",10,25)
    case "hillRight" => resultImage = getImageView("World/Items/Overworld/hillRight.png",10,25)
    case "hill" => resultImage = getImageView("World/Items/Overworld/hill.png",25,25)
    
    case "house0" => resultImage = getImageView("World/Items/House/house0.png",150,100)
    case "house1" => resultImage = getImageView("World/Items/House/house1.png",150,100)
    case "house2" => resultImage = getImageView("World/Items/House/house2.png",150,100)
    case "house3" => resultImage = getImageView("World/Items/House/house3.png",150,100)
    case "house4" => resultImage = getImageView("World/Items/House/house4.png",150,100)
    case "house5" => resultImage = getImageView("World/Items/House/house5.png",150,100)
    case "house6" => resultImage = getImageView("World/Items/House/house6.png",25,25)
    case "house7" => resultImage = getImageView("World/Items/House/house7.png",150,100)
    
    case "desertNPC" => resultImage = getImageView("World/Items/NPC/desertNPC.png",25,25)
    case "beachNPC" => resultImage = getImageView("World/Items/NPC/beachNPC.png",25,25)
    case "coderNPC" => resultImage = getImageView("World/Items/NPC/coderNPC.png",25,25)
    case "scientistNPC" => resultImage = getImageView("World/Items/NPC/scientistNPC.png",25,25)
    case "villageGirlNPC" => resultImage = getImageView("World/Items/NPC/villageGirlNPC.png",25,25)
    case "villageGuyNPC" => resultImage = getImageView("World/Items/NPC/villageGuyNPC.png",25,25)
    
    case "bed" => resultImage = getImageView("World/Items/House/bed.png",100,150)
  }
  
  setXY(x,y)
}