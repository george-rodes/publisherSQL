package br.com.anagnostou.publisher;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import br.com.anagnostou.publisher.grupos.Adriano;
import br.com.anagnostou.publisher.grupos.Anciaos;
import br.com.anagnostou.publisher.grupos.AnoBatismo;
import br.com.anagnostou.publisher.grupos.Centro;
import br.com.anagnostou.publisher.grupos.Irregulares;
import br.com.anagnostou.publisher.grupos.NaoBatizados;
import br.com.anagnostou.publisher.grupos.Pioneiros;
import br.com.anagnostou.publisher.grupos.Pregadores;
import br.com.anagnostou.publisher.grupos.SalaoDoReino;
import br.com.anagnostou.publisher.grupos.Servos;
import br.com.anagnostou.publisher.grupos.VaroesBatizados;
import br.com.anagnostou.publisher.grupos.VilaNova;
import br.com.anagnostou.publisher.asynctasks.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public boolean bancoTemDados = false;
    public boolean bBackgroundJobs = false;
    boolean isServiceBound;
    boolean mStopLoop;
    BroadcastReceiver broadcastReceiver;

    CheckSQLService checkSQLService;
    ConnectivityManager connMgr;
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    Intent checkSQLServerIntent;
    public static final String NA = "N/A";

    SecondSectionsPagerAdapter secondSectionsPagerAdapter;
    public SectionsPagerAdapter mSectionsPagerAdapter;
    ServiceConnection serviceConnection;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    SpecialPagerAdapter specialPagerAdapter;

    String DATABASE_NAME;
    String DB_FULL_PATH;
    String fosPublicador;
    String fosRelatorio;
    String fosUpdate;
    String nameSearch;

    String sdcard;
    String spCadastro;
    String spHomepage;
    String spRelatorio;
    String spUpdate;

    public ViewPager mViewPager;

    private void bindService(){
        if (serviceConnection == null){
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    CheckSQLService.MyServiceBinder myServiceBinder = (CheckSQLService.MyServiceBinder)iBinder;
                    checkSQLService = myServiceBinder.getService();
                    isServiceBound = true;
                }
                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    isServiceBound = false;
                }
            };
        }
        bindService(checkSQLServerIntent,serviceConnection,Context.BIND_AUTO_CREATE);
    }
    private void unbindService(){
        if(isServiceBound){
            unbindService(serviceConnection);
            isServiceBound=false;
        }
    }

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
        getSupportActionBar().setTitle(R.string.por_grupo);
        getSupportActionBar().setSubtitle(getString(R.string.atividades_da_congregacao));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        /** Shared Preferences **/
        sp = getSharedPreferences("myPreferences", MODE_PRIVATE);
        if (sp.contains("update") && sp.contains("cadastro")
                && sp.contains("relatorio") && sp.contains("homepage")) {
            spUpdate = sp.getString("update", NA);
            spCadastro = sp.getString("cadastro", NA);
            spRelatorio = sp.getString("relatorio", NA);
            spHomepage = sp.getString("homepage", NA);
            //sdcard = Environment.getExternalStorageDirectory().toString();
            //sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
            //sdcard = Environment.getExternalStorageDirectory().getPath();
            fosPublicador = getString(R.string.sdcard) + spCadastro;
            fosRelatorio = getString(R.string.sdcard) + spRelatorio;
            fosUpdate = getString(R.string.sdcard) + spUpdate;

            bBackgroundJobs = false;
            dbAdapter = new DBAdapter(getApplicationContext());
            sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
            DATABASE_NAME = dbAdapter.mydbHelper.getDatabaseName();
            DB_FULL_PATH = sqLiteDatabase.getPath();
            bBackgroundJobs = false;

            if (Utilidades.existeTabela("relatorio", MainActivity.this)
                    && Utilidades.existeTabela("publicador", MainActivity.this)
                    && Utilidades.existeTabela("versao", MainActivity.this)) {
                if (Utilidades.temDadosNoBanco(MainActivity.this)) {
                    final CheckUpdateAvailable checkUpdateAvailable = new CheckUpdateAvailable(MainActivity.this,this);
                    checkUpdateAvailable.execute(spHomepage + spUpdate, fosUpdate);
                    bancoTemDados = true;
                }
            } else {
                atualizarBancoDeDados(getCurrentFocus());
            }
        } else startActivity(new Intent(this, Settings.class));

        /**
       carregaPreferencias();
       checkSQLServerIntent = new Intent(this,CheckSQLService.class);
       startService(checkSQLServerIntent);
       bindService();
       */
    }

    private void carregaPreferencias() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean usarDadosCelurares = sharedPreferences.getBoolean("usarDadosCelurares", false);
        //L.t(this, "usarDadosCelurares: " + usarDadosCelurares);
        //L.t(this, sharedPreferences.getString("server", ""));
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
        /** Handle action bar item clicks here. The action bar will
         * automatically handle clicks on the Home/Up button, so long
         * as you specify a parent activity in AndroidManifest.xml. **/
        int id = item.getItemId();
        if (id == R.id.action_updateDatabase) {
            atualizarBancoDeDados(getCurrentFocus());
            return true;
        } else if (id == R.id.action_settings) {

            //startActivity(new Intent(this, Settings.class));
            startActivity(new Intent(this, AppPreferences.class));
            /* if(isServiceBound){
                L.t(this,"Get Randomnumber: " + checkSQLService.getmRandomNumber());

            }*/
            return true;
        } else  if (id == R.id.action_clear) {
            copyDataBaseSdCard();
        } else if (id == R.id.Json){
            /**
             * object to Json
             * */
           Gson gson = new GsonBuilder().create();

        }



        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0 && bancoTemDados) {
                return new Adriano();
            }
            if (position == 1 && bancoTemDados) {
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
            //IRREGULARES
            if (position == 0 && bancoTemDados) {
                return new Irregulares();
            }
            //VARÕES BATIZADOS
            if (position == 1 && bancoTemDados) {
                return new VaroesBatizados();
            }
            if (position == 2 && bancoTemDados) {
                return new AnoBatismo();
            }
            //NÃO BATIZADOS
            if (position == 3 && bancoTemDados) {
                return new NaoBatizados();
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
                    return "IRREGULARES";
                case 1:
                    return "VARÕES BATIZADOS";
                case 2:
                    return "MENOS DE UM ANO DE BATISMO";
                case 3:
                    return "NÃO BATIZADOS";

            }
            return null;
        }
    }

    @Override /***DrawerLayout **/
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.publicadores) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
            getSupportActionBar().setTitle(getString(R.string.por_grupo));
        } else if (id == R.id.porPrivilegio) {
            mViewPager.setAdapter(secondSectionsPagerAdapter);
            getSupportActionBar().setTitle(getString(R.string.por_privilegio));
        } else if (id == R.id.pesquisasEspeciais) {
            mViewPager.setAdapter(specialPagerAdapter);
            getSupportActionBar().setTitle(getString(R.string.pesquisas_especiais));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean atualizarBancoDeDados(View v) {
        if (Utilidades.isOnline((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
            /** em vez de chamar a activity, chamar várias Asynctask que uma chama a outra através do onPostExecute */
            if (!bBackgroundJobs) {
                final DownloadTaskUpdate downloadTaskUpdate = new DownloadTaskUpdate(MainActivity.this,this,mSectionsPagerAdapter);
                downloadTaskUpdate.execute(spHomepage + spUpdate);
            } else L.t(getApplicationContext(), "Background Jobs in Progress");
            return true;
        } else {
            L.t(getApplicationContext(), "Sem Conexão com a Internet");
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       //super.onActivityResult(requestCode, resultCode, data);
        /** http://stackoverflow.com/questions/22083639/calling-activity-from-fragment-then-return-to-fragment
         * returns to the calling fragment */
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

}
