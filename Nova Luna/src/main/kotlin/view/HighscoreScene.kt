package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * Highscore Scene can be viewed from the [ResultScene] or [StartMenuScene]
 */

class HighscoreScene(private val rootService: RootService) : MenuScene(400, 1080), Refreshable {


    /**
     * "Highscore" Label at the top of the scene
     */
    private val headlineLabel = Label(
        width = 300, height = 50,
        posX = 50, posY = 20,
        text = "Highscore List",
        font = Font(size = 40)
    )

    private val highScoreList = Array(10){
        Label(
            posX = 50,
            posY = 300 + (it * 45),
            width = 300,
            font = Font(size = 20),
            isWrapText = true
        )
    }

    /**
     * Back Button to go back to the [StartMenuScene].
     */
    val backButton = Button(
        width = 200, height = 50,
        posX = 100, posY = 800,
        text = "Back",
        font = Font(25)
    ).apply {
        visual = ColorVisual(244, 67, 54)
    }

    /**
     * Delete button that resets the highscore list.
     */
    private val deleteButton = Button(
        width = 100, height = 40,
        posX = 150, posY = 1000,
        text = "Delete List",
        font = Font(16)
    ).apply {
        visual = ColorVisual.RED
        onMouseClicked = {
            rootService.novaLunaApplication.highscore.clearHighScore()
            this@HighscoreScene.getHighScoreList()
        }
    }

    /**
     * Add all the components to the scene
     */
    init {
        getHighScoreList()
        addComponents(backButton, headlineLabel, *highScoreList, deleteButton)
    }

    /**
     * Crates and displays a high score Top10 list
     */
    fun getHighScoreList(){

        val scoreboard = rootService.novaLunaApplication.highscore.scoreboard

        val scoreboardSize = scoreboard.size

        val defaultP = Pair("empty", 0f)

        var highScoreTop10 : MutableList<Pair<String,Float>> = mutableListOf(defaultP)

        //fill the empty slots
        if(scoreboardSize < 10){

            highScoreTop10.clear()
            repeat(scoreboardSize){ highScoreTop10.add(scoreboard[it]) }
            for (i in scoreboardSize .. 10) highScoreTop10.add(defaultP)

        }
        //reduce the scoreboard to Top10
        else if(scoreboardSize > 10){

            highScoreTop10.clear()
            repeat(10){  highScoreTop10.add(scoreboard[it]) }

        }
        //scoreboard.size = 10
        else{ highScoreTop10 = scoreboard }

        for(i in highScoreList.indices){
            highScoreList[i].text = highScoreTop10[i].first + "   " + highScoreTop10[i].second
        }
    }

}