package entity

import java.io.Serializable

/**
 * Data class to represent a task on a tile.
 * @param isFinished Flag for the status of task object.
 * @param colors Array of the colors (of a tile) the task demands.
 */
data class Task(var isFinished: Boolean = false, val colors: MutableList<Color>): Serializable {

    /**
     * converts the tasks into an array that contains the number of required colors
     * @return returns the calculated array
     */
    fun taskToArray(): IntArray {
        val task = intArrayOf(0, 0, 0, 0) //[r, y, b, t]
        for (i in colors) {
            when(i) {
                Color.RED -> task[0]++
                Color.YELLOW -> task[1]++
                Color.BLUE -> task[2]++
                Color.TURQUOISE -> task[3]++
            }
        }
        return task
    }



    /**
     * Returns deep copy of this object. 
     */
    fun deepCopy() : Task{
        val newColors = mutableListOf<Color>()
        this.colors.forEach { newColors.add(it) }
        return Task(this.isFinished, newColors)
    }



}