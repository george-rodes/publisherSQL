package br.com.anagnostou.publisher.telas;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.phpmysql.LoginRequest;
import br.com.anagnostou.publisher.phpmysql.SendReportRequest;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

import static br.com.anagnostou.publisher.MainActivity.SP_AUTHENTICATED;
import static br.com.anagnostou.publisher.MainActivity.SP_SPNAME;

public class RelatorioActivity extends AppCompatActivity implements View.OnClickListener {
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    AutoCompleteTextView etPublicador;
    ArrayAdapter aPub, aAno, aMes;
    Spinner spAno, spMes;
    EditText etPublicacoes, etVideos, etHoras, etRevisitas, etEstudos;
    Button clearPublicador, clearPublicacoes, clearVideos, clearHoras, clearRevisitas, clearEstudos, btCancel, btSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.relatorio_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.relatorio);
        dbAdapter = new DBAdapter(getApplicationContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        etPublicador = (AutoCompleteTextView) findViewById(R.id.etPublicador);
        spAno = (Spinner) findViewById(R.id.spAno);
        spMes = (Spinner) findViewById(R.id.spMes);
        etPublicacoes = (EditText) findViewById(R.id.etPublicacoes);
        etVideos = (EditText) findViewById(R.id.etVideos);
        etHoras = (EditText) findViewById(R.id.etHoras);
        etRevisitas = (EditText) findViewById(R.id.etRevisitas);
        etEstudos = (EditText) findViewById(R.id.etEstudos);
        clearPublicador = (Button) findViewById(R.id.clearPublicador);
        clearPublicacoes = (Button) findViewById(R.id.clearPublicacoes);
        clearVideos = (Button) findViewById(R.id.clearVideos);
        clearHoras = (Button) findViewById(R.id.clearHoras);
        clearRevisitas = (Button) findViewById(R.id.clearRevisitas);
        clearEstudos = (Button) findViewById(R.id.clearEstudos);
        btCancel = (Button) findViewById(R.id.btnRelatorioCancel);
        btSend = (Button) findViewById(R.id.btnRelatorioSend);

        clearPublicador.setOnClickListener(this);
        clearPublicacoes.setOnClickListener(this);
        clearVideos.setOnClickListener(this);
        clearHoras.setOnClickListener(this);
        clearRevisitas.setOnClickListener(this);
        clearEstudos.setOnClickListener(this);
        btSend.setOnClickListener(this);
        btCancel.setOnClickListener(this);
        aPub = new ArrayAdapter<>(this, R.layout.item_height, dbAdapter.retrieveAllPublicadores());
        aAno = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.ano));
        aMes = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.mesesNomes));
        etPublicador.setAdapter(aPub);
        spAno.setAdapter(aAno);
        spMes.setAdapter(aMes);
        spAno.setSelection(anoNumero());
        spMes.setSelection(mesNumero() - 1);

        Intent intent = getIntent();
        if (intent.hasExtra("origem") && intent.hasExtra("objetivo")) {
            L.t(this, getIntent().getStringExtra("origem") + " / " + getIntent().getStringExtra("objetivo"));
        }
        //apos enviar e receber a confirmacao do servidor incluir no banco local.
        // nao esperar pelo serviço.
        //after we receive an ok by the server we put it in here
        //PHP check if it exists i not insert if yes update

        editTextListener(etPublicacoes);
        editTextListener(etVideos);
        editTextListener(etHoras);
        editTextListener(etRevisitas);
        editTextListener(etEstudos);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.clearPublicador) {
            etPublicador.setText("");
        } else if (id == R.id.clearPublicacoes) {
            etPublicacoes.setText("");
        } else if (id == R.id.clearVideos) {
            etVideos.setText("");
        } else if (id == R.id.clearHoras) {
            etHoras.setText("");
        } else if (id == R.id.clearRevisitas) {
            etRevisitas.setText("");
        } else if (id == R.id.clearEstudos) {
            etEstudos.setText("");
        } else if (id == R.id.btnRelatorioCancel) {
            dialogoCancelarEnvio();
        } else if (id == R.id.btnRelatorioSend) {
            //vamos verificar e enviar
            checkBeforeSend();
        }
    }

    public void verificaSeExistePublicador() {
        if (!dbAdapter.checkIfPublisherExists(etPublicador.getText().toString())) {
            dialogoPublicadorNaoExiste();
        }
    }

    public void checkBeforeSend() {
        final String nome = etPublicador.getText().toString();
        final String ano = spAno.getSelectedItem().toString();
        final String mes = "" + (Utilidades.getSpinnerIndex(spMes, spMes.getSelectedItem().toString()) + 1);
        final String publicacoes = notEmpty(etPublicacoes.getText().toString());
        final String videos = notEmpty(etVideos.getText().toString());
        final String horas = notEmpty(etHoras.getText().toString());
        final String revisitas = notEmpty(etRevisitas.getText().toString());
        final String estudos = notEmpty(etEstudos.getText().toString());
        //L.m(nome+"/"+ano+"/"+mes+"/"+ publicacoes+"/"+videos+"/"+horas+"/"+revisitas+"/"+estudos);

        if (!nome.isEmpty()) {
            if (dbAdapter.checkIfPublisherExists(etPublicador.getText().toString())) {
                if (!dbAdapter.checkIfReportExists(ano, mes, nome)) {
                    confirmarRelatorio(nome, ano, mes, publicacoes, videos, horas, revisitas, estudos);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
                    builder.setMessage(R.string.dialogo_relatorio_existe);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.SIM, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            confirmarRelatorio(nome, ano, mes, publicacoes, videos, horas, revisitas, estudos);
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton(R.string.NAO, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } else dialogoPublicadorNaoExiste();
        } else dialogoPublicadorNaoInformado();
    }

    private void confirmarRelatorio(final String nome, final String ano, final String mes, final String publicacoes, final String videos, final String horas, final String revisitas, final String estudos) {
        String msg = "Confirmar Envio:" + "\nNome: " + nome + "\nMes: " + ano + "/" + mes
                + "\nPublicações: " + publicacoes + "\nVideos: " + videos
                + "\nHoras: " + horas + "\nRevisitas: " + revisitas + "\nEstudos: " + estudos;
        AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.SIM, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                enviarRelatorio(nome, ano, mes, publicacoes, videos, horas, revisitas, estudos);
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void enviarRelatorio(String nome, String ano, String mes, String publicacoes, String videos, String horas, String revisitas, String estudos) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String url = sp.getString("php_report_send", "");
        SharedPreferences mySp = getSharedPreferences(SP_SPNAME, MODE_PRIVATE);
        String email = mySp.getString("email","");

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                L.m(response);
                try {
                    JSONArray arrayJSON = new JSONArray(response);
                    if (arrayJSON.length() > 0) {

                        JSONObject jsonObject = arrayJSON.getJSONObject(0);
                        if (!jsonObject.getString("result").isEmpty()) {
                            //


                        }
                    } else dialogoServidor();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        RequestQueue queue = Volley.newRequestQueue(RelatorioActivity.this);
        queue.add(new SendReportRequest(email, url, nome, ano, mes, publicacoes, videos, horas, revisitas, estudos, responseListener));

    }

    private void dialogoServidor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
        builder.setMessage(R.string.servidor_nao_respondeu);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String notEmpty(String s) {
        if (s.isEmpty()) return "0";
        else return s;
    }


    //ano, mes, nome


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_atividades, menu);
        /** SEARCH **/
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
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
        int ano, mes;
        Calendar now = Calendar.getInstance();
        ano = now.get(Calendar.YEAR);
        mes = now.get(Calendar.MONTH);
        if (mes == 0) {
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

    private void dialogoCancelarEnvio() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
        builder.setMessage(R.string.dialogo_cancelar_envio);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.SIM, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        builder.setNegativeButton(R.string.NAO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogoPublicadorNaoExiste() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
        builder.setMessage("Publicador não existe!\nEscolhe um nome da lista.");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogoPublicadorNaoInformado() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
        builder.setMessage(R.string.dialogo_escolhe_publicador);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogoRelatorioExiste() {

    }

    public void editTextListener(EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) { //entrando
                    verificaSeExistePublicador();
                }
            }
        });

    }
}
