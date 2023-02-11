package entity
/**
 * Entity to represent a player in the game "Swim". The Player consists of a name [pName], his [handCards]
 * and two variables, that show whether he/she [hasPassed] or [hasKnocked]
*/
 data class Player (
    val pName: String,
    var handCards : ArrayList<Card> = ArrayList(3)
) {
    var hasKnocked = false
    var hasPassed =false

    override fun toString(): String =
        "$pName: handCards$handCards"
    }
