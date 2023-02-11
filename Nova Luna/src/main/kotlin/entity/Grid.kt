package entity

import java.io.Serializable

/**
 * Data class represents the grid in which a player places his tiles
 * @param tiles Already placed tiles.
 * Realized with a two-dimensional Array, because the grid should never be bigger than 9x9 Tiles. Every Tile is nullable,
 * and null, if there is no tile yet
 */
data class Grid(var tiles: Array<Array<Tile?>> = Array(9){ arrayOfNulls(9) }): Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Grid

        if (!tiles.contentDeepEquals(other.tiles)) return false

        return true
    }

    override fun hashCode(): Int {
        return tiles.contentDeepHashCode()
    }

    /**
     * Returns deep copy of this object.
     */
    fun deepCopy(): Grid {
        val newGrid = Grid()
        for(i in this.tiles.indices){
            for(j in this.tiles[i].indices){
                val newTile = this.tiles[i][j]
                if(newTile != null){
                    newGrid.tiles[i][j] = newTile.deepCopy()
                }
            }
        }
        return newGrid
    }
}