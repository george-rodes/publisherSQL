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
import br.com.anagnostou.publisher.objetos.Relatorio;

/**
 * Created by George on 13/01/2017.
 */

public class JsonTaskAssistencia extends AsyncTask<JSONArray, Integer, Boolean> {
    //no Dialog please
    private Context context;
    private DBAdapter dbAdapter;
    private SQLiteDatabase sqLiteDatabase;

    public JsonTaskAssistencia(Context context) {
        this.context = context;
        dbAdapter = new DBAdapter(context);
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
                dbAdapter.insertDataAssistencia(
                        jsonObject.getInt("_id"),
                        jsonObject.getString("data"),
                        jsonObject.getString("reuniao"),
                        jsonObject.getInt("presentes"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
