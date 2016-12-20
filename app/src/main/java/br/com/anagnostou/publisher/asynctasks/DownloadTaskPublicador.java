package br.com.anagnostou.publisher.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.Utilidades;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by George on 20/12/2016.
 */

public class DownloadTaskPublicador extends AsyncTask<String, Integer, String> {
    private Context context;
    private AlertDialog alert11;
    private String fosPublicador;
    private String spCadastro;
    private MainActivity mainActivity;
    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    public DownloadTaskPublicador(Context context, MainActivity mainActivity,MainActivity.SectionsPagerAdapter mSectionsPagerAdapter) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.mSectionsPagerAdapter = mSectionsPagerAdapter;
        SharedPreferences sp = context.getSharedPreferences("myPreferences", MODE_PRIVATE);
        spCadastro = sp.getString("cadastro", "N/A");
        fosPublicador =  context.getString(R.string.sdcard) + spCadastro;

        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(R.string.aguardes_baixando_publicadores);
        alert11 = builder1.create();
        alert11.show();
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
            output = new FileOutputStream(fosPublicador);
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
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            alert11.dismiss();
        } else {
            alert11.dismiss();
            if (Utilidades.findLocalFiles(spCadastro)) {
                final TaskPublicador taskPublicador = new TaskPublicador(context,mainActivity,mSectionsPagerAdapter);
                taskPublicador.execute(context.getString(R.string.atualizando_publicadores));
            }
        }
    }
}