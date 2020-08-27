class Square(var piece: Piece? = null, val positionX: Int, val positionY: Int){

    fun hasPiece(): Boolean {
        return this.piece != null
    }

    fun putPiece(piece: Piece){
        this.piece = piece
    }

    fun getColor(): String?{
        return this.piece?.getColor()
    }
    fun getType(): Char?{
        return this.piece?.getType()
    }

    fun getHasMoved(): Boolean?{
        return this.piece?.getHasMoved()
    }

    fun emptySquare(){
        this.piece = null
    }

    fun getX(): Int{
        return this.positionX
    }

    fun getY(): Int{
        return this.positionY
    }


}

fun bound(int: Int): Int{
    return when (int){
        in 8..16 -> 7
        in -1 downTo -9 -> 0
        else -> int
    }
}

class Board(){


    var squares = arrayListOf<ArrayList<Square>>()

    init{
        for(i in 0..7){
            squares.add(arrayListOf<Square>())
            for (j in 0..7){
                squares[i].add(Square(null,i,j))
            }
        }
    }

    val moves = Moves()


    fun possibleMoves(square: Square): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        if (square.getType()=='P' && !this.squares[square.getX()+1][square.getY()].hasPiece()){
            if (!square.getHasMoved()!! && !this.squares[square.getX()+2][square.getY()].hasPiece() )
                result.add(this.squares[square.getX()+2][square.getY()])
            result.add(this.squares[square.getX()+1][square.getY()])
        }
        else {
            val threatenedSquares = threatenedSquares(square)
            threatenedSquares.forEach {
                if (it.getColor()!=square.getColor()) result.add(it)
            }
        }

