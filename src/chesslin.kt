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

    fun threatenedSquaresPawn(square: Square): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        result.add(this.squares[square.getX()+1][square.getY()-1])
        result.add(this.squares[square.getX()+1][square.getY()+1])
        return result
    }

    fun threatenedSquaresRook(square: Square): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        val possibleMoves = moves.getMove('R')
        return result
    }

    fun threatenedSquaresBishop(square: Square): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        val possibleMoves = moves.getMove('B')
        return result
    }

    fun threatenedSquaresKnight(square: Square): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        val possibleMoves = moves.getMove('N')
        return result
    }

    fun threatenedSquaresKing(square: Square): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        val possibleMoves = moves.getMove('K')
        return result
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

    fun getWhitePieces(pieces: Set<Square>): MutableSet<Square>{
        val result = mutableSetOf<Square>()
        pieces.forEach {
            if (it.getColor()=="w")
                result.add(it)
        }
        return result
    }

    fun getBlackPieces(pieces: Set<Square>): MutableSet<Square>{
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
    println(testGame.board.toASCII())

    val squares = testGame.board.threatenedSquaresPawn(testGame.board.squares[1][1])
    squares.forEach {
        print(it.getX())
        print("")
        println(it.getY())
    }




}