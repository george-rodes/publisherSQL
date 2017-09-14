package br.com.anagnostou.publisher;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.com.anagnostou.publisher.objetos.DozeMeses;
import br.com.anagnostou.publisher.objetos.Publicador;
import br.com.anagnostou.publisher.objetos.Relatorio;
import br.com.anagnostou.publisher.objetos.SeisMeses;
import br.com.anagnostou.publisher.utils.L;

import static br.com.anagnostou.publisher.utils.Utilidades.anoDeServico;


public class DBAdapter {
    public DBHelper mydbHelper;
    private HashMap<String, String> mAliasMap;

    public DBAdapter(Context context) {
        mydbHelper = DBHelper.getInstance(context);

        /******************IMPORTANT FOR SEARCH ********************/
        // This HashMap is used to map table fields to Custom Suggestion fields
        mAliasMap = new HashMap<>();
        // Unique id for the each Suggestions ( Mandatory )
        mAliasMap.put("_ID", "_id as _id");
        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, "nome as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        // Icon for Suggestions ( Optional )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_ICON_1, "1  as " + SearchManager.SUGGEST_COLUMN_ICON_1);
        // This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "_id as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);

    }

    /************************
     * SEARCH
     ****************************************/
    public Cursor getPublicadores(String[] selectionArgs) {
        String selection = DBHelper.NOME + " like ? ";
        if (selectionArgs != null) {
            selectionArgs[0] = "%" + selectionArgs[0] + "%";
        }
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);
        queryBuilder.setTables(DBHelper.TN_PUBLICADOR);
        return queryBuilder.query(mydbHelper.getReadableDatabase(),
                new String[]{"_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_ICON_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID},
                selection,
                selectionArgs,
                null,
                null,
                DBHelper.NOME + " asc ", "15"
        );
    }


