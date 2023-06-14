package com.example.lab3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class IntentService extends android.app.IntentService {
    private static final String ACTION_DOWNLOAD = "com.example.lab3.action.DOWNLOAD";
    private static final String Param1 = "com.example.intent_service.extra.PARAM1";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    public int bytes = 0;
    public int fileSize = 0;
    public final static String NOTIFICATION = "com.example.intent_service.receiver";
    public final static String INFO = "info";

    public static void startService(Context context, String param){
        Intent intent = new Intent(context, IntentService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(Param1, param);
        context.startService(intent);
    }

    public IntentService(){
        super("IntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prepareNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_DOWNLOAD.equals(action)){
                final String param1 = intent.getStringExtra(Param1);
                perfromTask(param1);
            }else{
                Log.e("intent_service", "Unknown action");
            }
        }
        Log.d("intent_service", "service has done the job");
    }

    private void perfromTask(String param){
        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            URL url = new URL(param);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            fileSize = urlConnection.getContentLength();

            File workingFile = new File(url.getFile());
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), workingFile.getName());
            if(outputFile.exists()){
                outputFile.delete();
            }

            DataInputStream reader = new DataInputStream(urlConnection.getInputStream());
            fileOutputStream = new FileOutputStream(outputFile.getPath());
            final int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int downloadedBytes = reader.read(buffer, 0, bufferSize);
            while(downloadedBytes != -1){
                fileOutputStream.write(buffer, 0, downloadedBytes);
                bytes += downloadedBytes;
                downloadedBytes = reader.read(buffer, 0, bufferSize);
                notificationManager.notify(NOTIFICATION_ID, createNotification());
                sendBroadcast(bytes, fileSize, "Downloading...", progressValue());
            }

            if(inputStream != null){
                try{
                    inputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            if(fileOutputStream != null){
                try{
                    fileOutputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            if(urlConnection != null){
                try{
                    urlConnection.disconnect();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            Log.d("intent_service", "download has been finished");
            Log.d("intent_service", "downloaded size: " + bytes);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void prepareNotificationChannel(){
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel("IntentService", name, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("downloadedBytes", bytes);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("IntentService")
                .setProgress(100, progressValue(), false)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH);


        if(progressValue() < 100){
            builder.setOngoing(true);
        }else{
            builder.setOngoing(false);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder.setChannelId("IntentService");
        }

        return builder.build();
    }

    private void sendBroadcast(int downloadedBytes, int fileSize, String status, int progress){
        Intent intent = new Intent(NOTIFICATION);
        StatusInfo statusInfo = new StatusInfo(downloadedBytes, fileSize, status, progress);
        intent.putExtra(INFO, statusInfo);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private int progressValue(){
        if(fileSize == 0){
            return 0;
        }else
            return (int) ((bytes * 100L) / fileSize);
    }


}
