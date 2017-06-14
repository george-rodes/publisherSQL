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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

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
    TextView tvPub1, tvPub2, tvPub3, tvPub4, tvPub5, tvPub6, tvPub7,tvPub71, tvPub81, tvPub91,
            tvPub131, tvPub141, tvPubTitle, tvPubFone, tvPubDados;
    String nome;
    private static final int LOGIN_INTENT = 275;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pioneiros);

        Toolbar toolbar = (Toolbar) findViewById(R.id.atividades_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setHomeButtonEnabled(true);
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
        tvPub71 = (TextView) findViewById(R.id.tvPub71);
        tvPub81 = (TextView) findViewById(R.id.tvPub81);
        tvPub91 = (TextView) findViewById(R.id.tvPub91);

        tvPub131 = (TextView) findViewById(R.id.tvPub131);
        tvPub141 = (TextView) findViewById(R.id.tvPub141);
        tvPubTitle = (TextView) findViewById(R.id.tvPubTitle);
        tvPubFone = (TextView) findViewById(R.id.tvPubFone);
        //tvNomePublicador = (TextView) findViewById(R.id.tvNomePublicador);
        nome = getIntent().getExtras().getString("nome", "George");
        getSupportActionBar().setTitle(nome);
        //tvNomePublicador.setText(nome);
        achaPublicador(nome);


    }

    public float mediasRequisito(int mes, int soma){
        int mesesrestantes = 12-mes;
        return (float) (840-soma)/mesesrestantes;
    }


    private int anoDeServico() {
        int ano;
        int mes;
        Calendar cal = new GregorianCalendar();
        cal.setTime(Calendar.getInstance().getTime());
        mes = cal.get(Calendar.MONTH) + 1;
        ano = cal.get(Calendar.YEAR);
        //comecar a considerar o novo ano de servico depois do segundo relatorio entre em novembro
        if (mes > 11) return ano + 1;
        else return ano;
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
            if (areWeAuthenticated()) {
                Intent intent = new Intent(this, RelatorioActivity.class);
                intent.putExtra("origem", "AtividadesActivity");
                intent.putExtra("objetivo", "novo relatorio");
                intent.putExtra("nome", nome);
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
        Publicador p;
        p = dbAdapter.retrievePublisherData(nome);

        if (p != null) {
            if (!p.getNascimento().isEmpty()) {
                idade = Utilidades.calculaTempoAnos(p.getNascimento()) + " Anos";
            } else idade = "Não Informada";
            if (!p.getBatismo().isEmpty()) {
                batismo = Utilidades.calculaTempoBatismo(p.getBatismo());
            } else batismo = "";

            String pio;
            if (p.getSexo().equals("M")) pio = "Pioneiro";
            else pio = "Pioneira";

            String str1 = pio + getString(R.string._regular);
            tvPubDados.setText(str1);
            String str2 = getString(R.string.familia_) + p.getFamilia();
            tvPub1.setText(str2);
            String str3 = getString(R.string.grupo_) + p.getGrupo();
            tvPub2.setText(str3);
            String str4 = getString(R.string.idade_) + idade;
            tvPub5.setText(str4);
            String str5 = getString(R.string.batismo_) + batismo;
            tvPub6.setText(str5);
            tvPub3.setText(p.getRua());
            String str6 = getString(R.string.bairro_) + p.getBairro();
            tvPub4.setText(str6);
            tvPubFone.setText(p.getCelular());
            String str61 = getString(R.string.dados_ano_servico);
            tvPubTitle.setText(str61);

            String anoini = "" +(anoDeServico()-1);
            String anofim = "" +anoDeServico();

            /** stat pioneer
             * COUNT(horas) 0
             * SUM(horas)   1
             * AVG(horas)   2
             * AVG(REVISITAS) 3
             * AVG(ESTUDOS) 4
             * AVG(videos)
             * AVG(publicacoes)
             */

            String[] statPion = dbAdapter.mediasPioneiro(p.getNome(),anoini,anofim);
            /** Total de Horas */
            String str7 = "Total de Horas em " + statPion[0] + " meses";
            tvPub7.setText(str7);
            String str71 = statPion[1] ;
            tvPub71.setText(str71);
            /** Media Mensal até o momento  */
            String str131 = String.format(Locale.getDefault(),"%.1f", Double.parseDouble(statPion[2])) ;
            tvPub131.setText(str131);
            /** Média para cumprir o requisito Mensal */
            String str141 = String.format(Locale.getDefault(), "%.1f",
                    mediasRequisito(Integer.parseInt(dbAdapter.mediasPioneiro(p.getNome(),anoini,anofim)[0]) ,
                            Integer.parseInt(dbAdapter.mediasPioneiro(p.getNome(),anoini,anofim)[1])));
            tvPub141.setText(str141);
            tvPub81.setText(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(statPion[3])));
            tvPub91.setText(String.format(Locale.getDefault(), "%.1f", Double.parseDouble(statPion[4])));
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
