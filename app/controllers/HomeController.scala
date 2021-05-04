package controllers

import controllers.GameMode.{CreateGameMode, PlayGameMode}
import javax.inject._
import models.Nickname
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import database.Database
import solver.SudokuSolver

import scala.util.Random

sealed abstract case class GameMode(value: String)
object GameMode{
  object PlayGameMode extends GameMode("play")
  object CreateGameMode extends GameMode("create")

  val values: Seq[GameMode] = Seq(PlayGameMode, CreateGameMode)
}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: MessagesControllerComponents)(implicit assetsFinder: AssetsFinder)
  extends MessagesAbstractController(cc) {

  def nicknameForm = Form(mapping("Nickname" -> nonEmptyText)(Nickname.apply)(Nickname.unapply))

  def sudokuForm: Form[List[List[Int]]] =
    Form(
      "tile" -> list(
        list(default(number(min = 0, max = SudokuSolver.SUDOKU_SIZE), 0))
      )
    )

  /**
   * Set values at given indices to 0
   *
   * @param sudoku sudoku to be changed
   * @param indicesToReset indices which tiles should be reseted to 0
   */
  def resetValues(sudoku: List[List[Int]], indicesToReset: List[(Int, Int)]): List[List[Int]] = {
    for(row <- sudoku.indices.toList)
      yield for (column <- sudoku(row).indices.toList)
        yield if (indicesToReset.contains((row, column))) 0 else sudoku(row)(column)
  }

  /**
   * Get list of indices where the old sudoku and new sudoku have different values
   */
  def differentIndices(oldSudoku: List[List[Int]], newSudoku: List[List[Int]]): List[(Int, Int)] = {
    (for{row <- oldSudoku.indices
        column <- oldSudoku(row).indices
        if oldSudoku(row)(column) != newSudoku(row)(column)}
      yield (row, column)
      ).toList
  }

  /**
   * Check if user's nick is in the database
   */
  def userValid(nick: String): Boolean = {
    Database.userDB.contains(nick)
  }

  /**
   * Render index screen
   */
  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index(nicknameForm))
  }

  /**
   * Add user to db and redirect to home screen for logged users
   */
  def createNickname: Action[AnyContent] = Action { implicit request =>
    nicknameForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.index(formWithErrors)),
      nickname => {
        Database.userDB += nickname.nick
        Redirect(controllers.routes.HomeController.loggedScreen(nickname.nick))
      })
  }

  /**
   * Renders home screen for logged users and clears player's last played sudoku
   *
   * @param nick user's nick
   */
  def loggedScreen(nick: String): Action[AnyContent] = Action { implicit request =>
    if (userValid(nick)) {
      Database.sudokuUserDB = Database.sudokuUserDB - nick
      Database.userMistakesDB = Database.userMistakesDB - nick
      Ok(views.html.loggedScreen(nick))
    } else
      Redirect(controllers.routes.HomeController.index())
  }

  /**
   * Save created sudoku if is valid, else do nothing
   *
   * @param nick creator's nick
   */
  def saveSudoku(nick: String): Action[AnyContent] = Action { implicit request =>
    if (userValid(nick)) {
      if (SudokuSolver.canBeSaved(Database.sudokuUserDB(nick), Database.userMistakesDB(nick))) {
        Database.sudokuDB = Database.sudokuDB :+ Database.sudokuUserDB(nick)
        Redirect(controllers.routes.HomeController.successScreen(nick))
      } else
        Redirect(controllers.routes.HomeController.create(nick))
    } else
      Redirect(controllers.routes.HomeController.index())
  }

  /**
   * Render success screen and clear user's last sudoku
   *
   * @param nick user's nick
   */
  def successScreen(nick: String): Action[AnyContent] = Action { implicit request =>
    if (userValid(nick)) {
      Database.sudokuUserDB = Database.sudokuUserDB - nick
      Database.userMistakesDB = Database.userMistakesDB - nick
      Ok(views.html.successScreen(nick, "Success!"))
    } else
      Redirect(controllers.routes.HomeController.index())
  }

  /**
   * Add user and the sudoku to the database, add empty list to user's mistakes database
   */
  def sudokuForNewUser(nick: String, sudoku: List[List[Int]]): Unit = {
    Database.sudokuUserDB += (nick -> sudoku)
    Database.userMistakesDB += (nick -> List.empty[(Int, Int)])
  }

  /**
   * Choose random sudoku for a new user, for known users decide if the sudoku is solved or not, and render appropriate screen
   *
   * @param nick user's nick
   */
  def playSudoku(nick: String): Action[AnyContent] = Action { implicit request =>
    if (userValid(nick)) {
      Database.sudokuUserDB.get(nick) match {
        case Some(sudoku) => {
          if (SudokuSolver.isSolved(sudoku, Database.userMistakesDB(nick))) {
            Redirect(controllers.routes.HomeController.successScreen(nick))
          }
          else
            Ok(views.html.playScreen(nick, sudoku, sudokuForm, Database.userMistakesDB(nick)))
        }
        case None => {
          val random = new Random
          sudokuForNewUser(nick, Database.sudokuDB(random.nextInt(Database.sudokuDB.length)))
          Ok(views.html.playScreen(nick, Database.sudokuUserDB(nick), sudokuForm, Database.userMistakesDB(nick)))
        }
      }
    } else
      Redirect(controllers.routes.HomeController.index())
  }

  /**
   * Create empty grid for new user, for known users render screen for creating
   *
   * @param nick user's nick
   */
  def create(nick: String): Action[AnyContent] = Action { implicit request =>
    if (userValid(nick)) {
      Database.sudokuUserDB.get(nick) match {
        case Some(sudoku) => {
          Ok(views.html.create(nick, sudoku, sudokuForm, Database.userMistakesDB(nick)))
        }
        case None => {
          val emptySudoku = List.fill(SudokuSolver.SUDOKU_SIZE) {
            List.fill(SudokuSolver.SUDOKU_SIZE)(0)
          }
          sudokuForNewUser(nick, emptySudoku)
          Ok(views.html.create(nick, emptySudoku, sudokuForm, Database.userMistakesDB(nick)))
        }
      }
    } else
      Redirect(controllers.routes.HomeController.index())
  }

  /**
   * Insert values to the sudoku. If the value is wrong, return form with errors. Render play or create screen.
   *
   * @param nick   user's nick
   * @param action play or create
   */
  def insert(nick: String, action: GameMode)(implicit e: play.api.mvc.QueryStringBindable[controllers.GameMode]): Action[AnyContent] = Action { implicit request =>
    if (userValid(nick)) {
      sudokuForm.bindFromRequest().fold(
        formWithErrors => {
          action match {
            case PlayGameMode => BadRequest(views.html.playScreen(nick, Database.sudokuUserDB(nick), formWithErrors))
            case CreateGameMode=> BadRequest(views.html.create(nick, Database.sudokuUserDB(nick), formWithErrors))
          }
        },
        newSudoku => {
          val changes = differentIndices(Database.sudokuUserDB(nick), newSudoku)
          // if a new value comes to the place where was a mistake, delete this tile from list of mistakes
          Database.userMistakesDB += (nick -> (Database.userMistakesDB(nick) diff changes))

          // if new sudoku is not valid, mark all the values as mistake and reset those values to 0
          if(!SudokuSolver.isValid(newSudoku, Database.userMistakesDB(nick))) {
            Database.userMistakesDB += (nick -> (Database.userMistakesDB(nick) ++ changes))
            Database.sudokuUserDB += (nick -> resetValues(newSudoku, Database.userMistakesDB(nick)))
          } else {
            Database.sudokuUserDB += (nick -> newSudoku)
          }

          action match {
            case PlayGameMode => Redirect(controllers.routes.HomeController.playSudoku(nick))
            case CreateGameMode => Redirect(controllers.routes.HomeController.create(nick))
          }
        }
      )
    } else
      Redirect(controllers.routes.HomeController.index())
  }
}


