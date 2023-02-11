package service

import entity.*
import org.junit.jupiter.api.Test
import kotlin.test.*

/**
 * Test of PlayerActionService
 */
class PlayerActionServiceTest {
    private val path = "CsvDatei/nl_tiles.csv"
    /**
     * Player take tile
     * checks the new cost, meeplePos  and new middleTiles
     * Case 1: available Card is choosen middleTiles card become null, cost, meeplePos is updated
     * Case 2: middleTile behind the meeple is choosen. No change in cost, meeple position, middleTiles
     * Case 3: middleTile more than 3 cards further is choosen. No change in cost, meeplePos, middleTiles
     * Case 4: second available and correctCard is choosen. Change in cost, meeplePos, middleTiles
     */
    @Test
    fun takeTileTest(){
        val rootService = RootService()
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid()))
        rootService.gameService.startNewGame(players, path)
        var game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val tileChoosen = game.middleTiles[2]
        rootService.playerActionService.takeTile(tileChoosen)

        //update current game, because a new game object is inizialized by initNewGame function
        game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        // Tile is removed of the middleTiles
        assertEquals(game.middleTiles[2], null)
        // moonPos is where we took the Card
        assertEquals(game.moonPos, 2)
        checkNotNull(tileChoosen)
        // check if the cost where updated
        assertEquals(game.currPlayer.totalCost, tileChoosen.cost)

        // Choose tile to far away
        rootService.playerActionService.takeTile(game.middleTiles[1])
        assertEquals(game.moonPos, 2)
        assertNotEquals(game.middleTiles[1], null)
        // check if the cost where not updated
        assertEquals(game.currPlayer.totalCost, tileChoosen.cost)

        // Choose tile to far away
        rootService.playerActionService.takeTile(game.middleTiles[6])

        //update current game, because a new game object is inizialized by initNewGame function
        game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        assertEquals(game.moonPos, 2)
        // is tile not taken
        assertNotEquals(game.middleTiles[6], null)
        // check if the cost where not updated
        assertEquals(game.currPlayer.totalCost, tileChoosen.cost)

        // Chose second valid card
        val tileChoosen2 = game.middleTiles[5]
        val oldCost = game.currPlayer.totalCost
        rootService.playerActionService.takeTile(game.middleTiles[5])

        //update current game, because a new game object is inizialized by initNewGame function
        game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        assertEquals(game.moonPos, 5)
        // card taken?
        assertEquals(game.middleTiles[5], null)
        // cost updated correctly?
        checkNotNull(tileChoosen2)
        assertEquals(game.currPlayer.totalCost, oldCost + tileChoosen2.cost )
    }
    /**
     * Player places the card on the Grid
     * Case 1: First tile is choosen and can be placed everywhere in the grid of the current player
     * Case 2: Wrong positon of the card which has no neighbors of the placed cards on the board
     * Case 3: Position out of board is choosen. Player should choose again
     * Case 4: Correct positon of the newCard on the grid of the current Player
     */
    @Test
    fun  placeOnGridTest(){
        val rootService = RootService()
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid()))
        rootService.gameService.startNewGame(players, path)
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        // Test of one Tile taken
        val tileChoosen = game.middleTiles[2]
        rootService.playerActionService.placeOnGrid(tileChoosen, 3, 3)
        // is the tile in the Grid of the player?
        assertEquals(tileChoosen, game.currPlayer.tileGrid.tiles[3][3])
        checkNotNull(tileChoosen)
        assertEquals(tileChoosen.xPos, 3)
        assertEquals(tileChoosen.yPos, 3)

        // Test one tile which is placed wrong
        val tile2 = game.middleTiles[5]
        rootService.playerActionService.placeOnGrid(tile2, 2, 2)
        assertEquals(null, game.currPlayer.tileGrid.tiles[2][2])
        checkNotNull(tile2)
        assertEquals(tile2.xPos, -1)
        assertEquals(tile2.yPos, -1)
        // Test one tile out of grid
        val tile3 = game.middleTiles[5]
        rootService.playerActionService.placeOnGrid(tile3, -1, 2)
        val gridBefore = game.currPlayer.tileGrid.tiles
        assertContentEquals(gridBefore, game.currPlayer.tileGrid.tiles)
        checkNotNull(tile3)
        assertEquals(tile3.xPos, -1)
        assertEquals(tile3.yPos, -1)

        // Correct position of second tile
        val tileChoosen2 = game.middleTiles[4]
        rootService.playerActionService.placeOnGrid(tileChoosen2, 3, 2)
        // is the tile in the Grid of the player?
        assertEquals(tileChoosen2, game.currPlayer.tileGrid.tiles[3][2])
        checkNotNull(tileChoosen2)
        assertEquals(tileChoosen2.xPos, 3)
        assertEquals(tileChoosen2.yPos, 2)
    }
    /**
     * function to test the fill tiles action
     * 1. try to fill when there are more then 2 tiles in the middle
     * 2. try to fill when the draw stack is not big enough
     * 3. try to fill when drawstack = 0 and middletiles = 0
     */
    @Test
    fun fillTilesTest(){
        val rootService = RootService()
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid()))
        rootService.gameService.startNewGame(players, path)
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        //1.
        val beforeFill = game.middleTiles
        rootService.playerActionService.fillTiles()
        assertEquals(beforeFill, game.middleTiles)

        //2.
        for (i in 0..11){
            game.middleTiles[i] = null
        }
        game.drawStack = game.drawStack.subList(0,1).toMutableList()
        val afterChosingAllTiles = game.middleTiles
        rootService.playerActionService.fillTiles()
        assertEquals(afterChosingAllTiles,game.middleTiles)

        //3.
        game.drawStack = game.drawStack.dropLast(1).toMutableList()
        for(i in 0..11){
            game.middleTiles[i] = null
        }
        rootService.playerActionService.fillTiles()
        //hier kommt der println() dass das Spiel zu ende ist weil der Nachziehstapel = 0 und MiddleTiles = 0 sind
    }

    /**
     *  try to fill when middletiles <= 2  && drawstack big enough
     */
    @Test
    fun fillTilesTestSuccessfully(){
        val rootService = RootService()
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid()))
        rootService.gameService.startNewGame(players, path)
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        for (i in 0..11){
            game.middleTiles[i] = null
        }


        val toAddTilesFromDrawStack = ArrayDeque<Tile>()
        for(i in game.drawStack.size-11-1 until game.drawStack.size){
            toAddTilesFromDrawStack.add(game.drawStack[i])
        }

        val oldMiddleTiles = ArrayDeque<Tile>()
        for (tile in game.middleTiles){
            if (tile != null){
                oldMiddleTiles.add(tile)
            }
        }

        rootService.playerActionService.fillTiles()

        val newAddedMiddleTiles = ArrayDeque<Tile>()
        for(tile in game.middleTiles){
            if (tile!=null) {
                for (tile2 in oldMiddleTiles) {
                    if (tile != tile2) {
                        newAddedMiddleTiles.add(tile)
                    }
                }
            }
        }
        var matches = 0
        for (tile in game.middleTiles){

            if(tile !=null) {
                for (tile2 in toAddTilesFromDrawStack) {
                    if (tile == tile2) {
                        matches++
                    }
                }
            }
        }
        //part to check if every middleTile comes from the drawStack
        var allIn = true
        for (tile in game.middleTiles){
            if (tile!=null){
                val foundIndex = toAddTilesFromDrawStack.indexOf(tile)

                if (foundIndex==-1){

                    allIn =  false
                }else{
                    //println(tile)
                    //println(toAddTilesFromDrawStack[foundIndex])
                }
            }
        }
        assertTrue { allIn }
    }

    /**
     * for debugging
     */
    /**
    private fun printStack(tileList : MutableList<Tile>){
        for(a in tileList){
            println(a)
        }
    }
    **/
    /**
     * checks if the cost of the player has updated after calling the  moveTokenOfCurrPlayer()
     */
    @Test
    fun moveCurrPlayerTokenTest(){

        val rootService = RootService()
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid()))
        rootService.gameService.startNewGame(players, path)
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val oldPosition  = game.currPlayer.totalCost
        rootService.playerActionService.moveTokenOfCurrPlayer(4)
        val newPosition = game.currPlayer.totalCost

        assertEquals(oldPosition,newPosition-4)

    }

    /**
     * Test for undo & redo function. Starts with an illegal call of the undo function (do undo in first turn of the
     * whole game), then tests undo function after a turn was done. The third test checks if the redo function works
     * properly by going back to the last turn that was made and in the last test an illegal call of the redo function
     * is tested (redo in the last turn that was ever made).
    **/
    @Test
    fun undoAndRedoTest(){
        val rootService = RootService()
        assertFails { rootService.playerActionService.undo() }
        assertFails { rootService.playerActionService.redo() }
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid()))
        rootService.gameService.startNewGame(players, path)
        val firstGame = rootService.novaLunaApplication.currentGame
        checkNotNull(firstGame)

        //first test
        assertFails { rootService.playerActionService.undo() }

        val tileToPlace = firstGame.middleTiles[1]
        checkNotNull(tileToPlace)
        rootService.playerActionService.takeTile(tileToPlace)
        val secondGame = rootService.novaLunaApplication.currentGame
        checkNotNull(secondGame)
        assertNotEquals(firstGame, secondGame)
        rootService.playerActionService.placeOnGrid(tileToPlace, 0, 0)
        rootService.playerActionService.endTurn()
        rootService.playerActionService.undo()

        //second test
        var gameToCompare = rootService.novaLunaApplication.currentGame
        checkNotNull(gameToCompare)
        assert(gameToCompare == firstGame)

        //third test
        rootService.gameService.startNextTurn()
        rootService.playerActionService.redo()
        gameToCompare = rootService.novaLunaApplication.currentGame
        checkNotNull(gameToCompare)
        assert(gameToCompare == secondGame)

        //fourth test
        assertFails("There is no next game state!") { rootService.playerActionService.redo() }

        //test for redoNotPossible = false
        rootService.gameService.startNewGame(players, path)
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        val tile = game.middleTiles[1]
        rootService.playerActionService.takeTile(tile)
        rootService.playerActionService.undo()
        assertFails { rootService.playerActionService.redo() }
    }

    /**
     * Checks if the updateTask method (and within the checkDoneTask, count, and check methods) works correctly
     */
    @Test
    fun updateTaskTest(){
        val rootService = RootService()
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid()))
        rootService.gameService.startNewGame(players, path)
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        val exercise1 = mutableListOf(Color.BLUE,Color.BLUE,Color.BLUE,Color.BLUE)
        val exercise2 = mutableListOf(Color.BLUE,Color.YELLOW,Color.RED)
        val exercise3 = mutableListOf(Color.BLUE,Color.RED)
        val exercise4 = mutableListOf(Color.BLUE,Color.YELLOW,Color.RED)
        val task1 = Task(false,exercise1)
        val task2 = Task(false,exercise2)
        val task3 = Task(false,exercise3)
        val task4 = Task(false,exercise4)
        val taskList1 = mutableListOf(task1,task2)
        val taskList2 = mutableListOf<Task>()
        val taskList3 = mutableListOf(task3,task4)

        val yellowTile1 = Tile(4,false,Color.YELLOW,taskList1,-1,3,3)
        val blueTile1 = Tile(1,false,Color.BLUE,taskList2,-1,2,3)
        val blueTile2 = Tile(1,false,Color.BLUE,taskList2,-1,3,4)
        val blueTile3 = Tile(1,false,Color.BLUE,taskList3,-1,3,5)
        val blueTile4 = Tile(1,false,Color.BLUE,taskList2,-1,2,5)
        val yellowTile2 =  Tile(1,false,Color.YELLOW,taskList2,-1,3,2)
        val redTile1 = Tile(1,false,Color.RED,taskList2,-1,4,3)

        game.currPlayer.tileGrid.tiles[3][3] = yellowTile1
        game.currPlayer.tileGrid.tiles[4][3] = redTile1
        game.currPlayer.tileGrid.tiles[2][3] = blueTile1
        game.currPlayer.tileGrid.tiles[3][4] = blueTile2
        game.currPlayer.tileGrid.tiles[3][5] = blueTile3
        game.currPlayer.tileGrid.tiles[3][2] = yellowTile2
        game.currPlayer.tileGrid.tiles[2][5] = blueTile4

        val tileToCheck = game.currPlayer.tileGrid.tiles[3][3]
        val tileToCheck2 = game.currPlayer.tileGrid.tiles[3][5]
        checkNotNull(tileToCheck)
        checkNotNull(tileToCheck2)

        val result = rootService.playerActionService.count(Color.BLUE, tileToCheck)
        assertEquals(4, result)

        rootService.playerActionService.updateTasks()
        assertEquals(true, tileToCheck.isFinished)
        assertEquals(false,tileToCheck2.isFinished)
        assertEquals(false, tileToCheck2.tasks[0].isFinished)

        val redTile2 = Tile(1,false,Color.RED,taskList2,-1,3,6)
        game.currPlayer.tileGrid.tiles[3][6] = redTile2
        rootService.playerActionService.updateTasks()
        assertEquals(false,tileToCheck2.isFinished)
        assertEquals(true, tileToCheck.tasks[0].isFinished)

        val redTile3 = Tile(1,false,Color.RED,taskList2,-1,4,6)
        val redTile4 = Tile(1,false,Color.RED,taskList2,-1,4,5)
        game.currPlayer.tileGrid.tiles[4][6] = redTile3
        game.currPlayer.tileGrid.tiles[4][5] = redTile4

        val result2 = rootService.playerActionService.count(Color.RED, tileToCheck2)
        assertEquals(3,result2)

    }

    @Test
    fun getHintTest(){
        val rootService = RootService()
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid()))
        rootService.gameService.startNewGame(players, path)
        println(rootService.playerActionService.getHint())
    }

    /**
     * Test for private function initNewGame.
     * Only works if initNewGame is not private!!
     */
    /*
    @Test
    fun initNewGameTest(){
        val rootService = RootService()
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid()))
        rootService.gameService.startNewGame(players, path)

        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        rootService.playerActionService.initNewGame()
        val secGame = rootService.novaLunaApplication.currentGame
        checkNotNull(secGame)

        assertEquals(secGame.listOfLastPayersColor, game.listOfLastPayersColor)
        assertNotEquals(secGame, game)

        secGame.listOfLastPayersColor.add(PlayerColor.BLUE)
        assertNotEquals(secGame.listOfLastPayersColor, game.listOfLastPayersColor)

        secGame.listOfPlayers.forEach { println(it.color) }
    }
    */
}