package br.com.anagnostou.publisher.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Relatorio;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by George on 20/12/2016.
 */

public class TaskRelatorio extends AsyncTask<String, Integer, String> {
    private Context context;
    private final ProgressDialog progressDialog;
    private DBAdapter dbAdapter;
    private SQLiteDatabase sqLiteDatabase;
    private String spRelatorio;
    private String spUpdate;
    private String spHomepage;
    private String spCadastro;
    private MainActivity mainActivity;
    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    public TaskRelatorio(Context context, MainActivity mainActivity,MainActivity.SectionsPagerAdapter mSectionsPagerAdapter) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.mSectionsPagerAdapter = mSectionsPagerAdapter;
        dbAdapter = new DBAdapter(context);
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        SharedPreferences sp = context.getSharedPreferences("myPreferences", MODE_PRIVATE);
        spUpdate = sp.getString("update", "N/A");
        spRelatorio = sp.getString("relatorio", "N/A");
        spHomepage = sp.getString("homepage", "N/A");
        spCadastro = sp.getString("cadastro", "N/A");

        progressDialog = new ProgressDialog(context);
        progressDialog.setMax(100);
        progressDialog.setMessage("Atualizando Registros");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.show();


    }

    @Override
    protected String doInBackground(String... sUrl) {
        if (!sqLiteDatabase.isOpen())
            sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        dbAdapter.mydbHelper.dropTableRelatorio(sqLiteDatabase);
        if (!sqLiteDatabase.isOpen())
            sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        dbAdapter.mydbHelper.dropTableVersao(sqLiteDatabase);
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, spRelatorio);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<String> myStringList = new ArrayList<String>();
        String str;
        try {
            while ((str = in.readLine()) != null) {
                myStringList.add(str);
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
        //2. Creates an Object array of Publicadores
        Relatorio rel[] = new Relatorio[myStringList.size()];
        int counter = 0;
        for (String what : myStringList) {
            rel[counter] = new Relatorio(what);
            counter++;
        }
        //4 . Populate Database
        for (Relatorio r : rel) {
            long i = dbAdapter.insertDataRelatorio(r);
            publishProgress((int) (i * 100 / counter));
        }
        /** inserir a data da ultima atualizacao insertDataVersao(String versao)
         * ler a data do arquivo e comparar
         */
        File fileU = new File(sdcard, spUpdate);
        BufferedReader inU = null;
        try {
            inU = new BufferedReader(new InputStreamReader(new FileInputStream(fileU), "UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String strU;
        String sDataServidor = dbAdapter.selectVersao();
        try {
            while ((strU = inU.readLine()) != null) {
                sDataServidor = strU;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inU != null) {
            try {
                inU.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dbAdapter.insertDataVersao(sDataServidor);
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            progressDialog.dismiss();
        } else {
            progressDialog.dismiss();
            final DownloadTaskPublicador downloadPublicadorTask = new DownloadTaskPublicador(context,mainActivity,mSectionsPagerAdapter);
            downloadPublicadorTask.execute(spHomepage + spCadastro);
        }
    }
}