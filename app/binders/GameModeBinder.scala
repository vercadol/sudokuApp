package binders

import controllers.GameMode
import controllers.GameMode.{CreateGameMode, PlayGameMode}
import play.api.mvc.{PathBindable, QueryStringBindable}

object GameModeBinder {
  implicit def pathBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[GameMode] {
    override def bind(key: String, value: String): Either[String, GameMode] = {
      value match {
        case "play" => Right(PlayGameMode)
        case "create" => Right(CreateGameMode)
        case _ => Left("error: unvalid GameMode value")
      }
    }
    override def unbind(key: String, mode: GameMode): String = {
      stringBinder.unbind(key, mode.value)
    }
  }

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[GameMode] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, GameMode]] = {
      for {
        mode <- stringBinder.bind("mode", params)
      } yield {
        mode match {
          case Right("play") => Right(PlayGameMode)
          case Right("create") => Right(CreateGameMode)
          case _ => Left("Unable to bind GameMode")
        }
      }
    }

    override def unbind(key: String, mode: GameMode): String = {
      stringBinder.unbind("mode", mode.value)
    }
  }
}
