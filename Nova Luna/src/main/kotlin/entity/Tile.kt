package entity

import java.io.Serializable

/**
 * Data class to represent one tile.
 * @param cost the tiles cost.
 * @param isFinished flag for status of the tiles tasks.
 * @param color The Tiles Color.
 * @param id ID of the tile corresponding to the tile map
 */
data class Tile(val cost : Int,
                var isFinished : Boolean = false,
                val color: Color,
                val tasks : List<Task>,
                val id : Int = -1,
                var xPos: Int = -1,
                var yPos: Int = -1): Serializable {

    /**
     * Returns deep copy of this object.
     */
    fun deepCopy()
    : Tile {
        val newTask = mutableListOf<Task>()
        this.tasks.forEach { newTask.add(it.deepCopy()) }
        return Tile(this.cost, this.isFinished, this.color, newTask, this.id, this.xPos, this.yPos)
    }



}
