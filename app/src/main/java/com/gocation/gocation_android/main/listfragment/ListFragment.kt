package com.gocation.gocation_android.main.listfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firststarcommunications.ampmscratchpower.android.adapters.UsersListAdapter
import com.gocation.gocation_android.R
import com.gocation.gocation_android.data.User
import com.gocation.gocation_android.data.getAllUsersFromSnapshot
import com.gocation.gocation_android.data.listenForAllUsers
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_list.*

/**
 * Created by dylanlange on 25/05/17.
 */

class ListFragment: android.support.v4.app.Fragment() {

    lateinit private var mUsers: List<User>

    companion object {
        @JvmStatic fun newInstance() = ListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        listenForAllUsers(object: ValueEventListener {

            override fun onCancelled(databaseError: DatabaseError?) { }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                mUsers = getAllUsersFromSnapshot(dataSnapshot)
                if(activity != null) {
                    listview.adapter = UsersListAdapter(activity, R.layout.list_item_user, mUsers.toMutableList())
                }
            }

        })


    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(R.layout.fragment_list, container, false)

}