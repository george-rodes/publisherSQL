package br.com.anagnostou.publisher;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import br.com.anagnostou.publisher.objetos.Publicador;
import br.com.anagnostou.publisher.objetos.Relatorio;

public class DBAdapter {
    public DBHelper mydbHelper;
    private HashMap<String, String> mAliasMap;

    public DBAdapter(Context context) {
        mydbHelper = DBHelper.getInstance(context);

        /******************IMPORTANT FOR SEARCH ********************/
        // This HashMap is used to map table fields to Custom Suggestion fields
        mAliasMap = new HashMap<String, String>();
        // Unique id for the each Suggestions ( Mandatory )
        mAliasMap.put("_ID", "_id as _id");
        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, "nome as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        // Icon for Suggestions ( Optional )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_ICON_1, "1  as " + SearchManager.SUGGEST_COLUMN_ICON_1);
        // This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "_id as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);

    }

    /************************ SEARCH ****************************************/
    /**
     * Returns Countries
     */
    public Cursor getPublicadores(String[] selectionArgs) {
        String selection = DBHelper.NOME + " like ? ";
        if (selectionArgs != null) {
            selectionArgs[0] = "%" + selectionArgs[0] + "%";
        }
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);
        queryBuilder.setTables(DBHelper.TABLE_NAME_PUBLICADOR);
        Cursor c = queryBuilder.query(mydbHelper.getReadableDatabase(),
                new String[]{"_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_ICON_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID},
                selection,
                selectionArgs,
                null,
                null,
                DBHelper.NOME + " asc ", "10"
        );
        return c;
    }

