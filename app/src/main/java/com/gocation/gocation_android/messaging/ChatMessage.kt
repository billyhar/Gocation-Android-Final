package com.gocation.gocation_android.messaging

/**
 * Created by dylanlange on 4/06/17.
 */
data class ChatMessage(
        val sender: String,
        val imageUrl: String,
        val timeStamp: String,
        val body: String
) {
    fun isEarlierThan(other: ChatMessage): Boolean {
        val otherDate: Int = Integer.parseInt(other.timeStamp.split(" ")[0])
        val thisDate: Int = Integer.parseInt(this.timeStamp.split(" ")[0])
        val otherMonth: Int = getMonthAsInt(other.timeStamp.split(" ")[1])
        val thisMonth: Int = getMonthAsInt(this.timeStamp.split(" ")[1])
        val otherHour: Int = Integer.parseInt(other.timeStamp.split(" ")[2].split(":")[0])
        val thisHour: Int = Integer.parseInt(this.timeStamp.split(" ")[2].split(":")[0])
        val otherMin: Int = Integer.parseInt(other.timeStamp.split(" ")[2].split(":")[1])
        val thisMin: Int = Integer.parseInt(this.timeStamp.split(" ")[2].split(":")[1])
        val otherSec: Int = Integer.parseInt(other.timeStamp.split(" ")[2].split(":")[2])
        val thisSec: Int = Integer.parseInt(this.timeStamp.split(" ")[2].split(":")[2])
        if(thisMonth < otherMonth) return true
        if(thisMonth == otherMonth && thisDate < otherDate) return true
        if(thisMonth == otherMonth && thisDate == otherDate && thisHour < otherHour) return true
        if(thisMonth == otherMonth && thisDate == otherDate && thisHour == otherHour && thisMin < otherMin) return true
        if(thisMonth == otherMonth && thisDate == otherDate && thisHour == otherHour && thisMin == otherMin && thisSec < otherSec) return true
        return false
    }

    fun getMonthAsInt(month: String): Int {
        when(month){
            "January," -> return 1
            "February," -> return 2
            "March," -> return 3
            "April," -> return 4
            "May," -> return 5
            "June," -> return 6
            "July," -> return 7
            "August," -> return 8
            "September," -> return 9
            "October," -> return 10
            "November," -> return 11
            "December," -> return 12
        }
        return -1
    }
}