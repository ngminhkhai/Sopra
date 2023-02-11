package service

import ai.AIService
import entity.*
import java.lang.IllegalStateException
import java.io.Serializable

/**
 * Player Action class
 * @property takeTile player Take a tile
 * @property placeOnGrid player places a tile
 * @property fillTiles the tiles around the board are filled
 * @property redo action redo
 * @property undo action undo
 * @property updateTasks tasks are checked
 * @property moveTokenOfCurrPlayer players token is moved
 */

class PlayerActionService (private val rootService: RootService):AbstractRefreshingService(), Serializable {

    /**
     * Moves the token of player, meeple, and adds the cost for the current player
     * also direcly removes the choosenTile from middleCards therefore in View store the card before!!!!!!!!!
     * then call move on grid with this card!!!!!
     *
     * @param choosenTile the tile which is choosen is used to move the player token position, meeple position
     * The middleTiles are automatically filled when no tile is around the board or the player decide to fill the tiles
     * @param print added by AI. Function is used to simulate the game, and will fill the console with seemingly
     *          nonsensical statements. If print is false, only error messages will be printed.
     */
    fun takeTile(choosenTile: Tile?, print: Boolean = true){
        //for redo & undo function
        initNewGame()

        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game) { "No started game found." }
        checkNotNull(choosenTile){"Tile is not allowed to be null"}

