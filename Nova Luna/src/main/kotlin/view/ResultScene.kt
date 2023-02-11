package view

import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import service.RootService

/**
 * Result Scene shown at the end of the game. Shows the player names with their corresponding points and placement.
 */

class ResultScene(private val rootService: RootService)  : MenuScene(500, 1080), Refreshable {

    /**
     * "Game Result" Label at the top of the scene
     */
    private val headlineLabel = Label(
        width = 300, height = 50,
        posX = 100, posY = 20,
        text = "Game Result",
        font = Font(size = 40)
    )

    /**
     * Button for displaying the game results
     */
    private val resultButton = Button(
        posX = 50,
        posY = 200,
        width = 400,
        height = 300,
        text = "no results",
        font = Font(size = 30)
    )

    /**
     * Creates a String with the names of the players based on the number of tokens they have left (ascending).
     * @return String with formatted player names
     */
    private fun getResults() : String{
        var playerString = "no results"
        val gameService = rootService.gameService
        // try to call the method calculate Winner only ones
        val playerList = gameService.calculateWinner()
        val playerNum = playerList.size
        // Storing the winner is a must-have
        rootService.gameService.addToScore(playerList)

        var winner = ""
        var sec = ""
        var thi = ""

       if(playerNum >= 2){
           winner = playerList[0].name
           sec = playerList[1].name
           playerString = "Winner  ${winner}\n\n2nd Place  $sec"
       }
        if(playerNum >= 3){
            thi = playerList[2].name
            playerString = "Winner  ${winner}\n\n2nd Place  ${sec}\n3rd Place  $thi"
        }
        if(playerNum == 4){
            val la = playerList[3].name
            playerString = "Winner  ${winner}\n\n2nd Place  ${sec}\n3rd Place  ${thi}\nLast Place  $la"
        }

        return playerString

    }

    /**
     * High Score Button: go to the [HighscoreScene]
     */
     val highScoreButton = Button(
        width = 200, height = 50,
        posX = 150, posY = 600,
        text = "Highscore",
        font = Font(30)
    ).apply {
        visual = ColorVisual(245, 127, 23)
        onMouseClicked = {

        }
    }

    /**
     * Back Button to go to the [StartMenuScene].
     */
    val backToButton = Button(
        width = 200, height = 50,
        posX = 150, posY = 700,
        text = "Back to Menu",
        font = Font(25)
    ).apply {
        visual = ColorVisual(244, 67, 54)
    }

    /**
     * Add all the components to the scene
     */
    init {
        addComponents(backToButton, highScoreButton, headlineLabel, resultButton)
    }

    override fun refreshAfterResult() {
        resultButton.apply {
            text = getResults()

        }
    }

}