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
import br.com.anagnostou.publisher.objetos.Relatorio;
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
    ArrayAdapter aPub, aAno, aMes, aModalidade;
    Spinner spAno, spMes, spModalidade;
    EditText etPublicacoes, etVideos, etHoras, etRevisitas, etEstudos;
    Button clearPublicador, clearPublicacoes, clearVideos, clearHoras, clearRevisitas, clearEstudos, btCancel, btSend;
    SharedPreferences sp, mySp;
    String url, email;


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
        spModalidade = (Spinner) findViewById(R.id.spModalidade);
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
        aAno = new ArrayAdapter<>(this, R.layout.spinner, getResources().getStringArray(R.array.ano));
        aMes = new ArrayAdapter<>(this, R.layout.spinner, getResources().getStringArray(R.array.mesesNomes));
        //aModalidade = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.modalidade));
        aModalidade = new ArrayAdapter<>(this, R.layout.spinner, getResources().getStringArray(R.array.modalidade));

        etPublicador.setAdapter(aPub);
        spAno.setAdapter(aAno);
        spMes.setAdapter(aMes);
        spModalidade.setAdapter(aModalidade);
        spAno.setSelection(anoNumero());
        spMes.setSelection(mesNumero() - 1);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        url = sp.getString("php_report_send", "");
        mySp = getSharedPreferences(SP_SPNAME, MODE_PRIVATE);
        email = mySp.getString("email", "");

        Intent intent = getIntent();
        if (intent.hasExtra("nome")) {
            String nome = intent.getStringExtra("nome");
            etPublicador.setText(nome);
            if (dbAdapter.retrieveModalidade(nome).contentEquals("Pioneiro")) {
                spModalidade.setSelection(Utilidades.getSpinnerIndex(spModalidade, "Pioneiro Regular"));
            }
        }
        if (intent.hasExtra("Origem") && intent.getStringExtra("Origem").contentEquals("CartaoAdapter")) {
            String nome = intent.getStringExtra("nome");
            int ano = intent.getIntExtra("ano", 0);
            int mes = intent.getIntExtra("mes", 0);
            etPublicador.setText(nome);
            if (dbAdapter.retrieveModalidade(nome).contentEquals("Pioneiro")) {
                spModalidade.setSelection(Utilidades.getSpinnerIndex(spModalidade, "Pioneiro Regular"));
            }
            spAno.setSelection(Utilidades.getSpinnerIndex(spAno, "" + ano));
            spMes.setSelection(mes - 1);
            buscaRelatorio(nome, ano, mes);

        }
        editTextListener(etPublicacoes);
        editTextListener(etVideos);
        editTextListener(etHoras);
        editTextListener(etRevisitas);
        editTextListener(etEstudos);
    }

    private void buscaRelatorio(String nome, int ano, int mes) {
        Relatorio r = dbAdapter.findRelatorio(nome, "" + ano, "" + mes);
        if (r != null) {
            String pub = ""+r.getPublicacoes();
            String vid = ""+r.getVideos();
            String hor = ""+r.getHoras();
            String rev = ""+r.getRevisitas();
            String est = ""+r.getEstudos();
            etPublicacoes.setText(pub);
            etVideos.setText(vid);
            etHoras.setText(hor);
            etRevisitas.setText(rev);
            etEstudos.setText(est);
        }
    }

    public void impedirPublicadorPassarPorPioneiro() {
        String nome = etPublicador.getText().toString();
        String mod = spModalidade.getSelectedItem().toString();
        if (dbAdapter.retrieveModalidade(nome).contentEquals("Publicador") && mod.contentEquals("Pioneiro Regular")) {
            //set the spinner to publicador
            spModalidade.setSelection(Utilidades.getSpinnerIndex(spModalidade, "Publicador"));
        }
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
            impedirPublicadorPassarPorPioneiro();
            checkBeforeSend();
        }
    }

    public void verificaSeExistePublicador() {
        if (!dbAdapter.checkIfPublisherExists(etPublicador.getText().toString())) {
            dialogoPublicadorNaoExiste();
        } else {
            if (dbAdapter.retrieveModalidade(etPublicador.getText().toString()).contentEquals("Pioneiro")) {
                //set the spinner to pioneiro
                spModalidade.setSelection(Utilidades.getSpinnerIndex(spModalidade, "Pioneiro Regular"));
            }
            impedirPublicadorPassarPorPioneiro();
        }
    }

    public void checkBeforeSend() {
        final String nome = etPublicador.getText().toString();
        final String ano = spAno.getSelectedItem().toString();
        final String mes = "" + (Utilidades.getSpinnerIndex(spMes, spMes.getSelectedItem().toString()) + 1);
        final String modalidade = spModalidade.getSelectedItem().toString();

        final String publicacoes = notEmpty(etPublicacoes.getText().toString());
        final String videos = notEmpty(etVideos.getText().toString());
        final String horas = notEmpty(etHoras.getText().toString());
        final String revisitas = notEmpty(etRevisitas.getText().toString());
        final String estudos = notEmpty(etEstudos.getText().toString());


        if (!nome.isEmpty()) {
            if (dbAdapter.checkIfPublisherExists(etPublicador.getText().toString())) {
                if (!dbAdapter.checkIfReportExists(ano, mes, nome)) {
                    confirmarRelatorio(nome, ano, mes, modalidade, publicacoes, videos, horas, revisitas, estudos);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
                    builder.setMessage(R.string.dialogo_relatorio_existe);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.SIM, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            confirmarRelatorio(nome, ano, mes, modalidade, publicacoes, videos, horas, revisitas, estudos);
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

    private void confirmarRelatorio(final String nome, final String ano, final String mes, final String modalidade,
                                    final String publicacoes, final String videos, final String horas, final String revisitas, final String estudos) {
        String msg = "Confirmar Envio:" + "\nNome: " + nome + "\nMes: " + mes + "/" + ano
                + "\nModalidade: " + modalidade + "\nPublicações: " + publicacoes + "\nVideos: " + videos
                + "\nHoras: " + horas + "\nRevisitas: " + revisitas + "\nEstudos: " + estudos;
        AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.SIM, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                enviarRelatorio(nome, ano, mes, modalidade, publicacoes, videos, horas, revisitas, estudos);
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void enviarRelatorio(String nome, String ano, String mes, String modalidade, String publicacoes, String videos, String horas, String revisitas, String estudos) {

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                L.m(response);
                try {
                    JSONArray arrayJSON = new JSONArray(response);
                    if (arrayJSON.length() > 0) {
                        JSONObject jsonObject = arrayJSON.getJSONObject(0);
                        if (!jsonObject.getString("result").isEmpty()) {
                            if (jsonObject.getString("result").contentEquals("SUCCESS")) {
                                if (jsonObject.getString("action").contentEquals("INSERT")) {
                                    dialogoServidorSucessoInsert();
                                } else {
                                    dialogoServidorSucessoUpdate();
                                }
                            } else dialogoServidorFailure();
                        }
                    } else dialogoServidor();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        RequestQueue queue = Volley.newRequestQueue(RelatorioActivity.this);
        queue.add(new SendReportRequest(email, url, nome, ano, mes, modalidade, publicacoes, videos, horas, revisitas, estudos, responseListener));
    }

    private void dialogoServidorSucessoUpdate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
        builder.setMessage(R.string.dialogo_servidor_corrigido);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogoServidorSucessoInsert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
        builder.setMessage(R.string.dialogo_servidor_gravado);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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

    private void dialogoServidorFailure() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RelatorioActivity.this);
        builder.setMessage(R.string.problema_servidor_verificar);
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
        builder.setMessage(R.string.dialogo_publicador_nao_existe);
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
