package br.com.anagnostou.publisher.phpmysql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.utils.L;

/**
 * Created by George on 09/04/2017.
 */

public class JsonTaskGrupos  extends AsyncTask<JSONArray, Integer, Boolean> {
    private Context context;
    private DBAdapter dbAdapter;
    private SQLiteDatabase sqLiteDatabase;
    private MainActivity mainActivity;

    public JsonTaskGrupos(Context context,MainActivity mainActivity) {
        dbAdapter = new DBAdapter(context);
        this.context = context;
        this.mainActivity = mainActivity;
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
    }

    @Override
    protected Boolean doInBackground(JSONArray... jsonArrays) {
        if (!sqLiteDatabase.isOpen())
            sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        for (int i = 0; i < jsonArrays[0].length(); i++) {
            JSONObject jsonObject;
            try {
                jsonObject = jsonArrays[0].getJSONObject(i);

                dbAdapter.insertDataGrupos(
                        jsonObject.getString("grupo"),
                        jsonObject.getInt("numero"),
                        jsonObject.getString("dirigente"),
                        jsonObject.getString("auxiliar"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
     //   mainActivity.bBackgroundJobs = false; //acabou, sempre a última task

    }

}

