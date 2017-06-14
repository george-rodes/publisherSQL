package br.com.anagnostou.publisher.telas;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import br.com.anagnostou.publisher.adapters.CartaoAdapter;
import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Relatorio;

public class CartaoActivity extends AppCompatActivity {
    private DBAdapter dbAdapter;
    private String nome;
    private String periodo;
    private ArrayList<Relatorio> relatorios;
    private TextView mesesTotal, publicacoesTotal, videosTotal, horasTotal, revisitasTotal, estudosTotal;
    private TextView mesesMedia, publicacoesMedia, videosMedia, horasMedia, revisitasMedia, estudosMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartao);

        mesesTotal = (TextView) findViewById(R.id.mesesTotal);
        publicacoesTotal = (TextView) findViewById(R.id.publicacoesTotal);
        videosTotal = (TextView) findViewById(R.id.videosTotal);
        horasTotal = (TextView) findViewById(R.id.horasTotal);
        revisitasTotal = (TextView) findViewById(R.id.revisitasTotal);
        estudosTotal = (TextView) findViewById(R.id.estudosTotal);
        mesesMedia = (TextView) findViewById(R.id.mesesMedia);
        publicacoesMedia = (TextView) findViewById(R.id.publicacoesMedia);
        videosMedia = (TextView) findViewById(R.id.videosMedia);
        horasMedia = (TextView) findViewById(R.id.horasMedia);
        revisitasMedia = (TextView) findViewById(R.id.revisitasMedia);
        estudosMedia = (TextView) findViewById(R.id.estudosMedia);

        Toolbar toolbar = (Toolbar) findViewById(R.id.cartao_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** RV START **/
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        relatorios = new ArrayList<>();
        /** RV END **/

        dbAdapter = new DBAdapter(getApplicationContext());

        nome = getIntent().getExtras().getString("nome", "George");
        periodo = getIntent().getExtras().getString("periodo", null);
        //L.m(periodo);
        getSupportActionBar().setTitle(nome);

        if (periodo.equals("anodeservico")) {
            carregarCartaoAnoDeServico(nome);
        } else {
            carregarCartao(nome);
        }

        RecyclerView.Adapter adapter = new CartaoAdapter(relatorios, this);
        rv.setAdapter(adapter);
    }

    public void carregarCartao(String nome) {
        Cursor c = dbAdapter.retrieveRelatorios(nome);
        //relatorios.clear();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                relatorios.add(new Relatorio(c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getInt(5), c.getInt(6), c.getInt(7), c.getInt(8), c.getInt(9)));
            }
        }

        String[] totais = dbAdapter.retrieveTotais(nome);
        mesesTotal.setText(totais[0]);
        publicacoesTotal.setText(totais[1]);
        videosTotal.setText(totais[2]);
        horasTotal.setText(totais[3]);
        revisitasTotal.setText(totais[4]);
        estudosTotal.setText(totais[5]);
    }

    public void carregarCartaoAnoDeServico(String nome) {
        int meses = 0;
        Cursor c = dbAdapter.retrieveRelatoriosAnoDeServico(nome);
        //relatorios.clear();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                relatorios.add(new Relatorio(c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getInt(5), c.getInt(6), c.getInt(7), c.getInt(8), c.getInt(9)));
                meses++;
            }
        }
        //L.m(""+meses);
        String[] totais = dbAdapter.retrieveTotaisAnoDeServico(nome);
        mesesTotal.setText(totais[0]);
        publicacoesTotal.setText(totais[1]);
        videosTotal.setText(totais[2]);
        horasTotal.setText(totais[3]);
        revisitasTotal.setText(totais[4]);
        estudosTotal.setText(totais[5]);

        mesesMedia.setText(R.string.medias);
        publicacoesMedia.setText(totais[6]);
        videosMedia.setText(totais[7]);
        horasMedia.setText(totais[8]);
        revisitasMedia.setText(totais[9]);
        estudosMedia.setText(totais[10]);
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cartao, menu);
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

        if (item.getItemId() == R.id.enviarCartao) {
            Intent intent, chooser;
            String cabecalho = "Registros de: " + nome;
            StringBuilder sb = new StringBuilder();

            sb.append("Seguem os Relatorios de Campo de ").append(nome).append(":\n\n");
            for (Relatorio r : relatorios) {
                sb.append("Data: ").append(r.getAno()).append("/").append(r.getMes()).
                        append(", Publicações: ").append(r.getPublicacoes()).append(", Videos: ").
                        append(r.getVideos()).append(", Horas: ").append(r.getHoras()).
                        append(", Revisitas: ").append(r.getRevisitas()).append(", Estudos: ").
                        append(r.getEstudos()).append("\n\n");
            }

            intent = new Intent(Intent.ACTION_SEND);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_SUBJECT, cabecalho);
            intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            intent.setType("message/rfc822");
            chooser = Intent.createChooser(intent, "Enviar Email");
            startActivity(chooser);

            L.t(getApplicationContext(), "enviarCartao");
        }
        return true;
    }


}
