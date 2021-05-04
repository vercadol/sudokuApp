package database

object Database {
  var sudokuDB = List(
    List(
      List(0, 3, 0, 0, 6, 2, 0, 7, 0),
      List(2, 7, 6, 8, 1, 5, 0, 0, 9),
      List(9, 0, 0, 0, 3, 0, 2, 5, 0),
      List(0, 0, 0, 5, 8, 0, 0, 0, 7),
      List(8, 0, 7, 0, 0, 0, 9, 0, 0),
      List(0, 2, 4, 0, 0, 0, 0, 0, 0),
      List(0, 4, 9, 6, 0, 0, 1, 3, 0),
      List(6, 1, 0, 7, 4, 0, 5, 0, 0),
      List(0, 0, 8, 2, 9, 0, 0, 6, 4),
    ),
    List(
      List(8, 2, 7,   1, 5, 4,   3, 9, 6),
      List(9, 6, 5,   3, 2, 7,   1, 4, 8),
      List(3, 4, 1,   6, 8, 9,   7, 5, 2),

      List(5, 9, 3,   4, 6, 8,   2, 7, 1),
      List(4, 7, 2,   5, 1, 3,   6, 8, 9),
      List(6, 1, 8,   9, 7, 2,   4, 3, 5),

      List(7, 8, 6,   2, 3, 5,   9, 1, 4),
      List(1, 5, 4,   7, 9, 6,   8, 2, 3),
      List(2, 3, 9,   8, 4, 1,   5, 6, 0),
    )
  )

  var userDB = scala.collection.mutable.ListBuffer.empty[String]

  var sudokuUserDB = Map.empty[String, List[List[Int]]]

  var userMistakesDB = Map.empty[String, List[(Int, Int)]]
}
