package br.com.anagnostou.publisher;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
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
import java.util.Calendar;

import br.com.anagnostou.publisher.asynctasks.CheckUpdateAvailable;
import br.com.anagnostou.publisher.asynctasks.DownloadTaskUpdate;
import br.com.anagnostou.publisher.objetos.Relatorio;
import br.com.anagnostou.publisher.phpmysql.JsonTaskPublicador;
import br.com.anagnostou.publisher.phpmysql.JsonTaskRelatorio;
import br.com.anagnostou.publisher.services.*;
import br.com.anagnostou.publisher.telas.Adriano;
import br.com.anagnostou.publisher.telas.Anciaos;
import br.com.anagnostou.publisher.telas.AnoBatismo;
import br.com.anagnostou.publisher.telas.Centro;
import br.com.anagnostou.publisher.telas.Irregulares;
import br.com.anagnostou.publisher.telas.LoginActivity;
import br.com.anagnostou.publisher.telas.NaoBatizados;
import br.com.anagnostou.publisher.telas.NaoRelataram;
import br.com.anagnostou.publisher.telas.Pioneiros;
import br.com.anagnostou.publisher.telas.Pregadores;
import br.com.anagnostou.publisher.telas.RelatorioActivity;
import br.com.anagnostou.publisher.telas.SalaoDoReino;
import br.com.anagnostou.publisher.telas.Servos;
import br.com.anagnostou.publisher.telas.VaroesBatizados;
import br.com.anagnostou.publisher.telas.Vazio;
import br.com.anagnostou.publisher.telas.VilaNova;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    int PERM_EXT_STORAGE = 99;
    public boolean bancoTemDados = false;
    public boolean bBackgroundJobs = false;

    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;

    public static final String NA = "";
    SecondSectionsPagerAdapter secondSectionsPagerAdapter;
    public SectionsPagerAdapter mSectionsPagerAdapter;

    SharedPreferences sp;
    SpecialPagerAdapter specialPagerAdapter;
    String DATABASE_NAME;
    String DB_FULL_PATH;
    String fosPublicador;
    String fosRelatorio;
    String fosUpdate;
    //String nameSearch;
    String sdcard;
    String spCadastro;
    String spHomepage;
    String spRelatorio;
    String spUpdate;
    public ViewPager mViewPager;

    public static final String SP_SPNAME = "mySharedPreferences";
    public static final String SP_AUTHENTICATED = "authenticated";
    public static final String DEFAULT = "N/A";
    private static final int LOGIN_INTENT = 572;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /******NEW DrawerLayout *********/
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        /******************/

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        secondSectionsPagerAdapter = new SecondSectionsPagerAdapter(getSupportFragmentManager());
        specialPagerAdapter = new SpecialPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.por_grupo);
        getSupportActionBar().setSubtitle(getString(R.string.atividades_da_congregacao));

        dbAdapter = new DBAdapter(getApplicationContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        DATABASE_NAME = dbAdapter.mydbHelper.getDatabaseName();
        DB_FULL_PATH = sqLiteDatabase.getPath();
        sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

        checkPermissions();
        PreferenceManager.setDefaultValues(this, R.xml.app_preferences, false);//set just once
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        spUpdate = sp.getString("update", NA);
        spCadastro = sp.getString("cadastro", NA);
        spRelatorio = sp.getString("relatorio", NA);
        spHomepage = sp.getString("homepage", NA);
        fosPublicador = sdcard + spCadastro;//where to store the textfiles
        fosRelatorio = sdcard + spRelatorio;
        fosUpdate = sdcard + spUpdate;
        bBackgroundJobs = false;

        if (tablesExist() && Utilidades.temDadosNoBanco(MainActivity.this)) {
            if (sp.getString("sourceDataImport", "").contentEquals("Texto")) {
                final CheckUpdateAvailable checkUpdateAvailable = new CheckUpdateAvailable(MainActivity.this, this);
                checkUpdateAvailable.execute(spHomepage + spUpdate, fosUpdate);
            }
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
        return Utilidades.existeTabela("relatorio", MainActivity.this)
                && Utilidades.existeTabela("publicador", MainActivity.this)
                && Utilidades.existeTabela("versao", MainActivity.this);
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
        requestQueue.add(srPublisher);
    }

    public void showPHPJsonPublisherData(String response) {
        try {
            JSONArray arrayJSON = new JSONArray(response);
            if (sp.getBoolean("fullMySQLImport", false)) {
                dbAdapter.mydbHelper.dropTablePublicador(sqLiteDatabase);
                L.m("Full Import, dropping table");
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

    public boolean atualizarBancoDeDados() {
        if (Utilidades.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
            /** em vez de chamar a activity, chamar várias Asynctask que uma chama a outra através do onPostExecute */
            if (!bBackgroundJobs) {
                if (sp.getString("sourceDataImport", "").contentEquals("SQL")) {
                    dbAdapter.mydbHelper.dropTablePublicador(sqLiteDatabase);
                    dbAdapter.mydbHelper.dropTableRelatorio(sqLiteDatabase);
                    dbAdapter.mydbHelper.dropTableVersao(sqLiteDatabase);
                    dbAdapter.mydbHelper.dropTableTTRelatorio(sqLiteDatabase);
                    Utilidades.resetPreferencesCounter(this);
                    getPHPJsonPublisherData();//onPostExecute chama a outra
                } else {
                    //importacao TEXTO
                    Utilidades.resetPreferencesCounter(this);
                    final DownloadTaskUpdate downloadTaskUpdate = new DownloadTaskUpdate(MainActivity.this, this, mSectionsPagerAdapter);
                    downloadTaskUpdate.execute(spHomepage + spUpdate);
                }
            } else L.t(getApplicationContext(), getString(R.string.background_jobs_in_progress));
            return true;
        } else {
            dialogoNoInternet();
            return false;
        }
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
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
                // Show an explanation to the user *asynchronously* -- don't block
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
                // Permission has been granted. Start something
            } else {
                // Permission request was denied.
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


        } else if (id == R.id.naorelatou) {
            int i = mViewPager.getCurrentItem();
            // L.m("" + mViewPager.getAdapter().getClass().getCanonicalName() + " " + mViewPager.getCurrentItem());
            //br.com.anagnostou.publisher.MainActivity.SectionsPagerAdapter 2
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
                    grupo = "Centro";
                    break;
            }

            StringBuilder sb = new StringBuilder();
            for (String n : dbAdapter.naoRelatouPorGrupo("" + anoNumero(), "" + mesNumero(), grupo)) {
                sb.append(n);
                sb.append("\n");
            }
            dialogoNaoRelatou(sb.toString());
        }
        return super.onOptionsItemSelected(item);
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
        L.m(sb.toString());
        PackageManager pm = getPackageManager();

        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = sb.toString();

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            L.t(this, "WhatsApp not Installed");

        }

    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0 && bancoTemDados) {
                //L.m("Adraino");
                return new Adriano();
            }
            if (position == 1 && bancoTemDados) {
                //L.m("SalaoDoReino");
                return new SalaoDoReino();
            }
            if (position == 2 && bancoTemDados) {
                return new VilaNova();
            }
            if (position == 3 && bancoTemDados) {
                return new Centro();
            }

            if (position == 4 && bancoTemDados) {
                return new Anciaos();
            }
            if (position == 5 && bancoTemDados) {
                return new Servos();
            }

            if (position == 6 && bancoTemDados) {
                return new Pregadores();
            }

            if (position == 7 && bancoTemDados) {
                return new Pioneiros();
            }

            return new Vazio();
        }

        @Override // Show x total pages.
        public int getCount() {
            return 8;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ADRIANO";
                case 1:
                    return "SALÃO DO REINO";
                case 2:
                    return "VILA NOVA";
                case 3:
                    return "CENTRO";

                /** TABS SCROLLABLE *******/
                case 4:
                    return "ANCIÃOS";
                case 5:
                    return "SERVOS";
                case 6:
                    return "PUBLICADORES";
                case 7:
                    return "PIONEIROS";

            }
            return null;
        }
    }

    public class SecondSectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SecondSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0 && bancoTemDados) {
                return new Anciaos();
            }
            if (position == 1 && bancoTemDados) {
                return new Servos();
            }

            if (position == 3 && bancoTemDados) {
                return new Pregadores();
            }

            if (position == 2 && bancoTemDados) {
                return new Pioneiros();
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
        public SpecialPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0 && bancoTemDados) {
                return new NaoRelataram();
            } else if (position == 1 && bancoTemDados) {
                return new Irregulares();
            } else if (position == 2 && bancoTemDados) {
                return new VaroesBatizados();
            } else if (position == 3 && bancoTemDados) {
                return new AnoBatismo();
            } else if (position == 4 && bancoTemDados) {
                return new NaoBatizados();
            }
            return new Vazio();
        }

        @Override // Show x total pages.
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "NÃO RELATARAM";
                case 1:
                    return "IRREGULARES";
                case 2:
                    return "VARÕES BATIZADOS";
                case 3:
                    return "MENOS DE UM ANO DE BATISMO";
                case 4:
                    return "NÃO BATIZADOS";

            }
            return null;
        }

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


}

/**
 Intent intent = null, chooser = null;
 String cabecalho = "Não Relataram até agora";
 intent = new Intent(Intent.ACTION_SEND);
 intent.setData(Uri.parse("mailto:"));
 intent.putExtra(Intent.EXTRA_SUBJECT, cabecalho);
 intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
 intent.setType("message/rfc822");
 chooser = Intent.createChooser(intent, "Enviar Email");
 startActivity(chooser);


 */