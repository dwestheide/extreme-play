package lib.game

import akka.actor._

import akka.event.LoggingReceive
import play.api.libs.concurrent.Akka
import play.api.Play.current

object GameProtocol {
  case class PlayerRegistered(name: String, url: String, uuid: String)
  case object GetRegisteredPlayers
  case class PlayerUnregistered(name: String, uuid: String)
}

object Game {
  val ref: ActorRef = Akka.system.actorOf(Props[Game])
  val history: ActorRef = Akka.system.actorOf(Props[History])
  val score: ActorRef = Akka.system.actorOf(Props[Scorer])
}

class Game extends Actor with ActorLogging {
  import GameProtocol._
  private[game] var playersByName = Map.empty[String, ActorPath]
  override def receive = LoggingReceive {
    case PlayerRegistered(name, url, uuid) =>
      if (!playersByName.contains(name)){
      	val actorPlayer = context.actorOf(Player.props(name, url))
      	playersByName += (name -> actorPlayer.path)
      	actorPlayer ! PlayerProtocol.GameStarted 
      } else {
      	//player already registered, what should we do???
      }

    case PlayerUnregistered(name, uuid) =>
    	if (!playersByName.contains(name)){
	      	//player is not registered, what should we do???
      } else {
      		val actorPath = playersByName(name)
      		playersByName -= name
      		context.actorSelection(actorPath) ! PlayerProtocol.KillYourself
      }

    case GetRegisteredPlayers =>
    	  sender ! playersByName.keySet 

  }
}
