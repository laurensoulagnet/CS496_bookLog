package com.cs496.lauren.booklog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

public class DisplayBooks extends AppCompatActivity {

    Context thisContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_books);

        //setting tabs
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("Books Read"));
        tabs.addTab(tabs.newTab().setText("Overview"));
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);

        final int[] tabSelected = {0};
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                tabSelected[0] = tab.getPosition();

                //setting views depending on which tab is selected
                if(tabSelected[0] == 0) {//if Books Recorded

                    ListView listSection = (ListView) findViewById(R.id.list_section);
                    listSection.setVisibility(View.VISIBLE);
                    ValueLineChart monthChart = (ValueLineChart) findViewById(R.id.month_chart);
                    monthChart.setVisibility(View.GONE);
                    TextView monthChartTitle = (TextView) findViewById(R.id.month_chart_title);
                    monthChartTitle.setVisibility(View.GONE);
                }
                if(tabSelected[0] == 1) {//if Overview

                    ListView listSection = (ListView) findViewById(R.id.list_section);
                    listSection.setVisibility(View.GONE);
                    TextView textSection = (TextView) findViewById(R.id.text_section);
                    textSection.setVisibility(View.GONE);
                    TextView errorSection = (TextView) findViewById(R.id.error_section);
                    errorSection.setVisibility(View.GONE);

                    //getting all charts and labels
                    ValueLineChart monthChart = (ValueLineChart) findViewById(R.id.month_chart);
                    TextView monthChartTitle = (TextView) findViewById(R.id.month_chart_title);
                    BarChart yearChart = (BarChart) findViewById(R.id.year_chart);
                    TextView yearChartTitle = (TextView) findViewById(R.id.year_chart_title);
                    PieChart genreChart = (PieChart) findViewById(R.id.genre_chart);
                    TextView genreChartTitle = (TextView) findViewById(R.id.genre_chart_title);
                    PieChart authorChart = (PieChart) findViewById(R.id.author_chart);
                    TextView authorChartTitle = (TextView) findViewById(R.id.author_chart_title);

                    //setting genre array
                    String[] genreArray = {"Action and Adventure", "Anthology", "Art", "Biography", "Children and Young Adult", "Comic and Graphic Novel", "Drama", "Fantasy", "Health", "History", "Horror", "How-To", "Mystery", "Poetry", "Religion, Spirituality, and New Age", "Romance", "Science", "Science Fiction", "Self Help","Travel", "Other"};
                    boolean genreRecorded = false;//records whether there are any records with genres


                    //declaring all containers for values used in charts
                    float[] monthlyReading = new float[12];//12 months, all initialized to 0.0
                    float[] yearlyReading = new float[120];//120 years between 1900 and 2020, all initialized to 0.0
                    float[] genreReading = new float[genreArray.length];//all genres, all initialized to 0.0
                    ArrayList authorReading = new ArrayList();//an array list, for a variable number of authors

                    //creating month input series
                    ValueLineSeries monthSeries = new ValueLineSeries();
                    monthSeries.setColor(0xFF64a0ca);

                    //getting the books from the cache
                    SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
                    String allBooks = sharedPreferences.getString("bookLogBooks", "DEFAULTBOOK");

                    JSONArray booksOwned = null;
                    try {
                        booksOwned = new JSONArray(allBooks);
                    } catch (JSONException e) {
                        //TODO: HANDLE JSON EXCEPTION
                    }

                    int booksOwnedCount = booksOwned.length();
                    String currentTitle = null;
                    JSONObject currentBook = null;

                    if (booksOwnedCount != 0) {//if the user has books
                        for (int i = 0; i < booksOwnedCount; i++) {
                            try {
                                currentBook = booksOwned.getJSONObject(i);

                            } catch (JSONException e) {
                                //TODO:HANDLE EXCEPTION
                            }

                            if (currentBook.has("dateRead") && !currentBook.isNull("dateRead")) {

                                Calendar calendar = Calendar.getInstance();
                                try {
                                    calendar.setTimeInMillis(Long.valueOf(currentBook.getString("dateRead")).longValue());
                                } catch (JSONException e) {
                                    //TODO:HANDLE EXCEPTION
                                }

                                monthlyReading[calendar.get(Calendar.MONTH)] += 1;
                                yearlyReading[calendar.get(Calendar.YEAR) - 1900] += 1;
                            }


                            String currentGenre = null;
                            if (currentBook.has("genre") && !currentBook.isNull("genre")) {

                                try {
                                    currentGenre = currentBook.getString("genre");
                                } catch (JSONException e) {
                                    //TODO:HANDLE EXCEPTION
                                }

                                for(int j = 0; j < genreArray.length; j++){
                                    if(currentGenre.equals(genreArray[j])){
                                        genreReading[j] += 1;
                                        genreRecorded = true;
                                    }
                                }
                            }

                        }
                    }

                    //adding values to month chart
                    monthSeries.addPoint(new ValueLinePoint("Jan", monthlyReading[0]));
                    monthSeries.addPoint(new ValueLinePoint("Feb", monthlyReading[1]));
                    monthSeries.addPoint(new ValueLinePoint("Mar", monthlyReading[2]));
                    monthSeries.addPoint(new ValueLinePoint("Apr", monthlyReading[3]));
                    monthSeries.addPoint(new ValueLinePoint("May", monthlyReading[4]));
                    monthSeries.addPoint(new ValueLinePoint("Jun", monthlyReading[5]));
                    monthSeries.addPoint(new ValueLinePoint("Jul", monthlyReading[6]));
                    monthSeries.addPoint(new ValueLinePoint("Aug", monthlyReading[7]));
                    monthSeries.addPoint(new ValueLinePoint("Sep", monthlyReading[8]));
                    monthSeries.addPoint(new ValueLinePoint("Oct", monthlyReading[9]));
                    monthSeries.addPoint(new ValueLinePoint("Nov", monthlyReading[10]));
                    monthSeries.addPoint(new ValueLinePoint("Dec", monthlyReading[11]));

                    monthChart.addSeries(monthSeries);
                    monthChart.startAnimation();
                    monthChart.setVisibility(View.VISIBLE);
                    monthChartTitle.setVisibility(View.VISIBLE);

                    //adding values to year chart
                    for(int i = 0; i < 120; i++) {
                        //only adding values > 0
                        if(yearlyReading[i] > 0) {
                            yearChart.addBar(new BarModel(Integer.toString((int)(i + 1900)), yearlyReading[i], 0xFF64a0ca));
                        }
                    }

                    yearChart.startAnimation();
                    yearChart.setVisibility(View.VISIBLE);
                    yearChartTitle.setVisibility(View.VISIBLE);

                    //adding values to genre chart, if there are any
                    if(genreRecorded == true) {
                        for(int k = 0; k < genreArray.length; k++){
                            if(genreReading[k] > 0) {
                                genreChart.addPieSlice(new PieModel(genreArray[k], (int)genreReading[k], (0xFF64a0ca+ (k*300))));//separating colors by arbitrary distance, offset of base chart color
                            }
                        }

                        genreChart.startAnimation();
                        genreChart.setVisibility(View.VISIBLE);
                        genreChartTitle.setVisibility(View.VISIBLE);
                    }

                    //adding values to author chart










                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

        });


        SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("bookLogLastEmail", "DEFAULTEMAIL");

        //putting the API call on a separate thread
        Runnable runnable = new Runnable() {
            public void run() {

                //creating URL string
                SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
                String email = sharedPreferences.getString("bookLogLastEmail", "DEFAULTEMAIL");

                String baseURL = "http://booklog.fkt9dqquch.us-west-2.elasticbeanstalk.com/";
                String getURL = baseURL;

                try{
                    getURL += "books?email="+URLEncoder.encode(email, "UTF-8");

                }
                catch(UnsupportedEncodingException except) {
                    // TODO: exception handling
                }

                HttpURLConnection connection = null;
                try{
                    connection = (HttpURLConnection) new URL(getURL).openConnection();
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

                    if (statusCode ==  200) {//if the email is in the records

                        InputStream is = connection.getInputStream();
                        final String response = IOUtils.toString(is, "UTF-8");
                        is.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                TextView textSection = (TextView)findViewById(R.id.text_section);

                                try {

                                    JSONObject responseObject = new JSONObject(response);
                                    JSONArray booksOwned = responseObject.getJSONArray("booksOwned");

                                    SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("bookLogBooks", booksOwned.toString());
                                    editor.commit();

                                    int booksOwnedCount = booksOwned.length();

                                    JSONObject currentObject;
                                    String[] bookTitles = new String[booksOwnedCount];

                                    if (booksOwnedCount == 0) {//if the user is recorded but has no books, printing a message

                                        textSection.setText("No books have been recorded.");
                                        textSection.setVisibility(View.VISIBLE);

                                        ListView listSection = (ListView) findViewById(R.id.list_section);
                                        listSection.setVisibility(View.GONE);
                                        TextView errorSection = (TextView) findViewById(R.id.error_section);
                                        errorSection.setVisibility(View.GONE);
                                        ValueLineChart monthChart = (ValueLineChart) findViewById(R.id.month_chart);
                                        monthChart.setVisibility(View.GONE);
                                        TextView monthChartTitle = (TextView) findViewById(R.id.month_chart_title);
                                        monthChartTitle.setVisibility(View.GONE);
                                        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
                                        tabs.setVisibility(View.GONE);

                                    } else {//if the user has books, setting the list

                                        for(int i = 0; i < booksOwnedCount; i++){
                                            currentObject = booksOwned.getJSONObject(i);
                                            bookTitles[i] = currentObject.getString("title");
                                        }

                                        findViewById(R.id.text_section).setVisibility(View.GONE);
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(thisContext, android.R.layout.simple_list_item_1, bookTitles);//note - simle_list_item is built-in
                                        final ListView listView = (ListView) findViewById(R.id.list_section);
                                        listView.setAdapter(adapter);
                                        listView.setOnItemClickListener(selectBook);
                                    }



                                } catch (JSONException except) {
                                    //TODO: handle JSON exception
                                }


                            }
                        });
                    }
                    else{//if no books have been recorded, print such a message

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                TextView textSection  = (TextView)findViewById(R.id.text_section);
                                textSection.setText("No books have been recorded.");

                                textSection.setVisibility(View.VISIBLE);
                                ListView listSection = (ListView) findViewById(R.id.list_section);
                                listSection.setVisibility(View.GONE);
                                TextView errorSection = (TextView) findViewById(R.id.error_section);
                                errorSection.setVisibility(View.GONE);
                                ValueLineChart monthChart = (ValueLineChart) findViewById(R.id.month_chart);
                                monthChart.setVisibility(View.GONE);
                                TextView monthChartTitle = (TextView) findViewById(R.id.month_chart_title);
                                monthChartTitle.setVisibility(View.GONE);
                                TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
                                tabs.setVisibility(View.GONE);
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

    //Called when + FAB clicked
    public void addBook(View view) {

        Intent newIntent = new Intent(this, AddBook.class);//moving to AddBook
        startActivity(newIntent);
        
    }

    private AdapterView.OnItemClickListener selectBook = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView , View view , int position ,long id) {

            String selectedEntry = adapterView.getItemAtPosition(position).toString();

            //saving the book selected in the cache
            SharedPreferences sharedPreferences = getSharedPreferences("BookLogPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("bookLogLastBookSelected", selectedEntry);
            editor.commit();

            Intent oldIntent = getIntent();
            //String email = oldIntent.getStringExtra(LogInSignUp.EMAIL_INPUT);

            Intent newIntent = new Intent(thisContext, ViewBook.class);
            //newIntent.putExtra(BOOK_SELECTED, selectedEntry);
            //newIntent.putExtra(EMAIL_INPUT, email);
            startActivity(newIntent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //adding items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_general, menu);

        return true;
    }

    //called when a menu item is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.log_out) {//if Log Out selected
            Intent nextIntent = new Intent(this, LogInSignUp.class);
            startActivity(nextIntent);

            return true;
        }
        else if (id== R.id.delete_account){//if Delete Account selected

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

                    //following URLConnection section based on code at http://stackoverflow.com/questions/4543894/android-java-http-post-request
                    //http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests also referenced
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

