package com.example.smackchat.Controller

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.example.smackchat.R
import com.example.smackchat.Services.AuthService
import com.example.smackchat.Services.UserDataService
import com.example.smackchat.Utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5,0.5,0.5,1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        createSpinner.visibility = View.INVISIBLE
    }

    fun generateUserAvatar(View: View){
        val random = Random()
        val color = random.nextInt(2)
        val avatarNumber = random.nextInt(28)

        if(color==0){
            userAvatar = "light$avatarNumber"
        }else{
            userAvatar = "dark$avatarNumber"
        }

        val resourceId = this.resources.getIdentifier(userAvatar,"drawable",this.packageName)
        createAvatarImageView.setImageResource(resourceId)

    }

    fun generateColorClicked(view:View){
        val random = Random()
        val red = random.nextInt(256)
        val green = random.nextInt(256)
        val blue = random.nextInt(256)
        createAvatarImageView.setBackgroundColor(Color.rgb(red,green, blue))

        val savedRed = red.toDouble()/255
        val savedGreen = green.toDouble()/255
        val savedBlue = blue.toDouble()/255

        avatarColor = "[$savedRed,$savedGreen,$savedBlue,1]"

    }

    fun createUserClicked(view:View){
        enableSpinner(true)
        val userName = createUserNameText.text.toString()
        val userEmail = createEmailText.text.toString()
        val password = createPasswordText.text.toString()

        if(userName.isNotEmpty() && userEmail.isNotEmpty() && password.isNotEmpty()){
            AuthService.registerUser(this,userEmail,password){registerSuccess->
                if(registerSuccess){
                    AuthService.loginUser(this,userEmail,password){
                            loginSuccess->
                        if(loginSuccess){
                            AuthService.createUser(this,userName,userEmail,userAvatar,avatarColor){
                                    createSuccess->
                                if(createSuccess){
                                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                                    enableSpinner(false)
                                    finish()
                                }
                                else{
                                    errorToast()
                                }
                            }
                        }
                        else{
                            errorToast()
                        }
                    }
                }
                else{
                    errorToast()
                }
            }
        }
        else{
            Toast.makeText(this,"Make sure name,email, password are filled in.",Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }


    }

    fun enableSpinner(enable:Boolean){
        createSpinner.visibility = if (enable) View.VISIBLE else View.INVISIBLE
        createUserButton.isEnabled = !enable
        createAvatarImageView.isEnabled = !enable
        backgroundColorButton.isEnabled = !enable
    }

    fun errorToast(){
        Toast.makeText(this,"Something went wrong please try again.",Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }
}
