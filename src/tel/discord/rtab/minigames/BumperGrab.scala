package tel.discord.rtab.minigames

import java.util

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.Random

class BumperGrab extends MiniGame {

  import tel.discord.rtab.minigames.BumperGrab._

  private val NAME: String = "Bumper Grab"
  private var boardWidth: Int = 0
  private var boardHeight: Int = 0
  private var board: Array[Array[Space]] = Array.empty
  private var player_X: Int = 0
  private var player_Y: Int = 0
  private var isFirstMove = true
  private var gameOver: Boolean = false
  private var winnings = 0
  private var maxWinnings = 0

  /**
    * Initialises the variables used in the minigame and prints the starting messages.
    *
    * @return A list of messages to send to the player.
    */
  override def initialiseGame(): util.LinkedList[String] = {
    generateBoard()
    isFirstMove = true
    gameOver = false
    winnings = 0
    maxWinnings = board.flatten.collect{
      case Cash(amount) => amount
    }.sum

    new util.LinkedList(List(
      "In Bumper Grab, your objective is to navigate an icy floating platform.",
      "Slide around, bounce off bumpers, and grab as much cash as you can!",
      "You're represented as an 'X', and exits are represented as 'O's.",
      "Other spaces are either cash or bumpers, but you won't know which until you hit them.",
      "Each move, you'll pick a direction (UP, LEFT, RIGHT, or DOWN), and " +
        "you'll slide in that direction until you hit a space you haven't been to.",
      "If it's cash, you grab it, and choose a new direction.",
      "If it's a bumper, you'll be pushed in a new direction.",
      "And if it's an exit, you're allowed to EXIT and escape with your loot!",
      "Or you can move again, but you won't be able to use that same exit later",
      "Oh, and if you slide off the edge, you fall to your doom and lose everything!",
      "Good luck!",
      drawScoreboard
    ).asJava)
  }

  private def generateBoard(): Unit = {
    //TODO: Alternate board setups
    val inner = Random.shuffle(
      Vector(
        Bumper(Left), Bumper(Left), Bumper(Left),
        Bumper(Up), Bumper(Up), Bumper(Up),
        Bumper(Right), Bumper(Right), Bumper(Right),
        Bumper(Down), Bumper(Down), Bumper(Down),
        Cash(10000), Cash(20000), Cash(20000),
        Cash(50000), Cash(50000), Cash(50000),
        Cash(75000), Cash(100000)
      ))

    val outer = Random.shuffle(
      Vector(
        Bumper(Left), Bumper(Left), Bumper(Left),
        Bumper(Up), Bumper(Up), Bumper(Up),
        Bumper(Right), Bumper(Right), Bumper(Right),
        Bumper(Down), Bumper(Down), Bumper(Down),
        Cash(50000), Cash(50000), Cash(50000),
        Cash(50000), Cash(75000), Cash(75000),
        Cash(75000), Cash(100000), Cash(100000),
        Cash(250000), Cash(250000), Cash(500000)
      ))

    boardWidth = 7
    boardHeight = 7
    player_X = 3
    player_Y = 3

    board = Array(
      Array(outer(0),  outer(1),  outer(2),  outer(3),  outer(4),  outer(5),  outer(6)),
      Array(outer(7),  Exit,      inner(0),  inner(1),  inner(2),  Exit,      outer(8)),
      Array(outer(9),  inner(3),  inner(4),  inner(5),  inner(6),  inner(7),  outer(10)),
      Array(outer(11), inner(8),  inner(9),  Ice,       inner(10), inner(11), outer(12)),
      Array(outer(13), inner(12), inner(13), inner(14), inner(15), inner(16), outer(14)),
      Array(outer(15), Exit,      inner(17), inner(18), inner(19), Exit,      outer(16)),
      Array(outer(17), outer(18), outer(19), outer(20), outer(21), outer(22), outer(23)),
    )
  }

