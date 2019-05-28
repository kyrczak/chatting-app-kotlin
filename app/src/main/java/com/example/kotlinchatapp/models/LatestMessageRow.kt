package com.example.kotlinchatapp.models

import android.util.Log
import com.example.kotlinchatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
    var ChatPartnerUser: User? = null
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.latestmessage_textview.text = chatMessage.text
        Log.d("messageActivity","From user ${chatMessage.fromId}")
        val chatPartnerId: String
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
            Log.d("messageActivity","Nie gówno From user $chatPartnerId")
        }else{
            chatPartnerId = chatMessage.fromId
            Log.d("messageActivity","Gówno From user $chatPartnerId")
        }
        val ref = FirebaseDatabase.getInstance().getReference("/user/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                ChatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.username_textview.text = ChatPartnerUser?.username
                val targetImageView = viewHolder.itemView.imageview_latestmessage
                Picasso.get().load(ChatPartnerUser?.profileImageUrl).into(targetImageView)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

}