        return result
    }

    private fun removeInvalidMoves(squares: MutableSet<List<Int>>): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        squares.forEach {
            if (it[0] in 0..7 && it[1] in 0..7) {
                result.add(this.squares[it[0]][it[1]])
            }
        }
        return result
    }


    fun defendedThreatenedSquares(square: Square): ArrayList<Set<Square>>{
        val color = square.getColor()
        val defSquares = mutableSetOf<Square>()
        val threatSquares = mutableSetOf<Square>()
        val threatenedSquares = threatenedSquares(square)
        threatenedSquares.forEach {
            if (it.getColor().equals(color)) defSquares.add(it)
            else threatSquares.add(it)
        }
        return arrayListOf(defSquares,threatSquares)
    }

    fun threatenedSquares(square: Square): Set<Square> {
        return when (square.getType()){
            'P' -> threatenedSquaresPawn(square)
            'K','N' -> threatenedSquaresKnightKing(square)
            'B','R','Q' -> threatenedSquaresRBQ(square)
            else -> setOf()
        }
    }
    private fun threatenedSquaresPawn(square: Square): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        if (square.getY()>0) result.add(this.squares[square.getX()+1][square.getY()-1])
        if (square.getY()<7) result.add(this.squares[square.getX()+1][square.getY()+1])
        return result
    }

    private fun checkBlock(squares: MutableSet<Square>): MutableSet<List<Int>> {
        val result = mutableSetOf<List<Int>>()
        squares.forEach {
            result.add(listOf(it.getX(),it.getY()))
            if (this.squares[it.getX()][it.getY()].hasPiece()) return result
        }
        return result

    }

    private fun threatenedSquaresRBQ(square: Square): MutableSet<Square>{
        val possibleMoves = moves.getMove(square.getType())
        val possibleSquaresUp: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresDown: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresLeft: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresRight: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresXX: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresXY: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresYX: MutableSet<List<Int>> = mutableSetOf()
        val possibleSquaresYY: MutableSet<List<Int>> = mutableSetOf()
        val x = square.getX()
        val y = square.getY()
        val possibleSquares = mutableSetOf<List<Int>>()
        possibleMoves.forEach {
            if (square.getType()=='R' || square.getType()=='Q') {
                if (it[0] > 0 && (it[1]) == 0)      possibleSquaresUp.add(listOf(x + it[0], y))
                else if (it[0] < 0 && (it[1]) == 0) possibleSquaresDown.add(listOf(x + it[0], y))
                else if ((it[0]) == 0 && it[1] < 0) possibleSquaresLeft.add(listOf(x, y + it[1]))
                else if ((it[0]) == 0 && it[1] > 0) possibleSquaresRight.add(listOf(x, y + it[1]))
            }
            if (square.getType()=='B' || square.getType()=='Q'){
                if      (it[0]>0 && it[1]>0) possibleSquaresXX.add(listOf(x+it[0],y+it[1]))
                else if (it[0]<0 && it[1]<0) possibleSquaresYY.add(listOf(x+it[0],y+it[1]))
                else if (it[0]>0 && it[1]<0) possibleSquaresXY.add(listOf(x+it[0],y+it[1]))
                else if (it[0]<0 && it[1]>0) possibleSquaresYX.add(listOf(x+it[0],y+it[1]))
            }
        }

        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresUp)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresDown)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresLeft)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresRight)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresXX)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresXY)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresYX)))
        possibleSquares.addAll(checkBlock(removeInvalidMoves(possibleSquaresYY)))


        return removeInvalidMoves(possibleSquares)

    }

    private fun threatenedSquaresKnightKing(square: Square): MutableSet<Square>{
        val possibleMoves = moves.getMove(square.getType())
        val possibleSquares: MutableSet<List<Int>> = mutableSetOf()
        possibleMoves.forEach {
            possibleSquares.add(listOf(square.getX()+it[0],square.getY()+it[1]))
        }
        return removeInvalidMoves(possibleSquares)
    }


    fun getPieces(): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        this.squares.forEach {
            it.forEach { it2 ->
                if (it2.hasPiece())
                    result.add(it2)
            }
        }
        return result
    }

    fun getWhitePieces(pieces: MutableSet<Square>): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        pieces.forEach {
            if (it.getColor()=="w")
                result.add(it)
        }
        return result
    }

    fun getBlackPieces(pieces: MutableSet<Square>): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        pieces.forEach {
            if (it.getColor()=="b")
                result.add(it)
        }
        return result
    }

    fun toASCII(): String{
        var string = String()

        for(i in 7 downTo 0){
            for(j in 0..7){
                if (this.squares[i][j].hasPiece()){
                    string += this.squares[i][j].getColor()+this.squares[i][j].piece!!.getType()
                }
                else {
                    string += "_ "
                }
            }
            if (i > 0) string += "\n"

        }
        string = mapToASCII(string)
        return string
    }

    fun basicMove(squareA: Square, squareB: Square){
        squareB.putPiece(squareA.piece!!)
        squareB.piece!!.setHasMoved()
        squareA.emptySquare()
    }

    fun castleShort(color: Boolean){
        var rank = 0
        if (!color) rank = 7
        this.squares[rank][6].putPiece(this.squares[rank][4].piece!!)
        this.squares[rank][5].putPiece(this.squares[rank][7].piece!!)
        this.squares[rank][4].emptySquare()
        this.squares[rank][7].emptySquare()
    }

    fun castleLong(color: Boolean){
        var rank = 0
        if (!color) rank = 7
        this.squares[rank][2].putPiece(this.squares[rank][4].piece!!)
        this.squares[rank][3].putPiece(this.squares[rank][0].piece!!)
        this.squares[rank][4].emptySquare()
        this.squares[rank][0].emptySquare()
    }

}

fun mapToASCII(string: String): String{
    var result = String()
    val pieceMap = mapOf<String, Char>("bP" to '♟', "wP" to '♙', "bR" to '♜', "wR" to '♖', "bB" to '♝', "wB" to '♗',
                                                        "bN" to '♞', "wN" to '♘', "bQ" to '♛', "wQ" to '♕', "bK" to '♚', "wK" to '♔')
    val lines = string.lines()
    lines.forEach{
        for (i in 0..14 step 2){
            if (pieceMap.contains(it.substring(i,i+2))){
                result += pieceMap[it.substring(i,i+2)] ?:""
                result += " "
            }
            else {
                result += it.substring(i,i+2)+" "
            }
        }
            result += "\n"

    }

    return result
}


fun main(){
    println("Chess Hype")
    val testGame = Game()
    testGame.startingPosition()
    testGame.board.basicMove(testGame.board.squares[0][6],testGame.board.squares[2][5])
    //println(testGame.board.toASCII())

    val squares = testGame.board.defendedThreatenedSquares(testGame.board.squares[0][3])
    val move = Move(testGame.board.squares[0][6],testGame.board.squares[2][5])
    println(testGame.board.possibleMoves(testGame.board.squares[1][2]))
    println(testGame.board.possibleMoves(testGame.board.squares[0][7]))
    println(testGame.board.possibleMoves(testGame.board.squares[0][3]))
    println(testGame.board.possibleMoves(testGame.board.squares[2][5]))

}

