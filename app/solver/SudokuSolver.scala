package solver

object SudokuSolver {
  val SUDOKU_SIZE = 9
  assume(math.sqrt(SUDOKU_SIZE).toInt == math.sqrt(SUDOKU_SIZE))
  /**
   * backtracking algorithm for constraint satisfaction problem (sudoku)
   *
   * @param sudoku - sudoku to be solved
   * @return None if solution doesn't exist, else Some() with the solution
   */
  def solveSudoku(sudoku: List[List[Int]]): Option[List[List[Int]]] = {

    /**
     * Checks if it's possible to fill in the number to sudoku
     */
    def isPossible(sudoku: List[List[Int]], tile: (Int, Int), value: Int): Boolean = {
      def column: List[Int] = sudoku.map(row => row(tile._2))

      def row: List[Int] = sudoku(tile._1)

      def square: List[Int] = {
        val squareSize = math.sqrt(SUDOKU_SIZE).toInt
        sudoku
          .slice(tile._1 / squareSize * squareSize, tile._1 / squareSize * squareSize + squareSize)
          .flatMap(row => row.slice((tile._2 / squareSize) * squareSize, (tile._2 / squareSize) * squareSize + squareSize))
      }

      def allDifferent(tiles: List[Int]): Boolean = {
        tiles.filter(_ != 0).distinct.size == tiles.count(_ != 0)
      }

      allDifferent(column :+ value) && allDifferent(row :+ value) && allDifferent(square :+ value)
    }

    /**
     * Add value to sudoku
     */
    def addValue(sudoku: List[List[Int]], tile: (Int, Int), value: Int): List[List[Int]] = {
      (for (row <- sudoku.indices)
        yield if (row != tile._1) sudoku(row) else sudoku(row).patch(tile._2, Seq(value), 1)
        ).toList
    }

    /**
     * Get indices of the first empty tile in sudoku
     */
    def emptyTile(sudoku: List[List[Int]]): Option[(Int, Int)] = {
      (for (row <- 0 until SUDOKU_SIZE;
            column <- 0 until SUDOKU_SIZE
            if sudoku(row)(column) == 0)
        yield (row, column))
        .headOption
    }

    /**
     * Check if fully filled sudoku is valid
     */
    def isFullSudokuValid(sudoku: List[List[Int]]): Option[List[List[Int]]] = {
      val valuePossible = for (row <- 0 until SUDOKU_SIZE;
                               column <- 0 until SUDOKU_SIZE)
        yield isPossible(sudoku, (row, column), 0)
      if (valuePossible.forall(_ == true))
        Some(sudoku)
      else
        None
    }

    /**
     * This is where the backtracking algorithm happens
     */
    def solveRecursive(sudoku: List[List[Int]]): Option[List[List[Int]]] = {
      emptyTile(sudoku) match {
        case Some((row, column)) => {
          (1 to SUDOKU_SIZE).toStream
            .map { number =>
              if (isPossible(sudoku, (row, column), number))
                solveRecursive(addValue(sudoku, (row, column), number))
              else
                None
            }.collectFirst { case Some(x) => x }
        }
        case None => Some(sudoku)
      }
    }

    emptyTile(sudoku) match {
        // empty value exists -> try to find solution
      case Some(_) => solveRecursive(sudoku)
        // sudoku is full -> check if is valid
      case None => isFullSudokuValid(sudoku)
    }
  }

  /**
   * Check if sudoku is valid, ignoring known mistakes
   * @param sudoku sudoku to check
   * @param mistakes list of known mistakes, will be ignored in the computation
   * @return true of the sudoku is valid, ignoring known mistakes, else false
   */
  def isValid(sudoku: List[List[Int]], mistakes: List[(Int, Int)]): Boolean = {
    def removeKnownMistakes(): List[List[Int]] = {
      for (row <- (0 until SUDOKU_SIZE).toList)
        yield for (column <- (0 until SUDOKU_SIZE).toList)
          yield if (mistakes.contains((row, column))) 0 else sudoku(row)(column)
    }

    solveSudoku(removeKnownMistakes()) match {
      case Some(_) => true
      case None => false
    }
  }

  /**
   * Checks if sudoku is fully and correctly solved
   * @param sudoku sudoku to be checked
   * @param mistakes known mistakes
   * @return true if there are no mistakes and no empty values
   */
  def isSolved(sudoku: List[List[Int]], mistakes: List[(Int, Int)]): Boolean = {
    // no mistakes and all values filled in
    mistakes.isEmpty && !sudoku.flatten.contains(0)
  }

  /**
   * Checks if sudoku is valid for save, ie. there are no mistakes, there is at least one tile empty and a solution exists
   * @param sudoku sudoku to be checked
   * @param mistakes known mistakes
   * @return true if sudoku is valid for save
   */
  def canBeSaved(sudoku: List[List[Int]], mistakes: List[(Int, Int)]): Boolean = {
    mistakes.isEmpty && sudoku.flatten.contains(0) &&
      (solveSudoku(sudoku) match {
        case Some(_) => true
        case None => false
      })
  }
}

