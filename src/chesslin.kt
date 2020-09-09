import java.lang.NullPointerException

fun bound(int: Int): Int{
    return when (int){
        in 8..16 -> 7
        in -1 downTo -9 -> 0
        else -> int
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


fun charToColor(char: Char): Boolean{
    return when (char){
            'w' -> true
            'b' -> false
        else -> true
    }
}

fun isValidMove(move: Move): Boolean{
    return move.squareFrom.getX() in 0..7 && move.squareFrom.getY() in 0..7 && move.squareTo.getX() in 0..7 && move.squareTo.getY() in 0..7
}

fun main(){

    val testgame = Game()
    testgame.startingPosition()
    val inputs = mutableListOf<Input>()
    var input: String?
    val capturedPieces: MutableSet<Piece> = mutableSetOf()

    while(true) {

        val currentState = testgame.transition(testgame.stateMachine, inputs)
        val possibleMoves:MutableSet<ArrayList<Square>> = mutableSetOf()
        val pieces = testgame.board.getPieces()
        val currentPieces:MutableSet<Square>
        currentPieces = if (currentState.value[0] == 'w') testgame.board.getWhitePieces(pieces)
            else testgame.board.getBlackPieces(pieces)
        currentPieces.forEach {
            if (testgame.board.hasMoves(it)) possibleMoves.add(testgame.board.possibleMovesSquare(it))
        }

        if (!testgame.stateMachine.isFinalState(currentState)) {

            print(testgame.board.toASCII())
            println(currentState)
            println(capturedPieces)
            println("Please input Move")

            input = readLine()
            val move: Move = testgame.parseMove(input?:"", possibleMoves)

            if (isValidMove(move)){

                try{
                    if (!move.special.isNullOrEmpty()){

                        when {
                            move.special.contains('c') -> {
                                capturedPieces.add(move.squareTo.piece!!)
                                testgame.executeMove(move)
                            }
                            move.special.contains('e') -> {
                                if (move.squareFrom.getColor()=="w"){
                                    testgame.board.squares[move.squareTo.getX()-1][move.squareTo.getY()].piece?.let {
                                        capturedPieces.add(it)
                                    }
                                    testgame.enPassantWhite(move)
                                } else{
                                    testgame.board.squares[move.squareTo.getX()+1][move.squareTo.getY()].piece?.let {
                                        capturedPieces.add(it)
                                    }
                                    testgame.enPassantBlack(move)
                                }
                            }
                            move.special.contains('s') ->{
                                testgame.board.castleShort(currentState.value[0])
                            }
                            move.special.contains('l') ->{
                                testgame.board.castleLong(currentState.value[0])
                            }

                        }
                    }

                    else {
                        testgame.executeMove(move)
                    }
                    inputs.add(Input("move"))
                }

                catch (e: NullPointerException){
                    print("oopsie")
                    println(e.stackTrace)
                }
            }
        }

        else{
            println("CHECKMATE")
            println("Moves:$inputs")
            println("Move History:"+testgame.board.moveHistory.toString())
            break
        }
    }
}