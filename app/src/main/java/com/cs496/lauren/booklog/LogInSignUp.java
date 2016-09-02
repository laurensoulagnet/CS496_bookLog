package com.cs496.lauren.booklog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class LogInSignUp extends AppCompatActivity {

    //copied from http://stackoverflow.com/questions/12947620/email-address-validation-in-android-on-edittext for Email validation
    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    //called when the Submit/Create Accont button is clicked
    private View.OnClickListener buttonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
            int tabPos = tabs.getSelectedTabPosition();

            if(v.getId() == R.id.submit_button && tabPos == 0){//if the submit button is clicked and the tab is Log In
                enterEmail(v);
            }
            else if(v.getId() == R.id.submit_button && tabPos == 1){//if the submit button is clicked and the tab is Sign Up
                newEmail(v);
            }
        }
    };

    //called if the user is creating a new account
    public void newEmail(View view) {

        //getting the necessary values from the form, and hashing
        EditText emailInputEditText = (EditText) findViewById(R.id.email_input);//gets the email input by ID
        final String emailInputUnhashed = emailInputEditText.getText().toString();//getting the email as a string
        final String emailInput = Integer.toString(emailInputEditText.getText().toString().hashCode());//getting the hashed email as a string
        EditText passInputEditText = (EditText) findViewById(R.id.password_input);//gets the password input by ID
        final String passInput = Integer.toString(passInputEditText.getText().toString().hashCode());//getting the hashed password as a string

        if(isValidEmail(emailInputEditText.getText().toString())) {//if the input email is valid
            final Context thisContext = this;

            Runnable runnable = new Runnable() {
                public void run() {

                    //creating the URL
                    String baseURL = "http://booklog.fkt9dqquch.us-west-2.elasticbeanstalk.com/";
                    String getURL = baseURL;

                    try{
                        getURL += "users?email="+URLEncoder.encode(emailInput, "UTF-8")+"&pass="+URLEncoder.encode(passInput, "UTF-8");
                    }
                    catch(UnsupportedEncodingException except) {
                        // TODO: exception handling
                    }

                    HttpURLConnection connection = null;
                    try{
                        connection = (HttpURLConnection) new URL(getURL).openConnection();//connecting
                        connection.setDoOutput(true);
                    }
                    catch(UnsupportedEncodingException except) {
                        // TODO: exception handling
                    }
                    catch(MalformedURLException except) {
                        // TODO: exception handling
                    }
                    catch(IOException except) {
                        // TODO: exception handling
                    }

                    try{
                        int statusCode = connection.getResponseCode();
                        if (statusCode ==  201) {//if the email and password are recorded

                            //saving email in cache
                            SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("bookLogLastEmailUnhashed", emailInputUnhashed);
                            editor.commit();
                            editor.putString("bookLogLastEmail", emailInput);
                            editor.commit();

                            Intent nextIntent = new Intent(thisContext, DisplayBooks.class);//moving to DisplayBooks
                            startActivity(nextIntent);
                        }
                        else{//if the email and password are not recorded
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView errorSection  = (TextView)findViewById(R.id.error_section);
                                    errorSection.setText("The email is already registered. Please log in or try a new email.");
                                    errorSection.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                    catch(IOException except) {
                        // TODO: exception handling
                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
        else {//if the input email is invalid

            TextView errorSection  = (TextView)findViewById(R.id.error_section);
            errorSection.setText("That is an invalid email.");
            errorSection.setVisibility(View.VISIBLE);
        }
    }

    // Called when the user presses the submit button to log in
    public void enterEmail(View view) {

        //getting the email and password input, and hashing them
        EditText emailInputEditText = (EditText) findViewById(R.id.email_input);//gets the email input by ID
        final String emailInputUnhashed = emailInputEditText.getText().toString();//getting the email as a string
        final String emailInput = Integer.toString(emailInputEditText.getText().toString().hashCode());//getting the hashed email as a string
        EditText passInputEditText = (EditText) findViewById(R.id.password_input);//gets the password input by ID
        final String passInput = Integer.toString(passInputEditText.getText().toString().hashCode());//getting the hashed password as a string

        if(isValidEmail(emailInputEditText.getText().toString())) {//if the input email is valid
            final Context thisContext = this;

            //putting the API call on a separate thread
            Runnable runnable = new Runnable() {
                public void run() {

                    //creating URL string
                    String baseURL = "http://booklog.fkt9dqquch.us-west-2.elasticbeanstalk.com/";
                    String getURL = baseURL;

                    try{
                        getURL += "users?email="+URLEncoder.encode(emailInput, "UTF-8")+"&pass="+URLEncoder.encode(passInput, "UTF-8");
                    }
                    catch(UnsupportedEncodingException except) {
                        // TODO: exception handling
                    }

                    HttpURLConnection connection = null;
                    try{
                        connection = (HttpURLConnection) new URL(getURL).openConnection();//connecting

                    }
                    catch(UnsupportedEncodingException except) {
                        // TODO: exception handling
                    }
                    catch(MalformedURLException except) {
                        // TODO: exception handling
                    }
                    catch(IOException except) {
                        // TODO: exception handling
                    }

                    try{
                        int statusCode = connection.getResponseCode();
                        connection.disconnect();
                        if (statusCode ==  200) {//if the email and password are recorded

                            //saving email in cache
                            SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("bookLogLastEmailUnhashed", emailInputUnhashed);
                            editor.commit();

                            editor.putString("bookLogLastEmail", emailInput);
                            editor.commit();

                            Intent nextIntent = new Intent(thisContext, DisplayBooks.class);//moving to DisplayBooks
                            startActivity(nextIntent);
                        }
                        else{//if the email and password are not recorded
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView errorSection  = (TextView)findViewById(R.id.error_section);
                                    errorSection.setText("There is no such account registered.");
                                    errorSection.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                    catch(IOException except) {
                        // TODO: exception handling
                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
        else {//if an invalid email is entered

            TextView errorSection  = (TextView)findViewById(R.id.error_section);
            errorSection.setText("That is an invalid email.");
            errorSection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_sign_up);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setting tabs
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("Log In"));
        tabs.addTab(tabs.newTab().setText("Sign Up"));
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);

        //setting password transformation method
        EditText password = (EditText) findViewById(R.id.password_input);
        password.setTransformationMethod(new PasswordTransformationMethod());

        //setting onClick listener
        Button submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(buttonOnClick);

        //setting email
        //checking if there is a saved email
        SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
        String lastEmail = sharedPreferences.getString("bookLogLastEmailUnhashed", "DEFAULTEMAIL");

        //make conditional on not being DEFAULTEMAIL
        if(lastEmail.compareTo("DEFAULTEMAIL") != 0) {
            EditText text = (EditText) findViewById(R.id.email_input);
            text.setText(lastEmail);
        }

        final int[] tabSelected = {0};//array overcomes needing to be declared final issue

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                tabSelected[0] = tab.getPosition();

                //setting the password hint and button text depending on which tab is selected
                if(tabSelected[0] == 0) {//if Log In

                    TextView emailInput = (TextView) findViewById(R.id.email_input);
                    emailInput.setHint("Your Email");
                    TextView passInput = (TextView) findViewById(R.id.password_input);
                    passInput.setHint("Your Password");
                    Button submitButton = (Button) findViewById(R.id.submit_button);
                    submitButton.setText("Log In");

                }
                if(tabSelected[0] == 1) {//if Sign Up

                    TextView emailInput = (TextView) findViewById(R.id.email_input);
                    emailInput.setHint("New Email");
                    TextView passInput = (TextView) findViewById(R.id.password_input);
                    passInput.setHint("New Password");
                    Button submitButton = (Button) findViewById(R.id.submit_button);
                    submitButton.setText("Create Account");
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //adding items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_log_in_sign_up, menu);

        return true;
    }

    //called when a menu item is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
