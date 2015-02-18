package com.looper.loop;

import android.content.Context;
import android.content.SharedPreferences;

import com.squareup.okhttp.ResponseBody;

/**
 * Created by rishabhpugalia on 05/02/15.
 *
 * Class representing a User and its cached data
 * (using sharedpreferences currently)
 */
public class AppUser {

    Context mContext;
    static AppUser singletonObject;

    public String mFirstName;
    public String mLastName;
    public String mCountryCode;
    public String mPhoneNumber;
    public String mSessionToken;

    public AppUser(Context context) {
        mContext = context;
    }

    public static AppUser getDefault(Context context) {
        if (singletonObject == null) {
            singletonObject = new AppUser(context);
        }

        return singletonObject;
    }

    public void storeSessionData() {
        SharedPreferences pref = mContext.getSharedPreferences("userData", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();

        edit.putString("first_name", mFirstName);
        edit.putString("last_name", mLastName);
        edit.putString("phone_number", mPhoneNumber);
        edit.putString("session_token", mSessionToken);

        // Commit changes
        edit.commit();
    }

    public SharedPreferences getPrefs() {
        SharedPreferences pref = mContext.getSharedPreferences("userData", Context.MODE_PRIVATE);

        return pref;
    }
}
