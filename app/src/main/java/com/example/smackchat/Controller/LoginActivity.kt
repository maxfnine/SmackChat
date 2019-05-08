package com.example.smackchat.Controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.smackchat.R
import com.example.smackchat.Services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginButtonClicked(view: View){
        val email = loginEmailText.text.toString()
        val password = loginPasswordText.text.toString()
        AuthService.loginUser(this,email,password){
            loginSuccess->
            if(loginSuccess){
                AuthService.findUserByEmail(this){
                    userFound->
                    if(userFound){
                        finish()
                    }
                }
            }
        }

    }

    fun loginCreateUserBtnClicked(view:View){
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }
}
