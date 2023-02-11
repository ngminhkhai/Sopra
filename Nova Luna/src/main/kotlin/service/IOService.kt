package service

import entity.Color
import entity.NovaLunaApplication
import entity.Task
import entity.Tile
import java.io.*
/**
 * For loading and storing games
 * @property loadGame loads the stored game
 * @property loadCsvTile reads the CSV and configures the tiles
 * @property saveGame stores the rootService
 * @property checkDir creats a storage folder if necessary
 */
class IOService (var rootService: RootService):AbstractRefreshingService(), Serializable{
    var mapOfID = mutableMapOf<Tile, Int>()
    val pfadName = "idCardMapping/CardID.bin"
    var idMappingSaved =  false
    var tile :Tile? = null
    init {
        //check if folder savedHighScores exist when not create the folder automatically
        checkDir("savedGames")
        if (checkDir("idCardMapping")){
            if (!File(pfadName).exists()){
                this.loadCsvTile("CsvDatei/nl_tiles.csv")
                val os = ObjectOutputStream(FileOutputStream(pfadName))
                os.writeObject(this.mapOfID)
            }
        }
    }

    /**
     * This method returns 2 values
     */
    fun loadCsvTile(path: String): Pair<Array<Tile?>, MutableList<Tile>> {
        // Es werden immer nur 11 middle Tiles gefuellt da eine Position fuer den Mond ist middleTile[0] nicht gefuellt am Anfang (Mondposition)
        val middleTiles = arrayOfNulls<Tile>(12)
        val stack = mutableListOf<Tile>()
        var color = Color.BLUE
        val usedID: Array<Int?> = arrayOfNulls(69)
        usedID[0] = 0
        mapOfID = loadMapOfId()
        try {
            // read all lines of csv
            val lines: List<String> = File(path).readLines()
            for(i in 1 until lines.size){
                val line = lines[i].split(",")
                // Task of the current card
                val tasks = mutableListOf<Task>()

                when(line[1]){
                    "cyan" -> color = Color.TURQUOISE
                    "blue" -> color = Color.BLUE
                    "red"  -> color = Color.RED
                    "yellow" -> color = Color.YELLOW
                }
                // fill Task list
                for (j in 3 until line.size){
                    val characterArr = line[j].toCharArray()
                    if (characterArr.isEmpty()){
                        continue
                    }else{
                        // extract char of task colors
                        val colorTask = mutableListOf<Color>()
                        for(sign in characterArr){
                            when(sign){
                                'b' -> colorTask.add(Color.BLUE)
                                'c' -> colorTask.add(Color.TURQUOISE)
                                'r' -> colorTask.add(Color.RED)
                                'y' -> colorTask.add(Color.YELLOW)
                            }
                        }
                        tasks.add(Task(false, colorTask))
                    }
                }
                // Card to the middle Cards first
                if (!idMappingSaved){
                    tile = Tile(line[2].toInt(), false, color, tasks, line[0].toInt())
                    // create a map when no map exists
                    val tileNeu = Tile(line[2].toInt(), false, color, tasks)
                    mapOfID.put(tileNeu, line[0].toInt())
                }
                else{
                    var id = mapOfID.get(Tile(line[2].toInt(), false, color, tasks))
                    checkNotNull(id)
                    // because some cards are doubled and has to recieve an unique ID
                    if (usedID[id] != null){
                        id = id - 1
                    }

                    tile = Tile(line[2].toInt(), false, color, tasks, id)
                    // Put tiles at first in the middle then on the stack
                    if(i < middleTiles.size){
                        checkNotNull(tile){"Tile ist null"}
                        middleTiles[i] = tile
                    }else{
                        checkNotNull(tile){"Tile ist null"}
                        stack.add(tile!!)
                    }
                    usedID[id] = id
                }
            }
        }catch (e:IOException){
            println("Exeption CSV read: " + e.message)
        }finally {
            println("CSV Read finished")
        }
        return Pair(middleTiles, stack)
    }

    /**
     * loads the mapping of Id's
     * @return returns the map of tiles and id's with reference to the nl_tiles.csv
     */
    fun loadMapOfId(): MutableMap<Tile, Int>{
        var map = mutableMapOf<Tile, Int>()
        if (File(pfadName).exists()) {
            try {
                val os = ObjectInputStream(FileInputStream(pfadName))
                map = os.readObject() as MutableMap<Tile, Int>
                os.close()
                idMappingSaved = true
            } catch (e: IOException) {
                println("Exeption loadGame" + e.message)
            }
        }else{
            idMappingSaved = false
        }
        return map
    }

    /**
     * @param name loads the data of this object which was saved
     */
    fun loadGame(name: String){
        try {
            val os = ObjectInputStream(FileInputStream("savedGames/$name.bin"))
            this.rootService.novaLunaApplication = os.readObject() as NovaLunaApplication
            os.close()
        }catch (e: IOException){
            println("Exeption loadGame" + e.message)
        }
        onAllRefreshables { refreshAfterLoadGame() }
    }

    /**
     * @param name stores the rootservice novaLunaApplication with this name
     */
    fun saveGame(name: String) {
        try {
            val os = ObjectOutputStream(FileOutputStream("savedGames/$name.bin"))
            os.writeObject(rootService.novaLunaApplication)
            os.close()
        }catch (e: IOException){
            println("Exeption saveGame" +  e.message)
        }
        onAllRefreshables { refreshAfterSaveGame() }
    }

    /**
     * Check it the path exist
     * if not create the folder
     */
    private fun checkDir(dirName: String): Boolean {
        val stats = File(dirName)
        return if (stats.exists()) // Überprüfen, ob es den Ordner gibt
        {
            true
        } else {
            stats.mkdir()
        }
    }
}