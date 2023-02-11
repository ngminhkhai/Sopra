package entity

import java.io.Serializable

/**
 * Abstract class to define necessary attributes of an actor in the game.
 * @param name Name of the player
 * @param color Players Color.
 * @param numberOfTokens Number of a players tokens, default is 21
 * @param totalCost
 * @param hintUsed Flag wether a player has used a hint yet or not. Default is false.
 * @param wantToFillTiles Flag wether a player want to fill the tiles when only 2 or less tiles are available
 * @param tileGrid Grid the Player places his tiles in.
 */
abstract class AbstractPlayer(val name : String,
                              val color: PlayerColor,
                              var numberOfTokens : Int = 21,
                              var totalCost : Int,
                              var hintUsed : Boolean = false,
                              var wantToFillTiles : Boolean = false,
                              val tileGrid: Grid): Serializable