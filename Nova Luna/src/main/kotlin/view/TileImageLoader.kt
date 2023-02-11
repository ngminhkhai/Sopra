package view

import entity.PlayerColor
import entity.Tile
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Provides access to the src/main/resources/TileMap.png file that contains all tile images
 * in a raster. From the tile map a single tile in form of a [BufferedImage] can be retrieved.
 * The returned [BufferedImage] are 158x158 pixels.
 */
class TileImageLoader {

    /**
     * [BufferedImage] to get the sub image out of the tile map
     */
    private val tileMapImage : BufferedImage = ImageIO.read(TileImageLoader::class.java.getResource(TILE_MAP_PATH))

    /**
     * [BufferedImage] to get the sub image out of the task token map
     */
    private val taskMapImage : BufferedImage = ImageIO.read(TileImageLoader::class.java.getResource(TASK_TOKEN_MAP_PATH))

    /**
     * Provides an image for the given [Tile].
     * @param tile The tile to get the image for
     * @return Image of the tile as [BufferedImage]
     * @throws IllegalArgumentException If [Tile.id] is not in the range of 1 to 68
     */
    fun loadTileImage(tile : Tile) : BufferedImage{
        require(tile.id in 1..68){ "Tile id not in the range of 1 to 68"}
        return tileMapImage.getSubimage(
            ((tile.id - 1) % FILE_NUMBER_OF_COLS) * TILE_IMAGE_WIDTH,
            ((tile.id - 1) / FILE_NUMBER_OF_ROWS) * TILE_IMAGE_HEIGHT,
            TILE_IMAGE_WIDTH,
            TILE_IMAGE_HEIGHT
        )
    }

    /**
     * Provides an image of a task token that can cover the task on a tile view.
     * @param color The color this token should have
     * @param taskNumber The task this task token should cover.
     */
    fun loadTaskTokenImage(color : PlayerColor, taskNumber : Int) : BufferedImage{
        require(taskNumber in 0..2)
        return taskMapImage.getSubimage(
            taskNumber * TILE_IMAGE_WIDTH,
            color.ordinal * TILE_IMAGE_HEIGHT,
            TILE_IMAGE_WIDTH,
            TILE_IMAGE_HEIGHT
        )
    }
}