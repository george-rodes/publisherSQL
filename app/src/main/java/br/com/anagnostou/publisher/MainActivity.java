package br.com.anagnostou.publisher;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.anagnostou.publisher.phpmysql.JsonTaskAssistencia;
import br.com.anagnostou.publisher.phpmysql.JsonTaskGrupos;
import br.com.anagnostou.publisher.phpmysql.JsonTaskPublicador;
import br.com.anagnostou.publisher.phpmysql.JsonTaskRelatorio;
import br.com.anagnostou.publisher.services.*;
import br.com.anagnostou.publisher.telas.AssistenciaActivity;
import br.com.anagnostou.publisher.telas.AssistenciaFragment;
import br.com.anagnostou.publisher.telas.ListViewFragment;
import br.com.anagnostou.publisher.telas.LoginActivity;
import br.com.anagnostou.publisher.telas.PioneirosFragment;
import br.com.anagnostou.publisher.telas.RelatorioActivity;
import br.com.anagnostou.publisher.telas.Vazio;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    int PERM_EXT_STORAGE = 99;
    public boolean bancoTemDados = false;
    //public boolean bBackgroundJobs = false;

    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;

    public static final String NA = "";
    SecondSectionsPagerAdapter secondSectionsPagerAdapter;
    public SectionsPagerAdapter mSectionsPagerAdapter;

    SharedPreferences sp;
    SpecialPagerAdapter specialPagerAdapter;
    String DATABASE_NAME;
    String DB_FULL_PATH;
    //String nameSearch;
    String sdcard;
    public ViewPager mViewPager;

    public static final String SP_SPNAME = "mySharedPreferences";
    public static final String SP_AUTHENTICATED = "authenticated";
    public static final String DEFAULT = "N/A";
    private static final int LOGIN_INTENT = 572;

    List<String> sGrupoDinamico = new ArrayList<>();
    int iGrupoDinamicoSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** NEW DrawerLayout **/
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dbAdapter = new DBAdapter(getApplicationContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        DATABASE_NAME = dbAdapter.mydbHelper.getDatabaseName();
        DB_FULL_PATH = sqLiteDatabase.getPath();
        sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        secondSectionsPagerAdapter = new SecondSectionsPagerAdapter(getSupportFragmentManager());
        specialPagerAdapter = new SpecialPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.por_grupo);
        getSupportActionBar().setSubtitle(getString(R.string.atividades_da_congregacao));

        checkPermissions();
        PreferenceManager.setDefaultValues(this, R.xml.app_preferences, false);//set just once
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (tablesExist() && Utilidades.temDadosNoBanco(MainActivity.this)) {
            bancoTemDados = true;
        } else {
            atualizarBancoDeDados();
        }

        Intent intent = new Intent(this, CheckSQLIntentService.class);
        startService(intent);
        Utilidades.checkPreferencesIntLimitReached(this);
        areWeAuthenticated();

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
            intent.putExtra("Origem", "MainActivity");
            startActivityForResult(intent, LOGIN_INTENT);
            return false;
        } else return true;
    }

    private void clearAuthenticationKey() {
        final SharedPreferences sp = getSharedPreferences(SP_SPNAME, MODE_PRIVATE);
        if (sp.getString(SP_AUTHENTICATED, DEFAULT).equals("authenticated")) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage(R.string.deseja_logoff);
            builder1.setCancelable(true);
            builder1.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(SP_AUTHENTICATED, "");
                    editor.apply();
                    dialog.cancel();
                }
            });
            builder1.setNegativeButton("NÂO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == LOGIN_INTENT) {
                SharedPreferences sp = getSharedPreferences(SP_SPNAME, MODE_PRIVATE);
                if (!sp.getString(SP_AUTHENTICATED, DEFAULT).equals("authenticated")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.autenticacao_falhou);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }

    public boolean tablesExist() {
        return Utilidades.existeTabela(DBAdapter.DBHelper.TN_RELATORIO, MainActivity.this)
                && Utilidades.existeTabela(DBAdapter.DBHelper.TN_PUBLICADOR, MainActivity.this)
                && Utilidades.existeTabela(DBAdapter.DBHelper.TN_VERSAO, MainActivity.this)
                && Utilidades.existeTabela(DBAdapter.DBHelper.TN_GRUPOS, MainActivity.this)
                && Utilidades.existeTabela(DBAdapter.DBHelper.TN_ASSISTENCIA, MainActivity.this);
    }

    public boolean atualizarBancoDeDados() {
        if (Utilidades.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
            /** em vez de chamar a activity, chamar várias Asynctask que uma chama a outra através do onPostExecute */
            //if (!bBackgroundJobs) {
            dbAdapter.mydbHelper.dropTablePublicador(sqLiteDatabase);
            dbAdapter.mydbHelper.dropTableRelatorio(sqLiteDatabase);
            dbAdapter.mydbHelper.dropTableVersao(sqLiteDatabase);
            dbAdapter.mydbHelper.dropTableTTRelatorio(sqLiteDatabase);
            dbAdapter.mydbHelper.dropTableAssistencia(sqLiteDatabase);
            dbAdapter.mydbHelper.dropTableGrupos(sqLiteDatabase);
            Utilidades.resetPreferencesCounter(this);
            getPHPJsonGrupos();//semDialogo
            getPHPJsonAssistenciaData(); //sem Dialogo
            getPHPJsonPublisherData();

            // } else L.t(getApplicationContext(), getString(R.string.background_jobs_in_progress));
            return true;
        } else {
            dialogoNoInternet();
            return false;
        }
    }


    public void getPHPJsonPublisherData() {
        String url = sp.getString("php_publisher_full", NA);
        StringRequest srPublisher = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showPHPJsonPublisherData(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        L.t(MainActivity.this, error.getMessage());
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        srPublisher.setShouldCache(false);
        requestQueue.add(srPublisher);
    }


    public void showPHPJsonPublisherData(String response) {
        try {
            JSONArray arrayJSON = new JSONArray(response);
            if (sp.getBoolean("fullMySQLImport", false)) {
                dbAdapter.mydbHelper.dropTablePublicador(sqLiteDatabase);
                L.m("Full Import, dropping table Publisher");
            }
            JsonTaskPublicador jsonTaskPublicador = new JsonTaskPublicador(MainActivity.this, this);
            jsonTaskPublicador.execute(arrayJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getPHPJsonRelatorioData() {
        String url = sp.getString("php_report_full", NA);
        StringRequest srRelatorio = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showPHPJsonRelatorioData(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        L.t(MainActivity.this, error.getMessage());

                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        srRelatorio.setShouldCache(false);
        requestQueue.add(srRelatorio);
    }

    public void showPHPJsonRelatorioData(String response) {
        try {
            JSONArray arrayJSON = new JSONArray(response);
            if (sp.getBoolean("fullMySQLImport", false)) {
                dbAdapter.mydbHelper.dropTableRelatorio(sqLiteDatabase);
                L.m("Full Import, dropping table");
            }
            JsonTaskRelatorio jsonTaskRelatorio = new JsonTaskRelatorio(MainActivity.this, this, mSectionsPagerAdapter);
            jsonTaskRelatorio.execute(arrayJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getPHPJsonAssistenciaData() {
        String url = sp.getString("php_assistencia", NA);
        StringRequest srAssistencia = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showPHPJsonAssistenciaData(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        L.t(MainActivity.this, error.getMessage());
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        srAssistencia.setShouldCache(false);
        requestQueue.add(srAssistencia);
    }

    public void showPHPJsonAssistenciaData(String response) {
        try {
            JSONArray arrayJSON = new JSONArray(response);
            dbAdapter.mydbHelper.dropTableAssistencia(sqLiteDatabase);
            L.m("Full Import, dropping table");
            JsonTaskAssistencia jsonTaskAssistencia = new JsonTaskAssistencia(this);
            jsonTaskAssistencia.execute(arrayJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showPHPJsonAGrupos(String response) {
        try {
            JSONArray arrayJSON = new JSONArray(response);
            dbAdapter.mydbHelper.dropTableGrupos(sqLiteDatabase);
            L.m("Full Import, dropping table Grupos");
            JsonTaskGrupos jsonTaskGrupos = new JsonTaskGrupos(MainActivity.this, this);
            jsonTaskGrupos.execute(arrayJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getPHPJsonGrupos() {

        String url = sp.getString("php_grupos", NA);
        L.m("getPHPJsonGrupos php grupos" + url);
        StringRequest srGrupos = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showPHPJsonAGrupos(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        L.t(MainActivity.this, error.getMessage());
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        srGrupos.setShouldCache(false);
        requestQueue.add(srGrupos);
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                L.t(this, "shouldShowRequestPermissionRationale");
            } else {
                int PERM_PHONE = 77;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERM_PHONE);
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                L.t(this, "Show an explanation to the user *asynchronously* -- don't block");
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_EXT_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERM_EXT_STORAGE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                L.t(this, "Permission has been granted. Start something");
            } else {
                L.t(this, "Permission request was denied.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        /** SEARCH **/
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String grupo = "";
        int id = item.getItemId();
        if (id == R.id.novo_relatorio) {
            //Adicionando modo offline
            if (areWeAuthenticated()) {
                Intent intent = new Intent(this, RelatorioActivity.class);
                intent.putExtra("origem", "MainActivity");
                intent.putExtra("objetivo", "novo relatorio");
                startActivity(intent);
            }
        } else if (id == R.id.enviar_assistencia) {

            if (areWeAuthenticated()) {
                Intent intent = new Intent(this, AssistenciaActivity.class);
                intent.putExtra("origem", "MainActivity");
                intent.putExtra("objetivo", "enviar assistencia");
                startActivity(intent);
            }


        } else if (id == R.id.naorelatou) {
            int i = mViewPager.getCurrentItem();
            // L.m("" + mViewPager.getAdapter().getClass().getCanonicalName() + " " + mViewPager.getCurrentItem());
            //br.com.anagnostou.publisher.MainActivity.SectionsPagerAdapter 2
            //
            grupo = sGrupoDinamico.get(i);
            /*
            switch (i) {
                case 0:
                    grupo = "Adriano";
                    break;
                case 1:
                    grupo = "Salão do Reino";
                    break;
                case 2:
                    grupo = "Vila Nova";
                    break;
                case 3:
                    grupo = "Siriemas";
                    break;
            }
            */

            StringBuilder sb = new StringBuilder();
            sb.append("Grupo: ").append(grupo).append("\n");
            for (String n : dbAdapter.naoRelatouPorGrupo("" + anoNumero(), "" + mesNumero(), grupo)) {
                sb.append(n);
                sb.append("\n");
            }
            dialogoNaoRelatou(sb.toString());
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    /***DrawerLayout **/
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.publicadores) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getString(R.string.por_grupo));
        } else if (id == R.id.porPrivilegio) {
            mViewPager.setAdapter(secondSectionsPagerAdapter);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getString(R.string.por_privilegio));
        } else if (id == R.id.pesquisasEspeciais) {
            mViewPager.setAdapter(specialPagerAdapter);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getString(R.string.pesquisas_especiais));
        } else if (id == R.id.atualizar) {
            atualizarBancoDeDados();
        } else if (id == R.id.preferencias) {
            startActivity(new Intent(this, AppPreferences.class));
        } else if (id == R.id.copy) {
            copyDataBaseSdCard();
        } else if (id == R.id.logoff) {
            clearAuthenticationKey();
        } else if (id == R.id.enviaNaoRelataram) {
            enviaNaoRelataram();
        } else if (id == R.id.teste_1) {
            teste_1();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void enviaNaoRelataram() {
        Cursor c = dbAdapter.naoRelatouMesPassado("" + anoNumero(), "" + mesNumero());
        StringBuilder sb = new StringBuilder();
        sb.append("Não relataram até o momento:\n");
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                sb.append(c.getString(1));
                sb.append("\n");
            }
        }
        PackageManager pm = getPackageManager();
        try {
            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = sb.toString();
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            L.t(this, "WhatsApp not Installed");
        }
    }

    public void grupoDinamico() {
        sGrupoDinamico = dbAdapter.retrieveGrupos();
        iGrupoDinamicoSize = sGrupoDinamico.size();
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            L.m("Entrei no SectionsPagerAdapter, super(fm); ");
            //chama só na inicialização do Adapter
            grupoDinamico();

        }

        @Override
        public Fragment getItem(int position) {

            if (bancoTemDados) {
                return criaFragment(sGrupoDinamico.get(position));
            }
            return new Vazio();
        }

        @Override // Show x total pages.
        public int getCount() {
            return iGrupoDinamicoSize;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return sGrupoDinamico.get(position);
        }
    }

    public class SecondSectionsPagerAdapter extends FragmentStatePagerAdapter {
        SecondSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0 && bancoTemDados) {
                return criaFragment("Ancião");
            }
            if (position == 1 && bancoTemDados) {
                return criaFragment("Servo");
            }

            if (position == 3 && bancoTemDados) {
                return criaFragment("Publicador");
            }

            if (position == 2 && bancoTemDados) {
                return new PioneirosFragment();
            }
            return new Vazio();
        }

        @Override // Show x total pages.
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ANCIÃOS";
                case 1:
                    return "SERVOS";
                case 3:
                    return "PUBLICADORES";
                case 2:
                    return "PIONEIROS";
            }
            return null;
        }
    }

    public class SpecialPagerAdapter extends FragmentStatePagerAdapter {
        SpecialPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1 && bancoTemDados) {
                return criaFragment("NaoRelataram");
            } else if (position == 5 && bancoTemDados) {
                //return new IrregularesFragment();
                return criaFragment("Irregulares");
            } else if (position == 2 && bancoTemDados) {
                return criaFragment("Varoes");
            } else if (position == 3 && bancoTemDados) {
                return criaFragment("AnoBatismo");
            } else if (position == 4 && bancoTemDados) {
                return criaFragment("NaoBatizados");
            } else if (position == 0 && bancoTemDados) {

                return new AssistenciaFragment();
            }
            return new Vazio();
        }

        @Override // Show x total pages.
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return "NÃO RELATARAM";
                case 5:
                    return "IRREGULARES";
                case 2:
                    return "VARÕES BATIZADOS";
                case 3:
                    return "MENOS DE UM ANO DE BATISMO";
                case 4:
                    return "NÃO BATIZADOS";
                case 0:
                    return "ASSISTENCIA";

            }
            return null;
        }

    }

    public Fragment criaFragment(String pesquisa) {
        Bundle bundle = new Bundle();
        bundle.putString("pesquisa", pesquisa);
        Fragment fragment = new ListViewFragment();
        fragment.setArguments(bundle);
        return fragment;

    }

    public void copyDataBaseSdCard() {

        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                String currentDBPath = DB_FULL_PATH;
                String backupDBPath = DATABASE_NAME;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                if (currentDB.exists()) {

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            L.m(e.toString());
        }
    }

    private void dialogoNoInternet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.sem_conexao_internet);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogoNaoRelatou(String sb) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(sb);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int mesNumero() {
        int mes;
        Calendar now = Calendar.getInstance();
        mes = now.get(Calendar.MONTH) + 1;
        if ((mes - 1) == 0) {
            return 12;
        } else {
            return mes - 1;
        }
    }

    private int anoNumero() {
        int ano, mes;
        Calendar now = Calendar.getInstance();
        ano = now.get(Calendar.YEAR);
        mes = now.get(Calendar.MONTH);
        if (mes == 0) {
            ano = ano - 1;
        }

        return ano;
    }

    private void teste_1() {

        dbAdapter.retrieveGrupos();
        //simular mudanca de banco de dados
        //creatre dbadapter cursors
        //insert data
        // String url = sp.getString("php_assistencia", NA);
        // L.m(url);
        //ad getPHPJsonAssistenciaData();
        Cursor p = dbAdapter.pragma(DBAdapter.DBHelper.TN_TESTE1);
        if (p.getCount() > 0) {
            while (p.moveToNext()) {
                // L.m("Field Name/Type: " + p.getString(p.getColumnIndex("name")) + " / " + p.getString(p.getColumnIndex("type")));
            }
        }
        for (int i = 1; i < 9; i++) {
            dbAdapter.insertTest1("Record # " + i);
            // L.m("Insert Record # " + i);
        }

        Cursor c = dbAdapter.test1();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                //L.m("A Record: " + c.getString(1) + " / " + c.getString(2));
            }
        } else L.t(this, "No Records, but table is there");

        Cursor c2 = dbAdapter.fetchAllAssistencia();
        if (c2.getCount() > 0) {
            while (c2.moveToNext()) {
                L.m("Assistencia: " + c2.getInt(0) + " / " + c2.getString(1) + " / " + c2.getString(2) + " / " + c2.getInt(3));
            }
        } else L.t(this, "No Assistencia, but table is there");

    }


}

/**
 * Intent intent = null, chooser = null;
 * String cabecalho = "Não Relataram até agora";
 * intent = new Intent(Intent.ACTION_SEND);
 * intent.setData(Uri.parse("mailto:"));
 * intent.putExtra(Intent.EXTRA_SUBJECT, cabecalho);
 * intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
 * intent.setType("message/rfc822");
 * chooser = Intent.createChooser(intent, "Enviar Email");
 * startActivity(chooser);
 */