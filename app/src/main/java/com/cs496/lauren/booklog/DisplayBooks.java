package com.cs496.lauren.booklog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import org.eazegraph.lib.charts.ValueLineChart;
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

                    ValueLineChart monthChart = (ValueLineChart) findViewById(R.id.month_chart);
                    monthChart.setVisibility(View.VISIBLE);
                    TextView monthChartTitle = (TextView) findViewById(R.id.month_chart_title);
                    monthChartTitle.setVisibility(View.VISIBLE);

                    //creating the month chart
                    ValueLineSeries series = new ValueLineSeries();
                    series.setColor(0xFF64a0ca);

                    float monthlyReading[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

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
                            }
                        }
                    }

                    series.addPoint(new ValueLinePoint("Jan", monthlyReading[0]));
                    series.addPoint(new ValueLinePoint("Feb", monthlyReading[1]));
                    series.addPoint(new ValueLinePoint("Mar", monthlyReading[2]));
                    series.addPoint(new ValueLinePoint("Apr", monthlyReading[3]));
                    series.addPoint(new ValueLinePoint("May", monthlyReading[4]));
                    series.addPoint(new ValueLinePoint("Jun", monthlyReading[5]));
                    series.addPoint(new ValueLinePoint("Jul", monthlyReading[6]));
                    series.addPoint(new ValueLinePoint("Aug", monthlyReading[7]));
                    series.addPoint(new ValueLinePoint("Sep", monthlyReading[8]));
                    series.addPoint(new ValueLinePoint("Oct", monthlyReading[9]));
                    series.addPoint(new ValueLinePoint("Nov", monthlyReading[10]));
                    series.addPoint(new ValueLinePoint("Dec", monthlyReading[11]));

                    monthChart.addSeries(series);
                    monthChart.startAnimation();
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

