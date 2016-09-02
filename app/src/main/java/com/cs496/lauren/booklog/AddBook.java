package com.cs496.lauren.booklog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class AddBook extends AppCompatActivity {

    final Context thisContext = this;

    public void recordBook(View view) {

        //getting the current email from the cache
        SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("bookLogLastEmail", "DEFAULTEMAIL");

        //getting other values from the form
        EditText editText = (EditText) findViewById(R.id.book_title);
        final String bookTitle = editText.getText().toString();

        editText = (EditText) findViewById(R.id.book_author);
        final String bookAuthor = editText.getText().toString();

        String bookGenre = null;
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.genre_radio_group);
        if(radioGroup.getCheckedRadioButtonId() != -1){
            int id= radioGroup.getCheckedRadioButtonId();
            View radioButton = radioGroup.findViewById(id);
            int radioId = radioGroup.indexOfChild(radioButton);
            RadioButton genreRadioButton = (RadioButton) radioGroup.getChildAt(radioId);
            bookGenre = (String) genreRadioButton.getText();
        }

        float bookRating = 0;
        RatingBar ratingBar = (RatingBar) findViewById(R.id.book_rating);
        bookRating = ratingBar.getRating();

        DatePicker datePicker = (DatePicker) findViewById(R.id.book_date);
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        final String bookDate = Long.toString(calendar.getTimeInMillis());

        //making the API call, but on a separate thread
        if(bookTitle != null && !bookTitle.isEmpty()) {//if the input title is not empty

            final String finalBookGenre = bookGenre;
            final String finalBookGenre1 = bookGenre;
            final float finalBookRating = bookRating;

            Runnable runnable = new Runnable() {
                public void run() {

                    //creating the URL string
                    //getting the email from the cache
                    SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
                    String email = sharedPreferences.getString("bookLogLastEmail", "DEFAULTEMAIL");

                    String baseURL = "http://booklog.fkt9dqquch.us-west-2.elasticbeanstalk.com/";
                    String getURL = baseURL + "books?";

                    try {
                        if(bookAuthor != null && !bookAuthor.isEmpty()) {
                            getURL += "&author=";
                            getURL += URLEncoder.encode(bookAuthor, "UTF-8");
                        }

                        if(finalBookGenre != null && !finalBookGenre.isEmpty()) {
                            getURL += "&genre=";
                            getURL += URLEncoder.encode(finalBookGenre, "UTF-8");
                        }

                        if(finalBookRating > 0) {
                            getURL += "&rating=";
                            getURL += URLEncoder.encode(Float.toString(finalBookRating), "UTF-8");
                        }

                        getURL += "&dateRead=";
                        getURL += URLEncoder.encode(bookDate, "UTF-8");

                        getURL += "&title=";
                        getURL += URLEncoder.encode(bookTitle, "UTF-8");

                        getURL += "&email=";
                        getURL += URLEncoder.encode(email, "UTF-8");


                    } catch (UnsupportedEncodingException except) {
                        // TODO: exception handling
                    }

                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(getURL).openConnection();
                        connection.setDoOutput(true);

                    } catch (UnsupportedEncodingException except) {
                        // TODO: exception handling
                    } catch (MalformedURLException except) {
                        // TODO: exception handling
                    } catch (IOException except) {
                        // TODO: exception handling
                    }

                    try {
                        int statusCode = connection.getResponseCode();
                        connection.disconnect();

                        if (statusCode == 201) {//success - book added
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Intent newIntent = new Intent(thisContext, DisplayBooks.class);//going to DisplayBooks
                                    startActivity(newIntent);
                                }
                            });

                        } else {//failure - book not added

                            final String finalGetURL = getURL;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    TextView errorSection  = (TextView)findViewById(R.id.error_section);
                                    errorSection.setText("There was an error adding the book.");
                                    errorSection.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } catch (IOException except) {
                        // TODO: exception handling
                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();


        }
        else {//if an invalid email is entered

            TextView errorSection  = (TextView)findViewById(R.id.error_section);
            errorSection.setText("A book title is required.");
            errorSection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
    }

    //adding items to the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_general, menu);

        return true;
    }

    //called when a menu item is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.log_out) {//if Log Out
            Intent nextIntent = new Intent(this, LogInSignUp.class);//going to LogInSignUp
            startActivity(nextIntent);

            return true;
        }
        else if (id== R.id.delete_account){//if Delete Account

            Runnable runnable = new Runnable() {
                public void run() {
                    //creating URL string
                    //getting the email from the cache
                    SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
                    String email = sharedPreferences.getString("bookLogLastEmail", "DEFAULTEMAIL");

                    String baseURL = "http://booklog.fkt9dqquch.us-west-2.elasticbeanstalk.com/";
                    String getURL = baseURL + "users?";

                    try {//copying over values into URL
                        getURL += "&email=";
                        getURL += URLEncoder.encode(email, "UTF-8");

                    } catch (UnsupportedEncodingException except) {
                        // TODO: exception handling
                    }

                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(getURL).openConnection();
                        connection.setRequestMethod("DELETE");

                    } catch (UnsupportedEncodingException except) {
                        // TODO: exception handling
                    } catch (MalformedURLException except) {
                        // TODO: exception handling
                    } catch (IOException except) {
                        // TODO: exception handling
                    }

                    try {
                        int statusCode = connection.getResponseCode();
                        connection.disconnect();

                        if (statusCode == 200) {//success - book deleted
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Intent newIntent = new Intent(thisContext, LogInSignUp.class);//going to LogInSignUp
                                    startActivity(newIntent);
                                }
                            });

                        } else {//error deleting book

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    TextView errorSection  = (TextView)findViewById(R.id.error_section);
                                    errorSection.setText("There was an error deleting the account.");
                                    errorSection.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } catch (IOException except) {
                        // TODO: exception handling
                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
