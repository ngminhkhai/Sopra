package entity

import java.io.*


/**
 *  Data class to save highscores.
 *  @property scoreboard List of Pairs of (PlayerName, PlayerScore) it contains every highscore
 *  that got saved in the ./savedHighScores/scores.bin
 *
 */
class Highscore: Serializable {

    var scoreboard = mutableListOf<Pair<String,Float>>()
    val pfadName = "savedHighScores/scores.bin"
    init {
        //check if folder savedHighScores exist when not create the folder automatically
        if (checkDir()){
            // Check if scores.bin already exist in the folder savedHighScores
            if (!File(pfadName).exists()){
                val os = ObjectOutputStream(FileOutputStream(pfadName))
                os.writeObject(this.scoreboard)
            }
            scoreboard = loadHighScore()
        }else{
            println("Keine Scoreliste angelegt")
        }
    }

    /**
     * Calculates the sum of total costs of the opponents divided by the
     * number of opponents for the score of the winner
     * @param listOfPlayers the players of the game
     * @return returns the score of the winner
     */
    fun calculateHighScorePointsWinner(listOfPlayers :MutableList<AbstractPlayer>): Float{
        val listOfPlayersSorted = listOfPlayers.sortedBy { it.numberOfTokens }
        var score = 0.0
        for(i in 1 until listOfPlayers.size){
            score = listOfPlayersSorted[i].numberOfTokens + score
        }
        // only the number ob opponents counts therefore playersize -1
        score = score / (listOfPlayers.size - 1)
        return score.toFloat()
    }

    /**
     * @return all the previous saved highScores
     *
     * reads the score file and updates the local scoreboard property
     * if the score file is empty, an empty mutable list gets returned
     *
     * @return returns the List of the old high score which was stored
     */
    fun loadHighScore(): MutableList<Pair<String,Float>> {
        val returnHighScore: MutableList<Pair<String,Float>> = try {
            val os = ObjectInputStream(FileInputStream(pfadName))
            os.readObject() as MutableList<Pair<String,Float>>
        } catch (e: IOException) {
            mutableListOf()
        }
        this.scoreboard = returnHighScore
        return returnHighScore
    }
    /**
     * loads the old highscores of the saved .bin file and saved them in the oldHighScores variable.
     * then the new highscores, which are the parameter of this function, get added to the oldhighscores
     * and saved in "newHighScores". newHighScores gets saved in the .bin file and the local scoreboard
     * gets updated
     *
     * @param toAddScores Adds the player and scores in the list
     * @param avoidSameNames when true each name only can appear one time with the highest score otherwise
     * one name can appear multiple times
     */
    fun saveHighScore(toAddScores: MutableList<Pair<String,Float>>, avoidSameNames: Boolean) {
        val oldHighScores = loadHighScore()
        // Does names appear multiple times?
        if (avoidSameNames){
            var inserted= false
            for (i in 0 until toAddScores.size){
                val (nameAdd, scoreAdd) = toAddScores[i]
                for (k in 0 until oldHighScores.size) {
                    val (oldName, scoreOld) = oldHighScores[k]
                    // pick the highest score when the name already existed
                    if(oldName == nameAdd && scoreAdd > scoreOld){
                        oldHighScores.removeAt(k)
                        oldHighScores.add(toAddScores[i])
                        inserted = true
                    }
                }
                // if the name doesn't appear
                if (!inserted){
                    oldHighScores.add(toAddScores[i])
                    inserted= false
                }
            }
        }else {
            // insert all names in toAddScores
            for (player in toAddScores) {
                oldHighScores.add(player)
            }
        }
        // descending Order of scores
        val newHighScores = oldHighScores.sortedByDescending { (_, value) -> value}.toMutableList()
        try {
            val os = ObjectOutputStream(FileOutputStream(pfadName))
            os.writeObject(newHighScores)
        }catch (e:IOException){
            println("Exeption saveHighScore" +  e.message)
        }
        this.scoreboard = newHighScores
    }

    /**
     * Saves an empty HighScore
     */
    fun clearHighScore(){
        this.scoreboard = mutableListOf()
        try {
            val os = ObjectOutputStream(FileOutputStream(pfadName))
            os.writeObject(this.scoreboard)
        }catch (e:IOException){
            println("Exeption clearHighScore" + e.message)
        }
    }
    /**
     * Check it the path exist
     * if not create the folder
     */
    private fun checkDir(): Boolean {
        val stats = File("savedHighScores")
        return if (stats.exists()) // Überprüfen, ob es den Ordner gibt
        {
            true
        } else {
            stats.mkdir()
        }
    }


}