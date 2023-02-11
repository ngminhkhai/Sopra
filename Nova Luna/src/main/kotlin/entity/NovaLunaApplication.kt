package entity

import java.io.Serializable

/**
 * Data class holds a [highscore] and the current [Game]
 */
class NovaLunaApplication: Serializable {
    var highscore = Highscore()
    var currentGame : Game? = null
}