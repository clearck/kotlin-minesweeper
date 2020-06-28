package minesweeper

import java.util.*
import kotlin.random.Random

const val DEFAULT_FIELD_SIZE = 9
const val DEFAULT_MINES = 8

class Minesweeper(private val fieldSize: Int, private val amountMines: Int) {

    var lost = false
        private set

    val won
        get() = mines == minesMarked && mines.isNotEmpty()

    // Initialize empty board with no mines
    private val board = Array(fieldSize) { _ ->
        Array(fieldSize) { _ -> false }
    }

    // Store positions of mines
    private val mines = mutableSetOf<Pair<Int, Int>>()

    // Keep track of marked mines
    private val minesMarked = mutableSetOf<Pair<Int, Int>>()

    // Keep track of explored safe cells
    private val explored = mutableSetOf<Pair<Int, Int>>()

    fun print() {
        // Print Head of grid
        print(" |")
        repeat(fieldSize) { print(it + 1) }
        println("|")
        println("-|".plus("-".repeat(fieldSize)).plus("|"))

        // Print Body of grid
        board.forEachIndexed { i, row ->
            print("${i + 1}|")
            row.forEachIndexed { j, _ ->
                // Cell cant be marked and explored at the same time
                when (val cell = Pair(i, j)) {
                    in minesMarked -> print('*')

                    // Cell is explored and is either safe or the user stepped on a mine
                    in explored -> {
                        if (cell in mines) {
                            print('X')
                        } else {
                            val mines = getNearbyMines(cell)
                            print(if (mines > 0) mines else '/')
                        }
                    }
                    // Cell is unidentified
                    else -> print('.')
                }
            }
            println("|")
        }
        println("-|".plus("-".repeat(fieldSize)).plus("|"))
    }

    fun mark(cell: Pair<Int, Int>) {
        when (cell) {
            in minesMarked -> minesMarked.remove(cell)
            else -> minesMarked.add(cell)
        }
    }

    fun free(cell: Pair<Int, Int>) {
        println("free")
        // Generate mines when the first cell is 'clicked'
        if (explored.size == 0) {
            generateMines(cell)
        }

        lost = cell in mines
        if (lost) {
            explored.addAll(mines)
            return
        }

        // Check if cell is free and has no mines around it
        explore(cell)
    }

    private fun explore(cell: Pair<Int, Int>) {
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(cell)

        while (!queue.isEmpty()) {
            val current = queue.poll()

            minesMarked.remove(current)
            explored.add(current)
            if (getNearbyMines(current) > 0) {
                continue
            }

            getNeighbors(current).forEach {
                if (it !in explored) {
                    queue.add(it)
                }
            }
        }
    }

    private fun getNearbyMines(cell: Pair<Int, Int>): Int {
        var count = 0

        for (i in (cell.first - 1)..(cell.first + 1)) {
            for (j in (cell.second - 1)..(cell.second + 1)) {

                // Ignore cell itself
                if (Pair(i, j) == cell) continue

                // Update count if cell is in bounds an is a mine
                if (i in 0 until fieldSize && j in 0 until fieldSize) {
                    if (board[i][j]) {
                        count++
                    }
                }
            }
        }

        return count
    }

    private fun getNeighbors(cell: Pair<Int, Int>): Set<Pair<Int, Int>> {
        val neighbors = mutableSetOf<Pair<Int, Int>>()

        for (i in (cell.first - 1)..(cell.first + 1)) {
            for (j in (cell.second - 1)..(cell.second + 1)) {

                // Ignore cell itself
                if (Pair(i, j) == cell) continue

                // Add cell to neighbors
                if (i in 0 until fieldSize && j in 0 until fieldSize) {
                    neighbors.add(Pair(i, j))
                }
            }
        }

        return neighbors
    }

    private fun generateMines(excluding: Pair<Int, Int>) {
        // Add mines randomly, making sure that the first clicked cell doesnt contain a mine
        while (mines.size != amountMines) {
            val i = Random.nextInt(fieldSize)
            val j = Random.nextInt(fieldSize)

            if (Pair(i, j) == excluding) continue

            if (!board[i][j]) {
                mines.add(Pair(i, j))
                board[i][j] = true
            }
        }
    }
}

fun main() {

    // val fieldSize = readLine()?.toIntOrNull() ?: DEFAULT_FIELD_SIZE
    val fieldSize = DEFAULT_FIELD_SIZE

    println("How many mines do you want on the field")
    val amountMines = readLine()?.toIntOrNull() ?: DEFAULT_MINES

    val minesweeper = Minesweeper(fieldSize, amountMines)
    var skipDraw = false

    // Game Loop
    while (true) {

        // Draw board
        if (!skipDraw) {
            minesweeper.print()
        }
        skipDraw = false

        if (minesweeper.won) {
            println("Congratulations! You found all the mines!")
            break
        } else if (minesweeper.lost) {
            println("You stepped on a mine and failed!")
            break
        }

        // Prompt for user move
        val (move, action) = promptMove()

        when (action) {
            "free" -> minesweeper.free(move)
            "mine" -> minesweeper.mark(move)
        }
    }
}

fun promptMove(): Pair<Pair<Int, Int>, String> {
    println("Set/unset mine marks or claim a cell as free:")
    var input: List<String>?
    do {
        input = readLine()?.split(' ')
    } while (input == null)

    return Pair(Pair(input[1].toInt() - 1, input[0].toInt() - 1), input[2])
}
