package World.Controller

import scalafx.scene.input._
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.image._
import scalafx.scene.layout._
import scalafx.Includes._
import scalafx.beans.property._
import scala.util.Random
import javafx.beans.value._
import scalafxml.core.macros.sfxml
import World._
import Messages._
import OutMessages._
import javafx.beans.value._
import javafx.application.Platform
import WorldObject._

@sfxml
class LoginController (
    private val IdText: TextField,
    private val FailLabel: Label,
    private val PassText: PasswordField,
    private val LoginButton: Button,
    private val NewButton:Button,
    private val LoadingImage:ImageView,
    private val PasswordLabel : Label,
    private val IdLabel : Label){
  
  FailLabel.text.value = "ID OR PASSWORD IS INCORRECT!!\nUSERID HAS BEEN USED (IF REGISTERING)"
  FailLabel.visible.bind(LoginStatics.loginRegFailed)
  
  LoginStatics.failBoolean.addListener(new ChangeListener[java.lang.Boolean]{
    override def changed(obVal : ObservableValue[_ <: java.lang.Boolean], oldVal : java.lang.Boolean, newVal : java.lang.Boolean){
      if (LoginStatics.failBoolean.value){
        LoginButton.visible = true
        NewButton.visible = true
        PassText.visible = true
        IdText.visible = true
        PasswordLabel.visible = true
        IdLabel.visible = true
        LoadingImage.visible = false
        LoginStatics.loginRegFailed.value = true
        LoginStatics.failBoolean.value = false
      }
    }
  })
  
  if(Random.nextBoolean){
    LoadingImage.image = new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Overworld/loading.gif"))
  }
  else{
    LoadingImage.image = new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Overworld/loading1.gif"))
  }
  LoadingImage.visible = false
  
  def OnKeyPressed {
    LoginStatics.loginRegFailed.value = false
  }
  
  def OnClick(e: MouseEvent){
    val button = new Button(e.getSource.asInstanceOf[javafx.scene.control.Button])
    val id = button.id.value
    id match{
      case "LoginButton" => {
        Screen.senderActor ! Login(IdText.text.value, PassText.text.value)
        LoginButton.visible = false
        NewButton.visible = false
        PassText.visible = false
        IdText.visible = false
        PasswordLabel.visible = false
        IdLabel.visible = false
        LoadingImage.visible = true
      }
      case "NewButton" => {
        Screen.senderActor ! Register(IdText.text.value, PassText.text.value)
        LoginButton.visible = false
        NewButton.visible = false
        PassText.visible = false
        IdText.visible = false
        PasswordLabel.visible = false
        IdLabel.visible = false
        LoadingImage.visible = true
      }
      
      case _ => 
    }
  }
}

object LoginStatics{
  val failBoolean = BooleanProperty(false)
  val loginRegFailed = BooleanProperty(false)
}