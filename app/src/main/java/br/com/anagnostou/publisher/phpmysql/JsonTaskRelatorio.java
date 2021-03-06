package br.com.anagnostou.publisher.phpmysql;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Relatorio;
import br.com.anagnostou.publisher.utils.L;



public class JsonTaskRelatorio extends AsyncTask<JSONArray,Integer,Boolean> {
    private Context context;
    private ProgressDialog progressDialog;
    private DBAdapter dbAdapter;
    private SQLiteDatabase sqLiteDatabase;
    private MainActivity mainActivity;
    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    public JsonTaskRelatorio(Context context, MainActivity mainActivity,MainActivity.SectionsPagerAdapter mSectionsPagerAdapter) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.mSectionsPagerAdapter = mSectionsPagerAdapter;
        dbAdapter = new DBAdapter(context);
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMax(100);
        progressDialog.setMessage("Atualizando Relatórios");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.show();
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
                dbAdapter.insertDataRelatorio(new Relatorio(
                        jsonObject.getInt ("ano"), jsonObject.getInt("mes"),
                        jsonObject.getString("nome"), jsonObject.getString("modalidade"),
                        jsonObject.getInt("videos"), jsonObject.getInt("horas"),
                        jsonObject.getInt("publicacoes"), jsonObject.getInt("revisitas"),
                        jsonObject.getInt("estudos")));
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
            L.t(context, context.getString(R.string.importacao_relatorios_MySQL_concluida));
            //THESE ROUTINES should be in the last AsyncTask
            L.m("mainActivity.mViewPager.setAdapter(mSectionsPagerAdapter)");
            mainActivity.bancoTemDados = true;
            mainActivity.grupoDinamico();
            mainActivity.mViewPager.setAdapter(mSectionsPagerAdapter);

        } else {
            progressDialog.dismiss();
            L.t(context, context.getString(R.string.error));
        }
    }


}
