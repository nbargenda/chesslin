import java.lang.NullPointerException

fun mapToASCII(string: String): String{
    var result = String()
    val pieceMap = mapOf("bP" to '♟', "wP" to '♙', "bR" to '♜', "wR" to '♖', "bB" to '♝', "wB" to '♗',
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

fun isValidMove(move: Move): Boolean{
    return move.squareFrom.getX() in 0..7 && move.squareFrom.getY() in 0..7 &&
            move.squareTo.getX() in 0..7 && move.squareTo.getY() in 0..7  && move.squareTo != move.squareFrom
}

fun main() {

    val testgame = Game()
    testgame.startingPosition()
    val inputs = mutableListOf<Input>()
    var input: String?
    val capturedPieces: MutableSet<Piece> = mutableSetOf()
    var turn = 1
    val history = History(arrayListOf())

    while (true) {

        val currentState = testgame.transition(testgame.stateMachine, inputs)
        var possibleMoves = mutableSetOf<ArrayList<Square>>()
        var pieces = testgame.board.getPieces()
        var currentPieces: MutableSet<Square>
        var otherPieces: MutableSet<Square>
        var otherMoves: MutableSet<ArrayList<Square>> = mutableSetOf()
        if (currentState.value[0] == 'w') {
            currentPieces = testgame.board.getWhitePieces(pieces)
            otherPieces = testgame.board.getBlackPieces(pieces)
        } else {
            currentPieces = testgame.board.getBlackPieces(pieces)
            otherPieces = testgame.board.getWhitePieces(pieces)
        }

        currentPieces.forEach {
            if (testgame.board.hasMoves(it)) possibleMoves.add(testgame.board.possibleMovesSquare(it))
        }
        otherPieces.forEach {
            if (testgame.board.hasMoves(it)) otherMoves.add(testgame.board.possibleMovesSquare(it))
        }
        possibleMoves = testgame.board.removePinnedMoves(possibleMoves, otherMoves)
        possibleMoves = testgame.board.removeKingMovesCheck(possibleMoves, otherPieces)

        if ("Check" in currentState.value) {
            possibleMoves = testgame.board.possibleMovesCheck(possibleMoves, otherMoves)
        }

        if (!testgame.stateMachine.isFinalState(currentState)) {

            print(testgame.board.toASCII())
            println(currentState)
            println(capturedPieces)
            println("Please input Move")

            input = readLine()
            val move: Move = testgame.parseMove(input ?: "", possibleMoves)

            if (isValidMove(move)) {
                history.turnHistory.add(Turn(turn, currentState.value[0], testgame.board.squares, possibleMoves, move))

                try {
                    if (!move.special.isNullOrEmpty()) {

                        when {
                            move.special.contains('p') -> {
                                if (move.special.contains('x')) capturedPieces.add(move.squareTo.piece!!)
                                possibleMoves.remove(testgame.board.possibleMovesSquare(move.squareFrom))
                                testgame.executePromotion(move)
                            }
                            move.special.contains('x') -> {
                                capturedPieces.add(move.squareTo.piece!!)
                                possibleMoves.remove(testgame.board.possibleMovesSquare(move.squareFrom))
                                testgame.executeMove(move)
                            }
                            move.special.contains('e') -> {
                                possibleMoves.remove(testgame.board.possibleMovesSquare(move.squareFrom))
                                if (move.squareFrom.getColor() == "w") {
                                    testgame.board.squares[move.squareTo.getX() - 1][move.squareTo.getY()].piece?.let {
                                        capturedPieces.add(it)
                                    }
                                    testgame.enPassantWhite(move)
                                } else {
                                    testgame.board.squares[move.squareTo.getX() + 1][move.squareTo.getY()].piece?.let {
                                        capturedPieces.add(it)
                                    }
                                    testgame.enPassantBlack(move)
                                }
                            }
                            move.special.contains('s') -> {
                                testgame.board.castleShort(currentState.value[0])
                            }
                            move.special.contains('l') -> {
                                testgame.board.castleLong(currentState.value[0])
                            }
                        }
                    } else {
                        testgame.executeMove(move)
                    }

                    testgame.board.moveHistory.add(move)
                    pieces = testgame.board.getPieces()
                    otherMoves = mutableSetOf()
                    if (currentState.value[0] == 'w') {
                        currentPieces = testgame.board.getWhitePieces(pieces)
                        otherPieces = testgame.board.getBlackPieces(pieces)
                    } else {
                        currentPieces = testgame.board.getBlackPieces(pieces)
                        otherPieces = testgame.board.getWhitePieces(pieces)
                    }
                    possibleMoves.remove(testgame.board.possibleMovesSquare(move.squareFrom))
                    possibleMoves.add(testgame.board.possibleMovesSquare(move.squareTo))
                    otherPieces.forEach {
                        if (testgame.board.hasMoves(it)) otherMoves.add(testgame.board.possibleMovesSquare(it))
                    }
                    otherMoves = testgame.board.removeKingMovesCheck(otherMoves, currentPieces)
                    otherMoves = testgame.board.removePinnedMoves(otherMoves, possibleMoves)

                    if (checkIfCheck(possibleMoves, currentState.value[0])) {
                        otherMoves = testgame.board.possibleMovesCheck(
                            otherMoves,
                            testgame.board.removeEmptyMoves(possibleMoves)
                        )
                    }

                    when {
                        otherMoves.isEmpty() -> {
                            when {
                                checkIfCheck(possibleMoves, currentState.value[0]) -> inputs.add(Input("checkmate"))
                                else -> inputs.add(Input("draw"))
                            }
                        }
                        threeRep() || fiftyMove() -> inputs.add(Input("draw"))
                        checkIfCheck(possibleMoves, currentState.value[0]) -> inputs.add(Input("check"))
                        else -> inputs.add(Input("move"))
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        } else {
            print(testgame.board.toASCII())
            println("CHECKMATE/DRAW")
            print(history.toString())
            break
        }
    }
}


// fifty move rule
fun fiftyMove(): Boolean {
    return false
}

// threefold repetition
fun threeRep(): Boolean{
    return false
}


fun checkIfCheck(moves: MutableSet<ArrayList<Square>>, color: Char): Boolean{

    moves.forEach { move ->
        move.forEach{ square ->
            if (color=='w'){
                if (square.getColor()=="b" && square.getType()=='K') return true
            }
            else{
                if (square.getColor()=="w" && square.getType()=='K') return true
            }
        }
    }
    return false
}