  /**
    * Takes the next player input and uses it to play the next "turn" - up until the next input is required.
    *
    * @param pick The next input sent by the player.
    * @return A list of messages to send to the player.
    */
  override def playNextTurn(pick: String): util.LinkedList[String] = {
    pick.toUpperCase match {
      case "U" | "UP" | "N" | "NORTH" => new util.LinkedList(("UP..." +: move(Up)).asJava)
      case "D" | "DOWN" | "S" | "SOUTH" => new util.LinkedList(("DOWN..." +: move(Down)).asJava)
      case "L" | "LEFT" | "W" | "WEST" => new util.LinkedList(("LEFT..." +: move(Left)).asJava)
      case "R" | "RIGHT" | "E" | "EAST" => new util.LinkedList(("RIGHT..." +: move(Right)).asJava)
      case "QUIT" | "EXIT" | "STOP" =>
        if (getSpace(player_X, player_Y) == Exit) escape
        else new util.LinkedList(List("There's no exit there, you gotta pick a direction!").asJava)
      case _ => new util.LinkedList[String]()
    }
  }

  //Turn the current space to ice and move past in the specified direction, continuing across ice and
  //bouncing off bumpers until hitting a non-ice, non-bumper space. Builds up drawings of the board
  //with the path overlaid after each bumper, and returns a list of strings to be sent to the player
  @tailrec
  private def move(direction: Direction,
                   currentSegment: Seq[(Int, Int)] = Vector((player_X, player_Y)),
                   pathDrawings: Seq[Seq[String]] = Vector.empty): Seq[String] = {

    turnToIce(player_X, player_Y)
    player_X += direction.deltaX
    player_Y += direction.deltaY

    getSpace(player_X, player_Y) match {
      case Ice =>
        move(direction, currentSegment :+ (player_X, player_Y), pathDrawings)
      case Bumper(bumperDir) =>
        val mapDrawing = drawBoard(showPlayer = false)
        val nextDrawing =
          if (direction.deltaX != 0)
            drawHorizontalLine(mapDrawing, currentSegment :+ (player_X, player_Y), bumperDir.char)
          else drawVerticalLine(mapDrawing, currentSegment :+ (player_X, player_Y), bumperDir.char)
        move(bumperDir, Vector((player_X, player_Y)), pathDrawings :+ nextDrawing)
      case Cash(amount) =>
        isFirstMove = false
        winnings += amount

        val mapDrawing = drawBoard(showPlayer = false)
        val cashDrawing =
          if(direction.deltaX != 0)
            drawHorizontalLine(mapDrawing, currentSegment :+ (player_X, player_Y), '$')
          else
            drawVerticalLine(mapDrawing, currentSegment :+ (player_X, player_Y), '$')

        bumperMessages(pathDrawings) :+
          ("```" +: cashDrawing :+ "```" :+ ("**$" + amount + "**")).mkString("\n") :+
            drawScoreboard

      case Exit =>
        isFirstMove = false

        bumperMessages(pathDrawings) :+
          "Reached an exit! You can EXIT, or keep going!" :+
          drawScoreboard

      case Hole =>
        //TODO: Consider showing an additional drawing of the path going off the edge?
        gameOver = true
        if (isFirstMove) {
          winnings = 100

          bumperMessages(pathDrawings) :+
            "You fell off on your first move?! Jeez, have $100 on me." :+
            drawScoreboard
        } else {
          winnings = 0

          bumperMessages(pathDrawings) :+
            "You fell off!"
        }
    }
  }

  private def drawBoard(showPlayer: Boolean): Seq[String] = {
    (0 until boardHeight).map { y =>
      (0 until boardWidth).map { x =>
        if (showPlayer && x == player_X && y == player_Y) 'X'
        else getSpace(x, y) match {
          case Cash(_) | Bumper(_) => '?'
          case Exit => 'O'
          case Ice => '-'
          case Hole => ' '
        }
      }.mkString(" ") //Add a space between each character
    }
  }

  def drawScoreboard: String = (
    "```" +:
      " BUMPER GRAB" +: //TODO: Center this based on board width
      drawBoard(showPlayer = true) :+
      ("Total: $ " + winnings) :+
      ("      /$ " + maxWinnings) :+ //TODO: Lay this out prettier
      "```"
    ).mkString("\n")

  def bumperMessages(boardDrawings: Seq[Seq[String]]): Seq[String] = boardDrawings.map { drawing =>
    ("```" +:
      drawing :+
      "```" :+
      ("**" + List("BING", "PING", "PONG", "BOING")(Random.nextInt(4)) + "**")
      ).mkString("\n")
  }

