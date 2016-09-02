package com.cs496.lauren.booklog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ViewBook extends AppCompatActivity {

    public final static String BOOK_SELECTED= "";
    Context thisContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_book);
        /*
        Intent intent = getIntent();
        String bookSelected = intent.getStringExtra(DisplayBooks.BOOK_SELECTED);
        */
        SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
        String bookSelected = sharedPreferences.getString("bookLogLastBookSelected", "DEFAULTEMAIL");
        String allBooks = sharedPreferences.getString("bookLogBooks", "DEFAULTBOOK");

        TextView error = (TextView) findViewById(R.id.error_section);



        //make conditional on not being DEFAULTEMAIL
        if(allBooks.compareTo("DEFAULTBOOK") == 0) {
            error.setText("There are no books recorded.");
            error.setVisibility(View.VISIBLE);
        }
        else {
            JSONArray booksOwned = null;
            try {
                booksOwned = new JSONArray(allBooks);
            } catch (JSONException e) {
                //TODO: HANDLE JSON EXCEPTION
            }

            int booksOwnedCount = booksOwned.length();
            String currentTitle = null;

            JSONObject currentBook = null;
            int soughtBookIndex = -1;
            int i = 0;


            if (booksOwnedCount == 0) {//if no books are recorded

                error.setText("There are no books recorded.");
                error.setVisibility(View.VISIBLE);

            } else {//if the user has books
                for (i = 0; i < booksOwnedCount && soughtBookIndex == -1; i++) {
                    try {

                        currentBook = booksOwned.getJSONObject(i);
                        currentTitle = currentBook.getString("title");

                    } catch (JSONException e) {
                        //TODO:HANDLE EXCEPTION
                    }

                    if (bookSelected.equals(currentTitle)) {
                        soughtBookIndex = i;
                    }
                }

                if(soughtBookIndex > -1) {//if the sought book is found, print it's info
                    try {
                        JSONObject theBook = booksOwned.getJSONObject(soughtBookIndex);
                        TextView title = (TextView)findViewById(R.id.title_section);
                        title.setText(theBook.getString("title"));

                        TextView author = (TextView)findViewById(R.id.author_section);
                        if (theBook.has("author") && !theBook.isNull("author")) {
                            author.setText(theBook.getString("author"));
                        }
                        else {
                            author.setVisibility(View.GONE);
                        }

                        TextView date = (TextView)findViewById(R.id.date_section);
                        if (theBook.has("dateRead") && !theBook.isNull("dateRead")) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(Long.valueOf(theBook.getString("dateRead")).longValue());

                            String formattedDate = new SimpleDateFormat("EEEE, MMMM d, yyyy").format(calendar.getTime());

                            date.setText(formattedDate);
                        }
                        else {
                            date.setVisibility(View.GONE);
                        }

                        TextView genre = (TextView)findViewById(R.id.genre_section);
                        if (theBook.has("genre") && !theBook.isNull("genre")) {
                            genre.setText(theBook.getString("genre"));
                        }
                        else {
                            genre.setVisibility(View.GONE);
                        }

                        RatingBar rating = (RatingBar)findViewById(R.id.rating_section);
                        if (theBook.has("rating") && !theBook.isNull("rating")) {
                            rating.setRating(Float.parseFloat(theBook.getString("rating")));
                        }
                        else {
                            rating.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        //TODO:HANDLE EXCEPTION
                    }
                }
                else {//if the book is not in the cache, but this error message should never be shown

                    error.setText("Sought book not found.");
                    error.setVisibility(View.VISIBLE);
                }

            }
        }
    }

    //Called when edit FAB clicked
    public void editBook(View view) {

        Intent newIntent = new Intent(this, UpdateBook.class);//moving to UpdateBook
        startActivity(newIntent);
    }

    //Called when delete FAB clicked
    public void deleteBook(View view) {

        Runnable runnable = new Runnable() {
            public void run() {

                //getting the email and book selected from cache
                SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
                String email = sharedPreferences.getString("bookLogLastEmail", "DEFAULTEMAIL");
                String bookTitle = sharedPreferences.getString("bookLogLastBookSelected", "DEFAULTEMAIL");

                //creating URL string
                String baseURL = "http://booklog.fkt9dqquch.us-west-2.elasticbeanstalk.com/";
                String getURL = baseURL + "books?";

                try {//copying over values into URL

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

                                Intent newIntent = new Intent(thisContext, DisplayBooks.class);//moving to DisplayBooks
                                startActivity(newIntent);
                            }
                        });

                    } else {//error deleting book

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                TextView errorSection  = (TextView)findViewById(R.id.error_section);
                                errorSection.setText("There was an error deleting the book.");
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

    //adding items to the action bar if it is present
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
            Intent nextIntent = new Intent(this, LogInSignUp.class);
            startActivity(nextIntent);

            return true;
        }
        else if (id== R.id.delete_account){//if Delete Account

            Runnable runnable = new Runnable() {
                public void run() {
                    //creating URL string
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
                                    Intent newIntent = new Intent(thisContext, LogInSignUp.class);
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
