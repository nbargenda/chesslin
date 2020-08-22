
class Moves(){

    val pawnMove = setOf<List<Byte>>(listOf<Byte>(1,0))
    val pawnStartMove = setOf<List<Byte>>(listOf<Byte>(1,0),  listOf(2,0))
    val pawnEnPassant = setOf<List<Byte>>(listOf<Byte>(1,1),  listOf(1,-1))
    val knightMove    = setOf<List<Byte>>(listOf<Byte>(1,2),  listOf(2,1),  listOf<Byte>(-1,2),  listOf(-2,1),
        listOf<Byte>(1,-2), listOf(2,-1), listOf<Byte>(-1,-2), listOf(-2,-1))
    var rookMove      = mutableSetOf<List<Byte>>()

    init{
        rookMove.addAll(rookMove())
    }

    var bishopMove  = mutableSetOf<List<Byte>>()

    init {
        bishopMove.addAll(bishopMove())
    }

    var queenMove = mutableSetOf<List<Byte>>()

    init {
        queenMove.addAll(bishopMove())
        queenMove.addAll(rookMove())
    }

    val kingMove = setOf<List<Byte>>(listOf(1,0), listOf(0,1),   listOf(-1,0), listOf(0,-1),
        listOf(1,1), listOf(-1,-1), listOf(-1,1), listOf(1,-1))

    val castleShort = setOf<List<Byte>>(listOf(0,2), listOf(0,-2))
    val castleLong= setOf<List<Byte>>(listOf(0,-2), listOf(0,3))

    fun getMove(type: Char?): Set<List<Byte>>{
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

fun rookMove(): MutableSet<List<Byte>>{
    val moves = mutableSetOf<List<Byte>>()
    for (i in 1..7){
        val j = i.toByte()
        val x1 = listOf<Byte>(j,0)
        val x2 = listOf<Byte>((-j).toByte(),0)
        val y1 = listOf<Byte>(0,j)
        val y2 = listOf<Byte>(0, (-j).toByte())
        moves.add(x1)
        moves.add(x2)
        moves.add(y1)
        moves.add(y2)
    }
    return moves
}

fun bishopMove(): MutableSet<List<Byte>>{
    val moves = mutableSetOf<List<Byte>>()
    for (i in 1..7){
        val j = i.toByte()
        val xx = listOf<Byte>(j,j)
        val xy = listOf<Byte>(j,(-j).toByte())
        val yx = listOf<Byte>((-j).toByte(),j)
        val yy = listOf<Byte>((-j).toByte(),(-j).toByte())
        moves.add(xx)
        moves.add(xy)
        moves.add(yx)
        moves.add(yy)
    }
    return moves
}