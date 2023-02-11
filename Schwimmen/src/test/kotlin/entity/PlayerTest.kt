package entity
import kotlin.test.*
/**
 * Test cases for [Player]
 */
class PlayerTest {
    /**
     * instances to test with
     */
    private val player1 = Player("player1")
    private val player2 = Player("player2")
    private val player3 = Player("player3")
    private val aceOfHeart = Card(CardSuit.HEARTS, CardValue.ACE)
    private val sixOfClubs = Card(CardSuit.CLUBS, CardValue.SIX)
    private val queenOfSpade = Card(CardSuit.SPADES, CardValue.QUEEN)
    private val handCards = arrayListOf<Card>(aceOfHeart,sixOfClubs,queenOfSpade)
    private val heartsChar = '\u2665' // ♥
    private val spadesChar = '\u2660' // ♠
    private val clubsChar = '\u2663' // ♣

    /**
     * test for equals
     */

    @Test
    fun testEquals(){
        assertTrue { player1 == player1 }
        assertFalse { player1 == player2 }
    }

    /**
     * test for toString
     */
    @Test
    fun testToString(){
        player3.handCards=handCards
        assertEquals("player3: handCards["+heartsChar + "A, "+clubsChar+"6, "+spadesChar+"Q]",player3.toString())
    }
}