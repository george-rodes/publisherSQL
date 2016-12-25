package br.com.anagnostou.publisher.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.objetos.Relatorio;
import br.com.anagnostou.publisher.utils.L;

/**
 * Created by George on 24/12/2016.
 */

public class CheckSQLIntentService extends IntentService {
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    SharedPreferences sp;
    SharedPreferences.Editor spEditor;
    String checkTTrelatorioUrl;
    String checkTTcadastroUrl;
    Integer checkIntervall;
    String ttrelatorio_id;
    String ttcadastro_id;
    Integer idRelatorio, idCadastro, idRegistroProcessadoRelatorio, idRegistroProcessadoCadastro;
    Boolean dataBaseOperationInProgress = false;

    public CheckSQLIntentService() {
        super("CheckSQLIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        dbAdapter = new DBAdapter(getApplicationContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        getCheckTT();
        dataBaseOperationInProgress = false;
        checkIntervall = 60000;
        checkUpdates();
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

    private void getCheckTT(){
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
    }


    private void chechTTcadastro() {
        getCheckTT();
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
        getCheckTT();
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

}
