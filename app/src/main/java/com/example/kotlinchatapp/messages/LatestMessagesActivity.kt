package com.example.kotlinchatapp.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.kotlinchatapp.R
import com.example.kotlinchatapp.models.ChatMessage
import com.example.kotlinchatapp.models.User
import com.example.kotlinchatapp.registerlogin.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.core.view.View
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {
    companion object{
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recyclerview_latestmessages.adapter = adapter
        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserIsLoggedIn()
    }
    class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.latestmessage_textview.text = chatMessage.text
            Log.d("messageActivity","From user ${chatMessage.fromId}")
            val chatPartnerId: String
            if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.ToId
                Log.d("messageActivity","Nie gówno From user $chatPartnerId")
            }else{
                chatPartnerId = chatMessage.fromId
                Log.d("messageActivity","Gówno From user $chatPartnerId")
            }
            val ref = FirebaseDatabase.getInstance().getReference("/user/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java)
                    viewHolder.itemView.username_textview.text = user?.username
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

        }

    }
    val latestMessagesMap = HashMap<String, ChatMessage>()
    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }
    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
               }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }
    val adapter = GroupAdapter<ViewHolder>()
        private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/user/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("messageActivity"," Current user ${currentUser?.username}")
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.menu_new_message ->{
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
