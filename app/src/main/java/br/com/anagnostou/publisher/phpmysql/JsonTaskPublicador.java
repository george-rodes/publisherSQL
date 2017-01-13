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
import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Publicador;

import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

public class JsonTaskPublicador extends AsyncTask<JSONArray, Integer, Boolean> {
    private Context context;
    private ProgressDialog progressDialog;
    private DBAdapter dbAdapter;
    private SQLiteDatabase sqLiteDatabase;
    private MainActivity mainActivity;
    public JsonTaskPublicador(Context context,MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;

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
    protected void onPreExecute() {
        mainActivity.bBackgroundJobs = true;
    }

    @Override
    protected Boolean doInBackground(JSONArray... jsonArrays) {
        if (!sqLiteDatabase.isOpen())
            sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        int counter = jsonArrays[0].length();
        for (int i = 0; i < jsonArrays[0].length(); i++) {
            JSONObject jsonObject;
            try {
               jsonObject = jsonArrays[0].getJSONObject(i);
                dbAdapter.insertDataPublicador(new Publicador(
                        jsonObject.getString("nome"), jsonObject.getString("familia"),
                        jsonObject.getString("grupo"),
                        Utilidades.trocaFormatoData(jsonObject.getString("databatismo")),
                        Utilidades.trocaFormatoData(jsonObject.getString("datanascimento")),
                        jsonObject.getString("fone"), jsonObject.getString("celular"),
                        jsonObject.getString("rua"), jsonObject.getString("bairro"),
                        jsonObject.getString("ASP"), jsonObject.getString("PP"),
                        jsonObject.getString("sexo")));
                publishProgress((int) ((i + 1) * 100 / counter));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            progressDialog.dismiss();
            L.t(context, context.getString(R.string.importacao_publicadores_MySQL_concluida));
            mainActivity.getPHPJsonRelatorioData();
        } else {
            progressDialog.dismiss();
            L.t(context, context.getString(R.string.error));
        }
    }

}
