class Turn(
    val turn: Int, val player: Char,
    currentSquares: ArrayList<ArrayList<Square>>,
    possibleMoves: MutableSet<ArrayList<Square>>,
    val move: Move, input: String)
{

    val moves = mutableSetOf<ArrayList<ArrayList<Int>>>()

    init {
        val tempSquares = arrayListOf<ArrayList<Int>>()
        possibleMoves.forEach {
            it.forEach { square ->
                tempSquares.add(arrayListOf(square.col, square.rank))
            }
            moves.add(tempSquares)
        }
    }

    val squares = arrayListOf<ArrayList<ArrayList<Char?>>>()

    init {
        val tempSquares = arrayListOf<ArrayList<Char?>>()
        currentSquares.forEach {
            it.forEach { square ->
                tempSquares.add(arrayListOf(square.col.toChar(), square.rank.toChar(), square.getType()))
            }
            squares.add(tempSquares)
        }
    }

    val string = input

}

class History(val turnHistory: ArrayList<Turn>) {

    override fun toString(): String {
        var i = 0
        var result = ""
        this.turnHistory.forEach {
            if (it.turn % 2 != 0) {
                i++
                result += "$i. "
            }
            result += it.string + " "
        }
        return result
    }
}
