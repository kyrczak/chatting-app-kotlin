package com.example.kotlinchatapp.messages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.kotlinchatapp.R
import com.example.kotlinchatapp.models.ChatMessage
import com.example.kotlinchatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_left_row.view.*
import kotlinx.android.synthetic.main.chat_right_row.view.*

class ChatLogActivity : AppCompatActivity() {


    companion object {
        val TAG = "Chatlog"
    }
    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username
        chatlog_recyclerview.adapter = adapter
        listenForMessages()
        sendbutton_chatlog.setOnClickListener {
            Log.d(TAG, "Attempt to send message")
            preformSendMessage()
        }
    }
    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if(chatMessage != null) {
                    Log.d(TAG, chatMessage?.text)
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatItemRight(chatMessage.text,currentUser!!))
                    } else {
                        adapter.add(ChatItemLeft(chatMessage.text,toUser!!))
                    }


                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun preformSendMessage(){
        val text = edittext_chatlog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        if(fromId==null) return
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(ref.key!!,text,fromId,toId,System.currentTimeMillis()/1000)
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG,"Saved our chat message: ${ref.key}")
            }
        toRef.setValue(chatMessage)
        edittext_chatlog.text.clear()
        chatlog_recyclerview.scrollToPosition(adapter.itemCount - 1)
        val latestMessageReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageReference.setValue(chatMessage)
        val latestMessageToReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToReference.setValue(chatMessage)
    }
}
class ChatItemLeft(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textViewLeft.text = text
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_left
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_left_row
    }
}
class ChatItemRight(val text: String,val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_right
        Picasso.get().load(uri).into(targetImageView)
        viewHolder.itemView.textViewRight.text = text

    }

    override fun getLayout(): Int {
        return R.layout.chat_right_row
    }

}
