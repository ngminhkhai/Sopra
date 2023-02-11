package view

import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual

/**
 * A [Pane] to represent a stack of tiles with a [Label] that shows the number of remaining tiles in the stack
 * if the mouse is hovered over
 */
class TileStack (
    posX : Number = 0,
    posY : Number = 0,
    width : Number = TILE_IMAGE_WIDTH,
    height : Number = TILE_IMAGE_HEIGHT,
): Pane<ComponentView>(posX,posY,width,height) {

    /**
     * [Label] to show the number of tiles
     */
    private val numberOfTiles = Label(0,0, width, height).apply {
        font = Font(18, java.awt.Color.WHITE, fontWeight = Font.FontWeight.BOLD)
        opacity = 0.0
    }

    init {
        this.visual = ImageVisual("tileStack.png")
        this.onMouseEntered = {
            this.numberOfTiles.opacity = 1.0
        }
        this.onMouseExited = {
            this.numberOfTiles.opacity = 0.0
        }
        this.add(numberOfTiles)
    }

    /**
     * Update the number of tiles that is shown. Should be called after a fill tile action or a redo action.
     * Sets other visual if number of tiles is 0.
     */
    fun updateNumberOfTiles(nTiles : Int){
        numberOfTiles.text = "Tiles left: $nTiles"
        if(nTiles == 0){
            this.visual = Visual.EMPTY
        }
    }

    /**
     * Reset the visual. Should be called after loading/undoing.
     */
    fun restoreVisual(){
        this.visual = ImageVisual("tileStack.png")
    }
}