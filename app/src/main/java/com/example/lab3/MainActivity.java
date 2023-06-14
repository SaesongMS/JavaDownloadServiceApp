package com.example.lab3;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lab3.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLEngineResult;

public class MainActivity extends AppCompatActivity{

    public TextView url;
    public TextView fileSize;
    public TextView fileType;
    public TextView downloadedBytes;
    public ProgressBar progressBar;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StatusInfo statusInfo = intent.getParcelableExtra(IntentService.INFO);
            downloadedBytes.setText(Integer.toString(statusInfo.getDownloadedBytes()));
            progressBar.setProgress(statusInfo.getProgress());
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(IntentService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url = findViewById(R.id.url);
        fileSize = findViewById(R.id.file_size);
        fileType = findViewById(R.id.file_type);
        downloadedBytes = findViewById(R.id.downloaded_bytes);
        progressBar = findViewById(R.id.progress_bar);

        url.setText("https://cdn.kernel.org/pub/linux/kernel/v5.x/linux-5.4.36.tar.xz");
        final Button infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlText = url.getText().toString();
                if (urlText.isEmpty()) {
                    url.setError("Please enter a URL");
                } else {
                    GetInfo getInfo = new GetInfo();
                    getInfo.execute(urlText);
                }
            }
        });

        final Button downloadButton = findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlText = url.getText().toString();
                if (urlText.isEmpty()) {
                    url.setError("Please enter a URL");
                } else {
                    if(ActivityCompat.checkSelfPermission(
                            MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED){
                        IntentService.startService(MainActivity.this, urlText);
                    }else{
                        if(ActivityCompat.shouldShowRequestPermissionRationale(
                                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(MainActivity.this, "Permission needed", Toast.LENGTH_SHORT).show();
                        }
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String fileSizeValue = fileSize.getText().toString();
        String fileTypeValue = fileType.getText().toString();
        String downloadedBytesValue = downloadedBytes.getText().toString();
        int progressBarValue = progressBar.getProgress();

        outState.putString("file_size", fileSizeValue);
        outState.putString("file_type", fileTypeValue);
        outState.putString("downloaded_bytes", downloadedBytesValue);
        outState.putInt("progress_bar", progressBarValue);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String fileSizeValue = savedInstanceState.getString("file_size");
        String fileTypeValue = savedInstanceState.getString("file_type");
        String downloadedBytesValue = savedInstanceState.getString("downloaded_bytes");
        int progressBarValue = savedInstanceState.getInt("progress_bar");


        fileSize.setText(fileSizeValue);
        fileType.setText(fileTypeValue);
        downloadedBytes.setText(downloadedBytesValue);
        progressBar.setProgress(progressBarValue);
    }


    class GetInfo extends AsyncTask<String, Integer, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {
            String mRozmiar="null", mTyp="null";
            HttpsURLConnection urlConnection = null;
            try{
                URL url = new URL(strings[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                mRozmiar = String.valueOf(urlConnection.getContentLength());
                mTyp = urlConnection.getContentType();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            Log.v("Async","Rozmiar"+ mRozmiar+ ", Typ"+ mTyp);
            return new String[]{mRozmiar, mTyp};

        }

        @Override
        protected void onPostExecute(String[] result) {
            fileSize.setText(result[0]);
            fileType.setText(result[1]);
        }

    }
}