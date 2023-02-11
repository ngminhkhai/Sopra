package view

import entity.*
import service.RootService
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.dialog.FileDialog
import tools.aqua.bgw.dialog.FileDialogMode
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.io.File

/**
 * Set Up Scene to configure a new Game, ypu can set up:
 * - the amount of players/ KI (names and difficulty)
 * - whether it's your first game or not
 */

class SetUpScene (private val rootService: RootService) : MenuScene(512, 768), Refreshable {
    /**
     * FileDialog which opens die CSV-Files to select from
     */
    var csvFileDialog = FileDialog(FileDialogMode.OPEN_FILE, title = "CSV Files" , initialDirectory = File("CsvDatei"))

    /**
     * path of CSV tiles is set to "CsvDatei/nl_tiles.csv" if not change through selecting a different
     * file via the [loadCsvButton]
     */
    var pathCsv="CsvDatei/nl_tiles.csv"

    /**
     * "Start New Game" Label at the top of the scene
     */
    private val headlineLabel = Label(
        width = 300, height = 50, posX = 110, posY = 20,
        text = "Start New Game",
        font = Font(size = 22)
    )

    /**
     * "Player 1...4" left from the text field.
     */
    private val playerTags = Array(4){
        Label(
            width = 110, height = 35,
            posX = 50, posY = 125 + (it * 45),
            text = "Player ${it+1}:"
        )
    }

    /**
     * [TextField] where players enter their names. At least two need to be not blank
     * to start a game.
     */
    private val pInputFields = Array(4){
        TextField(
            width = 200, height = 35,
            posX = 150, posY = 125 + (it * 45)
        ).apply {
            onKeyTyped = { this@SetUpScene.tryEnableStartButton() }
        }
    }

    /**
     * Drop down menu with the Ai difficulties or no Ai option.
     * [startGame] will create the player according to the selected Option
     * if no option was selected the player will be a normal player
     */
    private val aiDropDown = Array(4){
        ComboBox<String>(360,125 + (it*45), prompt = "Ai?").apply {
            items = mutableListOf("No Ai", "Easy", "Middle", "Hard")
        }
    }

    /**
     * Drop down menu with three different animation speed options for the AI.
     */
    val aiAnimationSpeedDropDown = ComboBox<String>(330, 80, width = 150, prompt = "Ai Animation Speed")
        .apply {
        items = mutableListOf("Slow", "Normal", "Fast")
    }

    /**
     * [CheckBox] to choose if this game is the first game.(Changes number of player tokens)
     */
    private val checkBoxFirstGame = CheckBox(50,300,200,50,"First Game")

    /**
     * [CheckBox] to choose if the order of players should be randomized. When not checked: Order of players is
     * from top to the bottom e.g. player 1 is the first player, player 2 the second etc.
     */
    private val checkBoxRandomOrder = CheckBox(180,300,200,50,"Random Order")

    /**
     * [CheckBox] to choose if the tiles should be mixed before a game.
     */
    private val checkBoxRandomOrderTiles= CheckBox(310,300,200,50,"Mix Tiles")

    /**
     * Back Button to go back to the [StartMenuScene].
     */
   val backButton = Button(
        width = 150, height = 50,
        posX = 100, posY = 420,
        text = "Back",
        font = Font(20)
    ).apply {
        visual = ColorVisual(244, 67, 54)
    }

    /**
     * Start button that starts the game. Gets activated by the [tryEnableStartButton] method.
     */
    private val startButton = Button(
        width = 150, height = 50,
        posX = 300, posY = 420,
        text = "Start Game",
        font = Font(20)
    ).apply {
        visual = ColorVisual(124, 179, 66)
        onMouseClicked = {
            this@SetUpScene.startGame(pathCsv)
        }
    }

    /**
     * Load CSV-Button to load custom tiles
     * onMouseClicked it will open [csvFileDialog] where one can select custom tiles
     */
    val loadCsvButton = Button(
        width = 150, height = 50,
        posX = 200, posY = 530,
        text = "Load CSV",
        font = Font(20)
    ).apply {
        visual = ColorVisual(255, 235, 59)
    }

    /**
     * Add all the components to the scene and fill out the text fields with default names.
     */
    init {
        opacity = 0.7
        val names = listOf("Frodo", "Gandalf", "Sauron", "Gollum")
        for(i in pInputFields.indices){
            pInputFields[i].text = names[i]
        }
        addComponents(headlineLabel, *playerTags, *pInputFields, startButton, backButton,loadCsvButton,
            checkBoxFirstGame,checkBoxRandomOrder, *aiDropDown, aiAnimationSpeedDropDown, checkBoxRandomOrderTiles)
    }

    /**
     * Starts the game by reading the player names out of the input fields and reading the checkboxes.
     * [PlayerColor] is determined by the actual input field and order in [PlayerColor]
     * Whenever the Player is an AI-Player or not will be determined by the input Field connected [aiDropDown].
     * e.g. name in the first player field gets always the color blue, the second one always the color black etc.
     */
    private fun startGame(pathCsv: String ){
        val names = pInputFields.filter { it.text.isNotBlank() }.map { it.text.trim() }
        val indexFilledNames = pInputFields.withIndex().filter { it.value.text.isNotBlank() }
            .map { it.index }.toIntArray()
        val colors = PlayerColor.values()
        val players = List(names.size){
            when (aiDropDown[indexFilledNames[it]].selectedItem) {
                "Easy" -> {
                    AI(Difficulty.EASY,names[it], colors[it], 21,0, Grid())
                }
                "Middle" -> {
                    AI(Difficulty.MEDIUM,names[it], colors[it], 21,0, Grid())
                }
                "Hard" -> {
                    AI(Difficulty.HARD,names[it], colors[it], 21,0, Grid())
                }
                else -> {
                    Player(names[it], colors[it], 21,0, Grid())
                }
            }
        }
        rootService.gameService.startNewGame(
            players,
            pathCsv,
            checkBoxFirstGame.checked,
            randomOrderPlayer = checkBoxRandomOrder.checked, randomOrderCards = checkBoxRandomOrderTiles.checked
        )
    }

    /**
     * Enables/Disables the start button. Button gets activated if at least two players entered their names.
     */
    private fun tryEnableStartButton(){
        startButton.isDisabled = pInputFields.count { it.text.isNotBlank() } < 2
    }
}