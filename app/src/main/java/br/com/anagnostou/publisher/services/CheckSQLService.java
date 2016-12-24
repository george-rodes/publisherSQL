package br.com.anagnostou.publisher.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Relatorio;
import br.com.anagnostou.publisher.phpmysql.TTrelatorioRequest;
import br.com.anagnostou.publisher.utils.*;

public class CheckSQLService extends Service {
    private boolean isCheckUpdatesOn;
    private Handler mHandler;
    public static final int NOTIFICATION_ID = 234;
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    SharedPreferences sp;
    SharedPreferences.Editor spEditor;
    String checkTTrelatorioUrl;
    String checkTTcadastroUrl;
    Integer checkIntervall;
    String ttrelatorio_id;
    Integer id, idRegistroProcessado;
    Boolean dataBaseOperationInProgress = false;

    public class CheckSQLServiceBinder extends Binder {

        public CheckSQLService getService() {
            //L.m("CheckSQLServiceBinder.getService");
            return CheckSQLService.this;
        }
    }

    private IBinder mBinder = new CheckSQLServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        //L.m("onBind");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkTTrelatorioUrl = sp.getString("php_ttrelatorio", "");
        checkTTcadastroUrl = sp.getString("php_ttcadastro", "");
        ttrelatorio_id = sp.getString("ttrelatorio_id", "");
        if (!ttrelatorio_id.isEmpty()) {
            id = Integer.parseInt(ttrelatorio_id);
            L.m("onStartCommand ttrelatorio_id: " + ttrelatorio_id);
        } else L.m("ttrelatorio_id.isEmpty");

        //L.m("On Start Command ttrelatorio_id: " + ttrelatorio_id);
        //id = Integer.parseInt(ttrelatorio_id);
        /*
        ttrelatorio_id = sp.getString("ttrelatorio_id", "");
        if (!ttrelatorio_id.isEmpty()) {
            id = Integer.parseInt(ttrelatorio_id);
            L.m("ttrelatorio_id: " + ttrelatorio_id);
        } else id = 1;
        idRegistroProcessado = id;
        */
        return START_NOT_STICKY; //START_STICKY
    }

    @Override
    public void onCreate() {
        // super.onCreate();
        dbAdapter = new DBAdapter(getApplicationContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sp.edit();
        checkTTrelatorioUrl = sp.getString("php_ttrelatorio", "");
        checkTTcadastroUrl = sp.getString("php_ttcadastro", "");
        id = 1;
        dataBaseOperationInProgress = false;
        checkIntervall = 60000;
        isCheckUpdatesOn = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkUpdates();
            }
        }).start();
    }

    private void checkUpdates() {
        L.m("checkUpdates every ms: " + checkIntervall);
        while (isCheckUpdatesOn && !dataBaseOperationInProgress) {
            try {
                //createNotification();
                Thread.sleep(5000);
                checkTTrelatorio();
                Thread.sleep(checkIntervall);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkTTrelatorio() {
       L.m("id beginning checkTTrelatorio: " + id);
        StringRequest stringRequest = new StringRequest(checkTTrelatorioUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.contentEquals("0")) {
                    dataBaseOperationInProgress = true;
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (!sqLiteDatabase.isOpen())
                            sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject.getInt("id") > id) {
                                if (jsonObject.getString("action").contentEquals("INSERT")) {
                                    dbAdapter.insertDataRelatorio(new Relatorio(jsonObject.getInt("ano"),
                                            jsonObject.getInt("mes"), jsonObject.getString("nome"),
                                            jsonObject.getString("modalidade"), jsonObject.getInt("videos"),
                                            jsonObject.getInt("horas"), jsonObject.getInt("publicacoes"),
                                            jsonObject.getInt("revisitas"), jsonObject.getInt("estudos")));

                                } else if (jsonObject.getString("action").contentEquals("UPDATE")) {
                                    dbAdapter.updateDataRelatorio(new Relatorio(jsonObject.getInt("ano"),
                                            jsonObject.getInt("mes"), jsonObject.getString("nome"),
                                            jsonObject.getString("modalidade"), jsonObject.getInt("videos"),
                                            jsonObject.getInt("horas"), jsonObject.getInt("publicacoes"),
                                            jsonObject.getInt("revisitas"), jsonObject.getInt("estudos")));
                                }
                                idRegistroProcessado = jsonObject.getInt("id");
                            }
                        }
                        id = idRegistroProcessado;
                        spEditor.putString("ttrelatorio_id",idRegistroProcessado.toString());
                        spEditor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dataBaseOperationInProgress = false;
                    L.m("DatabaseOperation Completed! with ID: " + idRegistroProcessado);
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
        L.m("onDestroy");
        super.onDestroy();
        isCheckUpdatesOn = false;
    }
}
