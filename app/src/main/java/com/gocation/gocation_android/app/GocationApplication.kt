package com.gocation.gocation_android.app

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v7.app.NotificationCompat
import com.facebook.FacebookSdk
import com.gocation.gocation_android.ID_PREFS_KEY
import com.gocation.gocation_android.IMAGE_URL_PREFS_KEY
import com.gocation.gocation_android.LAST_SEEN_AT_PREFS_KEY
import com.gocation.gocation_android.R
import com.gocation.gocation_android.background.BackgroundBeaconService
import com.gocation.gocation_android.data.User
import com.gocation.gocation_android.data.extractSingleMessage
import com.gocation.gocation_android.data.extractSingleUser
import com.gocation.gocation_android.main.MainActivity
import com.gocation.gocation_android.messaging.ChatMessage
import com.gocation.gocation_android.messaging.MessagingActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.startup.RegionBootstrap


/**
 * Created by dylanlange on 11/05/17.
 */

class GocationApplication:
        Application(),
        BootstrapNotifier {

    lateinit private var mBeaconManager: BeaconManager
    lateinit private var mRegionBootstrap: RegionBootstrap
    lateinit private var prefs: SharedPreferences
    lateinit private var editor: SharedPreferences.Editor

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(this)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        editor = prefs.edit()

        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        // beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        mBeaconManager = BeaconManager.getInstanceForApplication(this)

        mBeaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

        // wake up the app when any beacon is seen (you can specify specific id filers in the parameters below)
        val region = Region("myMonitoringUniqueId", null, null, null)
        mRegionBootstrap = RegionBootstrap(this, region)


//        //CUSTOM FONTS CHEAHOO
//        val myTextView = (R.id.tv_name) as TextView
//        myTextView.typeface = EasyFonts.robotoThin(this)
        FirebaseDatabase.getInstance().reference.child("users").addChildEventListener(object: ChildEventListener{
            override fun onChildMoved(snapshot: DataSnapshot?, p1: String?) { }

            override fun onChildAdded(snapshot: DataSnapshot?, p1: String?) { }

            override fun onChildRemoved(snapshot: DataSnapshot?) { }

            override fun onCancelled(error: DatabaseError?) { }

            override fun onChildChanged(snapshot: DataSnapshot?, p1: String?) {
                var user: User = extractSingleUser(snapshot?.value as Map<*,*>)
                sendNotification(user)
            }
        })

        FirebaseDatabase.getInstance().reference.child("messages").addChildEventListener(object: ChildEventListener{
            override fun onChildMoved(snapshot: DataSnapshot?, p1: String?) { }

            override fun onChildAdded(snapshot: DataSnapshot?, p1: String?) {
                var msg: ChatMessage = extractSingleMessage(snapshot?.value as Map<*,*>)
                sendNotification(msg)
            }

            override fun onChildRemoved(snapshot: DataSnapshot?) { }

            override fun onCancelled(error: DatabaseError?) { }

            override fun onChildChanged(snapshot: DataSnapshot?, p1: String?) { }
        })

    }

    private fun sendNotification(user: User) {
        if(user.id == prefs.getString(ID_PREFS_KEY, "")) return//if the change is your own user, don't notify
        val intent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val b = NotificationCompat.Builder(this)
        val notifTitle: String = "Activity: ${user.name}"
        val notifBody: String = "Seen at: ${user.lastSeenAt}"
        editor.putString(LAST_SEEN_AT_PREFS_KEY, user.lastSeenAt)
        editor.apply()

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_logo_g)
                .setContentTitle(notifTitle)
                .setContentText(notifBody)
                .setContentIntent(contentIntent)
                .setContentInfo("Info")

        var notifManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.notify(user.id.hashCode(), b.build())

    }

    private fun sendNotification(msg: ChatMessage) {
        var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if(msg.imageUrl == prefs.getString(IMAGE_URL_PREFS_KEY, "")) return//if the change is your own user, don't notify
        val intent = Intent(this, MessagingActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val b = NotificationCompat.Builder(this)
        val notifTitle: String = "From: ${msg.sender}"
        val notifBody: String = "Message: ${msg.body}"

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_logo_g)
                .setContentTitle(notifTitle)
                .setContentText(notifBody)
                .setContentIntent(contentIntent)
                .setContentInfo("Info")

        var notifManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.notify(msg.sender.hashCode(), b.build())

    }

    override fun didDetermineStateForRegion(p0: Int, p1: Region?) {

    }

    override fun didEnterRegion(p0: Region?) {
        // This call to disable will make it so the activity below only gets launched the first time a beacon is seen (until the next time the app is launched)
        // if you want the Activity to launch every single time beacons come into view, remove this call.
        mRegionBootstrap.disable()

        startService(Intent(this, BackgroundBeaconService::class.java))
    }

    override fun didExitRegion(p0: Region?) {

    }



    //Custom Fonts CHEEAHOO





}