package br.com.anagnostou.publisher.telas;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.utils.Utilidades;
import br.com.anagnostou.publisher.objetos.Publicador;

import static br.com.anagnostou.publisher.MainActivity.DEFAULT;
import static br.com.anagnostou.publisher.MainActivity.SP_AUTHENTICATED;
import static br.com.anagnostou.publisher.MainActivity.SP_SPNAME;

public class PioneirosActivity extends AppCompatActivity {
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    TextView tvPub1, tvPub2, tvPub3, tvPub4, tvPub5, tvPub6, tvPub7, tvPub8, tvPub9,
              tvPub13, tvPub14, tvPubTitle, tvPubFone, tvPubDados;
    String nome;
    private static final int LOGIN_INTENT = 275;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pioneiros);

        Toolbar toolbar = (Toolbar) findViewById(R.id.atividades_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbAdapter = new DBAdapter(getApplicationContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        tvPubDados = (TextView) findViewById(R.id.tvPubDados);
        tvPub1 = (TextView) findViewById(R.id.tvPub1);
        tvPub2 = (TextView) findViewById(R.id.tvPub2);
        tvPub3 = (TextView) findViewById(R.id.tvPub3);
        tvPub4 = (TextView) findViewById(R.id.tvPub4);
        tvPub5 = (TextView) findViewById(R.id.tvPub5);
        tvPub6 = (TextView) findViewById(R.id.tvPub6);
        tvPub7 = (TextView) findViewById(R.id.tvPub7);
        tvPub8 = (TextView) findViewById(R.id.tvPub8);
        tvPub9 = (TextView) findViewById(R.id.tvPub9);

        tvPub13 = (TextView) findViewById(R.id.tvPub13);
        tvPub14 = (TextView) findViewById(R.id.tvPub14);
        tvPubTitle = (TextView) findViewById(R.id.tvPubTitle);
        tvPubFone = (TextView) findViewById(R.id.tvPubFone);
        //tvNomePublicador = (TextView) findViewById(R.id.tvNomePublicador);
        nome = getIntent().getExtras().getString("nome", "George");
        getSupportActionBar().setTitle(nome);
        //tvNomePublicador.setText(nome);
        achaPublicador(nome);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            /** Ihave to return where I came From **/
            Intent returnIntent = new Intent();
            //returnIntent.putExtra("busca", nameSearch);
            setResult(RESULT_OK, returnIntent);
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_relatorio) {
            Intent intent = new Intent(this,CartaoActivity.class );
            intent.putExtra("nome", nome);
            startActivity(intent);

            return true;
        } else if (item.getItemId() == R.id.enviarRelatorio) {
            if (areWeAuthenticated()) {
                Intent intent = new Intent(this, RelatorioActivity.class);
                intent.putExtra("origem", "AtividadesActivity");
                intent.putExtra("objetivo", "novo relatorio");
                intent.putExtra("nome",nome);
                startActivity(intent);
            }
        }

        return true;
    }

    private boolean areWeAuthenticated() {
        //user, mail, cleareance, timestamp
        //check if we are on line
        SharedPreferences sp = getSharedPreferences(SP_SPNAME, MODE_PRIVATE);
        //'authenticated ' is what we receive from the server
        if (!sp.getString(SP_AUTHENTICATED, DEFAULT).equals("authenticated")
                && (Utilidades.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)))) {
            // we are not authenticated and we are on line, so lets login
            //call activity for result
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("Origem", "AtividadesActivity");
            startActivityForResult(intent, LOGIN_INTENT);
            return false;
        } else return true;
    }


    public void achaPublicador(String nome) {
        String idade;
        String batismo;
        Double mediahoras;
        Double mediarevisitas;
        Double mediaestudos;
        Double mediasvideos;
        Double mediapublicacoes;


        Publicador p;
        p = dbAdapter.retrievePublisherData(nome);

        if (p != null) {

            if (!p.getNascimento().isEmpty()) {
                idade = Utilidades.calculaTempoAnos(p.getNascimento()) + " Anos";
            } else idade = "Não Informada";

            if (!p.getBatismo().isEmpty()) {
                batismo = Utilidades.calculaTempoBatismo(p.getBatismo());
            } else batismo = "";

            //Verificar se existe data de batismo
            //
            String pio;
            if (p.getSexo().equals("M")) pio = "Pioneiro";
            else pio = "Pioneira";

            String[] resultado = dbAdapter.somaHorasMeses(p.getNome());
            mediahoras = (double) Integer.parseInt(resultado[0]) / Integer.parseInt(resultado[1]);
            mediarevisitas = Double.parseDouble(resultado[2]);
            mediaestudos = Double.parseDouble(resultado[3]);
            mediasvideos = Double.parseDouble(resultado[4]);
            mediapublicacoes = Double.parseDouble(resultado[5]);


            tvPubDados.setText(pio + getString(R.string._regular));

            tvPub1.setText("Familia " + p.getFamilia());
            tvPub2.setText("Grupo " + p.getGrupo());
            tvPub5.setText("Idade: " + idade);
            tvPub6.setText("Tempo de Batismo: " + batismo);
            tvPubTitle.setText(R.string.dados_ano_servico);
            tvPub7.setText("Média de " + String.format("%.1f", mediahoras) + " Horas");
            tvPub8.setText("Média de " + String.format("%.1f", mediarevisitas) + " Revisitas");
            tvPub9.setText("Média de " + String.format("%.1f", mediaestudos) + " Estudos");
            tvPub13.setText("Média de " + String.format("%.1f", mediasvideos) + " Videos");
            tvPub14.setText("Média de " + String.format("%.1f", mediapublicacoes) + " Publicações");
            tvPub3.setText(p.getRua());
            tvPub4.setText("Bairro " + p.getBairro());
            tvPubFone.setText(p.getCelular());

        } else {
            L.m("No data");
        }
    }

    public void ligarFone(View v) {
        dialPhoneNumber(tvPubFone.getText().toString());
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void returnMainActivity(View v) {
        v.setSelected(true);
        // Ihave to return where I came From
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", 7214);
        setResult(RESULT_OK, returnIntent);
        finish();
    }


}