  //Overlay a horizontal segment of a path, ending in a given character, on a drawing of the board
  private def drawHorizontalLine(boardDrawing: Seq[String],
                                 segment: Seq[(Int, Int)],
                                 terminatingChar: Char): Seq[String] = {

    //There's a space between each character, so x=n on the board maps to x=2n on the drawing
    def scale(x: Int) = 2 * x

    val xStart = scale(segment.head._1)
    val xEnd = scale(segment.last._1)
    //Assume all y-values are the same
    val row = segment.head._2

    (0 until boardHeight).map { y =>
      (0 to scale(boardWidth - 1)).map { x =>
        if (y == row && ((x >= xStart && x <= xEnd) || (x <= xStart && x >= xEnd))) {
          if (x == xEnd) terminatingChar
          else '-'
        } else boardDrawing(y)(x)
      }.mkString
    }
  }

  //Overlay a horizontal segment of a path, ending in a given character, on a drawing of the board
  private def drawVerticalLine(boardDrawing: Seq[String],
                               segment: Seq[(Int, Int)],
                               terminatingChar: Char): Seq[String] = {

    //There's a space between each character, so x=n on the board maps to x=2n on the drawing
    def scale(x: Int) = 2 * x

    val yStart = segment.head._2
    val yEnd = segment.last._2
    //Assume all x-values are the same
    val col = scale(segment.head._1)

    (0 until boardHeight).map { y =>
      (0 to scale(boardWidth - 1)).map { x =>
        if (x == col && ((y >= yStart && y <= yEnd) || (y <= yStart && y >= yEnd))) {
          if (y == yEnd) terminatingChar
          else '|'
        } else boardDrawing(y)(x)
      }.mkString
    }
  }

  private def escape: util.LinkedList[String] = {
    gameOver = true
    new util.LinkedList(List(
      if (winnings > 0) "You made it out!"
      else "You made it out...with no cash? You know there was cash there, right?!"
    ).asJava)
  }

  //Get the board space corresponding to an x and y position, or Hole if it's out of bounds
  private def getSpace(x: Int, y: Int) = (for {
    row <- board.lift(y)
    space <- row.lift(x)
  } yield space).getOrElse(Hole)

  //Change a space on the bord to ice
  private def turnToIce(x: Int, y: Int) = {
    board(y)(x) = Ice
  }

  /**
    * Returns true if the minigame has ended
    */
  override def isGameOver: Boolean = gameOver

  /**
    * Returns an int containing the player's winnings, pre-booster.
    * If game isn't over yet, should return lowest possible win (usually 0) because player timed out for inactivity.
    */
  override def getMoneyWon: Int = if (gameOver) winnings else 0

  /**
    * Returns true if the game is a bonus game (and therefore shouldn't have boosters or winstreak applied)
    * Returns false if it isn't (and therefore should have boosters and winstreak applied)
    */
  override def isBonusGame: Boolean = false

  /**
    * Calculates the next choice a bot should make in the minigame.
    *
    * @return The next input the bot should send to the minigame.
    */
  override def getBotPick: String = {
    //Exit if we can, otherwise just pick a direction at random lol
    if (board(player_X)(player_Y) == Exit && winnings > 0) "Exit"
    else Vector("LEFT", "RIGHT", "UP", "DOWN")(Random.nextInt(4))
  }
  override def toString: String = NAME
}

object BumperGrab {

  sealed trait Direction {
    def char: Char
    def deltaX: Int
    def deltaY: Int
  }

  case object Left extends Direction {
    override def char: Char = '<'
    override def deltaX: Int = -1
    override def deltaY: Int = 0
  }

  case object Right extends Direction {
    override def char: Char = '>'
    override def deltaX: Int = 1
    override def deltaY: Int = 0
  }

  case object Up extends Direction {
    override def char: Char = '^'
    override def deltaX: Int = 0
    override def deltaY: Int = -1
  }

  case object Down extends Direction {
    override def char: Char = 'v'
    override def deltaX: Int = 0
    override def deltaY: Int = 1
  }

  sealed abstract class Space

  case class Cash(amount: Int) extends Space

  case class Bumper(direction: Direction) extends Space

  case object Exit extends Space

  case object Ice extends Space

  case object Hole extends Space

}