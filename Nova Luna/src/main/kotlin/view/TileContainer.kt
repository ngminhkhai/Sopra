package view


import entity.PlayerColor
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual

/**
 * A TileContainer is a special [Area] mainly to receive a single [TileView] per drag and drop.
 * It can show if a tile is dragged over it ([showSelection]) and tokens for finished tasks can be added more
 * conveniently ([showTaskToken]). "OnDragDropped" and "dropAcceptor" should not be overwritten, instead use
 * [acceptDropWhen].
 */
class TileContainer  (
    posX: Number = 0,
    posY: Number = 0,
    width: Number = TILE_SIZE,
    height: Number = TILE_SIZE,
    defaultVisual: Visual = Visual.EMPTY
): Area<TokenView>(posX = posX, posY = posY, width = width, height = height, visual = defaultVisual){

    /**
     * Custom Lambda to check if a [TileView] can be dropped into this container.
     * Has to return a Boolean. Default is always true
     */
    var acceptDropWhen : () -> Boolean = {true}

    /**
     * Array of booleans to remember which task token have been added to this container
     */
    private val taskTokenShown = Array(3){ false }

    init {
        this.dropAcceptor = { dragEvent ->
            when (dragEvent.draggedComponent) {
                is TileView -> dropConstraint()
                else -> false
            }
        }
        this.onDragDropped = { dragEvent ->
            this.add((dragEvent.draggedComponent as TileView).apply { reposition(0, 0) })
            this.visual = Visual.EMPTY
        }
    }

    /**
     * Returns true if one of the given tasks can be added to the area as task token.
     * @param tasks The tasks that should be covered (0,1 and 2)
     * @throws IllegalArgumentException If there are more than 3 tasks or 0 and if the range of the tasks is not 0..2
     */
    fun canShowTaskToken(vararg tasks : Int) : Boolean{
        require(tasks.size in 1..3)
        require(tasks.none { it < 0 || it > 2 })
        for(t in tasks){
            if(!taskTokenShown[t]){
                return true
            }
        }
        return false
    }

    /**
     * Clear the container and reset [taskTokenShown].
     * Should be used instead of clear (if this container is used for task token)
     */
    fun resetContainer(){
        for(i in taskTokenShown.indices){
            taskTokenShown[i] = false
        }
        this.clear()
    }

    /**
     * @return true if this container contains a [TileView]
     */
    fun hasTileView() : Boolean{
        return this.components.find { it is TileView } != null
    }

    /**
     * @return The first [TileView] that is found in this container.
     * (Since you can add more TileViews by calling the add method, there can be more than on tile view)
     * Returns null if non are found.
     */
    fun getTileView() : TileView?{
        val tView = this.components.find { it is TileView }
        if(tView != null){
            return tView as TileView
        }
        return null
    }

    /**
     * Calling this method once overwrites the visual everytime a [TileView] is dragged over this container or away
     * from this container.
     */
    fun showSelection(pathUnselected: String = "SingleTile.png", pathSelected : String = "SingleTileMarker.png"){
        this.visual = ImageVisual(pathUnselected)
        this.onDragGestureEntered = {
            if(dropConstraint()){
                this.visual = ImageVisual(pathSelected)
            }
        }
        this.onDragGestureExited = {
            this.visual = ImageVisual(pathUnselected)
        }
    }

    /**
     * Show task token. If a task token already exists for this task nothing happens.
     * @param color The color of the task token
     * @param tasks The tasks that should be covered (0,1 and 2)
     * @throws IllegalArgumentException If there are more than 3 tasks or 0 and if the range of the tasks is not 0..2
     */
    fun showTaskToken(color: PlayerColor, vararg tasks : Int){
        require(tasks.size in 1..3)
        require(tasks.none { it < 0 || it > 2 })
        if(this.hasTileView()){
            val loader = TileImageLoader()
            for(t in tasks){
                if(!this.taskTokenShown[t]){
                    this.add(TokenView(0,0,
                        TILE_SIZE, TILE_SIZE, ImageVisual(loader.loadTaskTokenImage(color, t))))
                    this.taskTokenShown[t] = true
                    }
                }
            }
        }

    /**
     * Constraint to see if a [TileView] can be dropped into this container.
     */
    private fun dropConstraint() : Boolean{
        return !this.hasTileView() && acceptDropWhen()
    }
}