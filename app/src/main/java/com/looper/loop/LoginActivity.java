package com.looper.loop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class LoginActivity extends ActionBarActivity {

    private String TAG = LoginActivity.class.getSimpleName();

    User mUser;

    // Broadcast Receiver for SMS
    BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get the data (SMS data) bound to intent
            Bundle bundle = intent.getExtras();

            SmsMessage[] msgs = null;

            String verificationCode = "";
            String senderPhone = "";

            if (bundle != null) {
                // Retrieve the Binary SMS data
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];

                // For every SMS message received (although multipart is not supported with binary)
                for (int i = 0; i < msgs.length; i++) {
                    byte[] data = null;

                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

                    senderPhone = msgs[i].getOriginatingAddress();

                    // Return the User Data section minus the
                    // User Data Header (UDH) (if there is any UDH at all)
                    data = msgs[i].getUserData();

                    for (int index = 0; index < data.length; index++) {
                        verificationCode += Character.toString((char) data[index]);
                    }

                    break;
                }

                // Dump the entire message

                String userPhone = mUser.mPhoneNumber;
                // Log.d(TAG, userPhone + ":" + senderPhone + ":" + mUser.mVerificationCode + ":" + verificationCode);
                // senderPhone can be 919999999999 but userPhone will be 9999999999
                if (senderPhone.indexOf(userPhone) != -1 && mUser.mVerificationCode.equals(verificationCode)) {
                    // Phone numbers match and verification code also matched!

                    // Init a Session and then launch MainActivity
                    OkHttpClient client = new OkHttpClient();

                    HTTPRequestObject userData = new HTTPRequestObject();
                    userData.put("phone_number", mUser.mPhoneNumber);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), userData.getJSON());
                    Request request = new Request.Builder()
                            .url(LoopApplication.SERVER_URL + "/users/initSession")
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {

                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            // Background thread

                            ResponseBody responseBody = response.body();
                            String responseJSON = responseBody.string();

                            try {
                                JSONObject jsonObject = new JSONObject(responseJSON);
                                String sessionToken = jsonObject.getString("session_token");

                                // Store in SharedPrefs
                                AppUser appUser = AppUser.getDefault(LoginActivity.this);
                                appUser.mFirstName = mUser.mFirstName;
                                appUser.mLastName = mUser.mLastName;
                                appUser.mPhoneNumber = mUser.mPhoneNumber;
                                appUser.mSessionToken = sessionToken;
                                // Store in SharePrefs
                                appUser.storeSessionData();

                                // Shoot the main activity!!!
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }

            // end of onReceive()
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set action bar title
        getSupportActionBar().setTitle("Join Us");

        // All the views from our login form
        final EditText firstNameView = (EditText) findViewById(R.id.firstName);
        final EditText lastNameView = (EditText) findViewById(R.id.lastName);
        final Spinner countryView = (Spinner) findViewById(R.id.country);
        final EditText countryCodeView = (EditText) findViewById(R.id.countryCode);
        final EditText phoneNumberView = (EditText) findViewById(R.id.phoneNumber);
        Button loginButtonView = (Button) findViewById(R.id.loginButton);

        initCountryList(countryView);

        // Login button click
        loginButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*nDialog = new ProgressDialog(Login.this);
                nDialog.setTitle("Checking Network");
                nDialog.setMessage("Loading..");
                nDialog.setIndeterminate(false);
                nDialog.setCancelable(true);
                nDialog.show();*/

                // Create user object
                mUser = new User();
                mUser.mFirstName = firstNameView.getText().toString().trim();
                mUser.mLastName = lastNameView.getText().toString().trim();
                mUser.mCountryCode = countryCodeView.getText().toString().trim();
                mUser.mPhoneNumber = phoneNumberView.getText().toString().trim();

                loginUser(mUser);
            }
        });
    }

    private void initCountryList(Spinner countryView) {
        // Populating the Country Spinner
        // Set items for the Spinner dropdown
        ArrayList<String> countries = new ArrayList<String>();
        countries.add("India");
        countries.add("Australia");
        countries.add("Brazil");
        countries.add("China");
        countries.add("Canada");
        countries.add("Russia");
        countries.add("Singapore");
        countries.add("United States");

        // Create the adapter for the spinner
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_layout, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach the adapter to the spinner
        countryView.setAdapter(adapter);

        // Hide country for now
        countryView.setVisibility(View.GONE);
    }

    /*
    * User registration and/or Login
    * */
    private void loginUser(final User user) {
        // Make a request to the server

        OkHttpClient client = new OkHttpClient();

        HTTPRequestObject userData = new HTTPRequestObject();
        userData.put("first_name", user.mFirstName);
        userData.put("last_name", user.mLastName);
        userData.put("country_code", user.mCountryCode);
        userData.put("phone_number", user.mPhoneNumber);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), userData.getJSON());
        Request request = new Request.Builder()
                .url(LoopApplication.SERVER_URL + "/users/register")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                // Failed
                Log.d(TAG, "failed");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                // Background Thread

                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    String responseJSON = responseBody.string();
                    // Log.d(TAG, responseJSON);

                    try {
                        JSONObject json = new JSONObject(responseJSON);
                        String status = json.getString("status");

                        if (status.equals("success")) {
                            String verificationCode = json.getString("verification_code");
                            mUser.mVerificationCode = verificationCode;

                            byte[] verificationCodeData = verificationCode.getBytes();

                            // Send this verification code as an SMS
                            // to "self" and verify the number
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendDataMessage(
                                    user.mPhoneNumber,
                                    null,
                                    LoopApplication.SMS_PORT,
                                    verificationCodeData,
                                    null,
                                    null
                            );

                            // Register a broadcast receiver
                            IntentFilter intentFilter = new IntentFilter("android.intent.action.DATA_SMS_RECEIVED");
                            intentFilter.setPriority(10);
                            intentFilter.addDataScheme("sms");
                            intentFilter.addDataAuthority("*", String.valueOf(LoopApplication.SMS_PORT));
                            registerReceiver(smsReceiver, intentFilter);
                        }
                        else {
                            // Show a dialog with error
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    // Catch and do some shit
                }

            }
        });

        // Send Verification SMS
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class User {
        // Fields
        public String mFirstName;
        public String mLastName;
        public String mCountryCode;
        public String mPhoneNumber;
        public String mVerificationCode;

        public User() {
            // No shit to do :D
        }
    }
}
