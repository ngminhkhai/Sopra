package entity

import org.junit.jupiter.api.Test
import service.RootService
import kotlin.test.assertEquals

class highScoreTest {
    /**
     * inits a highscore object , this object then contains all previous highscores in its scoreboard
     * property. then a new score gets added with the saveHighScore() and it is checked if the list
     * gets longer
     */
    @Test
    fun testHighScore(){
        val rootservice = RootService()
        val toAdd = mutableListOf(Pair("Player1",(23.4f)))
        rootservice.novaLunaApplication.highscore.clearHighScore()

        // Check no mutlple names
        val oldSize = rootservice.novaLunaApplication.highscore.scoreboard.size
        rootservice.novaLunaApplication.highscore.saveHighScore(toAdd, false)
        rootservice.novaLunaApplication.highscore.loadHighScore()
        //inserted first one
        assertEquals(oldSize+1, rootservice.novaLunaApplication.highscore.scoreboard.size)
        assertEquals(toAdd , rootservice.novaLunaApplication.highscore.scoreboard)

        // inserted second one equal name
        rootservice.novaLunaApplication.highscore.saveHighScore(toAdd, false)
        rootservice.novaLunaApplication.highscore.loadHighScore()
        assertEquals(oldSize+2, rootservice.novaLunaApplication.highscore.scoreboard.size)
        assertEquals(mutableListOf(Pair("Player1",(23.4f)), Pair("Player1",(23.4f))) , rootservice.novaLunaApplication.highscore.scoreboard)

        // Tests 2
        // Multiple lists with players scores and the same names
        rootservice.novaLunaApplication.highscore.clearHighScore()
        val toAdd2 = mutableListOf(Pair("Player1",66.4f), Pair("Player2",23.4f),Pair("Player1",99.4f),  Pair("Player1",75.4f), Pair("Player2",20.4f))
        val toCheck = mutableListOf(Pair("Player1",99.4f), Pair("Player2",23.4f))
        rootservice.novaLunaApplication.highscore.saveHighScore(toAdd2, true)
       // rootservice.novaLunaApplication.highscore.loadHighScore()
        assertEquals(toCheck, rootservice.novaLunaApplication.highscore.scoreboard)

        val toAdd3 = mutableListOf(Pair("Player1",166.4f), Pair("Player2",223.4f),Pair("Player1",299.4f),  Pair("Player1",75.4f), Pair("Player2",200.4f))
        val toCheck3 = mutableListOf(Pair("Player1",299.4f), Pair("Player2",223.4f))
        rootservice.novaLunaApplication.highscore.saveHighScore(toAdd3, true)
        rootservice.novaLunaApplication.highscore.loadHighScore()
        assertEquals(toCheck3, rootservice.novaLunaApplication.highscore.scoreboard)
    }
}