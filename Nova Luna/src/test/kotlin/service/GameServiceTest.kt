package service

import entity.Grid
import entity.Player
import entity.PlayerColor
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for functions from [GameService].
 */
class GameServiceTest {
    private val path = "CsvDatei/nl_tiles.csv"

    /**
     * Tests if startNewGame recognizes [Player]-lists with duplicate [PlayerColor]s.
     */
    @Test
    fun startNewGamePlayerListTest(){
        var rootService = RootService()
        assertNull(rootService.novaLunaApplication.currentGame)
        //correct call
        val list1 = listOf(
            Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLUE, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid())
        )
        rootService.gameService.startNewGame(list1, path)
        assertNotNull(rootService.novaLunaApplication.currentGame)

        //call with duplicate PlayerColors
        rootService = RootService()
        assertNull(rootService.novaLunaApplication.currentGame)
        val list2 = listOf(
            Player("Willy", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Lukas", PlayerColor.BLACK, 21, 0, Grid()),
            Player("Dennis", PlayerColor.ORANGE, 21, 0, Grid()),
            Player("Lena", PlayerColor.WHITE, 21, 0, Grid())
        )
        assertFails{ rootService.gameService.startNewGame(list2, path) }
        assertNull(rootService.novaLunaApplication.currentGame)
    }
}