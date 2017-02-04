package br.com.anagnostou.publisher.telas;

import android.content.Context;
import android.content.DialogInterface;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.DatePickerDialog;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Assistencia;
import br.com.anagnostou.publisher.phpmysql.SendMeetingRequest;
import br.com.anagnostou.publisher.phpmysql.SendReportRequest;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

public class AssistenciaActivity extends AppCompatActivity implements View.OnClickListener {
    private DBAdapter dbAdapter;
    private String url;
    private TextView dataReuniao;
    private EditText etPresentes;
    private Spinner spReuniao;
    private String[] durante_a_semana;
    private String[] fim_de_semana;
    private boolean execaoReuniao;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistencia);
        Toolbar toolbar = (Toolbar) findViewById(R.id.relatorio_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.enviar_assistencia);

        dbAdapter = new DBAdapter(getApplicationContext());
        dataReuniao = (TextView) findViewById(R.id.dataReuniao);
        etPresentes = (EditText) findViewById(R.id.etPresentes);
        dataReuniao.setOnClickListener(this);
        spReuniao = (Spinner) findViewById(R.id.sp_reuniao);
        ArrayAdapter aReuniao = new ArrayAdapter<>(this, R.layout.spinner_assistencia, getResources().getStringArray(R.array.reuniao));
        spReuniao.setAdapter(aReuniao);
        Button clearPresentes = (Button) findViewById(R.id.clearPresentes);
        Button btnAssistenciaCancel = (Button) findViewById(R.id.btnAssistenciaCancel);
        Button btnAssistenciaSend = (Button) findViewById(R.id.btnAssistenciaSend);
        clearPresentes.setOnClickListener(this);
        btnAssistenciaCancel.setOnClickListener(this);
        btnAssistenciaSend.setOnClickListener(this);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        url = sp.getString("assistsend", "");
        url = "http://www.anagnostou.com.br/phptut/report_meetings_app.php";
        L.m("url:" + url);
        durante_a_semana = getResources().getStringArray(R.array.durante_a_semana);
        fim_de_semana = getResources().getStringArray(R.array.fim_de_semana);
        buscaDataHoje();
        execaoReuniao = false;
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.dataReuniao) {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int ano, int mes, int dia) {
                            dataReuniao.setText(dataCurtaLonga(dia + "/" + (mes + 1) + "/" + ano));
                            ajustaSpinner();
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        } else if (view.getId() == R.id.btnAssistenciaSend) {
            //1
            if (!comparaDataReuniao() && !execaoReuniao) {
                confirmaDiaDaReunião(spReuniao.getSelectedItem().toString());
            } else verificaAssistencia();
        } else if (view.getId() == R.id.btnAssistenciaCancel) {
            dialogoCancelarEnvio();
        } else if (view.getId() == R.id.clearPresentes) {

            etPresentes.setText("");
        }
    }

    //2
    private void confirmaDiaDaReunião(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
        builder.setMessage("É '" + str + "' mesmo?");
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.SIM, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                execaoReuniao = true;
                verificaAssistencia();
            }
        });
        builder.setNegativeButton(R.string.NAO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                execaoReuniao = false;
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //3
    private void verificaAssistencia() {
        int i = Integer.parseInt(notEmpty(etPresentes.getText().toString()));
        int m = dbAdapter.mediaReunioes();
        if ((i > 30) && (i < 200)) {
            if ((i < m * 0.7) || (i > m * 1.3)) {
                confirmaAbaixoDaMedia(i, m);
            } else confirmarEnvio();
        } else dialogoPresentesNaoInformado();
    }

    //4
    private void confirmaAbaixoDaMedia(int i, int m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
        builder.setMessage(String.format(getResources().getString(R.string.confirma_baixomedia), "" + i, "" + m));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.SIM, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                confirmarEnvio();
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

    //5
    private void confirmarEnvio() {
        final String data = dataLongaPadrao(dataReuniao.getText().toString());
        final String reuniao = getMeeting();
        final String presentes = etPresentes.getText().toString();
        execaoReuniao = false;
        String msg = "Confirmar envio:" +
                "\nData: " + data +
                "\nReuniao: " + spReuniao.getSelectedItem() +
                "\nPresentes: " + presentes;

        AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.SIM, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (Utilidades.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                    //modo online
                    enviarAssistenciaOnLine(data, reuniao, presentes);
                } else {
                    //modo off line
                    enviarAssistenciaOffline(data, reuniao, presentes);
                }
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

    private void enviarAssistenciaOffline(String data, String reuniao, String presentes) {

        if (dbAdapter.insertTTAssistencia(data,reuniao,presentes) > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
            builder.setMessage(R.string.offline_aguardando_online);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else L.t(this, getString(R.string.houston));


    }

    private void enviarAssistenciaOnLine(String data, String reuniao, String presentes) {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //L.m(response);
                try {
                    JSONArray arrayJSON = new JSONArray(response);
                    L.m("SendMeetingRequest: " + response);
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


        RequestQueue queue = Volley.newRequestQueue(AssistenciaActivity.this);
        SendMeetingRequest sendMeetingRequest = new SendMeetingRequest(url, data,reuniao,presentes,responseListener);
        sendMeetingRequest.setShouldCache(false);
        queue.add(sendMeetingRequest);
    }


    private void dialogoCancelarEnvio() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
        builder.setMessage(R.string.dialogo_cancelar_envio_assistencia);
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

    private void dialogoPresentesNaoInformado() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
        builder.setMessage(R.string.fora_da_faixa);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public String dataCurtaLonga(String data) {
        //data = "dd/MM/yyyy" -> "EEEE, dd/MM/yyyy"
        SimpleDateFormat sdfCurto = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfLongo = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        try {
            Date dataCurta = sdfCurto.parse(data);
            cal.setTime(dataCurta);
            return sdfLongo.format(cal.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "00/00/0000";
    }

    public String dataLongaPadrao(String data) {
        //"yyyy-MM-dd"
        SimpleDateFormat sdfPadrao = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfLongo = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        try {
            Date dataLonga = sdfLongo.parse(data);
            cal.setTime(dataLonga);
            return sdfPadrao.format(cal.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "0000-00-00";
    }

    public boolean comparaDataReuniao() {
        String dia = dataReuniao.getText().toString();
        String reuniao = spReuniao.getSelectedItem().toString();
        return (reuniao.contains("fim") && eFimDeSemana(dia)) || (reuniao.contains("durante") && !eFimDeSemana(dia));
    }

    public boolean eFimDeSemana(String dia) {
        for (String s : fim_de_semana) {
            if (dia.contains(s.substring(0, 3))) {
                return true;
            }
        }
        return false;
    }

    public void buscaDataHoje() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        String hoje = sdf.format(cal.getTime());
        dataReuniao.setText(hoje);
        if (eFimDeSemana(hoje)) {
            spReuniao.setSelection(1); //7,8
        } else {
            spReuniao.setSelection(0); //9-13
        }
    }

    public String getMeeting() {
        if (spReuniao.getSelectedItemId() == 0) {
            return "midweek";
        } else {
            return "weekend";
        }
    }

    public void ajustaSpinner() {
        if (eFimDeSemana(dataReuniao.getText().toString())) {
            spReuniao.setSelection(1); //7,8
        } else {
            spReuniao.setSelection(0); //9-13
        }
    }

    private String notEmpty(String s) {
        if (s.isEmpty()) return "0";
        else return s;
    }

    private void dialogoServidorSucessoUpdate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(AssistenciaActivity.this);
        builder.setMessage(R.string.problema_servidor_verificar);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
