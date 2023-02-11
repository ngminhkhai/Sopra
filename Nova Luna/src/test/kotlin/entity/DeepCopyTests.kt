package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for deepCopy() methods from our entity-layer.
 */
class DeepCopyTests {
    /**
     * Test for deepCopy method from [Task]
     */
    @Test
    fun taskCopyTest(){
        val task1 = Task(colors = mutableListOf(Color.BLUE, Color.BLUE))
        val task2 = task1.deepCopy()

        assert(task1 !== task2)
        assertEquals(task1.isFinished, task2.isFinished)
        assert(task1.colors !== task2.colors)
        assertEquals(task1.colors, task2.colors)
    }

    /**
     * Test for deepCopy method from [Tile]
     */
    @Test
    fun tileCopyTest(){
        val tile1 = Tile(10, false, Color.BLUE,
            listOf(Task(colors = mutableListOf(Color.BLUE, Color.BLUE))), 10, 20, 30)
        val tile2 = tile1.deepCopy()

        assert(tile1 !== tile2)
        assertEquals(tile1.cost, tile2.cost)
        assertEquals(tile1.color, tile2.color)
        assertEquals(tile1.isFinished, tile2.isFinished)
        assertEquals(tile1.tasks, tile2.tasks)
        assert(tile1.tasks !== tile2.tasks)
        assertEquals(tile1.id, tile2.id)
        assertEquals(tile1.xPos, tile2.xPos)
        assertEquals(tile1.yPos, tile2.yPos)
    }

    /**
     * Test for deepCopy method from [Grid]
     */
    @Test
    fun gridCopyTest(){
        val grid1 = Grid()
        grid1.tiles[3][2] = Tile(10, false, Color.BLUE,
            listOf(Task(colors = mutableListOf(Color.BLUE, Color.BLUE))), 10, 20, 30)
        val grid2 = grid1.deepCopy()

        assert(grid1 !== grid2)
        assertEquals(grid1, grid2)
        assert(grid1.tiles !== grid2.tiles)
    }

    /**
     * Test for deepCopy method from [Player]
     */
    @Test
    fun playerCopyTest(){
        val player1 = Player("Lukas", PlayerColor.BLUE, 21, 0, Grid())
        val player2 = player1.deepCopy()

        assert(player1 !== player2)
        assertEquals(player1.name, player2.name)
        assertEquals(player1.color, player2.color)
        assertEquals(player1.numberOfTokens, player2.numberOfTokens)
        assertEquals(player1.totalCost, player2.totalCost)
        assertEquals(player1.tileGrid, player2.tileGrid)
        assert(player1.tileGrid !== player2.tileGrid)
        assertEquals(player1.hintUsed, player2.hintUsed)
        assertEquals(player1.wantToFillTiles, player2.wantToFillTiles)
    }

    /**
     * Test for deepCopy method from [AI]
     */
    @Test
    fun aiCopyTest(){
        val player1 = AI(Difficulty.HARD ,"Lukas", PlayerColor.BLUE, 21, 0, Grid())
        val player2 = player1.deepCopy()

        assert(player1 !== player2)
        assertEquals(player1.name, player2.name)
        assertEquals(player1.color, player2.color)
        assertEquals(player1.numberOfTokens, player2.numberOfTokens)
        assertEquals(player1.totalCost, player2.totalCost)
        assertEquals(player1.tileGrid, player2.tileGrid)
        assert(player1.tileGrid !== player2.tileGrid)
        assertEquals(player1.hintUsed, player2.hintUsed)
        assertEquals(player1.wantToFillTiles, player2.wantToFillTiles)
        assertEquals(player1.difficulty, player2.difficulty)
    }

    /**
     * Test for deepCopy method from [Game]
     */
    @Test
    fun gameCopyTest(){
        val players = listOf(Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()))
        val game1 = Game(7, 5, players[0], null, null,
            arrayOf(Tile(10, false, Color.BLUE,
                listOf(Task(colors = mutableListOf(Color.BLUE, Color.BLUE))), 10, 20, 30)),
            mutableListOf(Tile(10, false, Color.BLUE,
                listOf(Task(colors = mutableListOf(Color.BLUE, Color.BLUE))), 10, 20, 30)),
            players, mutableListOf(PlayerColor.WHITE)
        )
        val game2 = game1.deepCopy()

        assert(game1 !== game2)
        assertEquals(game1.moonPos, game2.moonPos)
        assertEquals(game1.nMiddleTiles, game2.nMiddleTiles)
        assert(game1.currPlayer !== game2.currPlayer)
        assertEquals(game1.nextState, game2.nextState)
        assertEquals(game1.prevState, game2.prevState)
        assert(game1.middleTiles !== game2.middleTiles)
        assert(game1.listOfPlayers !== game2.listOfPlayers)
        assertEquals(game1.listOfLastPayersColor, game1.listOfLastPayersColor)
        assert(game1.listOfLastPayersColor !== game2.listOfLastPayersColor)
    }
}