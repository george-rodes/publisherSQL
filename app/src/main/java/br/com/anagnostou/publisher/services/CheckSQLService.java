package br.com.anagnostou.publisher.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.phpmysql.JsonTaskRelatorio;
import br.com.anagnostou.publisher.utils.*;

public class CheckSQLService extends Service {
    private boolean isCheckUpdatesOn;
    private Handler mHandler;
    public static final int NOTIFICATION_ID = 234;
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;

    public class CheckSQLServiceBinder extends Binder {
        public CheckSQLService getService() {
            return CheckSQLService.this;
        }
    }

    private IBinder mBinder = new CheckSQLServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler = new Handler();

        isCheckUpdatesOn = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkUpdates();
            }
        }).start();
        return START_NOT_STICKY; //START_STICKY
    }

    private void checkUpdates() {
        while (isCheckUpdatesOn) {
            try {
                Thread.sleep(10000);
                //createNotification();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkTTreport();
                        //L.t(getApplicationContext(), "Online: " + (Utilidades.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))));
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkTTreport() {
        // com url errado, parou, como tratar do erro
        String url = "http://www.anagnostou.com.br/phptut/json_check_ttreport.php";   //sp.getString("php_report_full", NA);
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                L.t(getApplicationContext(), response);
                if (!response.contentEquals("0")){
                                //change PHP to show either 0 or the JsonArray

                }

            }
        }, null);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public void createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = taskStackBuilder.
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Existem Arquivos a serem baixados")
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCheckUpdatesOn = false;
    }
}
