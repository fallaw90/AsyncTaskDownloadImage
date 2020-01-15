package com.fallntic.asynctaskdownloadimage;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private EditText selectionText;
    private ListView chooseImageList;
    public static String[] listOfImages;
    private ProgressBar downloadImmagesProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectionText = (EditText) findViewById(R.id.urlSelectionText);
        chooseImageList = (ListView) findViewById(R.id.chooseImageList);
        listOfImages = getResources().getStringArray(R.array.imageUrls);
        downloadImmagesProgress = (ProgressBar) findViewById(R.id.downloadProgress);

        chooseImageList.setOnItemClickListener(this);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }*/

    public void downloadImage(View view) {

        String url = selectionText.getText().toString();
        if (url != null && url.length() > 0) {
            MyTask myTask = new MyTask();
            myTask.execute(url);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        selectionText.setText(listOfImages[i]);
    }


    /*
     * AsyncTask<Params, Progress, Result>
     * Params: doInBackground()
     * Progress: Value of the update
     * Result: Return value of doInBackground()
     */
    class MyTask extends AsyncTask<String, Integer, Boolean> {

        public MyTask(){

        }

        private int contentLength, counter = 0, calulatedProgress = 0;

        @SuppressLint("WrongConstant")
        @Override
        protected void onPreExecute() {
            downloadImmagesProgress.setVisibility(View.VISIBLE);
            //Lock the screen
            if (MainActivity.this.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {

                MainActivity.this.setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
            }
            else {
                MainActivity.this.setRequestedOrientation(Configuration.ORIENTATION_LANDSCAPE);
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {

            boolean successful = false;
            URL downloadURL = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            File file = null;
            try {
                downloadURL = new URL(params[0]);
                //Recommend it by goole for all purposes while establishing connection
                connection = (HttpURLConnection) downloadURL.openConnection();
                contentLength = connection.getContentLength();
                inputStream = connection.getInputStream();

                file = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                .getAbsoluteFile() + "/" + Uri.parse(params[0]).getLastPathSegment());
                fileOutputStream = new FileOutputStream(file);
                L.m("" + file.getAbsolutePath());
                int read = -1;
                byte[] buffer = new byte[1024];
                while ((read = inputStream.read(buffer)) != -1) {

                    fileOutputStream.write(buffer, 0, read);
                    counter += read;
                    L.m("Counter: " + counter + " Content length: " + contentLength);
                    publishProgress(counter);

                }
                successful = true;
            } catch (MalformedURLException e) {
                L.m(e + "");
                e.printStackTrace();
            } catch (IOException e) {
                L.m(e + "");
                e.printStackTrace();
            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        L.m(e + "");
                        e.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        L.m(e + "");
                        e.printStackTrace();
                    }
                }
            }

            return successful;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //values[0] is the counter
            //contentLength is the total length of the file
            calulatedProgress = (int) (((double) values[0] / contentLength) * 100);
            downloadImmagesProgress.setProgress(calulatedProgress);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            downloadImmagesProgress.setVisibility(View.INVISIBLE);

            //Release the lock
            MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }
}
