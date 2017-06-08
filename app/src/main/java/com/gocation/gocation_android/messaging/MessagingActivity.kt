package com.gocation.gocation_android.messaging

import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.gocation.gocation_android.IMAGE_URL_PREFS_KEY
import com.gocation.gocation_android.NAME_PREFS_KEY
import com.gocation.gocation_android.R
import com.gocation.gocation_android.asMonth
import com.gocation.gocation_android.data.getAllMessagesFromSnapshot
import com.gocation.gocation_android.data.listenForChangeToMessages
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.activity_messaging.*
import java.util.*

/**
 * Created by dylanlange on 4/06/17.
 */

class MessagingActivity: AppCompatActivity() {

    var messages: List<ChatMessage> = emptyList()
    lateinit var listAdapter: ChatMessageListAdapter
    lateinit var sender: String
    lateinit var imageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)

        et_message.background.setColorFilter(resources.getColor(R.color.accent), PorterDuff.Mode.SRC_IN)
        btn_back.onClick { finish() }

        var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        sender = prefs.getString(NAME_PREFS_KEY, "Unknown")
        imageUrl = prefs.getString(IMAGE_URL_PREFS_KEY, "Unknown")

        listenForChangeToMessages(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError?) { }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                messages = getAllMessagesFromSnapshot(dataSnapshot)
                messages = messages.orderedByTime()
                listAdapter = ChatMessageListAdapter(this@MessagingActivity, R.layout.chat_message_list_item_view, messages)
                listview.adapter = listAdapter
            }
        })

        btn_send.onClick {
            FirebaseDatabase.getInstance().getReference("messages").push().setValue(
                    ChatMessage(
                            sender,
                            imageUrl,
                            getCurrentTime(),
                            et_message.text.toString()
                    )
            )
            et_message.text.clear()
        }

    }

    private fun getCurrentTime(): String {
        val c = Calendar.getInstance()
        val day = c.get(Calendar.DATE)
        val month = c.get(Calendar.MONTH).asMonth()
        val sec = c.get(Calendar.SECOND)
        val min = c.get(Calendar.MINUTE)
        val hour = c.get(Calendar.HOUR_OF_DAY)

        return "$day $month, $hour:$min:$sec"
    }

    fun List<ChatMessage>.orderedByTime(): List<ChatMessage> {
        var listCopy: MutableList<ChatMessage> = this.toMutableList()

        Collections.sort(listCopy, {
            o1: ChatMessage?, o2: ChatMessage? ->
            if(o1!!.isEarlierThan(o2!!)) -1
            else 1
        })

        return listCopy
    }

}