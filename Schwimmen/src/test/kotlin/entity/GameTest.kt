package entity
import kotlin.test.Test
import kotlin.test.assertEquals

import kotlin.test.assertTrue

/**
 * Test cases for [Game]
 */
class GameTest {

    /**
     * A PlayerList and Game instance to test with
     *
     */
    private val player1= Player("player1")
    private val player2=Player("player2")
    private val listOfPlayer = arrayListOf(player1,player2)
    private val game= Game(listOfPlayer)

    /**
     * Checks if nextPlayer works right
     */
    @Test
    fun testNextPlayer() {
        val test1 =game.currPlayerIndex
        if(test1==listOfPlayer.size){
        game.nextPlayer()
        assertTrue(1==game.currPlayerIndex )
        }
        else{
            game.nextPlayer()
            assertTrue(test1<game.currPlayerIndex )
        }
    }
    /**
     * Checks if getCurrentPlayer retrieves the right Player
     */
    @Test

    fun testGetCurrentPlayer(){
        val testIndex=game.currPlayerIndex
        val playerTest=game.getCurrentPlayer()
        assertEquals(listOfPlayer[testIndex],playerTest)
    }

}