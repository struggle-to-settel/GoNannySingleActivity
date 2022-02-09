package com.au.gonannysingleactivity.utils

import android.content.Context
import android.content.SharedPreferences
import com.au.gonannysingleactivity.objects.Constants.KEY_USER_SHARED_PREFERENCE
import com.au.gonannysingleactivity.objects.Constants.sharedPreferencesKey
import com.au.gonannysingleactivity.webservices.User
import com.google.gson.Gson

class PreferenceManager(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveUserObject(user: User?) {
        editor.putString(KEY_USER_SHARED_PREFERENCE, Gson().toJson(user))
        editor.apply()
    }

    fun getUserObject(): User? {

        return if (sharedPreferences.getString(KEY_USER_SHARED_PREFERENCE, null) != null) {
            Gson().fromJson(
                sharedPreferences.getString(KEY_USER_SHARED_PREFERENCE, null),
                User::class.java
            )
        } else null
    }

    fun setFirebaseId(id:String){
        editor.putString("fcm_id",id).apply()
    }

    fun getFirebaseId():String = sharedPreferences.getString("fcm_id","")!!

    fun clearAccessToken(){
        editor.putString("AccessToken", null)
    }



    fun setAccessToken(accessToken: String) =
        run {
            editor.putString("AccessToken", accessToken)
            editor.apply()
        }

    fun getAccessToken(): String? = sharedPreferences.getString("AccessToken", null)

    fun setScreenState(screenName: String?) {
        editor.putString("ScreenStatus", screenName)
        editor.commit()
    }

    fun getScreenState() = sharedPreferences.getString("ScreenStatus", null)

    fun setUserType(type:Int){
        editor.putInt("UserType",type)
    }
    fun getUserType() = sharedPreferences.getInt("UserType",0)
}