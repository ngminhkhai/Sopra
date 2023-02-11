package entity

import tools.aqua.bgw.visual.ColorVisual

/**
 * Enum class to distinguish between players colors.
 */
enum class PlayerColor {
    BLUE,
    BLACK,
    WHITE,
    ORANGE
    ;

    /**
     * @return corresponding [ColorVisual] to the player color
     */
    fun getVisual() : ColorVisual{
        return when(this){
            BLUE -> ColorVisual.BLUE
            BLACK -> ColorVisual.BLACK
            WHITE -> ColorVisual.WHITE
            ORANGE -> ColorVisual.ORANGE
        }
    }
}