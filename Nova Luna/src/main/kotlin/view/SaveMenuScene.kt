package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.ListView
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.io.File

/**
 * Save Menu Scene which shows the list of saved Games
 * shows a gameList where
 * has a TextInputField to save game under given name
 */
class SaveMenuScene (private val rootService: RootService) : MenuScene(800, 1080), Refreshable {
    /**
     * headLabel Save Game Menu
     */
    private val headlineLabel = Label(
        width = 300, height = 50, posX = 250, posY = 20,
        text = "Save Game Menu",
        font = Font(size = 30)
    )

    /**
     * temporary list of saved games (for the visuals)
     */
    private var gameList = getSavedGamesList()


    /**
     * gameListView to visualize [gameList]
     */
    private var gameListView= ListView(
        posX = 150, posY = 100,
        width = 500, height = 500,
        gameList,
        font= Font(30)
    )

    /**
     * Save button that saves the game via saveGame. Gets activated once a game is selected method.
     * gets activated as soon as a name is typed in
     */
    private val saveButton = Button(
        width = 200, height = 50,
        posX = 150, posY = 800,
        text = "Save Game",
        font = Font(25)
    ).apply {
        visual = ColorVisual(124, 179, 66)
        this.isDisabled = true
        onMouseClicked={
            rootService.ioService.saveGame(gameName.text)
            updateGameList()
        }
    }

    /**
     * Back Button to go back to the [PauseScene].
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
     * [TextField] where players enter the name under which the current game shall be saved under.
     * [saveButton] is unlocked only if you've entered a name
     */
    private val gameName : TextField =
        TextField(
            width = 200, height = 35,
            posX = 300, posY = 700
        ).apply {
        onKeyTyped={ saveButton.isDisabled = this.text.isBlank() }
    }

    /**
     * Add all the components to the scene.
     */
    init {
        addComponents(headlineLabel,saveButton,gameListView,backButton,gameName)
    }

    /**
     * Function to get the list of all saved games to give to [gameListView]
     * @return the saved Games as a list of strings
     */
    private fun getSavedGamesList ():List<String>{
        val tmpList=ArrayList<String>()
        val savedGames = File("savedGames")
        savedGames.listFiles()?.forEach {
                tmpList.add(it.name.removeSuffix(".bin"))
         }
        return tmpList
    }

    /**
     * updates the [gameListView] for saved Games after pressing the [saveButton]
     */
    private fun updateGameList(){
        removeComponents(gameListView)
        gameList=getSavedGamesList()
        gameListView = ListView(
                posX = 150, posY = 100,
        width = 500, height = 500,
        gameList,
        font= Font(30)
        )
        addComponents(gameListView)
    }
}