    /**
     * Return Publisher corresponding to the id, not used
     */
    public Cursor getPublicador(String id) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DBHelper.TABLE_NAME_PUBLICADOR);
        Cursor c = queryBuilder.query(mydbHelper.getReadableDatabase(),
                new String[]{"_id", "name", "familia", "grupo"},
                "_id = ?", new String[]{id}, null, null, null, "1"
        );
        return c;
    }

    /**
     * Return Publisher corresponding to the id
     */
    public Cursor getOnePublicador(String id) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DBHelper.TABLE_NAME_PUBLICADOR);
        Cursor c = queryBuilder.query(mydbHelper.getReadableDatabase(),
                new String[]{"nome"},
                "_id = ?", new String[]{id}, null, null, null, "1"
        );
        return c;
    }

    /*************************
     * PUBLICADOR
     ***************************/

    public Cursor retrieveRelatorios(String nome) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String orderBy = " ano asc, mes asc ";
        String[] selectionArgs = {nome};
        return db.query(DBHelper.TABLE_NAME_RELATORIO, null, DBHelper.NOME + " = ? ", selectionArgs, null, null, orderBy);
    }

    public String findFirstPublicador() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.NOME};
        Cursor cursor = db.query(DBHelper.TABLE_NAME_PUBLICADOR, columns, null, null, null, null, null, "1");
        StringBuilder sb = new StringBuilder();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String nome = cursor.getString(cursor.getColumnIndex(DBHelper.NOME));//1
                sb.append(nome + "\n");
            }
        } else sb.append("No Records");
        cursor.close();
        return sb.toString();
    }

    public String selectVersao() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.ULTIMA_ATUALIZACAO};
        Cursor cursor = db.query(DBHelper.TABLE_NAME_VERSAO, columns, null, null, null, null, null);
        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            String versao = cursor.getString(cursor.getColumnIndex(DBHelper.ULTIMA_ATUALIZACAO));
            sb.append(versao);
        }
        return sb.toString();
    }

    public Cursor cursorPublicadorPorGrupo(String grupo) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.NOME, DBHelper.FAMILIA};
        String[] selectionArgs = {grupo};
        return db.query(DBHelper.TABLE_NAME_PUBLICADOR, columns, DBHelper.GRUPO + " = ?", selectionArgs, null, null, DBHelper.FAMILIA);
    }

    public Cursor cursorVaroesBatizados() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {"", "M", "Publicador"};
        return db.rawQuery("SELECT _id,nome,familia FROM publicador WHERE data_batismo <> ? AND sexo = ? AND ansepu = ?", selectionArgs);

    }

    public Cursor cursorNaoBatizados() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {""};
        return db.rawQuery("SELECT _id,nome,familia FROM publicador WHERE data_batismo = ? ORDER BY nome", selectionArgs);

    }

    public Cursor irregularesJaneiroDezembro(String ano, String mesini, String mesfim) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {ano, mesini, mesfim};
        return db.rawQuery("SELECT DISTINCT publicador._id,  relatorio.nome, publicador.familia FROM relatorio,publicador " +
                "WHERE  relatorio.nome = publicador.nome " +
                "AND relatorio.horas < 1 " +
                "AND relatorio.ano = ? AND relatorio.mes >= ? AND relatorio.mes <= ?" +
                "GROUP BY relatorio.nome HAVING count(publicador._id) < 6 ", selectionArgs);
    }

    public Cursor irregularesCruzaAno(String anoini, String mesini, String mesfim, String anofim, String mesini1, String mesfim1) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {anoini, mesini, mesfim, anofim, mesini1, mesfim1};
        return db.rawQuery("SELECT DISTINCT publicador._id,  relatorio.nome, publicador.familia FROM relatorio,publicador " +
                "WHERE  relatorio.nome = publicador.nome " +
                "AND relatorio.horas < 1 " +
                "AND ((relatorio.ano = ? AND relatorio.mes >= ? AND relatorio.mes <= ?) " +
                "OR (relatorio.ano = ? AND relatorio.mes >= ? AND relatorio.mes <= ? )) " +
                "GROUP BY relatorio.nome HAVING count(publicador._id) < 6 ", selectionArgs);
    }

    public Cursor menosDeUmAnoDeBatismo(String anoini, String mesini, String anofim) {
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        String[] selectionArgs = {anoini, mesini, anofim};
        return db.rawQuery("select  _id, nome, familia from publicador where data_batismo <> '' AND  " +
                "((substr(data_batismo,7,4) = ? AND substr(data_batismo,4,2) >= ? )  OR substr(data_batismo,7,4) = ? ) order by nome", selectionArgs);

    }


    public Cursor cursorPublicadorBusca(String query) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.NOME, DBHelper.FAMILIA};
        query = "%" + query.trim() + "%";
        String[] selectionArgs = {query};
        return db.query(DBHelper.TABLE_NAME_PUBLICADOR, columns, DBHelper.NOME + " LIKE ? ", selectionArgs, null, null, DBHelper.FAMILIA);
    }

    public Cursor cursorPublicadorPorAnsepu(String ansepu) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.NOME, DBHelper.FAMILIA};
        String[] selectionArgs = {ansepu};
        return db.query(DBHelper.TABLE_NAME_PUBLICADOR, columns, DBHelper.ANSEPU + " = ?", selectionArgs, null, null, DBHelper.NOME);
    }

    public Cursor cursorPioneiroPublicador(String pipu) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.NOME, DBHelper.FAMILIA};
        String[] selectionArgs = {pipu};
        return db.query(DBHelper.TABLE_NAME_PUBLICADOR, columns, DBHelper.PIPU + " = ?", selectionArgs, null, null, DBHelper.NOME);
    }



    /********
     * 14/12/2016
     *******/
     public Publicador retrievePublisherData(String name) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.FAMILIA, DBHelper.GRUPO, DBHelper.BATISMO, DBHelper.CELULAR,
                DBHelper.RUA, DBHelper.NASCIMENTO, DBHelper.FONE, DBHelper.BAIRRO, DBHelper.ANSEPU, DBHelper.PIPU, DBHelper.SEXO};
        String[] selectionArgs = {name};
        Cursor cursor = db.query(DBHelper.TABLE_NAME_PUBLICADOR, columns, DBHelper.NOME + " = ?", selectionArgs, null, null, null);
        Publicador pub = new Publicador();
        while (cursor.moveToNext()) {

            pub.setNome(name);
            pub.setFamilia(cursor.getString(cursor.getColumnIndex(DBHelper.FAMILIA)));
            pub.setGrupo(cursor.getString(cursor.getColumnIndex(DBHelper.GRUPO)));
            pub.setBatismo(cursor.getString(cursor.getColumnIndex(DBHelper.BATISMO)));
            pub.setCelular(cursor.getString(cursor.getColumnIndex(DBHelper.CELULAR)));
            pub.setRua(cursor.getString(cursor.getColumnIndex(DBHelper.RUA)));
            pub.setNascimento(cursor.getString(cursor.getColumnIndex(DBHelper.NASCIMENTO)));
            pub.setFone(cursor.getString(cursor.getColumnIndex(DBHelper.FONE)));
            pub.setBairro(cursor.getString(cursor.getColumnIndex(DBHelper.BAIRRO)));
            pub.setAnsepu(cursor.getString(cursor.getColumnIndex(DBHelper.ANSEPU)));
            pub.setPipu(cursor.getString(cursor.getColumnIndex(DBHelper.PIPU)));
            pub.setSexo(cursor.getString(cursor.getColumnIndex(DBHelper.SEXO)));
        }
        cursor.close();
        return pub;

    }

    /*****************************************
     * RELATORIO
     **************************************/

     public String findFirstRelatorio() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.ANO, DBHelper.MES, DBHelper.NOME, DBHelper.HORAS};
        StringBuilder sb = new StringBuilder();
        try {
            Cursor cursor = db.query(DBHelper.TABLE_NAME_RELATORIO, columns, null, null, null, null, null, "1");
            //check cursor
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int ano = cursor.getInt(cursor.getColumnIndex(DBHelper.ANO));
                    int mes = cursor.getInt(cursor.getColumnIndex(DBHelper.MES));
                    int horas = cursor.getInt(cursor.getColumnIndex(DBHelper.HORAS));
                    String nome = cursor.getString(cursor.getColumnIndex(DBHelper.NOME));
                    sb.append(ano + " " + mes + " " + nome + " " + horas + "\n");
                }
            } else sb.append("No Records");
            cursor.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            sb.append("No Records");
            return sb.toString();
        }
    }

     public String[] somaHorasMeses(String nome) {
        String[] resultado = {"n/a", "n/a", "n/a", "n/a", "n/a", "n/a"};
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {nome};
        Cursor cursor = db.rawQuery("SELECT SUM(HORAS),COUNT(HORAS),AVG(REVISITAS),AVG(ESTUDOS),AVG(videos),AVG(publicacoes) FROM RELATORIO WHERE NOME = ?", selectionArgs);
        if (cursor.moveToFirst()) {
            resultado[0] = String.valueOf(cursor.getInt(0));
            resultado[1] = String.valueOf(cursor.getInt(1));
            resultado[2] = String.valueOf(cursor.getDouble(2));
            resultado[3] = String.valueOf(cursor.getDouble(3));
            resultado[4] = String.valueOf(cursor.getDouble(4));
            resultado[5] = String.valueOf(cursor.getDouble(5));
            cursor.close();
            return resultado;
        } else {
            resultado[0] = "0";
            resultado[1] = "1";
            resultado[2] = "0";
            resultado[3] = "0";
            resultado[4] = "0";
            resultado[5] = "0";
            cursor.close();
            return resultado;
        }

    }

     public String[] retrieveTotais(String nome) {
        String[] resultado = {"n/a", "n/a", "n/a", "n/a", "n/a", "n/a"};
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {nome};
        Cursor cursor = db.rawQuery("SELECT COUNT(_id),SUM(publicacoes),SUM(videos), SUM(HORAS),SUM(REVISITAS),SUM(estudos) FROM RELATORIO WHERE NOME = ?", selectionArgs);
        if (cursor.moveToFirst()) {
            resultado[0] = String.valueOf(cursor.getInt(0));
            resultado[1] = String.valueOf(cursor.getInt(1));
            resultado[2] = String.valueOf(cursor.getInt(2));
            resultado[3] = String.valueOf(cursor.getInt(3));
            resultado[4] = String.valueOf(cursor.getInt(4));
            resultado[5] = String.valueOf(cursor.getInt(5));
            cursor.close();
            return resultado;
        } else {
            resultado[0] = "0";
            resultado[1] = "0";
            resultado[2] = "0";
            resultado[3] = "0";
            resultado[4] = "0";
            resultado[5] = "0";
            cursor.close();
            return resultado;
        }

    }

     public String contaPioneiroAuxiliar(String nome) {
        String resultado;
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {nome, "Pioneiro Auxiliar"};
        Cursor cursor = db.rawQuery("SELECT COUNT(nome) FROM RELATORIO WHERE NOME = ? AND MODALIDADE = ?", selectionArgs);
        if (cursor.moveToFirst()) {
            resultado = String.valueOf(cursor.getInt(0));
            cursor.close();
            return resultado;
        } else {
            resultado = "0";
            cursor.close();
            return resultado;
        }
    }

     public String deixouDeRelatar(String nome) {
        String resultado;
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {nome, "0"};
        Cursor cursor = db.rawQuery("SELECT COUNT(nome) FROM RELATORIO WHERE NOME = ? AND HORAS = ? ", selectionArgs);
        if (cursor.moveToFirst()) {
            resultado = String.valueOf(cursor.getInt(0));
            cursor.close();
            return resultado;
        } else {
            resultado = "0";
            cursor.close();
            return resultado;
        }
    }

    /*****************************************
     * INSERT
     ****************************************/
     public long insertDataPublicador(Publicador p) {

        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DBHelper.NOME, p.getNome());
        cv.put(DBHelper.FAMILIA, p.getFamilia());
        cv.put(DBHelper.GRUPO, p.getGrupo());
        cv.put(DBHelper.BATISMO, p.getBatismo());
        cv.put(DBHelper.NASCIMENTO, p.getNascimento());
        cv.put(DBHelper.FONE, p.getFone());
        cv.put(DBHelper.CELULAR, p.getCelular());
        cv.put(DBHelper.RUA, p.getRua());
        cv.put(DBHelper.BAIRRO, p.getBairro());
        cv.put(DBHelper.ANSEPU, p.getAnsepu());
        cv.put(DBHelper.PIPU, p.getPipu());
        cv.put(DBHelper.SEXO, p.getSexo());

        //id of the column or -1 when insert failed
        long id = db.insert(DBHelper.TABLE_NAME_PUBLICADOR, null, cv);

         db.close();
        return id;
    }

     public long insertDataVersao(String versao) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.ULTIMA_ATUALIZACAO, versao);
        //id of the column or -1 when insert failed
        long id = db.insert(DBHelper.TABLE_NAME_VERSAO, null, cv);
        db.close();
        return id;
    }

     public long insertDataRelatorio(Relatorio r) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DBHelper.ANO, r.getAno());
        cv.put(DBHelper.MES, r.getMes());
        cv.put(DBHelper.NOME, r.getNome());
        cv.put(DBHelper.MODALIDADE, r.getModalidade());
        cv.put(DBHelper.VIDEOS, r.getVideos());
        cv.put(DBHelper.HORAS, r.getHoras());
        cv.put(DBHelper.PUBLICACOES, r.getPublicacoes());
        cv.put(DBHelper.REVISITAS, r.getRevisitas());
        cv.put(DBHelper.ESTUDOS, r.getEstudos());

        //id of the column or -1 when insert failed
        long id = db.insert(DBHelper.TABLE_NAME_RELATORIO, null, cv);
        db.close();
        return id;
    }
    /****************
     * UPDATE
     */
    public boolean updateDataRelatorio(Relatorio r) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        String ano = String.valueOf(r.getAno());
        String mes = String.valueOf(r.getMes());
        String nome = String.valueOf(r.getNome());

        String selection = DBHelper.ANO + " = ? AND " + DBHelper.MES + " = ? AND " + DBHelper.NOME + " = ? ";
        String[] selectionArgs = {ano,mes,nome};

        cv.put(DBHelper.MODALIDADE, r.getModalidade());
        cv.put(DBHelper.VIDEOS, r.getVideos());
        cv.put(DBHelper.HORAS, r.getHoras());
        cv.put(DBHelper.PUBLICACOES, r.getPublicacoes());
        cv.put(DBHelper.REVISITAS, r.getRevisitas());
        cv.put(DBHelper.ESTUDOS, r.getEstudos());

        int count = db.update(DBHelper.TABLE_NAME_RELATORIO, cv, selection, selectionArgs);
        return count > 0;

    }


    /*************************************
     * SQLiteOpenHelper
     ************************************/
     public static class DBHelper extends SQLiteOpenHelper {
        private static DBHelper sInstance;

        public static synchronized DBHelper getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new DBHelper(context.getApplicationContext());
            }
            return sInstance;
        }

        private DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);


        }

        //DATABASE COMMON
        private static final String DB_NAME = "appledore";
        private static final int DB_VERSION = 1;
        private static final String VIRGULA = ", ";

        //TABLE versao
        private static final String TABLE_NAME_VERSAO = "versao";
        private static final String ULTIMA_ATUALIZACAO = "data_ultima_atualizacao";

        private static final String CREATE_TABLE_VERSAO = "CREATE TABLE "
                + TABLE_NAME_VERSAO + " ( "
                + ULTIMA_ATUALIZACAO + " TEXT UNIQUE  );";
        private static final String DROP_TABLE_VERSAO = "DROP TABLE IF EXISTS " + TABLE_NAME_VERSAO;

        /**
         * TABLE publicador
         **/
        private static final String TABLE_NAME_PUBLICADOR = "publicador";
        private static final String UID = "_id";
        private static final String NOME = "nome";
        private static final String FAMILIA = "familia";
        private static final String GRUPO = "grupo";
        private static final String BATISMO = "data_batismo";
        private static final String NASCIMENTO = "data_nascimento";
        private static final String FONE = "fone";
        private static final String CELULAR = "celular";
        private static final String RUA = "rua";
        private static final String BAIRRO = "bairro";
        private static final String ANSEPU = "ansepu";
        private static final String PIPU = "pipu";
        private static final String SEXO = "sexo";


        private static final String CREATE_TABLE_PUBLICADOR = "CREATE TABLE "
                + TABLE_NAME_PUBLICADOR + " ( "
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOME + " TEXT UNIQUE, "
                + FAMILIA + " TEXT, "
                + GRUPO + " TEXT, "
                + BATISMO + " TEXT, "
                + NASCIMENTO + " TEXT, "
                + FONE + " TEXT, "
                + CELULAR + " TEXT, "
                + RUA + " TEXT, "
                + BAIRRO + " TEXT, "
                + ANSEPU + " TEXT, "
                + PIPU + " TEXT, "
                + SEXO + " TEXT );";

        private static final String DROP_TABLE_PUBLICADOR = "DROP TABLE IF EXISTS " + TABLE_NAME_PUBLICADOR;

        /**
         * TABLE RELATORIO
         **/
        private static final String TABLE_NAME_RELATORIO = "relatorio";
        private static final String ANO = "ano";
        private static final String MES = "mes";
        private static final String MODALIDADE = "modalidade";
        private static final String VIDEOS = "videos";
        private static final String HORAS = "horas";
        private static final String PUBLICACOES = "publicacoes";
        private static final String REVISITAS = "revisitas";
        private static final String ESTUDOS = "estudos";

        //2016;6;Rosangela Souza;Pioneiro Regular;10;71;26;15;6
        private static final String CREATE_TABLE_RELATORIO = "CREATE TABLE "
                + TABLE_NAME_RELATORIO + " ( "
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ANO + " INTEGER, "
                + MES + " INTEGER, "
                + NOME + " TEXT , "
                + MODALIDADE + " TEXT, "
                + VIDEOS + " INTEGER, "
                + HORAS + " INTEGER, "
                + PUBLICACOES + " INTEGER, "
                + REVISITAS + " INTEGER, "
                + ESTUDOS + " INTEGER "
                + VIRGULA + " UNIQUE ( "
                + ANO + VIRGULA + MES + VIRGULA + NOME + ") );";

        private static final String DROP_TABLE_RELATORIO = "DROP TABLE IF EXISTS " + TABLE_NAME_RELATORIO;

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                //L.m("on Create called. Nao tem nada aqui");
                //db.execSQL(CREATE_TABLE_PUBLICADOR);
                //db.execSQL(CREATE_TABLE_RELATORIO);
                // db.execSQL(CREATE_TABLE_VERSAO);
                //db.execSQL(INSERT_VERSAO);
            } catch (SQLException e) {
                // L.m( e + "on Create failed");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                // L.m("on Upgrade called" + db.getVersion());
                db.execSQL(DROP_TABLE_PUBLICADOR);
                db.execSQL(DROP_TABLE_RELATORIO);
                db.execSQL(DROP_TABLE_VERSAO);
                onCreate(db);
            } catch (SQLException e) {
                // L.m(e + "on Upgrade failed");
            }
        }

        public void dropTablePublicador(SQLiteDatabase db) {
            try {
                //L.m("Dropping Table Publicador\n" + db.getVersion());
                db.execSQL(DROP_TABLE_PUBLICADOR);
                db.execSQL(CREATE_TABLE_PUBLICADOR);
                onCreate(db);
            } catch (SQLException e) {
                //L.m(e + "\nDropping Table Publicador failed");
            }
        }

        public void dropTableVersao(SQLiteDatabase db) {
            try {
                //L.m("Dropping Table Versao\n" + db.getVersion());
                db.execSQL(DROP_TABLE_VERSAO);
                db.execSQL(CREATE_TABLE_VERSAO);
                //db.execSQL(INSERT_VERSAO);
                onCreate(db);
            } catch (SQLException e) {
                //L.m(e + "\nDropping Table Versao failed");
            }
        }

        public void dropTableRelatorio(SQLiteDatabase db) {
            try {
                //L.m("Dropping Table Relaotorio" + db.getVersion());
                db.execSQL(DROP_TABLE_RELATORIO);
                db.execSQL(CREATE_TABLE_RELATORIO);
                onCreate(db);
            } catch (SQLException e) {
                //L.m("Dropping Table Relatorio failed");
            }
        }
    }
}