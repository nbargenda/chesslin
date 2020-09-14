import java.lang.NullPointerException
import java.io.File

fun main(args: Array<String>) {

    val testgame = Game()
    testgame.startingPosition()
    val capturedPieces: MutableSet<Piece> = mutableSetOf()
    var turn = 1
    val history = History(arrayListOf())
    val inputs = mutableListOf<Input>()
    var input: String?
    var gameInputs: List<String> = listOf()
    if (args[0] == "-t"){
        gameInputs = parsePGN(File("${args[1]}.txt").readText())
        gameInputs = removeEmptyString(gameInputs)
    }

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
        possibleMoves.clear()
        currentPieces.forEach {
            val tempMoves = testgame.board.possibleMovesSquare(it)
            if (tempMoves.isNotEmpty()) possibleMoves.add(tempMoves)
        }
        otherMoves.clear()
        otherPieces.forEach {
            val tempMoves = testgame.board.possibleMovesSquare(it)
            if (tempMoves.isNotEmpty()) otherMoves.add(tempMoves)
        }

        otherMoves = testgame.board.removePinnedMoves(otherMoves, possibleMoves)
        otherMoves = testgame.board.removeKingMovesCheck(otherMoves, possibleMoves)

        possibleMoves = testgame.board.removePinnedMoves(possibleMoves, otherMoves)
        possibleMoves = testgame.board.removeKingMovesCheck(possibleMoves, otherMoves)


        if ("Check" in currentState.value) {
            possibleMoves = testgame.board.possibleMovesCheck(possibleMoves, otherMoves)
        }

        if (!testgame.stateMachine.isFinalState(currentState)) {

            print(testgame.board.toASCII())
            println(currentState)
            println(capturedPieces)
            println("Please input Move")

            input = if (args[0] == "-t") gameInputs[turn-1] else readLine()
            println(input)

            val move: Move = testgame.parseMove(input ?: "", possibleMoves)
            history.turnHistory.add(Turn(turn, currentState.value[0], testgame.board.squares, possibleMoves, Move(move.squareFrom, move.squareTo, move.special), input ?: ""))

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
                                    testgame.board.squares[move.squareTo.rank - 1][move.squareTo.column].piece?.let {
                                        capturedPieces.add(it)
                                    }
                                    testgame.executeEnPassantWhite(move)
                                } else {
                                    testgame.board.squares[move.squareTo.rank + 1][move.squareTo.column].piece?.let {
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
                    pieces.clear()
                    currentPieces.clear()
                    otherPieces.clear()
                    pieces = testgame.board.getPieces()

                    if (currentState.value[0] == 'w') {
                        currentPieces = testgame.board.getWhitePieces(pieces)
                        otherPieces = testgame.board.getBlackPieces(pieces)
                    } else {
                        currentPieces = testgame.board.getBlackPieces(pieces)
                        otherPieces = testgame.board.getWhitePieces(pieces)
                    }
                    otherMoves.clear()
                    otherPieces.forEach {
                        val tempMoves = testgame.board.possibleMovesSquare(it)
                        if (tempMoves.isNotEmpty()) otherMoves.add(tempMoves)
                    }
                    possibleMoves.clear()
                    currentPieces.forEach {
                        val tempMoves = testgame.board.possibleMovesSquare(it)
                        if (tempMoves.isNotEmpty()) possibleMoves.add(tempMoves)
                    }
                    possibleMoves = testgame.board.removePinnedMoves(possibleMoves, otherMoves)
                    possibleMoves = testgame.board.removeKingMovesCheck(possibleMoves, otherMoves)

                    otherMoves = testgame.board.removePinnedMoves(otherMoves, possibleMoves)
                    otherMoves = testgame.board.removeKingMovesCheck(otherMoves, possibleMoves)

                    if (checkIfCheck(possibleMoves, currentState.value[0].toString())) {
                        otherMoves = testgame.board.possibleMovesCheck(otherMoves, testgame.board.removeEmptyMoves(possibleMoves))
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

fun removeEmptyString(list: List<String>): MutableList<String> {

    val result = mutableListOf<String>()
    list.forEach {
        if(it!="") result.add(it)
    }
    return result
}


fun parsePGN(string: String) =
    string.split("]").last().trim().replace("\\{.*?}".toRegex(),"").replace( /*"[0-9]*\\. "*/"""[0-9]*\."""
        .toRegex(),"").replace("+","").replace("#","").replace(" \n"," ").replace("\r","")
        .replace("\n"," ").split(" ").toTypedArray().dropLast(1)

fun checkFiftyMoves(history: History, limit: Int): Boolean {
    if (history.turnHistory.size < limit) return false
    history.turnHistory.takeLast(limit).forEach {
        if (it.squares[it.move.squareFrom.rank][it.move.squareFrom.column][2] != 'P' || it.move.special?.contains('x') == true) return false
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

fun isValidMove(move: Move) =  move.squareFrom.rank in 0..7 && move.squareFrom.column in 0..7 && move.squareTo.rank in 0..7 && move.squareTo.column in 0..7 && move.squareTo != move.squareFrom

