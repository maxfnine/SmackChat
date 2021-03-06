package com.example.smackchat.Controller

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.smackchat.Adapters.MessageAdapter
import com.example.smackchat.Model.Channel
import com.example.smackchat.Model.Message
import com.example.smackchat.R
import com.example.smackchat.Services.AuthService
import com.example.smackchat.Services.MessageService
import com.example.smackchat.Services.UserDataService
import com.example.smackchat.Utilities.BROADCAST_USER_DATA_CHANGE
import com.example.smackchat.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter:ArrayAdapter<Channel>
    lateinit var messageAdapter:MessageAdapter
    var selectedChannel:Channel? = null

    private fun setupAdapters(){
        channelAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,MessageService.channels)
        channel_list.adapter = channelAdapter
        messageAdapter = MessageAdapter(this,MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        socket.connect()
        socket.on("channelCreated",onNewChannel)
        socket.on("messageCreated",onNewMessage)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        setupAdapters()
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))
        channel_list.setOnItemClickListener{
            _,_,i,l->
            selectedChannel = MessageService.channels[i]
            drawerLayout.closeDrawer(GravityCompat.START)
            updateWithChannel()

        }

        if(App.prefs.isLoggedIn){
            AuthService.findUserByEmail(this){

            }
        }


    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        socket.disconnect()
        super.onDestroy()
    }



    private val userDataChangeReceiver = object:BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent?) {
            if(App.prefs.isLoggedIn){
                userNameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName,"drawable",packageName)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginButtonNavHeader.text = "Logout"

                MessageService.getChannels{
                    complete->
                    if(complete){
                        if(MessageService.channels.count() > 0){
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }

                    }
                }
            }
        }

    }

    fun updateWithChannel(){
        mainChannelName.text = "#${selectedChannel?.name}"
        if(selectedChannel != null){
            MessageService.getMessages(selectedChannel!!.id){
                complete->
                if(complete){
                    messageAdapter.notifyDataSetChanged()
                    if(messageAdapter.itemCount > 0){
                        messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }


    }

    fun loginButtonNavClicked(view: View){

        if(App.prefs.isLoggedIn){
            UserDataService.logout()
            channelAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()
            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginButtonNavHeader.text = "Login"
            mainChannelName.text = "Please login"

        }else{
            val loginActivityIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginActivityIntent)
        }


    }

    fun addChannelClicked(view:View){
        if(App.prefs.isLoggedIn){
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog,null)
            builder.setView(dialogView)
                .setPositiveButton("Add"){
                    _,_ ->
                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                    val descTextfield = dialogView.findViewById<EditText>(R.id.addChannelDescText)
                    val channelName = nameTextField.text.toString()
                    val channelDesc = descTextfield.text.toString()

                    socket.emit("newChannel",channelName,channelDesc)

                }.setNegativeButton("Cancel"){
                    _, _ ->


                }
                .show()
        }
    }

    private val onNewChannel = Emitter.Listener {
        args ->
        if(App.prefs.isLoggedIn){
            runOnUiThread{

                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName,channelDescription,channelId)
                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
            }
        }

    }

    private val onNewMessage = Emitter.Listener { args ->
        if(App.prefs.isLoggedIn){
            runOnUiThread {
                val channelId = args[2] as String
                if( selectedChannel?.id == channelId){
                    val messageBody = args[0] as String
                    val userName  = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id =  args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage =  Message(messageBody,userName,channelId,userAvatar,userAvatarColor,id, timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }

    }

    fun sendMessageBtnClicked(view:View){
        if(App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null){
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage",messageTextField.text.toString(),userId,channelId,UserDataService.name,UserDataService.avatarName,UserDataService.avatarColor)
            messageTextField.text.clear()
            hideKeyboard()
        }

    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken,0)
        }
    }




}
