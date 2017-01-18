package br.com.anagnostou.publisher.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
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
import br.com.anagnostou.publisher.phpmysql.AssistenciaRequest;
import br.com.anagnostou.publisher.phpmysql.LoginRequest;
import br.com.anagnostou.publisher.phpmysql.SendReportRequest;
import br.com.anagnostou.publisher.telas.LoginActivity;
import br.com.anagnostou.publisher.telas.RelatorioActivity;
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
    private String urlAssistencia;

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
        //L.m("checkUpdates every ms: " + checkIntervall);
        while (!dataBaseOperationInProgress) {
            try {
                //createNotification();
                //L.m("Thread ID " + Thread.currentThread().getId());
                Thread.sleep(3000);
                getAssistencia();
                if (sp.getString("sourceDataImport", "").contentEquals("SQL")) checkTTrelatorio();
                Thread.sleep(3000);
                if (sp.getString("sourceDataImport", "").contentEquals("SQL")) checkTTcadastro();
                Thread.sleep(2158);
                if (sp.getString("sourceDataImport", "").contentEquals("SQL"))
                    checkLocalRelatorio();
                Thread.sleep(checkIntervall);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getMaxIdAssistencia() {
        if (!sqLiteDatabase.isOpen())
            sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        return "" + dbAdapter.getMaxIdAssistencia();

    }

    private void getAssistencia() {
        if (!sqLiteDatabase.isOpen())
            sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.length() > 4) {
                    JSONArray jsonArray;
                    try {
                        jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject;
                            jsonObject = jsonArray.getJSONObject(i);
                            dbAdapter.insertDataAssistencia(
                                    jsonObject.getInt("_id"),
                                    jsonObject.getString("data"),
                                    jsonObject.getString("reuniao"),
                                    jsonObject.getInt("presentes"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(new AssistenciaRequest(urlAssistencia, getMaxIdAssistencia(), responseListener));

    }

    private void getCheckTT() {
        checkTTrelatorioUrl = sp.getString("php_ttrelatorio", "");
        checkTTcadastroUrl = sp.getString("php_ttcadastro", "");
        urlAssistencia = sp.getString("php_assistencia","");

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

    private void checkLocalRelatorio() {
        Cursor c = dbAdapter.retrieveTTRelatorio();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex("_id"));
                String email = c.getString(c.getColumnIndex("email"));
                String nome = c.getString(c.getColumnIndex("nome"));
                String ano = c.getString(c.getColumnIndex("ano"));
                String mes = (c.getString(c.getColumnIndex("mes")));
                String modalidade = c.getString(c.getColumnIndex("modalidade"));
                String publicacoes = c.getString(c.getColumnIndex("publicacoes"));
                String videos = c.getString(c.getColumnIndex("videos"));
                String horas = c.getString(c.getColumnIndex("horas"));
                String revisitas = c.getString(c.getColumnIndex("revisitas"));
                String estudos = c.getString(c.getColumnIndex("estudos"));
                String entregue = c.getString(c.getColumnIndex("entregue"));


                /*L.m("" + c.getInt(c.getColumnIndex("_id")));
                L.m(c.getString(c.getColumnIndex("email")));
                L.m(c.getString(c.getColumnIndex("nome")));
                L.m(c.getString(c.getColumnIndex("ano")));
                L.m(c.getString(c.getColumnIndex("mes")));
                L.m(c.getString(c.getColumnIndex("modalidade")));
                L.m(c.getString(c.getColumnIndex("publicacoes")));
                L.m(c.getString(c.getColumnIndex("videos")));
                L.m(c.getString(c.getColumnIndex("horas")));
                L.m(c.getString(c.getColumnIndex("revisitas")));
                L.m(c.getString(c.getColumnIndex("estudos")));
                L.m(c.getString(c.getColumnIndex("entregue")));*/

                if (Utilidades.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                    //L.m("We are online, sending...");
                    enviarRelatorio(id, email, nome, ano, mes, modalidade, publicacoes, videos, horas, revisitas, estudos, entregue);
                } //else L.m("We are offline ");

            }
        }
    }


    public void enviarRelatorio(final int id, String email, String nome, String ano, String mes, String modalidade, String publicacoes,
                                String videos, String horas, String revisitas, String estudos, String entregue) {
        String url = sp.getString("php_report_send", "");
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //L.m(response);
                try {
                    JSONArray arrayJSON = new JSONArray(response);
                    if (arrayJSON.length() > 0) {
                        JSONObject jsonObject = arrayJSON.getJSONObject(0);
                        if (!jsonObject.getString("result").isEmpty()) {
                            if (jsonObject.getString("result").contentEquals("SUCCESS")) {
                                dbAdapter.deleteTTRelatorio("" + id);
                                // L.m("deleted: " + dbAdapter.deleteTTRelatorio(""+id));
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        RequestQueue queue = Volley.newRequestQueue(CheckSQLIntentService.this);
        queue.add(new SendReportRequest(email, url, nome, ano, mes, modalidade, publicacoes, videos, horas, revisitas, estudos, entregue, responseListener));
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
                        }//else L.m("checkTTcadastro idRegistroProcessado is null ");
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
                        } //else L.m("checkTTrelatorio idRegistroProcessado is null ");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dataBaseOperationInProgress = false;
                    // L.m("checkTTrelatorio DatabaseOperation Completed! with ID: " + idRegistroProcessadoRelatorio);
                }
            }
        }, null);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(srRelatorio);
    }


}
