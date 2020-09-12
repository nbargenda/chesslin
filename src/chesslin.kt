import java.lang.NullPointerException

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
            val tempMoves = testgame.board.possibleMovesSquare(it)
            if (tempMoves.isNotEmpty()) possibleMoves.add(tempMoves)
        }

        possibleMoves = testgame.board.removePinnedMoves(possibleMoves, otherMoves)
        possibleMoves = testgame.board.removeKingMovesCheck(possibleMoves, otherPieces)

        if ("Check" in currentState.value) {
            possibleMoves = testgame.board.possibleMovesCheck(possibleMoves, otherMoves)
        }

        otherPieces.forEach {
            val tempMoves = testgame.board.possibleMovesSquare(it)
            if (tempMoves.isNotEmpty()) otherMoves.add(tempMoves)
        }



        if (!testgame.stateMachine.isFinalState(currentState)) {

            print(testgame.board.toASCII())
            println(currentState)
            println(capturedPieces)
            println("Please input Move")

            input = readLine()
            val move: Move = testgame.parseMove(input ?: "", possibleMoves)
            history.turnHistory.add(
                Turn(
                    turn,
                    currentState.value[0],
                    testgame.board.squares,
                    possibleMoves,
                    Move(move.squareFrom, move.squareTo, move.special),
                    input ?: ""
                )
            )

            if (isValidMove(move)) {
                turn++
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
                                    testgame.board.squares[move.squareTo.col - 1][move.squareTo.rank].piece?.let {
                                        capturedPieces.add(it)
                                    }
                                    testgame.executeEnPassantWhite(move)
                                } else {
                                    testgame.board.squares[move.squareTo.col + 1][move.squareTo.rank].piece?.let {
                                        capturedPieces.add(it)
                                    }
                                    testgame.executeEnPassantBlack(move)
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
                        val tempMoves = testgame.board.possibleMovesSquare(it)
                        if (tempMoves.isNotEmpty()) otherMoves.add(tempMoves)
                    }
                    otherMoves = testgame.board.removeKingMovesCheck(otherMoves, currentPieces)
                    otherMoves = testgame.board.removePinnedMoves(otherMoves, possibleMoves)

                    if (checkIfCheck(possibleMoves, currentState.value[0].toString())) {
                        otherMoves = testgame.board.possibleMovesCheck(
                            otherMoves,
                            testgame.board.removeEmptyMoves(possibleMoves)
                        )
                    }

                    when {
                        otherMoves.isEmpty() -> {
                            when {
                                checkIfCheck(
                                    possibleMoves,
                                    currentState.value[0].toString()
                                ) -> inputs.add(Input("checkmate"))
                                else -> inputs.add(Input("draw"))
                            }
                        }
                        checkRepetition(history, 5) || checkFiftyMoves(
                            history,
                            75
                        ) -> inputs.add(Input("draw")) // remove last turn from history tbh
                        checkIfCheck(possibleMoves, currentState.value[0].toString()) -> inputs.add(Input("check"))
                        else -> inputs.add(Input("move"))
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            } else {
                if (input == "draw") {
                    if (checkFiftyMoves(history, 50) || checkRepetition(history, 3)) inputs.add(Input("draw"))
                }
            }

        } else {
            print(testgame.board.toASCII())
            print(history.toString())
            if (inputs.last().value == "checkmate") {
                if (history.turnHistory.last().player == 'w') println("1-0")
                else println("0-1")
            } else println("1/2-1/2")
            break
        }
    }
}

fun checkFiftyMoves(history: History, limit: Int): Boolean {
    if (history.turnHistory.size < limit) return false
    history.turnHistory.takeLast(limit).forEach {
        if (it.squares[it.move.squareFrom.col][it.move.squareFrom.rank][2] != 'P' || it.move.special?.contains('x') == true) return false
    }
    return true
}

fun checkRepetition(history: History, limit: Int): Boolean {

    val whiteTurns = mutableSetOf<Turn>()
    val blackTurns = mutableSetOf<Turn>()
    history.turnHistory.forEach {
        if (it.player == 'w') whiteTurns.add(it)
        else blackTurns.add(it)
    }

    var count = 0
    whiteTurns.forEach { turn1 ->
        whiteTurns.forEach { turn2 ->
            if (turn1.moves == turn2.moves) {
                count++
                if (count >= limit) return true
            }
        }
        count = 0
    }

    blackTurns.forEach { turn1 ->
        blackTurns.forEach { turn2 ->
            if (turn1.moves == turn2.moves) {
                count++
                if (count >= limit) return true
            }
        }
        count = 0
    }
    return false
}

fun checkIfCheck(moves: MutableSet<ArrayList<Square>>, color: String): Boolean {

    moves.forEach { move ->
        move.forEach { square ->
            if (color == "w") {
                if (square.getColor() == "b" && square.getType() == 'K') return true
            } else {
                if (square.getColor() == "w" && square.getType() == 'K') return true
            }
        }
    }
    return false
}

fun mapToASCII(string: String): String {
    var result = String()
    val pieceMap = mapOf(
        "bP" to '♟', "wP" to '♙', "bR" to '♜', "wR" to '♖', "bB" to '♝', "wB" to '♗',
        "bN" to '♞', "wN" to '♘', "bQ" to '♛', "wQ" to '♕', "bK" to '♚', "wK" to '♔'
    )
    val lines = string.lines()
    lines.forEach {
        for (i in 0..14 step 2) {
            if (pieceMap.contains(it.substring(i, i + 2))) {
                result += pieceMap[it.substring(i, i + 2)] ?: ""
                result += " "
            } else {
                result += it.substring(i, i + 2) + " "
            }
        }
        result += "\n"
    }

    return result
}

fun isValidMove(move: Move): Boolean {
    return  move.squareFrom.col in 0..7 && move.squareFrom.rank in 0..7 &&
            move.squareTo.col in 0..7 && move.squareTo.rank in 0..7 && move.squareTo != move.squareFrom
}
