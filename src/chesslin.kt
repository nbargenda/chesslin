
class Square(var piece: Piece? = null){

    fun hasPiece(): Boolean {
        return this.piece != null
    }

    fun putPiece(piece: Piece){
        this.piece = piece
    }

    fun getColor(): String?{
        return  this.piece?.getColor()
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
}

open class Piece(private val color: Boolean, private val type: Char, private var hasMoved: Boolean = false){


    fun getColor(): String{
        return if (this.color) "w" else "b"
    }

    fun getType(): Char{
        return this.type
    }

    fun getHasMoved(): Boolean{
        return this.hasMoved
    }

    fun setHasMoved(){
        this.hasMoved = true
    }
}


class Board(){
    val squares = arrayOf(
        arrayOf(Square(),Square(),Square(),Square(),Square(),Square(),Square(),Square()),
        arrayOf(Square(),Square(),Square(),Square(),Square(),Square(),Square(),Square()),
        arrayOf(Square(),Square(),Square(),Square(),Square(),Square(),Square(),Square()),
        arrayOf(Square(),Square(),Square(),Square(),Square(),Square(),Square(),Square()),
        arrayOf(Square(),Square(),Square(),Square(),Square(),Square(),Square(),Square()),
        arrayOf(Square(),Square(),Square(),Square(),Square(),Square(),Square(),Square()),
        arrayOf(Square(),Square(),Square(),Square(),Square(),Square(),Square(),Square()),
        arrayOf(Square(),Square(),Square(),Square(),Square(),Square(),Square(),Square())
    )
    val moves = Moves()

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
            string += "\n"
        }
        return string
    }

    fun basicMove(squareA: Square, squareB: Square){
        squareB.putPiece(squareA.piece!!)
        squareB.piece!!.setHasMoved()
        squareA.emptySquare()
    }
}


class Game(){
    val board = Board()

    fun startingPosition(){
        for (i in 0..7){
            val pawnW: Piece = Piece(true,'P')
            val pawnB: Piece = Piece(false, 'P')
            this.board.squares[1][i].putPiece(pawnW)
            this.board.squares[6][i].putPiece(pawnB)

        }

        for (i in 0..1){
            val rookW: Piece = Piece(true, 'R')
            val rookB: Piece = Piece(false,'R')
            this.board.squares[0][i*7].putPiece(rookW)
            this.board.squares[7][i*7].putPiece(rookB)
        }

        val knightPos = listOf(1,6)
        for (i in knightPos){
            val knightW: Piece = Piece(true, 'N')
            val knightB: Piece = Piece(false,'N')
            this.board.squares[0][i].putPiece(knightW)
            this.board.squares[7][i].putPiece(knightB)
        }

        val bishopPos = listOf(2,5)
        for (i in bishopPos){
            val bishopW: Piece = Piece(true,  'B')
            val bishopB: Piece = Piece(false, 'B')
            this.board.squares[0][i].putPiece(bishopW)
            this.board.squares[7][i].putPiece(bishopB)
        }

        val queenW: Piece = Piece(true, 'Q')
        val queenB: Piece = Piece(false,'Q')
        val kingW:  Piece = Piece(true, 'K')
        val kingB:  Piece = Piece(false,'K')

        this.board.squares[0][3].putPiece(queenW)
        this.board.squares[7][3].putPiece(queenB)
        this.board.squares[0][4].putPiece(kingW)
        this.board.squares[7][4].putPiece(kingB)
    }


}

fun main(){
    println("Chess Hype")
    val testGame: Game = Game()
    testGame.startingPosition()
    println(testGame.board.toASCII())
    testGame.board.basicMove(testGame.board.squares[0][6],testGame.board.squares[2][5])
    println(testGame.board.toASCII())

}