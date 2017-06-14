package br.com.anagnostou.publisher.telas;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.MainActivity;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.utils.Utilidades;
import br.com.anagnostou.publisher.objetos.Publicador;

import static br.com.anagnostou.publisher.MainActivity.DEFAULT;
import static br.com.anagnostou.publisher.MainActivity.SP_AUTHENTICATED;
import static br.com.anagnostou.publisher.MainActivity.SP_SPNAME;

public class AtividadesActivity extends AppCompatActivity {
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    TextView tvPub1, tvPub2, tvPub3, tvPub4, tvPub5, tvPub6, tvPub7, tvPub8, tvPub9,
            tvPub10, tvPub11, tvPub12, tvPub13, tvPub14, tvPubTitle, tvPubFone, tvNomePublicador;
    String nome;
    private static final int LOGIN_INTENT = 275;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atividades);

        Toolbar toolbar = (Toolbar) findViewById(R.id.atividades_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbAdapter = new DBAdapter(getApplicationContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();

        tvPub1 = (TextView) findViewById(R.id.tvPub1);
        tvPub2 = (TextView) findViewById(R.id.tvPub2);
        tvPub3 = (TextView) findViewById(R.id.tvPub3);
        tvPub4 = (TextView) findViewById(R.id.tvPub4);
        tvPub5 = (TextView) findViewById(R.id.tvPub5);
        tvPub6 = (TextView) findViewById(R.id.tvPub6);
        tvPub7 = (TextView) findViewById(R.id.tvPub7);
        tvPub8 = (TextView) findViewById(R.id.tvPub8);
        tvPub9 = (TextView) findViewById(R.id.tvPub9);
        tvPub10 = (TextView) findViewById(R.id.tvPub10);
        tvPub11 = (TextView) findViewById(R.id.tvPub11);
        tvPub12 = (TextView) findViewById(R.id.tvPub12);
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
            /*** Ihave to return where I came From **/
            Intent returnIntent = new Intent();
            //returnIntent.putExtra("busca", nameSearch);
            setResult(RESULT_OK, returnIntent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_relatorio) {
            Intent intent = new Intent(this, CartaoActivity.class);
            intent.putExtra("nome", nome);
            intent.putExtra("periodo", "dozemeses");
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_anodeservico) {
            Intent intent = new Intent(this, CartaoActivity.class);
            intent.putExtra("nome", nome);
            intent.putExtra("periodo", "anodeservico");
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.enviarRelatorio) {
            //Modo OFFLINE adicionado
            if (areWeAuthenticated()) {
                Intent intent = new Intent(this, RelatorioActivity.class);
                intent.putExtra("origem", "AtividadesActivity");
                intent.putExtra("objetivo", "novo relatorio");
                intent.putExtra("nome", nome);
                startActivity(intent);
            }

        } else if (item.getItemId() == R.id.casa) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        return true;
    }

    private void dialogoNoInternet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AtividadesActivity.this);
        builder.setMessage(R.string.sem_conexao_internet);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
        String pioneiroauxiliar;
        String irregular;

        Publicador p;
        p = dbAdapter.retrievePublisherData(nome);

        if (p != null) {

            if (!p.getNascimento().isEmpty()) {
                idade = Utilidades.calculaTempoAnos(p.getNascimento()) + " Anos";
            } else idade = "Não Informada";

            if (!p.getBatismo().isEmpty()) {
                batismo = Utilidades.calculaTempoBatismo(p.getBatismo());
            } else {

                if (p.getSexo().equals("M")) batismo = "Não Batizado";
                else batismo = "Não Batizada";

            }

            String[] resultado = dbAdapter.somaHorasMeses(p.getNome());
            mediahoras = (double) Integer.parseInt(resultado[0]) / Integer.parseInt(resultado[1]);
            mediarevisitas = Double.parseDouble(resultado[2]);
            mediaestudos = Double.parseDouble(resultado[3]);
            mediasvideos = Double.parseDouble(resultado[4]);
            mediapublicacoes = Double.parseDouble(resultado[5]);
            pioneiroauxiliar = dbAdapter.contaPioneiroAuxiliar(p.getNome());
            irregular = dbAdapter.deixouDeRelatar(p.getNome());

            String s1 = getString(R.string.familia_) + p.getFamilia();
            tvPub1.setText(s1);
            String s2 = getString(R.string.grupo_) + p.getGrupo();
            tvPub2.setText(s2);
            String s3 = getString(R.string.idade_) + idade;
            tvPub5.setText(s3);
            String s4 = getString(R.string.batismo_) + batismo;
            tvPub6.setText(s4);
            String s5 = "Médias dos últimos " + resultado[1] + " meses ";
            tvPubTitle.setText(s5);
            String s6 = "Média de " + String.format(Locale.getDefault(), "%.1f", mediahoras) + " Horas";
            tvPub7.setText(s6);
            String s7 = "Média de " + String.format(Locale.getDefault(), "%.1f", mediarevisitas) + " Revisitas";
            tvPub8.setText(s7);
            String s8 = "Média de " + String.format(Locale.getDefault(), "%.1f", mediaestudos) + " Estudos";
            tvPub9.setText(s8);
            String s9 = "Média de " + String.format(Locale.getDefault(), "%.1f", mediasvideos) + " Videos";
            tvPub13.setText(s9);
            String s10 = "Média de " + String.format(Locale.getDefault(), "%.1f", mediapublicacoes) + " Publicações";
            tvPub14.setText(s10);

            //Verificar se existe data de batismo
            //
            String pio;
            if (p.getSexo().equals("M")) pio = "Pioneiro";
            else pio = "Pioneira";

            String s11;
            if (Integer.parseInt(pioneiroauxiliar) > 1) {
                s11 = "Saiu de " + pio + " Auxiliar " + pioneiroauxiliar + " vezes";
            } else if (Integer.parseInt(pioneiroauxiliar) == 0) {
                s11 = "Não saiu de " + pio + " Auxiliar";
            } else s11 = "Saiu de " + pio + " Auxiliar " + pioneiroauxiliar + " vez";

            tvPub10.setText(s11);


            if (p.getBatismo().isEmpty()) tvPub10.setText("");

            if (p.getPipu().equals("Pioneiro")) {
                String s = pio + getString(R.string._regular);
                tvPub10.setText(s);
            }

            String s12;
            if (Integer.parseInt(irregular) > 1) {
                s12 = "Deixou de relatar " + irregular + " meses";
            } else if (Integer.parseInt(irregular) == 0) {
                s12 = getString(R.string.relatou);
            } else s12 =  getString(R.string.nao_relatou) + irregular + " mês";
            tvPub11.setText(s12);

            if (mediahoras.isNaN()) {
                if (p.getSexo().equals("M")) tvPub11.setText(getString(R.string.inativo));
                else tvPub11.setText(getString(R.string.inativa));
            }

            tvPub3.setText(p.getRua());

            String s13 = getString(R.string.bairro_) + p.getBairro();
            tvPub4.setText(s13);
            tvPub12.setText(p.getFone());
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
