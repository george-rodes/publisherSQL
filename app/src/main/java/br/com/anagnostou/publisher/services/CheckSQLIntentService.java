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
import br.com.anagnostou.publisher.objetos.Publicador;
import br.com.anagnostou.publisher.objetos.Relatorio;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

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
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        L.m("checkUpdates every ms: " + checkIntervall);
        while (!dataBaseOperationInProgress) {
            try {
                //createNotification();
                //L.m("Thread ID " + Thread.currentThread().getId());
                Thread.sleep(5000);
                if (sp.getString("sourceDataImport", "").contentEquals("SQL")) checkTTrelatorio();
                Thread.sleep(5000);
                if (sp.getString("sourceDataImport", "").contentEquals("SQL")) checkTTcadastro();
                Thread.sleep(checkIntervall);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getCheckTT() {
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


    private void checkTTcadastro() {
        getCheckTT();
        //L.m("checkTTcadastro idCadastro beginning: " + idCadastro);
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

                                    dbAdapter.insertDataPublicador(new Publicador(
                                            jsonObject.getString("nome"), jsonObject.getString("familia"),
                                            jsonObject.getString("grupo"),
                                            Utilidades.trocaFormatoData(jsonObject.getString("databatismo")),
                                            Utilidades.trocaFormatoData(jsonObject.getString("datanascimento")),
                                            jsonObject.getString("fone"), jsonObject.getString("celular"),
                                            jsonObject.getString("rua"), jsonObject.getString("bairro"),
                                            jsonObject.getString("ASP"), jsonObject.getString("PP"),
                                            jsonObject.getString("sexo")));

                                } else if (jsonObject.getString("action").contentEquals("UPDATE")) {
                                    dbAdapter.updateDataPublicador(new Publicador(
                                            jsonObject.getString("nome"), jsonObject.getString("familia"),
                                            jsonObject.getString("grupo"),
                                            Utilidades.trocaFormatoData(jsonObject.getString("databatismo")),
                                            Utilidades.trocaFormatoData(jsonObject.getString("datanascimento")),
                                            jsonObject.getString("fone"), jsonObject.getString("celular"),
                                            jsonObject.getString("rua"), jsonObject.getString("bairro"),
                                            jsonObject.getString("ASP"), jsonObject.getString("PP"),
                                            jsonObject.getString("sexo")));

                                }
                                idRegistroProcessadoCadastro = jsonObject.getInt("id");
                            }
                        }
                        if (idRegistroProcessadoCadastro != null) {
                            idCadastro = idRegistroProcessadoCadastro;
                            spEditor = sp.edit();
                            spEditor.putString("ttcadastro_id", idRegistroProcessadoCadastro.toString());
                            spEditor.commit();
                        }; //else L.m("checkTTcadastro idRegistroProcessado is null ");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dataBaseOperationInProgress = false;
                   // L.m("checkTTcadastro DatabaseOperation Completed! with ID: " + idRegistroProcessadoCadastro);
                }
            }
        }, null);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(srCadastro);


    }

    public void checkTTrelatorio() {
        getCheckTT();
        //L.m("checkTTrelatorio idRelatorio beginning: " + idRelatorio);
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
