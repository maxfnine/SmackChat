package com.example.smackchat.Controller

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.smackchat.R
import com.example.smackchat.Services.AuthService
import com.example.smackchat.Services.UserDataService
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5,0.5,0.5,1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
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
        val userName = createUserNameText.text.toString()
        val userEmail = createEmailText.text.toString()
        val password = createPasswordText.text.toString()
        AuthService.registerUser(this,userEmail,password){registerSuccess->
            if(registerSuccess){
                AuthService.loginUser(this,userEmail,password){
                    loginSuccess->
                    if(loginSuccess){
                       AuthService.createUser(this,userName,userEmail,userAvatar,avatarColor){
                           createSuccess->
                           if(createSuccess){
                               println(UserDataService.avatarName)
                               println(UserDataService.avatarColor)
                               println(UserDataService.name)
                               finish()
                           }
                       }
                    }
                }
            }
        }

    }
}
