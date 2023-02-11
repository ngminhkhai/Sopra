package entity
import kotlin.test.*
/**
 * Test cases for [Card]
 */
class CardTest {
    // Some cards to perform the tests with
    private val aceOfHeart = Card(CardSuit.HEARTS, CardValue.ACE)
    private val sixOfClubs = Card(CardSuit.CLUBS, CardValue.SIX)
    private val queenOfSpade = Card(CardSuit.SPADES, CardValue.QUEEN)
    private val otherQueenOfSpade = Card(CardSuit.SPADES, CardValue.QUEEN)
    private val twoOfDiamonds = Card(CardSuit.DIAMONDS, CardValue.TWO)

    // unicode characters for the suits, as those should be used by [Card.toString]
    private val heartsChar = '\u2665' // ♥
    private val diamondsChar = '\u2666' // ♦
    private val spadesChar = '\u2660' // ♠
    private val clubsChar = '\u2663' // ♣

    @Test
    fun testToString() {
        assertEquals(heartsChar + "A", aceOfHeart.toString())
        assertEquals(clubsChar + "6", sixOfClubs.toString())
        assertEquals(spadesChar + "Q", queenOfSpade.toString())
        assertEquals(diamondsChar + "2", twoOfDiamonds.toString())
    }
    /**
     * Check if toString produces a 2 character string for every possible card
     * except the 10 (for which length=3 is ensured)
     */
    @Test
    fun testToStringLength() {
        CardSuit.values().forEach {suit ->
            CardValue.values().forEach {value ->
                if (value == CardValue.TEN)
                    assertEquals(3, Card(suit, value).toString().length)
                else
                    assertEquals(2, Card(suit, value).toString().length)
            }
        }
    }

    /**
     * test for equals
     */
    @Test
    fun testEquals() {
        assertEquals(queenOfSpade, otherQueenOfSpade)
        assertNotSame(queenOfSpade, otherQueenOfSpade)
    }
}