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

fun main(){

    val testgame = Game()
    testgame.startingPosition()
    println(testgame.board.possibleMoves(testgame.board.squares[0][6]))
    val inputs = mutableListOf<Input>()
    var input: String?
    val pieces = testgame.board.getPieces()

    while(true) {

        val currentState = testgame.transition(testgame.stateMachine, inputs)
        val possibleMoves:MutableSet<MutableSet<Square>> = mutableSetOf()
        val currentPieces:MutableSet<Square>
        currentPieces = if (currentState.value[0] == 'w') testgame.board.getWhitePieces(pieces)
            else testgame.board.getBlackPieces(pieces)
        currentPieces.forEach {

            possibleMoves.add(testgame.board.possibleMoves(it))
        }
        println(possibleMoves)

        if (!testgame.stateMachine.isFinalState(currentState)) {
            print(testgame.board.toASCII())
            println(currentState)
            println("Please input Move")
            input = readLine()
            inputs.add(Input(input.toString()))
        }

        else{
            println("CHECKMATE")
            print("Moves:$inputs")
            break
        }
    }
}