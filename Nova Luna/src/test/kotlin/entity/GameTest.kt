package entity

import org.junit.jupiter.api.Test
import service.RootService
import kotlin.test.assertEquals


class GameTest {
    private val path = "CsvDatei/nl_tiles.csv"

    /**
     * testing the player swap by creating a game with 2 players that first draw both a tile with the same cost
     * then its the turn of the 2. player twice because he is on top of the first player and after that its the
     * first players turn
     */
    @Test
    fun testNextPlayer(){
        val rootService = RootService()
        val players = listOf(Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid())
        )
        val taskList2 = mutableListOf<Task>()
        val yellowTile1 = Tile(4,false,Color.YELLOW,taskList2,-1,3,3)
        val yellowTile2 = Tile(4,false,Color.YELLOW,taskList2,-1,3,3)
        rootService.gameService.startNewGame(players, path)
        var game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        var player1 = game.listOfPlayers[0]
        //first player is the first to enter in menu scene
        assertEquals(player1,game.currPlayer)
        game.middleTiles[1] = yellowTile1
        val toTakeTile = game.middleTiles[1]
        rootService.playerActionService.takeTile(toTakeTile)
        //updates because working with new game instance
        game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        player1 = game.listOfPlayers[0]
        var player2 = game.listOfPlayers[1]
        rootService.playerActionService.placeOnGrid(toTakeTile,0,0)

        //first player only took 1 tile so his totalcosts are the costs of the tile
        //switching to the next round
        rootService.playerActionService.endTurn()
        assertEquals(player1.totalCost,toTakeTile?.cost)

        //player 2 taking a tile with the same costs, so they both land on the same field on the board
        game.middleTiles[2] = yellowTile2
        val toTakeTile2 = game.middleTiles[2]
        assertEquals(player2,game.currPlayer)
        rootService.playerActionService.takeTile(toTakeTile2)
        //updates because working with new game instance
        game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        player1 = game.listOfPlayers[0]
        player2 = game.listOfPlayers[1]
        rootService.playerActionService.placeOnGrid(toTakeTile2,0,0)
        assertEquals(player2.totalCost,player1.totalCost)
        rootService.playerActionService.endTurn()


        //then the player2 is playing again because he is "on top" of player 1
        assertEquals(player2,game.currPlayer)
        val toTakeTile4 = game.middleTiles[4]
        rootService.playerActionService.takeTile(toTakeTile4)
        //updates because working with new game instance
        game = rootService.novaLunaApplication.currentGame
        checkNotNull(game)
        player1 = game.listOfPlayers[0]
        rootService.playerActionService.placeOnGrid(toTakeTile4,1,0)
        rootService.playerActionService.endTurn()

        //now the player 1 is again because he is behind the player2
        assertEquals(player1,game.currPlayer)
    }
    /**
     * Checks if the getNeighbourTiles method works correctly and puts the
     * (at most four) possible neighbours of a tile into a list
     */
    @Test
    fun getNeighbourTilesTest(){

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
        val task1 = Task(false,exercise1)
        val task2 = Task(false,exercise2)
        val taskList1 = mutableListOf(task1,task2)
        val taskList2 = mutableListOf<Task>()

        val yellowTile1 = Tile(4,false,Color.YELLOW,taskList1,-1,3,3)
        val blueTile1 = Tile(1,false,Color.BLUE,taskList2,-1,2,3)
        val blueTile2 = Tile(1,false,Color.BLUE,taskList2,-1,3,4)
        val blueTile3 = Tile(1,false,Color.BLUE,taskList2,-1,3,5)
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
        checkNotNull(tileToCheck)

        val resultList = mutableListOf<Tile>()
        resultList.add(blueTile1)
        resultList.add(redTile1)
        resultList.add(yellowTile2)
        resultList.add(blueTile2)


        val listToCheck = game.getNeighbourTiles(game.currPlayer,tileToCheck)

        assertEquals(resultList,listToCheck)




    }
}