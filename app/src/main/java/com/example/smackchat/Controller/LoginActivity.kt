package com.example.smackchat.Controller

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.smackchat.R
import com.example.smackchat.Services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginSpinner.visibility = View.INVISIBLE
    }

    fun loginButtonClicked(view: View){
        enableSpinner(true)
        val email = loginEmailText.text.toString()
        val password = loginPasswordText.text.toString()
        hideKeyboard()
        if(email.isNotEmpty() && password.isNotEmpty()){
            AuthService.loginUser(email,password){
                    loginSuccess->
                if(loginSuccess){
                    AuthService.findUserByEmail(this){
                            userFound->
                        if(userFound){
                            enableSpinner(false)
                            finish()
                        }
                        else{
                            errorToast()
                        }
                    }
                }else{
                    errorToast()
                }
            }
        }
        else
        {
            Toast.makeText(this,"Make sure email, password are filled in.",Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }


    }

    fun loginCreateUserBtnClicked(view:View){
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

    fun enableSpinner(enable:Boolean){
        loginSpinner.visibility = if (enable) View.VISIBLE else View.INVISIBLE
        loginCreateUserButton.isEnabled = !enable
        loginButton.isEnabled = !enable
    }

    fun errorToast(){
        Toast.makeText(this,"Something went wrong please try again.",Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken,0)
        }
    }
}
