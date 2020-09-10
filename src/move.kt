
class Moves {

    private val pawnMove = setOf(listOf(1,0))
    private val knightMove    = setOf(listOf(1,2),  listOf(2,1),  listOf(-1,2),  listOf(-2,1),
        listOf(1,-2), listOf(2,-1), listOf(-1,-2), listOf(-2,-1))
    private var rookMove      = mutableSetOf<List<Int>>()

    init{
        rookMove.addAll(rookMove())
    }

    private var bishopMove  = mutableSetOf<List<Int>>()

    init {
        bishopMove.addAll(bishopMove())
    }

    private var queenMove = mutableSetOf<List<Int>>()

    init {
        queenMove.addAll(bishopMove())
        queenMove.addAll(rookMove())
    }

    private val kingMove = setOf(listOf(1,0), listOf(0,1),   listOf(-1,0), listOf(0,-1),
        listOf(1,1), listOf(-1,-1), listOf(-1,1), listOf(1,-1))

    fun getMove(type: Char?): Set<List<Int>>{
        return when (type){
            'K' -> kingMove
            'Q' -> queenMove
            'R' -> rookMove
            'B' -> bishopMove
            'N' -> knightMove
            else -> pawnMove
        }
    }
}

fun rookMove(): MutableSet<List<Int>>{
    val moves = mutableSetOf<List<Int>>()
    for (i in 1..7){
        val x1 = listOf(i,0)
        val x2 = listOf(-i,0)
        val y1 = listOf(0,i)
        val y2 = listOf(0,-i)
        moves.add(x1)
        moves.add(x2)
        moves.add(y1)
        moves.add(y2)
    }
    return moves
}

fun bishopMove(): MutableSet<List<Int>>{
    val moves = mutableSetOf<List<Int>>()
    for (i in 1..7){
        val xx = listOf(i,i)
        val xy = listOf(i,-i)
        val yx = listOf(-i,i)
        val yy = listOf(-i,-i)
        moves.add(xx)
        moves.add(xy)
        moves.add(yx)
        moves.add(yy)
    }
    return moves
}

class Move(val squareFrom: Square, val squareTo: Square, val special: String? = null)