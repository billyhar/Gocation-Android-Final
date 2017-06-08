package com.gocation.gocation_android.messaging;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.gocation.gocation_android.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatMessageListAdapter(context: Context,
                             resId: Int,
                             val mChatMessagesList: List<ChatMessage>) : ArrayAdapter<ChatMessage>(context, resId) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var v: View? = convertView

        if (convertView == null) {
            val vi: LayoutInflater = LayoutInflater.from(context)
            v = vi.inflate(R.layout.chat_message_list_item_view, null)
        }

        val chatMessage: ChatMessage = mChatMessagesList[position]

//        access and change view elements like below:
        val senderImage: ImageView? = v?.findViewById(R.id.iv_profile_image) as CircleImageView
        val senderName: TextView? = v.findViewById(R.id.tv_name) as TextView
        val msgTimestamp: TextView? = v.findViewById(R.id.tv_timestamp) as TextView
        val msgBody: TextView? = v.findViewById(R.id.tv_message_body) as TextView

        Picasso.with(context)
                .load(chatMessage.imageUrl)
                .into(senderImage)

        senderName?.text = chatMessage.sender
        msgTimestamp?.text = chatMessage.timeStamp
        msgBody?.text = chatMessage.body

        return v
    }

    override fun getCount(): Int {
        return mChatMessagesList.size
    }

}