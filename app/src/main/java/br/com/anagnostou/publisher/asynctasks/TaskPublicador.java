package br.com.anagnostou.publisher.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

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
import br.com.anagnostou.publisher.objetos.Publicador;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by George on 20/12/2016.
 */

public class TaskPublicador extends AsyncTask<String, Integer, String> {
    private Context context;
    private final ProgressDialog progressDialog;
    private DBAdapter dbAdapter;
    private SQLiteDatabase sqLiteDatabase;
    private String spCadastro;
    public MainActivity mainActivity;
    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    public TaskPublicador(Context context, MainActivity mainActivity,MainActivity.SectionsPagerAdapter mSectionsPagerAdapter) {
        this.context = context;
        this.mainActivity = mainActivity;
        this.mSectionsPagerAdapter = mSectionsPagerAdapter;
        dbAdapter = new DBAdapter(context);
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        spCadastro = sp.getString("cadastro", "");
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
        dbAdapter.mydbHelper.dropTablePublicador(sqLiteDatabase);
        //1. Loads file from external storage /sdcard, accessible via explorer
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, spCadastro);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
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
        Publicador pub[] = new Publicador[myStringList.size()];
        int counter = 0;
        for (String what : myStringList) {
            pub[counter] = new Publicador(what);
            counter++;
        }
        //4 . Populate Database
        for (Publicador p : pub) {
            long i = dbAdapter.insertDataPublicador(p);
            publishProgress((int) (i * 100 / counter));
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mainActivity.bBackgroundJobs = false;
        if (result != null) {
            progressDialog.dismiss();
        } else {
            progressDialog.dismiss();
            mainActivity.bancoTemDados = true;
            mainActivity.mViewPager.setAdapter(mSectionsPagerAdapter);
        }
    }
}