package br.com.anagnostou.publisher.telas;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.DatePickerDialog;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.utils.L;

public class AssistenciaActivity extends AppCompatActivity implements View.OnClickListener {
    private DBAdapter dbAdapter;
    private String url;
    private TextView data;
    Spinner spReuniao;
    int mYear, mMonth, mDay;

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
        data = (TextView) findViewById(R.id.dataReuniao);
        data.setOnClickListener(this);
        spReuniao = (Spinner) findViewById(R.id.sp_reuniao);
        ArrayAdapter aReuniao = new ArrayAdapter<>(this, R.layout.spinner_assistencia, getResources().getStringArray(R.array.reuniao));
        spReuniao.setAdapter(aReuniao);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        url = sp.getString("php_assistencia_send", ""); //http://www.anagnostou.com.br/phptut/report_meetings_app.php


        inicializar();
    }

    private void inicializar() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat test = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());

        data.setText(sdf.format(cal.getTime()));

        comparaDataReuniao(data.getText().toString(), spReuniao.getSelectedItem().toString());
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

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.dataReuniao) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int ano, int mes, int dia) {
                            //L.m(dataCurtaLonga(dia + "/" + (mes + 1) + "/" + ano));
                            data.setText(dataCurtaLonga(dia + "/" + (mes + 1) + "/" + ano));
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    }

    public void comparaDataReuniao(String dia, String reuniao) {

        if (dia.contains("quarta")) {

            L.m("Quarta");
        }

    }

}
