class Eval(private val color: Char){


    fun evaluateBoard(board: Board): Int{


        return evalPieces(board)+2
    }

    private fun evalPieces(board: Board): Int{

        var result = 0
        var pieces = board.getPieces()
        val otherPieces = if (this.color == 'w') board.getBlackPieces(pieces)
        else board.getWhitePieces(pieces)
        pieces = if (this.color == 'w') board.getWhitePieces(pieces)
        else board.getBlackPieces(pieces)


        pieces.forEach {
            when (it.getType()){
                'P' ->      result++
                'B','N' ->  result += 3
                'R'     ->  result += 5
                'Q'     ->  result += 9
            }
        }

        otherPieces.forEach {
            when (it.getType()){
                'P' ->      result--
                'B','N' ->  result -= 3
                'R'     ->  result -= 5
                'Q'     ->  result -= 9
            }
        }

        return result
    }
}