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

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Relatorio;
import br.com.anagnostou.publisher.utils.*;

/*******
 * Creates multiples THREADs, not good. using Intentservice
 */

public class CheckSQLService extends Service {
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
    String ttcadastro_id;
    Thread thread;

    Integer idRelatorio, idCadastro, idRegistroProcessadoRelatorio, idRegistroProcessadoCadastro;
    Boolean dataBaseOperationInProgress = false;

    public class CheckSQLServiceBinder extends Binder {
        public CheckSQLService getBinder() {
            //L.m("CheckSQLServiceBinder.getService");
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
        checkTTrelatorioUrl = sp.getString("php_ttrelatorio", "");
        checkTTcadastroUrl = sp.getString("php_ttcadastro", "");

        ttrelatorio_id = sp.getString("ttrelatorio_id", "");
        if (!ttrelatorio_id.isEmpty()) {
            idRelatorio = Integer.parseInt(ttrelatorio_id);
        }

        ttcadastro_id = sp.getString("ttcadastro_id", "");
        if (!ttcadastro_id.isEmpty()) {
            idCadastro = Integer.parseInt(ttcadastro_id);
            L.m("onStartCommand ttcadastro_id: " + ttcadastro_id);
        } else L.m("ttcadastro_id.isEmpty");

        return START_NOT_STICKY; //START_STICKY
    }

    @Override
    public void onCreate() {
        // super.onCreate();
        dbAdapter = new DBAdapter(getApplicationContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        checkTTrelatorioUrl = sp.getString("php_ttrelatorio", "");
        checkTTcadastroUrl = sp.getString("php_ttcadastro", "");

        ttrelatorio_id = sp.getString("ttrelatorio_id", "");
        if (!ttrelatorio_id.isEmpty()) {
            idRelatorio = Integer.parseInt(ttrelatorio_id);
        } else {
            idRelatorio = 0;
        }
        ttcadastro_id = sp.getString("ttcadastro_id", "");
        if (!ttcadastro_id.isEmpty()) {
            idCadastro = Integer.parseInt(ttcadastro_id);
        } else {
            idCadastro = 0;
        }

        dataBaseOperationInProgress = false;
        checkIntervall = 60000;
        if (thread == null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    checkUpdates();
                }
            });
        }
        thread.start();
    }

    private void checkUpdates() {
        L.m("checkUpdates every ms: " + checkIntervall);
        while (!dataBaseOperationInProgress) {
            try {
                //createNotification();
                L.m("Thread ID " + Thread.currentThread().getId());
                Thread.sleep(5000);
                checkTTrelatorio();
                Thread.sleep(5000);
                chechTTcadastro();
                Thread.sleep(checkIntervall);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    private void chechTTcadastro() {
        L.m("chechTTcadastro idCadstro beginning: " + idCadastro);
        StringRequest srCadastro = new StringRequest(checkTTcadastroUrl, new Response.Listener<String>() {
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
                            if (jsonObject.getInt("id") > idCadastro) {
                                if (jsonObject.getString("action").contentEquals("INSERT")) {
                                    /*
                                    dbAdapter.insertDataRelatorio(new Relatorio(jsonObject.getInt("ano"),
                                            jsonObject.getInt("mes"), jsonObject.getString("nome"),
                                            jsonObject.getString("modalidade"), jsonObject.getInt("videos"),
                                            jsonObject.getInt("horas"), jsonObject.getInt("publicacoes"),
                                            jsonObject.getInt("revisitas"), jsonObject.getInt("estudos")));
                                    */
                                } else if (jsonObject.getString("action").contentEquals("UPDATE")) {
                                    /*
                                    dbAdapter.updateDataRelatorio(new Relatorio(jsonObject.getInt("ano"),
                                            jsonObject.getInt("mes"), jsonObject.getString("nome"),
                                            jsonObject.getString("modalidade"), jsonObject.getInt("videos"),
                                            jsonObject.getInt("horas"), jsonObject.getInt("publicacoes"),
                                            jsonObject.getInt("revisitas"), jsonObject.getInt("estudos")));
                                    */
                                }
                                idRegistroProcessadoCadastro = jsonObject.getInt("id");
                            }
                        }
                        if (idRegistroProcessadoCadastro != null) {
                            idCadastro = idRegistroProcessadoCadastro;
                            spEditor = sp.edit();
                            spEditor.putString("ttcadastro_id", idRegistroProcessadoCadastro.toString());
                            spEditor.commit();
                        } else L.m("chechTTcadastro idRegistroProcessado is null ");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dataBaseOperationInProgress = false;
                    L.m("chechTTcadastro DatabaseOperation Completed! with ID: " + idRegistroProcessadoCadastro);
                }
            }
        }, null);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(srCadastro);


    }

    public void checkTTrelatorio() {
        L.m("checkTTrelatorio idRelatorio beginning: " + idRelatorio);
        StringRequest srRelatorio = new StringRequest(checkTTrelatorioUrl, new Response.Listener<String>() {
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
                            if (jsonObject.getInt("id") > idRelatorio) {
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
                                idRegistroProcessadoRelatorio = jsonObject.getInt("id");
                            }
                        }
                        if (idRegistroProcessadoRelatorio != null) {
                            idRelatorio = idRegistroProcessadoRelatorio;
                            spEditor = sp.edit();
                            spEditor.putString("ttrelatorio_id", idRegistroProcessadoRelatorio.toString());
                            spEditor.commit();
                        } else L.m("checkTTrelatorio idRegistroProcessado is null ");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dataBaseOperationInProgress = false;
                    L.m("checkTTrelatorio DatabaseOperation Completed! with ID: " + idRegistroProcessadoRelatorio);
                }
            }
        }, null);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(srRelatorio);
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


}

/*
<service
            android:name=".services.CheckSQLService"
            android:enabled="true"
            android:exported="false"/>
*************


Intent checkSQLServerIntent;
ServiceConnection serviceConnection;
CheckSQLService checkSQLService;
boolean isServiceBound;

@Override
    protected void onStart() {
        super.onStart();
        checkSQLServerIntent = new Intent(this, CheckSQLService.class);
        bindService();

    }

    private void bindService() {
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    CheckSQLService.CheckSQLServiceBinder checkSQLBinder = (CheckSQLService.CheckSQLServiceBinder) iBinder;
                    checkSQLService = checkSQLBinder.getBinder();
                    isServiceBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    isServiceBound = false;
                }
            };

        }
        bindService(checkSQLServerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }





 */