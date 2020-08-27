
class Moves(){

    val pawnMove = setOf<List<Int>>(listOf<Int>(1,0))
    val pawnStartMove = setOf<List<Int>>(listOf<Int>(1,0),  listOf(2,0))
    val knightMove    = setOf<List<Int>>(listOf<Int>(1,2),  listOf(2,1),  listOf<Int>(-1,2),  listOf(-2,1),
        listOf<Int>(1,-2), listOf(2,-1), listOf<Int>(-1,-2), listOf(-2,-1))
    var rookMove      = mutableSetOf<List<Int>>()

    init{
        rookMove.addAll(rookMove())
    }

    var bishopMove  = mutableSetOf<List<Int>>()

    init {
        bishopMove.addAll(bishopMove())
    }

    var queenMove = mutableSetOf<List<Int>>()

    init {
        queenMove.addAll(bishopMove())
        queenMove.addAll(rookMove())
    }

    val kingMove = setOf<List<Int>>(listOf(1,0), listOf(0,1),   listOf(-1,0), listOf(0,-1),
        listOf(1,1), listOf(-1,-1), listOf(-1,1), listOf(1,-1))

    fun getMove(type: Char?): Set<List<Int>>{
        when (type){
            'K' -> return kingMove
            'Q' -> return queenMove
            'R' -> return rookMove
            'B' -> return bishopMove
            'N' -> return knightMove
            'P' -> return pawnMove
            else -> return kingMove
        }
    }


}

fun rookMove(): MutableSet<List<Int>>{
    val moves = mutableSetOf<List<Int>>()
    for (i in 1..7){
        val x1 = listOf<Int>(i,0)
        val x2 = listOf<Int>(-i,0)
        val y1 = listOf<Int>(0,i)
        val y2 = listOf<Int>(0,-i)
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
        val xx = listOf<Int>(i,i)
        val xy = listOf<Int>(i,-i)
        val yx = listOf<Int>(-i,i)
        val yy = listOf<Int>(-i,-i)
        moves.add(xx)
        moves.add(xy)
        moves.add(yx)
        moves.add(yy)
    }
    return moves
}

class Move(private val squareFrom: Square, private val squareTo: Square){

    fun getSquareFrom(): Square{
        return this.squareFrom
    }

    fun getSquareTo(): Square{
        return this.squareTo
    }

}
