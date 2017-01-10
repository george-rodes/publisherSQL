package br.com.anagnostou.publisher.asynctasks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.utils.Utilidades;



public class CheckUpdateAvailable extends AsyncTask<String, Integer, String> {
    private Context context;
    private DBAdapter dbAdapter;
    private String spUpdate;
    private String sDataServidor;
    private MainActivity mainActivity;

    public CheckUpdateAvailable(Context context, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
        dbAdapter = new DBAdapter(context);
        //SQLiteDatabase sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        spUpdate = sp.getString("update", "");

    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }
            input = connection.getInputStream();
            output = new FileOutputStream(sUrl[1]);
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException ignored) {
            }
            if (connection != null) connection.disconnect();
        }
        //ler a data do arquivo e comparar
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, spUpdate);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        String str;
        try {
            while ((str = in.readLine()) != null) {
                sDataServidor = str;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null) {

            if (!Utilidades.comparaData(dbAdapter.selectVersao(), sDataServidor).contentEquals("mesma data")) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage(R.string.atualizar_banco_pergunta);
                builder1.setCancelable(true);
                builder1.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        mainActivity.atualizarBancoDeDados();
                    }
                });
                builder1.setNegativeButton("NÂO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }
    }
}