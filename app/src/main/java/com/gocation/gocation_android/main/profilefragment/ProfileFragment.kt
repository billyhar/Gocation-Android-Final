package com.gocation.gocation_android.main.profilefragment

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.gocation.gocation_android.*
import com.gocation.gocation_android.data.User
import com.gocation.gocation_android.data.getAllUsersFromSnapshot
import com.gocation.gocation_android.data.listenForAllUsers
import com.gocation.gocation_android.messaging.ChatMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shitij.goyal.slidebutton.SwipeButton
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

/**
 * Created by dylanlange on 25/05/17.
 */

class ProfileFragment: android.support.v4.app.Fragment() {

    companion object {
        @JvmStatic fun newInstance() = ProfileFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        var imageUrl: String = prefs.getString(IMAGE_URL_PREFS_KEY, "")
        var name: String = prefs.getString(NAME_PREFS_KEY, "")
        var email: String = prefs.getString(EMAIL_PREFS_KEY, "")
        var lastSeenAt: String = prefs.getString(LAST_SEEN_AT_PREFS_KEY, "")

        listenForAllUsers(object: ValueEventListener {

            override fun onCancelled(databaseError: DatabaseError?) { }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(context == null) return
                var users = getAllUsersFromSnapshot(dataSnapshot)
                var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                var listOfPotentiallyMeUsers: List<User> = users.filter { it.email == prefs.getString(EMAIL_PREFS_KEY, "").toFirebaseKey() }
                if(listOfPotentiallyMeUsers.isNotEmpty()) {
                    tv_last_seen_at.text = listOfPotentiallyMeUsers[0].lastSeenAt
                }
            }

        })

        alert_btn.setOnTouchListener {
            _, event ->
            when(event.action) {
                MotionEvent.ACTION_MOVE ->
                    root_view.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_BUTTON_RELEASE ->
                    root_view.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
        alert_btn.addOnSwipeCallback(object: SwipeButton.Swipe {
            override fun onButtonPress() {

            }

            override fun onSwipeCancel() {

            }

            override fun onSwipeConfirm() {
                FirebaseDatabase.getInstance().getReference("messages").push().setValue(
                        ChatMessage(
                                name,
                                imageUrl,
                                getCurrentTime(),
                                "SOS - $name \nLAST SEEN AT - ${if(lastSeenAt == "") "UNKNOWN" else lastSeenAt}"
                        )
                )
            }
        })

        Picasso.with(activity)
                .load(imageUrl)
                .into(iv_profile_image)

        tv_profile_name.text = name
        tv_profile_email.text = email
        tv_last_seen_at.text = lastSeenAt
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View? =  inflater?.inflate(R.layout.fragment_profile, container, false)

        return view
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

}