    public Cursor getPublicador(String id) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DBHelper.TN_PUBLICADOR);
        return queryBuilder.query(mydbHelper.getReadableDatabase(),
                new String[]{DBHelper.UID, DBHelper.NOME, DBHelper.FAMILIA, DBHelper.GRUPO},
                DBHelper.UID + " = ?", new String[]{id}, null, null, null, "1"
        );
    }


    public Cursor getOnePublicador(String id) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DBHelper.TN_PUBLICADOR);
        return queryBuilder.query(mydbHelper.getReadableDatabase(),
                new String[]{DBHelper.NOME},
                DBHelper.UID + " = ?", new String[]{id}, null, null, null, "1"
        );
    }

    /*************************
     * PUBLICADOR
     ***************************/
    public List<String> retrieveAllPublicadores() {
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        String[] columns = {DBHelper.NOME};
        Cursor c = db.query(DBHelper.TN_PUBLICADOR, columns, null, null, null, null, DBHelper.NOME);
        List<String> nomes = new ArrayList<>();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                nomes.add(c.getString(c.getColumnIndex(DBHelper.NOME)));
            }
        } else nomes.add("Sem Nomes");
        c.close();
        return nomes;
    }

    //máximo 12 meses
    public Cursor retrieveRelatorios(String nome) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String orderBy = " ano asc, mes asc ";
        //SeisMeses sm = new SeisMeses();
        DozeMeses sm = new DozeMeses();
        String[] selectionArgs = {nome, sm.getAnoIni(), sm.getMesIni(), sm.getAnoFim(), sm.getMesFim()};
        String condicao = " = ?  AND ((ano = ? AND mes >= ?) OR ( ano = ? AND mes <= ?    ))  ";
        return db.query(DBHelper.TN_RELATORIO, null, DBHelper.NOME + condicao, selectionArgs, null, null, orderBy);
    }

    public Cursor retrieveRelatoriosAnoDeServico(String nome) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String orderBy = " ano asc, mes asc ";
        String[] selectionArgs = {nome, "" + (anoDeServico() - 1), "" + anoDeServico()};
        String condicao = " = ?  AND ((ano = ? AND mes >= 9 ) OR ( ano = ? AND mes <= 8 ))  ";
        return db.query(DBHelper.TN_RELATORIO, null, DBHelper.NOME + condicao, selectionArgs, null, null, orderBy);
    }


    public Cursor retrieveRelatorioSeisMeses(String nome) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String orderBy = " ano desc, mes desc ";
        String[] selectionArgs = {nome};
        //Precisa calcular Ano ini e mes
        return db.query(DBHelper.TN_RELATORIO, null, DBHelper.NOME + " = ? ", selectionArgs, null, null, orderBy);
    }

    public String findFirstPublicador() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.NOME};
        Cursor cursor = db.query(DBHelper.TN_PUBLICADOR, columns, null, null, null, null, null, "1");
        StringBuilder sb = new StringBuilder();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String nome = cursor.getString(cursor.getColumnIndex(DBHelper.NOME));//1
                sb.append(nome).append("\n");
            }
        } else sb.append("No Records");
        cursor.close();
        return sb.toString();
    }

    public boolean checkIfPublisherExists(String name) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.NOME};
        String[] selectionArgs = {name};
        Cursor cursor = db.query(DBHelper.TN_PUBLICADOR, columns, DBHelper.NOME + " = ?", selectionArgs, null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    public String retrieveModalidade(String name) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.PIPU};
        String[] selectionArgs = {name};
        Cursor cursor = db.query(DBHelper.TN_PUBLICADOR, columns, DBHelper.NOME + " = ?", selectionArgs, null, null, null);
        if (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndex(DBHelper.PIPU));
        } else return "Publicador";

    }

    public boolean checkIfReportExists(String ano, String mes, String nome) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.NOME};
        String[] selectionArgs = {ano, mes, nome};
        String criteria = " ano = ? AND mes = ? AND " + DBHelper.NOME + " = ? ";
        Cursor cursor = db.query(DBHelper.TN_RELATORIO, columns, criteria, selectionArgs, null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;

    }


    public Cursor cursorPublicadorPorGrupo(String grupo) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.NOME, DBHelper.FAMILIA};
        String[] selectionArgs = {grupo};
        return db.query(DBHelper.TN_PUBLICADOR, columns, DBHelper.GRUPO + " = ?", selectionArgs, null, null, DBHelper.FAMILIA);
    }

    public Cursor cursorVaroesBatizados() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {"", "M", "Publicador"};
        return db.rawQuery("SELECT " + DBHelper.UID + "," + DBHelper.NOME + "," + DBHelper.FAMILIA + " FROM " +
                DBHelper.TN_PUBLICADOR + " WHERE " + DBHelper.BATISMO + " <> ? AND " + DBHelper.SEXO + " = ? AND " + DBHelper.ANSEPU + " = ?", selectionArgs);

    }

    public Cursor cursorNaoBatizados() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {""};
        return db.rawQuery("SELECT " + DBHelper.UID + ", " + DBHelper.NOME + "," + DBHelper.FAMILIA + " FROM " +
                DBHelper.TN_PUBLICADOR + " WHERE " + DBHelper.BATISMO + " = ? ORDER BY " + DBHelper.NOME, selectionArgs);

    }

    public Cursor irregularesJaneiroDezembro(String ano, String mesini, String mesfim) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {ano, mesini, mesfim};
        return db.rawQuery("SELECT DISTINCT " + DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.UID + ", "
                + DBHelper.TN_RELATORIO + DBHelper.DOT + DBHelper.NOME + ", " +
                DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.FAMILIA +
                " FROM " + DBHelper.TN_RELATORIO + "," + DBHelper.TN_PUBLICADOR +
                " WHERE  " + DBHelper.TN_RELATORIO + DBHelper.DOT + DBHelper.NOME + " = " + DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.NOME + " " +
                " AND " + DBHelper.TN_RELATORIO + DBHelper.DOT + DBHelper.HORAS + " < 1 " +
                " AND " + DBHelper.TN_RELATORIO + DBHelper.DOT + DBHelper.ANO + " = ? " +
                " AND " + DBHelper.TN_RELATORIO + ".mes >= ? " +
                " AND " + DBHelper.TN_RELATORIO + ".mes <= ? " +
                " GROUP BY " + DBHelper.TN_RELATORIO + DBHelper.DOT + DBHelper.NOME +
                " HAVING count(" + DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.UID + ") < 6 ", selectionArgs);
    }

    public Cursor irregularesCruzaAno(String anoini, String mesini, String mesfim, String anofim, String mesini1, String mesfim1) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {anoini, mesini, mesfim, anofim, mesini1, mesfim1};
        return db.rawQuery("SELECT DISTINCT " + DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.UID + ", " +
                DBHelper.TN_RELATORIO + DBHelper.DOT + DBHelper.NOME + ", " +
                DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.FAMILIA +
                " FROM " + DBHelper.TN_RELATORIO + ", " + DBHelper.TN_PUBLICADOR +
                " WHERE  " + DBHelper.TN_RELATORIO + DBHelper.DOT + DBHelper.NOME + " = " + DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.NOME +
                " AND " + DBHelper.TN_RELATORIO + ".horas < 1 " +
                " AND ((" + DBHelper.TN_RELATORIO + ".ano = ? " +
                " AND " + DBHelper.TN_RELATORIO + ".mes >= ? " +
                " AND " + DBHelper.TN_RELATORIO + ".mes <= ?) " +
                " OR (" + DBHelper.TN_RELATORIO + ".ano = ? " +
                " AND " + DBHelper.TN_RELATORIO + ".mes >= ? " +
                " AND " + DBHelper.TN_RELATORIO + ".mes <= ? )) " +
                " GROUP BY " + DBHelper.TN_RELATORIO + DBHelper.DOT + DBHelper.NOME +
                " HAVING count(publicador._id) < 6 ", selectionArgs);
    }


    public Cursor menosDeUmAnoDeBatismo(String anoini, String mesini, String anofim) {
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        String[] selectionArgs = {anoini, mesini, anofim};
        return db.rawQuery("select  " + DBHelper.UID + ", " + DBHelper.NOME + ", " + DBHelper.FAMILIA + " from " +
                DBHelper.TN_PUBLICADOR + " where data_batismo <> '' AND  " +
                "((substr(data_batismo,7,4) = ? AND substr(data_batismo,4,2) >= ? )  OR substr(data_batismo,7,4) = ? ) " +
                "order by " + DBHelper.NOME + "  ", selectionArgs);

    }


    public Cursor cursorPublicadorBusca(String query) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.NOME, DBHelper.FAMILIA};
        query = "%" + query.trim() + "%";
        String[] selectionArgs = {query};
        return db.query(DBHelper.TN_PUBLICADOR, columns, DBHelper.NOME + " LIKE ? ", selectionArgs, null, null, DBHelper.FAMILIA);
    }

    public Cursor cursorPublicadorPorAnsepu(String ansepu) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.NOME, DBHelper.FAMILIA};
        String[] selectionArgs = {ansepu};
        return db.query(DBHelper.TN_PUBLICADOR, columns, DBHelper.ANSEPU + " = ? ", selectionArgs, null, null, DBHelper.NOME);
    }

    public Cursor cursorPioneiroPublicador(String pipu) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.NOME, DBHelper.FAMILIA};
        String[] selectionArgs = {pipu};
        return db.query(DBHelper.TN_PUBLICADOR, columns, DBHelper.PIPU + " = ? ", selectionArgs, null, null, DBHelper.NOME);
    }


    /********
     * 14/12/2016
     *******/
    public Publicador retrievePublisherData(String name) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.FAMILIA, DBHelper.GRUPO, DBHelper.BATISMO, DBHelper.CELULAR,
                DBHelper.RUA, DBHelper.NASCIMENTO, DBHelper.FONE, DBHelper.BAIRRO, DBHelper.ANSEPU, DBHelper.PIPU, DBHelper.SEXO};
        String[] selectionArgs = {name};
        Cursor cursor = db.query(DBHelper.TN_PUBLICADOR, columns, DBHelper.NOME + " = ? ", selectionArgs, null, null, null);
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
            Cursor cursor = db.query(DBHelper.TN_RELATORIO, columns, null, null, null, null, null, "1");
            //check cursor
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int ano = cursor.getInt(cursor.getColumnIndex(DBHelper.ANO));
                    int mes = cursor.getInt(cursor.getColumnIndex(DBHelper.MES));
                    int horas = cursor.getInt(cursor.getColumnIndex(DBHelper.HORAS));
                    String nome = cursor.getString(cursor.getColumnIndex(DBHelper.NOME));
                    sb.append(ano).append(" ").append(mes).append(" ").append(nome).append(" ").append(horas).append("\n");
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


    public Relatorio findRelatorio(String nome, String ano, String mes) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.PUBLICACOES, DBHelper.VIDEOS, DBHelper.HORAS, DBHelper.REVISITAS, DBHelper.ESTUDOS};
        String[] selectionArgs = {nome, ano, mes};
        String str = DBHelper.NOME + " = ? AND " + DBHelper.ANO + " = ?  AND " + DBHelper.MES + " = ? ";
        Cursor cursor = db.query(DBHelper.TN_RELATORIO, columns, str, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            return new Relatorio(Integer.parseInt(ano),
                    Integer.parseInt(mes), nome, "modalidade",
                    cursor.getInt(cursor.getColumnIndex(DBHelper.VIDEOS)),
                    cursor.getInt(cursor.getColumnIndex(DBHelper.HORAS)),
                    cursor.getInt(cursor.getColumnIndex(DBHelper.PUBLICACOES)),
                    cursor.getInt(cursor.getColumnIndex(DBHelper.REVISITAS)),
                    cursor.getInt(cursor.getColumnIndex(DBHelper.ESTUDOS)));
        } else return null;
    }

    public String[] mediasPioneiro(String nome, String anoini, String anofim) {
        String[] resultado = {"n/a", "n/a", "n/a", "n/a", "n/a", "n/a", "n/a"};
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {nome, anoini, anofim};
        Cursor c = db.rawQuery("SELECT COUNT(horas), SUM(horas), AVG(horas), AVG(REVISITAS),AVG(ESTUDOS)," +
                " AVG(videos),AVG(publicacoes) FROM " + DBHelper.TN_RELATORIO + " WHERE NOME = ? AND " +
                " ((Ano = ? and mes >= 9) OR (ano = ? and mes<=8)) GROUP BY nome ", selectionArgs);
        if (c.moveToFirst()) {
            resultado[0] = String.valueOf(c.getInt(0));
            resultado[1] = String.valueOf(c.getInt(1));
            resultado[2] = String.valueOf(c.getDouble(2));
            resultado[3] = String.valueOf(c.getDouble(3));
            resultado[4] = String.valueOf(c.getDouble(4));
            resultado[5] = String.valueOf(c.getDouble(5));
            resultado[6] = String.valueOf(c.getDouble(6));
            c.close();
            return resultado;
        } else {
            resultado[0] = "0";
            resultado[1] = "1";
            resultado[2] = "0";
            resultado[3] = "0";
            resultado[4] = "0";
            resultado[5] = "0";
            resultado[6] = "0";
            c.close();
            return resultado;
        }
    }

    public Cursor cursorPioneiros(String anoini, String anofim) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {anoini, anofim};
        return db.rawQuery("SELECT " + DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.UID + " , " + DBHelper.TN_PUBLICADOR + ".nome ," +
                "  COUNT(" + DBHelper.TN_RELATORIO + ".horas) as meses, SUM(" + DBHelper.TN_RELATORIO + ".horas) as horas, " +
                "AVG(" + DBHelper.TN_RELATORIO + ".horas) as mediahoras, AVG(" + DBHelper.TN_RELATORIO + ".REVISITAS) as mediarevisitas, " +
                "AVG(" + DBHelper.TN_RELATORIO + ".ESTUDOS) as mediaestudos " +
                "FROM " + DBHelper.TN_RELATORIO + "  JOIN " + DBHelper.TN_PUBLICADOR + "  ON " + DBHelper.TN_PUBLICADOR + ".nome = " +
                " " + DBHelper.TN_RELATORIO + ".nome  AND " + DBHelper.TN_PUBLICADOR + ".pipu = 'Pioneiro' " +
                "AND ((Ano = ? and mes >= 9) OR (ano = ? and mes<=8))  GROUP BY " + DBHelper.TN_RELATORIO + ".nome ", selectionArgs);
    }

    // 2017.06.13 colocar só para seis meses
    public String[] somaHorasMeses(String nome) {
        String[] resultado = {"n/a", "n/a", "n/a", "n/a", "n/a", "n/a"};
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        SeisMeses sm = new SeisMeses();
        String[] selectionArgs = {nome, sm.getAnoIni(), sm.getMesIni(), sm.getAnoFim(), sm.getMesFim()};
        Cursor cursor = db.rawQuery("SELECT SUM(HORAS),COUNT(HORAS),AVG(REVISITAS),AVG(ESTUDOS)," +
                "AVG(videos),AVG(publicacoes) FROM " + DBHelper.TN_RELATORIO + " WHERE NOME = ? " +
                "AND ((ano = ? AND mes >= ?) OR (ano = ? AND mes <= ? ))  ", selectionArgs);
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

    //12 meses
    public String[] retrieveTotais(String nome) {
        String[] resultado = {"n/a", "n/a", "n/a", "n/a", "n/a", "n/a"};
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        DozeMeses sm = new DozeMeses();
        String[] selectionArgs = {nome, sm.getAnoIni(), sm.getMesIni(), sm.getAnoFim(), sm.getMesFim()};
        Cursor cursor = db.rawQuery("SELECT COUNT(" + DBHelper.UID + "),SUM(publicacoes),SUM(videos), SUM(HORAS)," +
                "SUM(REVISITAS),SUM(estudos) FROM " + DBHelper.TN_RELATORIO + " WHERE NOME = ?  " +
                " AND ((ano = ? AND mes >= ?) OR (ano = ? AND mes <= ? ))  ", selectionArgs);
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

    //ano de servico
    public String[] retrieveTotaisAnoDeServico(String nome) {
        String[] resultado = {"n/a", "n/a", "n/a", "n/a", "n/a", "n/a", "n/a", "n/a", "n/a", "n/a", "n/a"};
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {nome, "" + (anoDeServico() - 1), "" + anoDeServico()};
        Cursor cursor = db.rawQuery("SELECT COUNT(" + DBHelper.UID + "),SUM(publicacoes),SUM(videos), SUM(HORAS)," +
                "SUM(REVISITAS),SUM(estudos) FROM " + DBHelper.TN_RELATORIO + " WHERE NOME = ?  " +
                " AND ((ano = ? AND mes >= 9) OR (ano = ? AND mes <= 8 ))  ", selectionArgs);
        if (cursor.moveToFirst()) {
            resultado[0] = String.valueOf(cursor.getInt(0));
            resultado[1] = String.valueOf(cursor.getInt(1));
            resultado[2] = String.valueOf(cursor.getInt(2));
            resultado[3] = String.valueOf(cursor.getInt(3));
            resultado[4] = String.valueOf(cursor.getInt(4));
            resultado[5] = String.valueOf(cursor.getInt(5));

            //String.format(Locale.getDefault(), "%.1f", mediahoras)
            //resultado[6] = String.valueOf( (float) cursor.getInt(1)/cursor.getInt(0));
            resultado[6] = String.format(Locale.getDefault(), "%.1f", (float) cursor.getInt(1) / cursor.getInt(0));
            resultado[7] = String.format(Locale.getDefault(), "%.1f", (float) cursor.getInt(2) / cursor.getInt(0));
            resultado[8] = String.format(Locale.getDefault(), "%.1f", (float) cursor.getInt(3) / cursor.getInt(0));
            resultado[9] = String.format(Locale.getDefault(), "%.1f", (float) cursor.getInt(4) / cursor.getInt(0));
            resultado[10] = String.format(Locale.getDefault(), "%.1f", (float) cursor.getInt(5) / cursor.getInt(0));
            cursor.close();
            return resultado;
        } else {
            resultado[0] = "0";
            resultado[1] = "0";
            resultado[2] = "0";
            resultado[3] = "0";
            resultado[4] = "0";
            resultado[5] = "0";
            resultado[6] = "0";
            resultado[7] = "0";
            resultado[8] = "0";
            resultado[9] = "0";
            resultado[10] = "0";
            cursor.close();
            return resultado;
        }
    }

    public String contaPioneiroAuxiliar(String nome) {
        String resultado;
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {nome, "Pioneiro Auxiliar"};
        Cursor cursor = db.rawQuery("SELECT COUNT(nome) FROM " + DBHelper.TN_RELATORIO + " WHERE NOME = ? AND MODALIDADE = ?", selectionArgs);
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
        Cursor cursor = db.rawQuery("SELECT COUNT(nome) FROM " + DBHelper.TN_RELATORIO + " WHERE NOME = ? AND HORAS = ? ", selectionArgs);
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

    public List<String> naoRelatouPorGrupo(String ano, String mes, String grupo) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {ano, mes, grupo};
        List<String> nomes = new ArrayList<>();
        Cursor c = db.rawQuery(
                " SELECT " + DBHelper.TN_PUBLICADOR + ".nome, " + DBHelper.TN_RELATORIO + ".horas " +
                        "FROM " + DBHelper.TN_PUBLICADOR + " " +
                        "LEFT  JOIN " + DBHelper.TN_RELATORIO + " ON " + DBHelper.TN_PUBLICADOR + ".nome = " + DBHelper.TN_RELATORIO + ".nome " +
                        "AND  " + DBHelper.TN_RELATORIO + ".ano = ? AND " + DBHelper.TN_RELATORIO + ".mes = ? " +
                        "WHERE " + DBHelper.TN_PUBLICADOR + ".grupo = ? " +
                        "AND  " + DBHelper.TN_RELATORIO + ".horas is NULL " +
                        "ORDER BY " + DBHelper.TN_RELATORIO + ".horas, " + DBHelper.TN_PUBLICADOR + ".nome "
                , selectionArgs);

        if (c.getCount() > 0) {
            while (c.moveToNext()) {

                nomes.add(c.getString(0));

            }
        } else nomes.add("Todos Relataram");
        c.close();
        return nomes;
    }

    public Cursor naoRelatouMesPassado(String ano, String mes) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] selectionArgs = {ano, mes};
        return db.rawQuery(
                " SELECT " + DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.UID + ", " +
                        DBHelper.TN_PUBLICADOR + ".nome, " +
                        DBHelper.TN_PUBLICADOR + DBHelper.DOT + DBHelper.FAMILIA + ", " + DBHelper.TN_RELATORIO + ".horas " +
                        "FROM " + DBHelper.TN_PUBLICADOR + " " +
                        "LEFT  JOIN " + DBHelper.TN_RELATORIO + " ON " + DBHelper.TN_PUBLICADOR + ".nome = " + DBHelper.TN_RELATORIO + ".nome " +
                        "AND  " + DBHelper.TN_RELATORIO + ".ano = ? AND " + DBHelper.TN_RELATORIO + ".mes = ? " +
                        "WHERE " + DBHelper.TN_RELATORIO + ".horas is NULL " +
                        "ORDER BY " + DBHelper.TN_PUBLICADOR + ".nome "
                , selectionArgs);
    }

    public Cursor fetchAllAssistencia() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        return db.query(DBHelper.TN_ASSISTENCIA, null, null, null, null, null, null);

    }

    public int getMaxIdAssistencia() {
        int maxId = 0;
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("Select max(_id) FROM " + DBHelper.TN_ASSISTENCIA, null);
        if (c.moveToNext()) {
            maxId = c.getInt(0);
        }
        c.close();
        return maxId;
    }

    /*
    SELECT MONTH(assistencia.data) as mes, YEAR(assistencia.data) as ano,
    assistencia.reuniao as reuniao, COUNT(*) as numero, SUM(assistencia.presentes) as total,
    AVG(assistencia.presentes) as media
    FROM assistencia
    GROUP BY MONTH(assistencia.data), assistencia.reuniao
    ORDER BY assistencia.data

     */

    public Cursor fetchGroupedAssistencia() {
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        return db.rawQuery("SELECT strftime('%m',data) as mes, " +
                " substr(data,1,4) as ano, " +
                " reuniao as reuniao, COUNT(*) as numero, " +
                " SUM(presentes) as total, " +
                " AVG(presentes) as media FROM assistencia " +
                " GROUP BY strftime('%Y',data), strftime('%m',data), reuniao " +
                " ORDER BY data DESC", null);

    }

    public String mediaAnualMidWeek(String ini, String fim) {
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        String result;
        String[] selectionArgs = {ini, fim};
        Cursor c = db.rawQuery("SELECT AVG(presentes) FROM " + DBHelper.TN_ASSISTENCIA +
                " WHERE " + DBHelper.REUNIAO + " = 'midweek' AND  data >= ? AND data <= ?  ", selectionArgs);
        if (c.moveToNext()) {
            result = String.format(Locale.getDefault(), "%.1f", c.getFloat(0));
        } else result = "99,9";
        c.close();
        return result;

    }

    public String mediaAnualWeekEnd(String ini, String fim) {
        String result;
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        String[] selectionArgs = {ini, fim};
        Cursor c = db.rawQuery("SELECT AVG(presentes) FROM " + DBHelper.TN_ASSISTENCIA +
                " WHERE " + DBHelper.REUNIAO + " = 'weekend' AND  data >= ? AND data <= ?  ", selectionArgs);
        if (c.moveToNext()) {
            result = String.format(Locale.getDefault(), "%.1f", c.getFloat(0));
        } else result = "99,9";
        c.close();
        return result;
    }

    public String assistenciaDoMes(String reuniao, String data) {
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        String[] selectionArgs = {reuniao, data};
        Cursor c = db.rawQuery("SELECT data, presentes FROM " + DBHelper.TN_ASSISTENCIA +
                " WHERE " + DBHelper.REUNIAO + " = ? AND  substr(data,1,7) = ? ", selectionArgs);
        StringBuilder sb = new StringBuilder();

        while (c.moveToNext()) {
            if (c.getString(1).length() == 3) {
                sb.append(c.getString(0)).append(": ").append(c.getString(1)).append("\n");
            } else sb.append(c.getString(0)).append(":   ").append(c.getString(1)).append("\n");
        }
        c.close();
        return sb.toString();

    }

    public String dataBatismoNascimento(String nome) {
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        String[] selectionArgs = {nome};
        StringBuilder sb = new StringBuilder();
        Cursor c = db.rawQuery("SELECT data_batismo, data_nascimento FROM " + DBHelper.TN_PUBLICADOR +
                " WHERE " + DBHelper.NOME + " = ? ", selectionArgs);

        while (c.moveToNext()) {
            sb.append("Batismo: ").append(c.getString(0)).append("\nNascimento: ").append(c.getString(1));

        }
        c.close();
        return sb.toString();

    }

    public int mediaReunioes() {
        int result;
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("Select avg(presentes) from " + DBHelper.TN_ASSISTENCIA, null);
        if (c.moveToNext()) {
            result = c.getInt(0);
        } else result = 1;
        c.close();
        return result;
    }
    //só para verificar se io grupos existem

    public List<String> retrieveGrupos() {
        SQLiteDatabase db = mydbHelper.getReadableDatabase();
        String[] columns = {DBHelper.GRUPO};
        Cursor c = db.query(DBHelper.TN_GRUPOS, columns, null, null, null, null, DBHelper.GRUPO);
        List<String> grupos = new ArrayList<>();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                //L.m(c.getString(c.getColumnIndex(DBHelper.GRUPO)));
                grupos.add(c.getString(c.getColumnIndex(DBHelper.GRUPO)));
            }
        } else grupos.add("Sem Grupo");
        c.close();
        return grupos;

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
        //  db.close();
        return db.insert(DBHelper.TN_PUBLICADOR, null, cv);
    }

    public long insertDataVersao(String versao) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.ULTIMA_ATUALIZACAO, versao);
        //id of the column or -1 when insert failed
        //db.close();
        return db.insert(DBHelper.TN_VERSAO, null, cv);
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
        //db.close();
        return db.insert(DBHelper.TN_RELATORIO, null, cv);
    }

    public long insertTTRelatorio(String email, String nome, String ano, String mes, String modalidade,
                                  String publicacoes, String videos, String horas, String revisitas,
                                  String estudos, String entregue) {

        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.EMAIL, email);
        cv.put(DBHelper.NOME, nome);
        cv.put(DBHelper.ANO, ano);
        cv.put(DBHelper.MES, mes);
        cv.put(DBHelper.MODALIDADE, modalidade);
        cv.put(DBHelper.VIDEOS, videos);
        cv.put(DBHelper.HORAS, horas);
        cv.put(DBHelper.PUBLICACOES, publicacoes);
        cv.put(DBHelper.REVISITAS, revisitas);
        cv.put(DBHelper.ESTUDOS, estudos);
        cv.put(DBHelper.ENTREGUE, entregue);
        //id of the column or -1 when insert failed
        //db.close();
        return db.insert(DBHelper.TN_TT_RELATORIO, null, cv);
    }


    public long insertDataAssistencia(int id, String data, String reuniao, int presentes) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.UID, id);
        cv.put(DBHelper.DATA, data);
        cv.put(DBHelper.REUNIAO, reuniao);
        cv.put(DBHelper.PRESENTES, presentes);
        return db.insert(DBHelper.TN_ASSISTENCIA, null, cv);
    }

    public long insertDataGrupos(String grupo, int numero, String dirigente, String auxiliar) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.GRUPO, grupo);
        cv.put(DBHelper.NUMERO, numero);
        cv.put(DBHelper.DIRIGENTE, dirigente);
        cv.put(DBHelper.AUXILIAR, auxiliar);
        return db.insert(DBHelper.TN_GRUPOS, null, cv);

    }

    public long insertTTAssistencia(String data, String reuniao, String presentes) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.DATA, data);
        cv.put(DBHelper.REUNIAO, reuniao);
        cv.put(DBHelper.PRESENTES, presentes);
        return db.insert(DBHelper.TN_TT_ASSISTENCIA, null, cv);
    }


    public Cursor retrieveTTRelatorio() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.EMAIL, DBHelper.ANO, DBHelper.MES, DBHelper.NOME,
                DBHelper.MODALIDADE, DBHelper.PUBLICACOES,
                DBHelper.VIDEOS, DBHelper.HORAS, DBHelper.REVISITAS, DBHelper.ESTUDOS, DBHelper.ENTREGUE};
        return db.query(DBHelper.TN_TT_RELATORIO, columns, null, null, null, null, null);

    }

    public Cursor retrieveTTAssistencia() {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String[] columns = {DBHelper.UID, DBHelper.DATA, DBHelper.REUNIAO, DBHelper.PRESENTES};
        return db.query(DBHelper.TN_TT_ASSISTENCIA, columns, null, null, null, null, null);

    }

    public boolean deleteTTRelatorio(String id) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String selection = DBHelper.UID + " = ? ";
        String[] selectionArgs = {id};
        int count = db.delete(DBHelper.TN_TT_RELATORIO, selection, selectionArgs);
        return count > 0;
    }

    public boolean deleteTTAssistencia(String id) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        String selection = DBHelper.UID + " = ? ";
        String[] selectionArgs = {id};
        int count = db.delete(DBHelper.TN_TT_ASSISTENCIA, selection, selectionArgs);
        return count > 0;
    }


    public boolean updateDataRelatorio(Relatorio r) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String ano = String.valueOf(r.getAno());
        String mes = String.valueOf(r.getMes());
        String nome = String.valueOf(r.getNome());
        String selection = DBHelper.ANO + " = ? AND " + DBHelper.MES + " = ? AND " + DBHelper.NOME + " = ? ";
        String[] selectionArgs = {ano, mes, nome};
        cv.put(DBHelper.MODALIDADE, r.getModalidade());
        cv.put(DBHelper.VIDEOS, r.getVideos());
        cv.put(DBHelper.HORAS, r.getHoras());
        cv.put(DBHelper.PUBLICACOES, r.getPublicacoes());
        cv.put(DBHelper.REVISITAS, r.getRevisitas());
        cv.put(DBHelper.ESTUDOS, r.getEstudos());
        int count = db.update(DBHelper.TN_RELATORIO, cv, selection, selectionArgs);
        // db.close();
        return count > 0;
    }

    public boolean updateDataPublicador(Publicador p) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String selection = DBHelper.NOME + " = ? ";
        String[] selectionArgs = {p.getNome()};
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

        int count = db.update(DBHelper.TN_PUBLICADOR, cv, selection, selectionArgs);
        return count > 0;
    }


    /**
     * TESTS, NICE TESTS
     * PRAGMA table_info(table_name);
     */
    public Cursor test1() {
        //CREATE TABLE teste1 ( _id INTEGER PRIMARY KEY AUTOINCREMENT, f1 TEXT)
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        return db.rawQuery("SELECT " + DBHelper.UID + ",  " + DBHelper.FIELD1 + ", " +
                DBHelper.TIMESTAMP + " FROM " + DBHelper.TN_TESTE1, null);
    }

    public Cursor pragma(String table_name) {
        //CREATE TABLE teste1 ( _id INTEGER PRIMARY KEY AUTOINCREMENT, f1 TEXT)
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        return db.rawQuery("PRAGMA table_info(" + table_name + ")", null);
    }

    public long insertTest1(String str) {
        SQLiteDatabase db = mydbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.FIELD1, str);
        return db.insert(DBHelper.TN_TESTE1, null, cv);
    }


    /*************************************
     * SQLiteOpenHelper
     ************************************/
    //public not working
    public static class DBHelper extends SQLiteOpenHelper {

        private static DBHelper sInstance;

        static synchronized DBHelper getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new DBHelper(context.getApplicationContext());
            }
            return sInstance;
        }

        //first step fpr a singleton is to make the constructor private
        private DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }


        static final String DB_NAME = "appledore";
        private static final int DB_VERSION = 8;//13/6/2017


        /**
         * TABLE NAMES
         **/
        public static final String TN_VERSAO = "versao";
        public static final String TN_PUBLICADOR = "publicador";
        public static final String TN_RELATORIO = "relatorio";
        public static final String TN_TT_RELATORIO = "ttrelatorio";
        public static final String TN_TESTE1 = "t1";
        public static final String TN_ASSISTENCIA = "assistencia";
        public static final String TN_TT_ASSISTENCIA = "ttassistencia";
        public static final String TN_GRUPOS = "grupos";


        static final String ANO = "ano";
        static final String ABREPARENTESE = " ( ";
        static final String ANSEPU = "ansepu";
        static final String AUXILIAR = "auxiliar";
        static final String BAIRRO = "bairro";
        static final String BATISMO = "data_batismo";
        static final String CELULAR = "celular";
        static final String DATA = "data";
        static final String DIRIGENTE = "dirigente";
        static final String REUNIAO = "reuniao";
        static final String PRESENTES = "presentes";
        static final String DOT = ".";
        static final String EMAIL = "email";
        static final String ENTREGUE = "entregue";
        static final String ESTUDOS = "estudos";
        static final String FAMILIA = "familia";
        static final String FECHAPARENTESE = " ) ";
        static final String FIELD1 = "f1";
        static final String FONE = "fone";
        static final String GRUPO = "grupo";
        static final String HORAS = "horas";
        static final String MES = "mes";
        static final String MODALIDADE = "modalidade";
        static final String NASCIMENTO = "data_nascimento";
        static final String NOME = "nome";
        static final String NUMERO = "numero";
        static final String PIPU = "pipu";
        static final String PUBLICACOES = "publicacoes";
        static final String REVISITAS = "revisitas";
        static final String RUA = "rua";
        static final String SEXO = "sexo";
        static final String TIMESTAMP = "timestamp";
        static final String UID = "_id";
        static final String ULTIMA_ATUALIZACAO = "data_ultima_atualizacao";
        static final String VIDEOS = "videos";
        static final String VIRGULA = ", ";

        /**
         * TABLE CREATE STATEMENTS
         */
        static final String CREATE_TABLE_TEST1 = "CREATE TABLE " + TN_TESTE1 +
                " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD1 + " TEXT, " + TIMESTAMP +
                " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);";
        private static final String CREATE_TABLE_ASSISTENCIA = "CREATE TABLE "
                + TN_ASSISTENCIA + ABREPARENTESE
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATA + " TEXT, "
                + REUNIAO + " TEXT, "
                + PRESENTES + " INTEGER, "
                + " UNIQUE " + ABREPARENTESE + DATA + VIRGULA + REUNIAO + FECHAPARENTESE + FECHAPARENTESE + ";";

        private static final String CREATE_TABLE_TT_ASSISTENCIA = "CREATE TABLE "
                + TN_TT_ASSISTENCIA + ABREPARENTESE
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATA + " TEXT, "
                + REUNIAO + " TEXT, "
                + PRESENTES + " INTEGER, "
                + " UNIQUE " + ABREPARENTESE + DATA + VIRGULA + REUNIAO + FECHAPARENTESE + FECHAPARENTESE + ";";

        private static final String CREATE_TABLE_RELATORIO = "CREATE TABLE "
                + TN_RELATORIO + ABREPARENTESE
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ANO + " INTEGER, " + MES + " INTEGER, "
                + NOME + " TEXT , " + MODALIDADE + " TEXT, "
                + VIDEOS + " INTEGER, " + HORAS + " INTEGER, " + PUBLICACOES + " INTEGER, "
                + REVISITAS + " INTEGER, " + ESTUDOS + " INTEGER "
                + VIRGULA + " UNIQUE (" + ANO + VIRGULA + MES + VIRGULA + NOME + "));";

        private static final String CREATE_TABLE_TT_RELATORIO = "CREATE TABLE "
                + TN_TT_RELATORIO + ABREPARENTESE
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, "
                + NOME + " TEXT, " + ANO + " TEXT, " + MES + " TEXT, "
                + MODALIDADE + " TEXT, " + PUBLICACOES + " TEXT, "
                + VIDEOS + " TEXT, " + HORAS + " TEXT, "
                + REVISITAS + " TEXT, " + ESTUDOS + " TEXT, entregue TEXT );";

        private static final String CREATE_TABLE_PUBLICADOR = "CREATE TABLE "
                + TN_PUBLICADOR + ABREPARENTESE
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOME + " TEXT UNIQUE, " + FAMILIA + " TEXT, "
                + GRUPO + " TEXT, " + BATISMO + " TEXT, "
                + NASCIMENTO + " TEXT, " + FONE + " TEXT, "
                + CELULAR + " TEXT, " + RUA + " TEXT, "
                + BAIRRO + " TEXT, " + ANSEPU + " TEXT, "
                + PIPU + " TEXT, " + SEXO + " TEXT );";

        private static final String CREATE_TABLE_VERSAO = "CREATE TABLE "
                + TN_VERSAO + ABREPARENTESE + ULTIMA_ATUALIZACAO + " TEXT UNIQUE);";

        static final String CREATE_TABLE_TEST2 = "CREATE TABLE teste2 ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD1 + " TEXT);";

        static final String CREATE_TABLE_GRUPOS = "CREATE TABLE "
                + TN_GRUPOS + ABREPARENTESE
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GRUPO + " TEXT UNIQUE, "
                + NUMERO + " INTEGER, "
                + DIRIGENTE + " TEXT, "
                + AUXILIAR + " TEXT "
                + FECHAPARENTESE + ";";


        // grupo TEXT , numero INTEGER, dirigente TEXT, auxiliar TEXT );";

        /**
         * DROP TABLE STATEMENTS
         **/
        static final String DROP_TABLE_TEST1 = "DROP TABLE IF EXISTS " + TN_TESTE1;
        static final String DROP_TABLE_TEST2 = "DROP TABLE IF EXISTS teste2 ";
        static final String DROP_TABLE_TTRELATORIO = "DROP TABLE IF EXISTS " + TN_TT_RELATORIO;
        static final String DROP_TABLE_RELATORIO = "DROP TABLE IF EXISTS " + TN_RELATORIO;
        static final String DROP_TABLE_PUBLICADOR = "DROP TABLE IF EXISTS " + TN_PUBLICADOR;
        static final String DROP_TABLE_VERSAO = "DROP TABLE IF EXISTS " + TN_VERSAO;
        static final String DROP_TABLE_ASSISTENCIA = "DROP TABLE IF EXISTS " + TN_ASSISTENCIA;
        static final String DROP_TABLE_TT_ASSISTENCIA = "DROP TABLE IF EXISTS " + TN_TT_ASSISTENCIA;
        static final String DROP_TABLE_GRUPOS = "DROP TABLE IF EXISTS " + TN_GRUPOS;

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                //L.m("onUpgrade called, db.getVersion() " + db.getVersion());
                //L.m("oldVersion: " + oldVersion);
                //L.m("newVersion: " + newVersion);
                db.execSQL(DROP_TABLE_PUBLICADOR);
                db.execSQL(DROP_TABLE_RELATORIO);
                db.execSQL(DROP_TABLE_TTRELATORIO);
                db.execSQL(DROP_TABLE_VERSAO);
                db.execSQL(DROP_TABLE_TEST1);
                db.execSQL(DROP_TABLE_ASSISTENCIA);
                db.execSQL(DROP_TABLE_TT_ASSISTENCIA);
                db.execSQL(DROP_TABLE_GRUPOS);
                //L.m("onUpgrade calls onCreate");
                onCreate(db);
            } catch (SQLException e) {
                //L.m(e + "on Upgrade failed");
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                //L.m("onCreate called");
                db.execSQL(CREATE_TABLE_PUBLICADOR);
                db.execSQL(CREATE_TABLE_RELATORIO);
                db.execSQL(CREATE_TABLE_TT_RELATORIO);
                db.execSQL(CREATE_TABLE_VERSAO);
                db.execSQL(CREATE_TABLE_TEST1);
                db.execSQL(CREATE_TABLE_ASSISTENCIA);
                db.execSQL(CREATE_TABLE_TT_ASSISTENCIA);
                db.execSQL(CREATE_TABLE_GRUPOS);

            } catch (SQLException e) {
                //L.m(e + "on Create failed");
            }
        }


        public void dropTablePublicador(SQLiteDatabase db) {
            try {
                db.execSQL(DROP_TABLE_PUBLICADOR);
                db.execSQL(CREATE_TABLE_PUBLICADOR);
            } catch (SQLException ignored) {
            }
        }

        public void dropTableVersao(SQLiteDatabase db) {
            try {
                db.execSQL(DROP_TABLE_VERSAO);
                db.execSQL(CREATE_TABLE_VERSAO);
            } catch (SQLException ignored) {

            }
        }

        public void dropTableRelatorio(SQLiteDatabase db) {
            try {
                db.execSQL(DROP_TABLE_RELATORIO);
                db.execSQL(CREATE_TABLE_RELATORIO);
            } catch (SQLException ignored) {
            }
        }

        public void dropTableTTRelatorio(SQLiteDatabase db) {
            try {

                db.execSQL(DROP_TABLE_TTRELATORIO);
                db.execSQL(CREATE_TABLE_TT_RELATORIO);
            } catch (SQLException ignored) {

            }
        }

        public void dropTableAssistencia(SQLiteDatabase db) {
            try {
                db.execSQL(DROP_TABLE_ASSISTENCIA);
                db.execSQL(CREATE_TABLE_ASSISTENCIA);
            } catch (SQLException ignored) {

            }
        }

        public void dropTableTTAssistencia(SQLiteDatabase db) {
            try {
                db.execSQL(DROP_TABLE_TT_ASSISTENCIA);
                db.execSQL(CREATE_TABLE_TT_ASSISTENCIA);
            } catch (SQLException ignored) {

            }
        }

        public void dropTableGrupos(SQLiteDatabase db) {
            try {
                db.execSQL(DROP_TABLE_GRUPOS);
                db.execSQL(CREATE_TABLE_GRUPOS);
            } catch (SQLException ignored) {

            }
        }

    }
}