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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class UpdateBook extends AppCompatActivity {

    final Context thisContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_book);

        //getting the email and books from the cache
        SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
        String bookSelected = sharedPreferences.getString("bookLogLastBookSelected", "DEFAULTEMAIL");
        String allBooks = sharedPreferences.getString("bookLogBooks", "DEFAULTBOOK");

        //getting the error section
        TextView error = (TextView) findViewById(R.id.error_section);

        //make conditional on not being DEFAULTEMAIL
        if(allBooks.compareTo("DEFAULTBOOK") == 0) {
            error.setText("There are no books recorded.");
            error.setVisibility(View.VISIBLE);
        }
        else{
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

            if (booksOwnedCount == 0) {//if no books are recorded

                error.setText("There are no books recorded.");
                error.setVisibility(View.VISIBLE);

            } else {//if the user has books
                for (int i = 0; i < booksOwnedCount && soughtBookIndex == -1; i++) {
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

                if (soughtBookIndex > -1) {//if the sought book is found, copy its values to the form
                    try {

                        JSONObject theBook = booksOwned.getJSONObject(soughtBookIndex);
                        EditText title = (EditText)findViewById(R.id.book_title);
                        title.setText(theBook.getString("title"));

                        EditText author = (EditText)findViewById(R.id.book_author);
                        if (theBook.has("author") && !theBook.isNull("author")) {
                            author.setText(theBook.getString("author"));
                        }

                        DatePicker date = (DatePicker) findViewById(R.id.book_date);
                        if (theBook.has("dateRead") && !theBook.isNull("dateRead")) {

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(Long.valueOf(theBook.getString("dateRead")).longValue());

                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);

                            date.init(year, month, day, null);
                        }

                        if (theBook.has("genre") && !theBook.isNull("genre")) {

                            String oldGenre = theBook.getString("genre");
                            int genrePosition = -1;
                            String[] radioText = {"Action and Adventure", "Anthology", "Art", "Biography", "Children and Young Adult", "Comic and Graphic Novel", "Drama", "Fantasy", "Health", "History", "Horror", "How-To", "Mystery", "Poetry", "Religion, Spirituality, and New Age", "Romance", "Science", "Science Fiction", "Self Help","Travel", "Other"};
                            int[] radioId = {R.id.genre_action_and_adventure, R.id.genre_anthology, R.id.genre_art, R.id.genre_biography, R.id.genre_children_and_young_adult, R.id.genre_comic_and_graphic_novel, R.id.genre_drama, R.id.genre_fantasy, R.id.genre_health, R.id.genre_history, R.id.genre_horor, R.id.genre_how_to, R.id.genre_mystery, R.id.genre_poetry, R.id.genre_religion_spirituality_and_new_age, R.id.genre_romance, R.id.genre_science, R.id.genre_science_fiction, R.id.genre_self_help, R.id.genre_travel, R.id.genre_other};

                            RadioGroup radio = (RadioGroup)findViewById(R.id.genre_radio_group);

                            for(int i = 0; i < radioText.length && genrePosition == -1; i++){
                                if(oldGenre.equals(radioText[i])) {
                                    genrePosition = i;
                                }
                            }
                            if(genrePosition > -1) {
                                radio.check(radioId[genrePosition]);
                            }

                        }

                        RatingBar rating = (RatingBar)findViewById(R.id.book_rating);
                        if (theBook.has("rating") && !theBook.isNull("rating")) {
                            rating.setRating(Float.parseFloat(theBook.getString("rating")));
                        }

                    } catch (JSONException e) {
                        //TODO:HANDLE EXCEPTION
                    }
                } else {//if the book is not found - this error message should never be shown

                    error.setText("Sought book not found.");
                    error.setVisibility(View.VISIBLE);
                    RatingBar rating = (RatingBar)findViewById(R.id.book_rating);
                    rating.setVisibility(View.GONE);
                }
            }
        }
    }

    //when the user clicks the save button, or whatever it's called
    public void updateBook(View view) {

        //getting the email from the cache
        SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("bookLogLastEmail", "DEFAULTEMAIL");

        //getting the other values from form
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

        //putting the API call on a separate thread
        if(bookTitle != null && !bookTitle.isEmpty()) {//if the input title is not empty

            final String finalBookGenre = bookGenre;
            final String finalBookGenre1 = bookGenre;
            final float finalBookRating = bookRating;
            Runnable runnable = new Runnable() {
                public void run() {

                    //getting the email from the cache
                    SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
                    String email = sharedPreferences.getString("bookLogLastEmail", "DEFAULTEMAIL");

                    //creating URL string
                    String baseURL = "http://booklog.fkt9dqquch.us-west-2.elasticbeanstalk.com/";
                    String getURL = baseURL + "books?";

                    try {//copying over values into URL

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
                        connection.setRequestMethod("PUT");

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

                        if (statusCode == 201 || statusCode == 304) {//if success - book added
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Intent newIntent = new Intent(thisContext, DisplayBooks.class);//moving to DisplayBooks
                                    startActivity(newIntent);
                                }
                            });

                        } else {//failure - book not added
                            final String finalGetURL = getURL;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    TextView errorSection  = (TextView)findViewById(R.id.error_section);
                                    errorSection.setText("There was an error updating the book.");
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
            Intent nextIntent = new Intent(this, LogInSignUp.class);//moving to LogInSignUp
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
                                    Intent newIntent = new Intent(thisContext, LogInSignUp.class);//moving to LogInSignUp
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
