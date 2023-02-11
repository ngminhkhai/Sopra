package entity

/**
 * Enum to distinguish between the colors of a tile or task.
 * Order of the enums represents the order in the tile map.
 */
enum class Color{
    YELLOW,
    RED,
    BLUE,
    TURQUOISE
    ;

    override fun toString(): String =
        when(this){
            YELLOW -> "yellow"
            RED -> "red"
            BLUE -> "blue"
            TURQUOISE -> "turquoise"
        }

}
