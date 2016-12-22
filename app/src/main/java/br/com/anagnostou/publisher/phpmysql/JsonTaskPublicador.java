package br.com.anagnostou.publisher.phpmysql;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.objetos.Publicador;

import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

/**
 * Created by George on 21/12/2016.
 */

public class JsonTaskPublicador extends AsyncTask<JSONArray, Integer, Boolean> {
    private Context context;
    private ProgressDialog progressDialog;
    private DBAdapter dbAdapter;
    private SQLiteDatabase sqLiteDatabase;

    public JsonTaskPublicador(Context context) {
        this.context = context;
        dbAdapter = new DBAdapter(context);
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMax(100);
        progressDialog.setMessage("Atualizando Registros");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.show();
    }


    @Override
    protected Boolean doInBackground(JSONArray... jsonArrays) {
        if (!sqLiteDatabase.isOpen())
            sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        dbAdapter.mydbHelper.dropTablePublicador(sqLiteDatabase);
        int counter = jsonArrays[0].length();

        for (int i = 0; i < jsonArrays[0].length(); i++) {
            JSONObject jsonObject = null;
            try {


                jsonObject = jsonArrays[0].getJSONObject(i);
                dbAdapter.insertDataPublicador(new Publicador(
                        jsonObject.getString("nome"),
                        jsonObject.getString("familia"),
                        jsonObject.getString("grupo"),
                        Utilidades.trocaFormatoData(jsonObject.getString("databatismo")),
                        Utilidades.trocaFormatoData(jsonObject.getString("datanascimento")),
                        jsonObject.getString("fone"),
                        jsonObject.getString("celular"),
                        jsonObject.getString("rua"),
                        jsonObject.getString("bairro"),
                        jsonObject.getString("ASP"),
                        jsonObject.getString("PP"),
                        jsonObject.getString("sexo")));
                publishProgress((int) ((i + 1) * 100 / counter));


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
         progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result != null) {
             progressDialog.dismiss();
        } else {
             progressDialog.dismiss();
            L.m("its done");
        }
    }

}
