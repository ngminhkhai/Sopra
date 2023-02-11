package entity

data class Card(val suit: CardSuit, var value: CardValue){
    override fun toString()= "$suit$value"

}