        // check if the choosenTile is one Tile of the middleTiles
        val positionTile = game.middleTiles.indexOf(choosenTile)
        if (positionTile == -1){
            println("Take another Tile: Choice Wrong. Tile not in middleTiles")
        }
        else{
            // new meeple Position when possible
            // only the next three cards are allowed to be choosen
            var checkHowMuchCardsInFrontOfTheMoon = 0
            var meeplePos = game.moonPos
            while (meeplePos != positionTile){
                meeplePos++
                meeplePos %= 12
                if (game.middleTiles[meeplePos] != null){
                    checkHowMuchCardsInFrontOfTheMoon++
                }
            }
            if (checkHowMuchCardsInFrontOfTheMoon > 3){
                println("The Position is not possible to choose. Maximum are 3 positions")
            }else {
                moveTokenOfCurrentPlayer(choosenTile.cost)
                val gameService = rootService.gameService
                //Move meeple to position of tile
                gameService.moveMeeple(positionTile)
                // Remove the tile from middleCardstack
                game.middleTiles[positionTile] = null
                game.nMiddleTiles = game.middleTiles.filterNotNull().size
                if(print) {
                    print(game.currPlayer.name + " chooses at position (${positionTile+1}) a ")
                    tileToText(choosenTile)
                }

                // Refresh the View after the tile was picked
                onAllRefreshables { refreshAfterTilePicked() }
            }
        }
    }

    /**
     * @param tiles 2d array
     * @return returns true if all elements in the array are null otherwise false
     */
    private fun gridConsistsOfNulls(tiles: Array<Array<Tile?>>): Boolean{
        for(row in tiles){
            for(elem in row){
                if (elem != null){
                    return false
                }
            }
        }
        return true
    }

    /**
     * When no card is placed in the grid the first card can be placed everywhere
     * when more than one card is placed the card must have at least one neighbor card
     *
     * @param newTile tile which has to be placed
     * @param xPosition xPosition in the grid
     * @param yPosition yPosition in the grid
     * @param print added by AI. Function is used to simulate the game, and will fill the console with seemingly
     *          nonsensical statements. If print is false, only error messages will be printed.
     */
    fun placeOnGrid(newTile: Tile?, xPosition: Int, yPosition: Int, print : Boolean = true){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game) { "No started game found." }
        if (xPosition < game.currPlayer.tileGrid.tiles.size && xPosition >= 0
            && yPosition < game.currPlayer.tileGrid.tiles.size && yPosition >= 0 && newTile != null ){
            // first card is placed
            if (gridConsistsOfNulls(game.currPlayer.tileGrid.tiles)){
                newTile.xPos = xPosition
                newTile.yPos = yPosition
                game.currPlayer.tileGrid.tiles[xPosition][yPosition] = newTile
                if(print){
                    println("The tile is placed at ("+indexToLetter(xPosition)+",${yPosition+1})")
                }
                // Refresh the View of the Method
                onAllRefreshables { refreshAfterTilePlaced() }
            }else{
                val validPlace = validPosition(xPosition, yPosition)
                // Is a valid Place of the Card choosen?
                // set the Position on the grid and the information inn the tile
                if (validPlace){
                    newTile.xPos = xPosition
                    newTile.yPos = yPosition
                    game.currPlayer.tileGrid.tiles[xPosition][yPosition] = newTile
                    if(print) {
                        println("The tile is placed at (" + indexToLetter(xPosition) + ",${yPosition + 1})")
                    }
                    // Refresh the View of the Method
                    onAllRefreshables { refreshAfterTilePlaced() }
                    updateTasks(print)
                    onAllRefreshables { refreshAfterUpdateTask() }
                }else{
                    println("Kein passender Platz fuer die Karte. Sie muss wo anders hingelegt werden.")
                }
            }
        }else{
            println("Der Platz der Karte ist außerhalb des Grids. Bitte erneut Karte platzieren.")
        }
    }

    /**
     * If card are already placed on the Grid the function return if the next card is placed correctly
     *
     * @param xPosition xPosition of the Grid
     * @param yPosition yPosition of the Grid
     */
    private fun validPosition(xPosition: Int, yPosition: Int): Boolean{
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val tiles = game.currPlayer.tileGrid.tiles
        // checks if the second card is around the already placed Cards
        if(xPosition - 1 >= 0 && tiles[xPosition -1][yPosition] != null ){
            return true
        }else if(xPosition + 1 < game.currPlayer.tileGrid.tiles.size && tiles[xPosition + 1][yPosition] != null){
            return true
        }else if(yPosition - 1 >= 0 && tiles[xPosition][yPosition -1] != null){
            return true
        }else if(yPosition + 1 < game.currPlayer.tileGrid.tiles.size && tiles[xPosition][yPosition + 1] != null){
            return true
        }
        return false
    }

    /**
     * moving the token of the current player as many fields as the cost of the chosen is
     */
    fun moveTokenOfCurrPlayer(costs : Int){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game) { "No started game found." }
        game.currPlayer.totalCost += costs
        onAllRefreshables {refreshAfterTilePicked() }
    }
    /**
     * action where player can fill the middle tiles which are accessible for all players.
     * fill tiles is available when there are only 2 or less middle tiles.
     * checking if the drawstack is big enough and then filling the tiles from the drawstack
     * in a loop
     */
    fun fillTiles(){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game) { "No started game found." }
        val currDrawstack = game.drawStack

        game.nMiddleTiles = game.middleTiles.filterNotNull().size
        if (game.nMiddleTiles > 2){
            //println("[PLAYERACTIONSERVICE] : Es liege mer als 2 Tiles in der Mitte, es kann nicht aufgefüllt werden.")
        }
        else{
            println("Filling Tiles")
            var pos = game.moonPos + 1
            pos %= game.middleTiles.size
            while( pos != game.moonPos){
                if (game.middleTiles[pos] == null && game.drawStack.isNotEmpty()){
                    game.middleTiles[pos] = game.drawStack.removeLast()
                }
                pos++
                pos %= game.middleTiles.size
            }
            game.nMiddleTiles = game.middleTiles.filterNotNull().size
            onAllRefreshables { refreshAfterFillTile() }
        }
    }


    /**
     * Updates GUI after a Player ends his turn.
     */
    fun endTurn(){
        rootService.gameService.startNextTurn()
        onAllRefreshables { refreshAfterEndTurn() }
    }

    /**
     * Redoes the previous turn. Only possible if the turn you want to redo has been finished.
     *
     * @throws IllegalStateException if there is no next turn
     */
    fun redo(){
        //check if there is a current game
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        //redo not possible if this is the last turn a player made or the last turn wasnt finished
        if(game.nextState == null){
            throw IllegalStateException("There is no next game state!")
        }else if(!game.nextState!!.redoPossible){
            throw IllegalStateException("The last turn was not finished so you cant redo it!")
        }else {
            println(game.currPlayer.name + " redoes a turn")
            rootService.novaLunaApplication.currentGame = game.nextState
            val nextGame = game.nextState
            checkNotNull(nextGame)
            println("\nIt is " + nextGame.currPlayer.name + "s turn.")
            onAllRefreshables { refreshAfterRedo(nextGame.currPlayer) }
        }
    }

    /**
     *  Undoes the last turn.
     *
     *  @throws IllegalStateException if this is the first turn of the game.
     */
    fun undo(){
        //check if there is a current game
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        //undo not possible if this is the first turn of the game
        if(game.prevState == null){
            throw IllegalStateException()
        }else {
            println(game.currPlayer.name + " undoes a turn")
            rootService.novaLunaApplication.currentGame = game.prevState
            println("\nIt is " + rootService.novaLunaApplication.currentGame!!.currPlayer.name + "s turn.")
            onAllRefreshables { refreshAfterUndo(game.currPlayer) }
        }
    }

    /**
     * Initializes a new Game Object for redo and undo function.
     */
    private fun initNewGame(){
        //check if there is a current game
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        //create copy of game
        val newGame = game.deepCopy()
        newGame.nextState = null
        newGame.redoPossible = false

        //set pointer for "doubly linked list"
        newGame.prevState = game
        game.nextState = newGame
        //update currentGame of root service
        rootService.novaLunaApplication.currentGame = newGame
    }

    /**
     * Methode to receive a hint.
     *
     * @return A [String] containing the hint.
     */
    fun getHint(): String{
        //check if there is a current game
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        //disable highscore
        game.currPlayer.hintUsed = true
        val aiService = AIService()
        val hint = aiService.findStep(game.currPlayer, rootService)
        val tileToTake = game.middleTiles[hint[0]]
        checkNotNull(tileToTake)
        //write hint
        var out = "Take ${hint[0] + 1}. tile with color ${tileToTake.color}, cost ${tileToTake.cost}, task(s) ("
        tileToTake.tasks.forEach { out += "${it.colors}," }
        out = out.dropLast(1)
        out += ") and place it at ${indexToLetter(hint[1])}${hint[2] + 1}."
        if(hint[3] == 1){
            out += "You should fill the tiles!"
        }

        return out
    }

    /**
     *  Method to map indices (0-8) to letters (A-I).
     *
     *  @return a [String] with the letter.
     *  @throws IllegalArgumentException if the given index is smaller than 0 or greater than 8
     */
    private fun indexToLetter(index: Int): String {
        return when (index) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            4 -> "E"
            5 -> "F"
            6 -> "G"
            7 -> "H"
            8 -> "I"
            else -> throw IllegalArgumentException("")
        }
    }

    /**
     * Moves the token of the current Player
     * @param costs the new costs to the oldCosts
     */
    fun moveTokenOfCurrentPlayer(costs: Int){
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        game.currPlayer.totalCost = game.currPlayer.totalCost + costs
    }


    /**
     * goes through all of a player's unfinished tasks and checks if they are finished
     * (as long as the player still has tokens)
     * @param print added by AI. Function is used to simulate the game, and will fill the console with seemingly
     *          nonsensical statements. If print is false, only error messages will be printed.
     */
    fun updateTasks(print: Boolean = true) {

        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        tilesLoop@ for (i in game.currPlayer.tileGrid.tiles) {
            for (j in i) {
                if (j != null && !j.isFinished && game.currPlayer.numberOfTokens >=1) {
                    checkDoneTask(j,print)
                }
                if (game.currPlayer.numberOfTokens == 0) {
                    break@tilesLoop
                }
            }
        }

        if (game.currPlayer.numberOfTokens == 0) {
            onAllRefreshables {  refreshAfterResult() }
        }
    }

    /**
     * checks if one of the (not finished) tasks of a specific tile is finished after the player
     * placed a tile on the grid
     * @param tile is the tile whose tasks should be checked
     * @param print added by AI. Function is used to simulate the game, and will fill the console with seemingly
     *          nonsensical statements. If print is false, only error messages will be printed.
     */
    private fun checkDoneTask(tile: Tile,print: Boolean = true) {

        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val r = count(Color.RED, tile)
        val y = count(Color.YELLOW, tile)
        val b = count(Color.BLUE, tile)
        val t = count(Color.TURQUOISE, tile)
        var tempIsFinished = true


        taskLoop@ for (i in tile.tasks) {
            if (!i.isFinished) {
                val taskArray = i.taskToArray()
                val red = taskArray[0] <= r
                val yellow = taskArray[1] <= y
                val blue = taskArray[2] <= b
                val turquoise = taskArray[3] <= t
                val temp = red && yellow && blue && turquoise

                if (temp) {
                    i.isFinished = true
                    game.currPlayer.numberOfTokens--
                    if(print) {
                        println(
                            "The task " + taskToText(i) + " of the tile at (" + indexToLetter(tile.xPos)
                                    + ",${tile.yPos + 1}) is finished"
                        )
                    }
                }
                if (game.currPlayer.numberOfTokens == 0) {
                    break@taskLoop
                }
            }
        }

        for (i in tile.tasks) {
            if (!i.isFinished) {
                tempIsFinished = false
                break
            }
        }

        tile.isFinished = tempIsFinished
    }

    /**
     * counts the amount of neighbour tiles of one specific color for a given tile
     * @param tile is the tile of interest
     * @param color is the color of interest
     * @return returns the amount of neighbours of the color
     */
    fun count(color: Color, tile: Tile): Int {
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        var tileList = listOf(tile)
        var colorList = tileList
        do {
            tileList = colorList
            colorList = check(color, tileList)
        } while(tileList.size != colorList.size)

        return colorList.size - 1
    }

    /**
     * helper method for counting all horizontally and/or vertically adjacent tiles of the
     * same color together
     * @param tileList is the current list of adjacent tiles of the same color
     * @param color is the color of interest
     * @return returns a possibly enlarged list of adjacent tiles of the same color
     */
    private fun check(color: Color, tileList: List<Tile>): List<Tile> {
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val tiles = mutableListOf<Tile>()

        for (i in tileList){
            tiles.add(i)
        }

        val neighbours = mutableListOf<Tile>()

        for (i in tiles) {
            val tempNeighbours = game.getNeighbourTiles(game.currPlayer, i)
            for (j in tempNeighbours) {

                if ((j !in tiles) && (j !in neighbours)) {
                    neighbours.add(j)
                }
            }
        }

        for (i in neighbours) {
            if (i.color == color) {
                tiles.add(i)
            }
        }
        return tiles
    }

    /**
     * writes one task into text
     * @return returns the String of the task
     */
    private fun taskToText(task: Task): String{
        var result = ""
        for (i in task.colors) {
            result += when (i) {
                Color.RED -> "r"
                Color.YELLOW -> "y"
                Color.BLUE -> "b"
                Color.TURQUOISE -> "t"
            }
        }
        return result
    }

    /**
     * writes all information of a tile into text
     */
    private fun tileToText(tile: Tile) {
        println("tile with color " + tile.color.toString() + ", costs of " + tile.cost +
                "\nand the following " + tile.tasks.size + " tasks: "
        )
        for (i in tile.tasks) {
            print(taskToText(i) + "  ")
        }
        println("")
    }

}