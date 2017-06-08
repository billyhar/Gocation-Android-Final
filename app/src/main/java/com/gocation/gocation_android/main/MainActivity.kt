package com.gocation.gocation_android.main



import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.facebook.login.LoginManager
import com.gocation.gocation_android.*
import com.gocation.gocation_android.background.BackgroundBeaconService
import com.gocation.gocation_android.login.LoginActivity
import com.gocation.gocation_android.main.listfragment.ListFragment
import com.gocation.gocation_android.main.profilefragment.ProfileFragment
import com.gocation.gocation_android.messaging.MessagingActivity
import com.mcxiaoke.koi.ext.onClick
import com.mikepenz.materialdrawer.Drawer
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*




/**
 * Created by dylanlange on 11/05/17.
 */

class MainActivity: AppCompatActivity() {
    val PERMISSION_REQUEST_CODE: Int = 69

    lateinit private var mSharedPreferences: SharedPreferences
    lateinit private var mEditor: SharedPreferences.Editor
    lateinit private var mDrawer: Drawer
    lateinit var mBeaconServiceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //notifcation popup window

        btn_notif.onClick {
            var i: Intent = Intent(this@MainActivity, MessagingActivity::class.java)
            startActivity(i)
        }

        //TODO: Add custom font (Raleway)

        viewpager.adapter = ViewPagerAdapter(supportFragmentManager)
        mBeaconServiceIntent = Intent(this@MainActivity, BackgroundBeaconService::class.java)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mEditor = mSharedPreferences.edit()

        setupActionBar()
        requestLocationPermissions()
        startService(mBeaconServiceIntent)

        var name: String = mSharedPreferences.getString(NAME_PREFS_KEY, "")
        var email: String = mSharedPreferences.getString(EMAIL_PREFS_KEY, "")
        var imageUrl: String = mSharedPreferences.getString(IMAGE_URL_PREFS_KEY, "")


        Picasso.with(this)
                .load(imageUrl)
                .into(iv_action_bar_image)

        viewpager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) { }

            override fun onPageSelected(position: Int) { }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                iv_action_bar_image.visibility = if(position == 0) VISIBLE else GONE
            }
        })

        mDrawer = drawer {
            accountHeader {
                selectionListEnabledForSingleProfile = false
                background = R.drawable.menu_bg
                profile(name, email) {
                    iconUrl = imageUrl
                }
            }

            primaryItem("Friends") {
                icon = R.drawable.ic_profile
                onClick { _ ->
                    var intent: Intent = Intent(this@MainActivity, MainActivity::class.java)
                    startActivity(intent)
                    false

                }
            }


            primaryItem("Messages") {
                icon = R.drawable.ic_message_men
                onClick { _ ->
                    var intent: Intent = Intent(this@MainActivity, MessagingActivity::class.java)
                    startActivity(intent)
                    false
                }
            }

            primaryItem("Notifications") {
                icon = R.drawable.ic_notifications
                onClick { _ ->
//                    var intent: Intent = Intent(this@MainActivity, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
                    false
                }

            }

            divider {  }

            primaryItem("Event Info") {
                icon = R.drawable.ic_info
                onClick { _ ->
//                    var intent: Intent = Intent(this@MainActivity, EventInfo::class.java)
//                    startActivity(intent)
//                    finish()
                    false
                }

            }

            primaryItem("Event Map") {
                icon = R.drawable.ic_map
                onClick { _ ->
//                    var intent: Intent = Intent(this@MainActivity, EventMap::class.java)
//                    startActivity(intent)
                    false
                }
            }

            divider {  }

            primaryItem("Log out") {
                icon = R.drawable.ic_logout
                onClick { _ ->
                    mEditor.putString(ID_PREFS_KEY, null)
                    mEditor.apply()
                    LoginManager.getInstance().logOut()
                    var intent: Intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    false
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            showStandardAlertDialog("Bluetooth Required", "To use the features of this app, your device needs to have bluetooth.")
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                showStandardAlertDialog("Bluetooth Required", "To use the features of this app, you need to turn blueooth on.")
            }
        }
    }

    private fun showStandardAlertDialog(title: String, msg: String) {
        val alertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(msg)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        alertDialog.show()
    }

    private fun requestLocationPermissions() {
        val locationFineCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        val locationCoarseCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        if (locationFineCheck != PermissionChecker.PERMISSION_GRANTED
                && locationCoarseCheck != PermissionChecker.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    , PERMISSION_REQUEST_CODE)
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btn_menu.setOnClickListener { menuBtnClicked() }
        btn_alternate.setOnClickListener { alternateBtnClicked() }
    }


    private fun menuBtnClicked() {
        if (!mDrawer.isDrawerOpen)
            mDrawer.openDrawer()
        else
            mDrawer.closeDrawer()
    }

    private fun alternateBtnClicked() {
        viewpager.setCurrentItem(1, true)
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        val NUM_PAGES: Int = 2
        lateinit var mListFragment: ListFragment
        lateinit var mProfileFragment: ProfileFragment

        override fun instantiateItem(container: ViewGroup?, position: Int): Any {
            when (position) {
                0 -> {
                    mListFragment = super.instantiateItem(container, position) as ListFragment
                    return mListFragment
                }
                1 -> {
                    mProfileFragment = super.instantiateItem(container, position) as ProfileFragment
                    return mProfileFragment
                }
                else -> return super.instantiateItem(container, position)
            }
        }

        override fun getItem(position: Int): Fragment? {
            if (position == 0) {
                return ListFragment.newInstance()
            } else if (position == 1) {
                return ProfileFragment.newInstance()
            } else {
                return null
            }
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }

        override fun getPageTitle(position: Int): CharSequence {
            return super.getPageTitle(position)
        }

    }

}

