package com.gocation.gocation_android.background

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.RemoteException
import android.preference.PreferenceManager
import android.util.Log
import com.gocation.gocation_android.*
import com.gocation.gocation_android.data.SimpleBeacon
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.altbeacon.beacon.*
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import java.io.FileDescriptor

/**
 * Created by dylanlange on 5/05/17.
 */

class BackgroundBeaconService: Service(), BeaconConsumer {

    val TAG: String = BackgroundBeaconService::class.java.simpleName

    var mBeacons: List<SimpleBeacon> = emptyList()

    lateinit var mId: String
    lateinit var mName: String
    lateinit var mEmail: String
    lateinit var mGender: String
    lateinit var mAgeRange: String
    lateinit var mImageUrl: String

    lateinit var mBeaconManager: BeaconManager
    lateinit var mBackgroundPowerSaver: BackgroundPowerSaver//apparently holding reference to this in the activity saves about 60% battery?

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        mBeaconManager = BeaconManager.getInstanceForApplication(this)
        mBackgroundPowerSaver = BackgroundPowerSaver(this)
        mBeaconManager.bind(this)

        var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        mId = prefs.getString(ID_PREFS_KEY, "")
        mName = prefs.getString(NAME_PREFS_KEY, "")
        mEmail = prefs.getString(EMAIL_PREFS_KEY, "")
        mGender = prefs.getString(GENDER_PREFS_KEY, "")
        mAgeRange = prefs.getString(AGE_RANGE_PREFS_KEY, "")
        mImageUrl = prefs.getString(IMAGE_URL_PREFS_KEY, "")
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null) init()
        return START_STICKY
    }

    override fun onBeaconServiceConnect() {
        setupMonitor()
        setupRangeNotifier()
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(Region("myMonitoringUniqueId", null, null, null))
            mBeaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun setupMonitor(){
        mBeaconManager.addMonitorNotifier(object: MonitorNotifier {

            override fun didDetermineStateForRegion(state: Int, region: Region?) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state)
            }

            override fun didEnterRegion(region: Region?) {
                Log.i(TAG, "I just saw a beacon for the first time!")
            }

            override fun didExitRegion(region: Region?) {
                Log.i(TAG, "I no longer see a beacon")
            }

        })
    }

    private fun setupRangeNotifier() {
        mBeaconManager.addRangeNotifier(object: RangeNotifier {

            override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
                mBeacons = emptyList()
                if(beacons == null) return
                Log.d(TAG, "BEACONS IN RANGE: ${beacons.size}")
                for(b: Beacon in beacons){
                    mBeacons = mBeacons.plus(
                            SimpleBeacon(
                                    b.id1.toString(),   //UUID
                                    b.id2.toInt(),      //Major
                                    b.id3.toInt()       //Minor
                            )
                    )
                    Log.d(TAG, "MINOR: ${b.id3}")
                }
                if(mBeacons.isNotEmpty()) {
                    var beaconName: String? = BEACON_MAP[getMinorOfClosestBeacon(beacons)]
                    if(beaconName != null) {
                        setFirebaseLastSeenAt(beaconName)
                    }
                }
            }

        })
    }

    private fun getMinorOfClosestBeacon(beacons: MutableCollection<Beacon>): String {
        var closestBeacon: Beacon? = null
        for(beacon: Beacon in beacons){
            if(closestBeacon == null) {
                closestBeacon = beacon
            }
            if(beacon.distance < closestBeacon.distance){
                closestBeacon = beacon
            }
        }
        return closestBeacon?.id3.toString()
    }

    private fun setFirebaseLastSeenAt(lastSeenAt: String) {
        if(mEmail == "") return//this happens when the application starts this service and no preferences have been saved
        var usersReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users/$mId")
        usersReference.updateChildren(
                mapOf(
                        "lastSeenAt" to lastSeenAt
                )
        )
        Log.d(TAG, "SETTING FIREBASE LAST SEEN AT: $lastSeenAt")
    }

    private class LocalBinder: IBinder {
        override fun getInterfaceDescriptor(): String = ""

        override fun isBinderAlive(): Boolean = true

        override fun linkToDeath(recipient: IBinder.DeathRecipient?, flags: Int) { }

        override fun queryLocalInterface(descriptor: String?): IInterface = IInterface { this }

        override fun transact(code: Int, data: Parcel?, reply: Parcel?, flags: Int): Boolean = false

        override fun dumpAsync(fd: FileDescriptor?, args: Array<out String>?) { }

        override fun dump(fd: FileDescriptor?, args: Array<out String>?) { }

        override fun unlinkToDeath(recipient: IBinder.DeathRecipient?, flags: Int): Boolean = false

        override fun pingBinder(): Boolean = false

    }

}