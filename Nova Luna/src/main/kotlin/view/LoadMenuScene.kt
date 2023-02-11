package view

import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.dialog.FileDialog
import tools.aqua.bgw.dialog.FileDialogMode
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.io.File



/**
 * Load Scene which displays all the previously saved games. One can choose to continue one of those games.
 * Has to Buttons:
 * -[startButton] to start the selected saved Game
 * -[backButton] to return to [StartMenuScene]
 */
class LoadMenuScene : MenuScene(800, 1080), Refreshable {

    /**
     * head Label Load Game Menu
     */

    private val headlineLabel = Label(
        width = 300, height = 50, posX = 250, posY = 20,
        text = "Saved Games",
        font = Font(size = 30)
    )


    /**
     * FileDialog which opens the directory saved Games
     * here one can choose to select a previous game
     */
    var gameList = FileDialog(FileDialogMode.OPEN_FILE, title = "Saved Games" , initialDirectory = File("savedGames"))



    /**
     * Start button that starts the game. Gets activated once a game is selected
     */
    val startButton = Button(
        width = 200, height = 50,
        posX = 150, posY = 800,
        text = "Start Game",
        font = Font(25)
    ).apply {
        visual = ColorVisual(124, 179, 66)
        this.isDisabled = true
    }

    /**
     * Back Button to go back to the [StartMenuScene] or [PauseScene] .
     */
   val backButton = Button(
        width = 200, height = 50,
        posX = 450, posY = 800,
        text = "Back",
        font = Font(25)
    ).apply {
        visual = ColorVisual(244, 67, 54)
    }

    /**
     * Select Button opens the [gameList].
     */
    val selectButton = Button(
        width = 200, height = 50,
        posX = 300, posY = 500,
        text = "Select Game",
        font = Font(25)
    ).apply {
        visual = ColorVisual(129, 212, 250)

    }
    /**
     * Add all the components to the scene
     */
    init {
        addComponents(headlineLabel,startButton,backButton,selectButton)
    }



}