package br.com.anagnostou.publisher.telas;

import android.app.SearchManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

public class RelatorioActivity extends AppCompatActivity {
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    AutoCompleteTextView etPublicador;
    ArrayAdapter aPub, aAno, aMes;
    Spinner spAno, spMes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.relatorio_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbAdapter = new DBAdapter(getApplicationContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        etPublicador = (AutoCompleteTextView) findViewById(R.id.etPublicador);
        aPub = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, dbAdapter.retrieveAllPublicadores());
        etPublicador.setAdapter(aPub);
        spAno = (Spinner) findViewById(R.id.spAno);
        spMes = (Spinner) findViewById(R.id.spMes);
        aAno = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, anoRelatorio());
        spAno.setAdapter(aAno);
        aMes = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mesRelatorio());
        spMes.setAdapter(aMes);

        spAno.setSelection(anoNumero());
        spMes.setSelection(mesNumero() - 1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_atividades, menu);
        /** SEARCH **/
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    public String[] anoRelatorio() {
        String[] ano = getResources().getStringArray(R.array.ano);
        return ano;
    }

    public String[] mesRelatorio() {
        String[] mes = getResources().getStringArray(R.array.mesesNomes);
        return mes;
    }

    public int mesNumero() {
        int mes;
        Calendar now = Calendar.getInstance();
        mes = now.get(Calendar.MONTH) + 1;
        if ((mes - 1) == 0) {
            return 12;
        } else {
            return mes - 1;
        }
    }

    public int anoNumero() {
        int ano,mes;
        Calendar now = Calendar.getInstance();
        ano = now.get(Calendar.YEAR);
        mes = now.get(Calendar.MONTH);
        if ( mes == 0) {
            ano = ano - 1;
        }
        switch (ano) {
            case 2016:
                return 0;
            case 2017:
                return 1;
            case 2018:
                return 2;
        }
        return 0;
    }


}
