package World.Controller

import scalafx.scene.input.{MouseEvent}
import scalafx.scene.control.{TextField, Label, PasswordField, Button}
import scalafx.scene.image.{Image, ImageView}
import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scala.util.Random
import javafx.beans.value.{ChangeListener, ObservableValue}
import scalafxml.core.macros.sfxml
import World.{Screen, Messages, OutMessages, Sender}
import Messages._
import OutMessages._
import javafx.application.Platform
import WorldObject.Pokimon
import akka.actor.{Props, DeadLetter}

@sfxml
class LoginController (
    private val IdText: TextField,
    private val FailLabel: Label,
    private val PassText: PasswordField,
    private val LoginButton: Button,
    private val NewButton:Button,
    private val LoadingImage:ImageView,
    private val PasswordLabel : Label,
    private val IdLabel : Label,
    private val ipLabel : Label,
    private val ipText : TextField){
  
  FailLabel.text.bind(LoginStatics.loginFailText)
  ipText.text.value = Screen.ipAddress
  LoginStatics.loginFailText.value = ""
  
  LoginStatics.changeListener = new ChangeListener[java.lang.Boolean]{
    override def changed(obVal : ObservableValue[_ <: java.lang.Boolean], oldVal : java.lang.Boolean, newVal : java.lang.Boolean){
      if (LoginStatics.failBoolean.value){
        LoginButton.visible = true
        NewButton.visible = true
        PassText.visible = true
        IdText.visible = true
        PasswordLabel.visible = true
        IdLabel.visible = true
        LoadingImage.visible = false
        ipLabel.visible = true
        ipText.visible = true
        LoginStatics.failBoolean.value = false
        Screen.system.stop(Screen.senderActor)
      }
    }
  }
  
  LoginStatics.failBoolean.addListener(LoginStatics.changeListener)
  LoginStatics.loginFailText.value = ""
  
  if(Random.nextBoolean){
    LoadingImage.image = new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Overworld/loading.gif"))
  }
  else{
    LoadingImage.image = new Image(getClass.getClassLoader.getResourceAsStream("World/Items/Overworld/loading1.gif"))
  }
  
  LoadingImage.visible = false
  
  def OnClick(e: MouseEvent){
    val button = new Button(e.getSource.asInstanceOf[javafx.scene.control.Button])
    val id = button.id.value
    id match{
      case "LoginButton" => {
        if(ipText.text.value.isEmpty || PassText.text.value.isEmpty || IdText.text.value.isEmpty){
          FailLabel.text.value = "Make sure all fields are filled. Empty ID or password is not allowed"
        }
        else{
          Screen.ipAddress = ipText.text.value
          LoginStatics.loginFailText.value = ""
          Screen.senderActor = Screen.system.actorOf(Props[Sender], "SenderActor")
          Screen.system.eventStream.subscribe(Screen.senderActor, classOf[DeadLetter])
          Screen.senderActor ! Login(IdText.text.value, PassText.text.value)
          LoginButton.visible = false
          NewButton.visible = false
          PassText.visible = false
          IdText.visible = false
          PasswordLabel.visible = false
          IdLabel.visible = false
          ipLabel.visible = false
          ipText.visible = false
          LoadingImage.visible = true
        }
      }
      case "NewButton" => {
        if(ipText.text.value.isEmpty || PassText.text.value.isEmpty || IdText.text.value.isEmpty){
          FailLabel.text.value = "Make sure all fields are filled. Empty ID or password is not allowed"
        }
        else{
          Screen.ipAddress = ipText.text.value
          LoginStatics.loginFailText.value = ""
          Screen.senderActor = Screen.system.actorOf(Props[Sender], "SenderActor")
          Screen.system.eventStream.subscribe(Screen.senderActor, classOf[DeadLetter])
          Screen.senderActor ! Register(IdText.text.value, PassText.text.value)
          LoginButton.visible = false
          NewButton.visible = false
          PassText.visible = false
          IdText.visible = false
          PasswordLabel.visible = false
          IdLabel.visible = false
          ipLabel.visible = false
          ipText.visible = false
          LoadingImage.visible = true
        }
      }
      case _ => 
    }
  }
}

object LoginStatics{
  var failBoolean = BooleanProperty(false)
  val loginFailText = new StringProperty("")
  var changeListener : ChangeListener[java.lang.Boolean] = null
}