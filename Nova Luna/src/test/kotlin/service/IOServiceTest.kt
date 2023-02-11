package service

import entity.*
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * Test of IO Service
 */
class IOServiceTest {
    private val path = "CsvDatei/nl_tiles.csv"
    /**
     * Checks the mapping of ids of different CSV
     * Every id should be unique
     */
    @Test
    fun loadTest(){
        val rootService = RootService()
        val pfade = mutableListOf<String>("CsvDatei/nl_tiles.csv", "CsvDatei/nl_tiles_random01.csv")
        val numberIDReference: MutableList<Int> = mutableListOf()
        val listID: MutableList<Int> = mutableListOf()
        for (i in 1..68) {
            numberIDReference.add(i)
        }
        for (name in pfade) {
            val (middleTiles, drawStack) = rootService.ioService.loadCsvTile(name)
            for (card in middleTiles) {
                if (card != null) {
                    listID.add(card.id)
                }
            }

            for (card in drawStack) {
                listID.add(card.id)
            }
            listID.sort()
            assertEquals(numberIDReference, listID)
            listID.clear()
        }
    }

    /**
     * Test loadCsv and return the middleTiles and drawStack
     * Test the last card of middleTiles and first card of draw stack
     * with reference to nl_tiles.csv (Card 11 and 12)
     */
    @Test
    fun  loadCSVTest(){
        val rootService = RootService()
        val (middleTiles, drawStack) = rootService.ioService.loadCsvTile(path)

        assertEquals(drawStack.size, 57)
        // 11 Elements should be not null because 1 field is used by the meeple
        var k = 0
        for (elem in middleTiles){
            if (elem != null){
                k++
            }
        }
        // Test of the last tile in MiddleCards
        assertEquals(k, 11)
        assertEquals(middleTiles[11]!!.cost, 4)
        assertEquals(middleTiles[11]!!.color, Color.TURQUOISE)
        assertEquals(middleTiles[11]!!.tasks, mutableListOf(
            Task(false, mutableListOf(Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE)),
            Task(false, mutableListOf(Color.BLUE, Color.BLUE))))

        // Test of drawStack
        assertEquals(drawStack[0].cost, 5)
        assertEquals(drawStack[0].color, Color.TURQUOISE)
        assertEquals(drawStack[0].tasks, mutableListOf(
            Task(false, mutableListOf(Color.TURQUOISE, Color.TURQUOISE)),
            Task(false, mutableListOf(Color.RED, Color.RED)),
            Task(false, mutableListOf(Color.YELLOW, Color.YELLOW))))
    }
    /**
     * Tests all the Attributes of game and player after the game is loaded
     */
    @Test
    fun saveAndLoad(){
        val rootService = RootService()
        val players = listOf(
            Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid())
        )
        rootService.gameService.startNewGame(players, path)
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        rootService.ioService.saveGame("save1")
        rootService.ioService.loadGame("save1")
        val game2 = rootService.novaLunaApplication.currentGame
        checkNotNull(game2)
        // Tests der Player
        for (i in 0 until game2.listOfPlayers.size){
            assertEquals(game2.listOfPlayers[i].totalCost, game.listOfPlayers[i].totalCost)
            assertEquals(game2.listOfPlayers[i].color, game.listOfPlayers[i].color)
            assertEquals(game2.listOfPlayers[i].name, game.listOfPlayers[i].name)
            assertEquals(game2.listOfPlayers[i].hintUsed, game.listOfPlayers[i].hintUsed)
            for (k in 0 until 9){
                for (j in 0 until 9){
                    if(game2.listOfPlayers[i].tileGrid.tiles[k][j] != null) {
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.cost,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.cost
                        )
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.color,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.color
                        )
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.id,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.id
                        )
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.isFinished,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.isFinished
                        )
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.tasks.size,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.tasks.size
                        )
                    }
                }
            }
        }
        assertEquals(game2.moonPos, game.moonPos)
        assertContentEquals(game2.middleTiles, game.middleTiles)
        assertEquals(game2.nMiddleTiles, game.nMiddleTiles)
        assertEquals(game2.nextState, game.nextState)
        assertEquals(game2.prevState, game.prevState)
        assertEquals(game2.drawStack.size, game.drawStack.size)
    }
   /* /**
     * Tests all the Attributes of game and player after the game is loaded
     */
    @Test
    fun saveAndLoad(){
        val rootService = RootService()
        val players = listOf(
            Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid())
        )
        rootService.gameService.startNewGame(players, path)
        val game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)

        rootService.ioService.saveGame("save1")
        val root2 = rootService.ioService.loadGame("save1").novaLunaApplication
        val game2 = root2.currentGame
        checkNotNull(game2)
        // Tests der Player
        for (i in 0 until game2.listOfPlayers.size){
            assertEquals(game2.listOfPlayers[i].totalCost, game.listOfPlayers[i].totalCost)
            assertEquals(game2.listOfPlayers[i].color, game.listOfPlayers[i].color)
            assertEquals(game2.listOfPlayers[i].name, game.listOfPlayers[i].name)
            assertEquals(game2.listOfPlayers[i].hintUsed, game.listOfPlayers[i].hintUsed)
            for (k in 0 until 9){
                for (j in 0 until 9){
                    if(game2.listOfPlayers[i].tileGrid.tiles[k][j] != null) {
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.cost,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.cost
                        )
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.color,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.color
                        )
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.id,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.id
                        )
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.isFinished,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.isFinished
                        )
                        assertEquals(
                            game2.listOfPlayers[i].tileGrid.tiles[k][j]!!.tasks.size,
                            game.listOfPlayers[i].tileGrid.tiles[k][j]!!.tasks.size
                        )
                    }
                }
            }
        }
        assertEquals(game2.moonPos, game.moonPos)
        assertContentEquals(game2.middleTiles, game.middleTiles)
        assertEquals(game2.nMiddleTiles, game.nMiddleTiles)
        assertEquals(game2.nextState, game.nextState)
        assertEquals(game2.prevState, game.prevState)
        assertEquals(game2.drawStack.size, game.drawStack.size)
    }*/
}