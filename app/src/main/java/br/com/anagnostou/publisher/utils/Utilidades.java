package br.com.anagnostou.publisher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Spinner;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.MainActivity;

import java.util.StringTokenizer;

public class Utilidades extends AppCompatActivity {

    public static String calculaTempoBatismo(String t) {
        //transformar string em data
        //calcular a diferenca e retorna anos
        Date first;
        Date last = Calendar.getInstance().getTime();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            first = sdf.parse(t);
            Calendar startCalendar = new GregorianCalendar();
            startCalendar.setTime(first);
            Calendar endCalendar = new GregorianCalendar();
            endCalendar.setTime(last);
            int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
            int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
            //fim alternativa

            if (diffYear < 3) {
                return diffMonth + " meses";
            } else {
                return diffYear + " anos";
            }
        } catch (ParseException ex) {
            return "Nao foi possivel calcular";
        }
    }

    public static String trocaFormatoData(String s) {
        Date date;
        SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            date = ymd.parse(s);
            if (s.contentEquals("0000-00-00")) {
                return "";
            } else {
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
            }

        } catch (ParseException e) {
            return "";
        }


    }

    public static String calculaTempoAnos(String t) {
        Date first;
        Date last = Calendar.getInstance().getTime();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            first = sdf.parse(t);
            return String.valueOf(getDiffYears(first, last));
        } catch (ParseException ex) {
            return "Nao foi possivel calcular";
        }
    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }


    public static int randInt(int min, int max) {
        // Usually this can be a field rather than a method variable
        Random rand = new Random();
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }

    public static boolean isOnline(ConnectivityManager connMgr) {
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static String comparaData(String local, String baixada) {
        //transformar string em data
        //calcular a diferenca e retorna anos
        Date dataLocal;
        Date dataBaixada;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dataLocal = sdf.parse(local);
            dataBaixada = sdf.parse(baixada);
            Calendar localCalendar = new GregorianCalendar();
            localCalendar.setTime(dataLocal);
            Calendar baixadaCalendar = new GregorianCalendar();
            baixadaCalendar.setTime(dataBaixada);
            if (baixadaCalendar.get(Calendar.YEAR) - localCalendar.get(Calendar.YEAR) == 0) {
                if (baixadaCalendar.get(Calendar.MONTH) - localCalendar.get(Calendar.MONTH) == 0) {
                    if (baixadaCalendar.get(Calendar.DAY_OF_MONTH) - localCalendar.get(Calendar.DAY_OF_MONTH) == 0) {
                        return "mesma data";
                    } else {
                        return "Dias Diferentes: " + (baixadaCalendar.get(Calendar.DAY_OF_MONTH) - localCalendar.get(Calendar.DAY_OF_MONTH));
                    }
                } else {
                    return "Meses Diferentes: " + (baixadaCalendar.get(Calendar.MONTH) - localCalendar.get(Calendar.MONTH));
                }
            } else {
                return "Anos Diferentes: " + (baixadaCalendar.get(Calendar.YEAR) - localCalendar.get(Calendar.YEAR));
            }
        } catch (ParseException ex) {
            return "Nao foi possivel calcular";
        }
    }

    public static boolean existeTabela(String tabela, Context context) {
        DBAdapter dbAdapter = new DBAdapter(context);
        SQLiteDatabase sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tabela + "' ", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public static boolean temDadosNoBanco(Context context) {
        DBAdapter dbAdapter;
        dbAdapter = new DBAdapter(context);
        //L.m("No Records in RELATORIOS");
        return !dbAdapter.findFirstRelatorio().contentEquals("No Records") && !dbAdapter.findFirstPublicador().contentEquals("No Records");
    }

    public static boolean findLocalFiles(String name) {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, name);
        return file.exists();
    }

    public static void checkPreferencesIntLimitReached(Context context) {
        //I hope we will never need it
        final int LIMIT = 2000000000;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String ttrelatorio_id = sp.getString("ttrelatorio_id", "");
        String ttcadastro_id = sp.getString("ttcadastro_id", "");


        if (!ttrelatorio_id.isEmpty()) {
            if (Integer.parseInt(ttrelatorio_id) > LIMIT) {
                SharedPreferences.Editor spEditor = sp.edit();
                spEditor.putString("ttrelatorio_id", "1");
                spEditor.apply();
            }

            if (!ttcadastro_id.isEmpty()) {
                if (Integer.parseInt(ttcadastro_id) > LIMIT) {
                    SharedPreferences.Editor spEditor = sp.edit();
                    spEditor.putString("ttcadastro_id", "1");
                    spEditor.apply();
                }
            }
        }
    }

    public static void resetPreferencesCounter(MainActivity mainActivity) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putString("ttrelatorio_id", "0");
        spEditor.putString("ttcadastro_id", "0");
        spEditor.apply();
    }

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static int getSpinnerIndex(Spinner spinner, String myString) {
        // USE spMes.setSelection(Utilidades.getSpinnerIndex(spMes, "Março"));
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }
    public static int anoDeServico() {
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

    public static int[] diaMesAno(String data) {
        int[] result = {1, 1, 2016};
        String dia;
        String mes;
        String ano;
        StringTokenizer token = new StringTokenizer(data, "/");

        dia = token.nextToken();
        mes = token.nextToken();
        ano = token.nextToken();

        result[0] = Integer.parseInt(dia);
        result[1] = Integer.parseInt(mes);
        result[2] = Integer.parseInt(ano);
        return result;
    }


    public static boolean validarData(String data) {
        String dia;
        String mes;
        String ano;
        StringTokenizer token = new StringTokenizer(data, "/");
        try {
            dia = token.nextToken();
            mes = token.nextToken();
            ano = token.nextToken();

            if (dia.length() < 1 || dia.length() > 2)
                return false;
            if (mes.length() < 1 || mes.length() > 2)
                return false;
            if (ano.length() != 4)
                return false;

            int intDia = Integer.parseInt(dia);
            int intMes = Integer.parseInt(mes);
            int intAno = Integer.parseInt(ano);
            if (intMes < 1 || intMes > 12)
                return false;

            if (intMes == 1 || intMes == 3 || intMes == 5 || intMes == 7 || intMes == 8 || intMes == 10 || intMes == 12) {
                if (intDia < 1 || intDia > 31) return false;

            } else if (intMes == 4 || intMes == 6 || intMes == 9 || intMes == 11) {
                if (intDia < 1 || intDia > 30) return false;

            } else if (intMes == 2) {
                if (new GregorianCalendar().isLeapYear(intAno)) {
                    if (intDia < 1 || intDia > 29) return false;

                } else {
                    if (intDia < 1 || intDia > 28) return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public static String mesNomeParaNumero(String mes) {
        switch (mes) {
            case "Janeiro":
                return "01";
            case "Fevereiro":
                return "02";

            case "Março":
                return "03";

            case "Abril":
                return "04";

            case "Maio":
                return "05";

            case "Junho":
                return "06";

            case "Julho":
                return "07";

            case "Agosto":
                return "08";

            case "Setembro":
                return "09";

            case "Outubro":
                return "10";

            case "Novembro":
                return "11";

            case "Dezembro":
                return "12";
        }
        return "01";
    }

    public static String mesNumeroParaNome(String mes) {
        switch (mes) {
            case "01":
                return "Janeiro";

            case "02":
                return "Fevereiro";

            case "03":
                return "Março";

            case "04":
                return "Abril";

            case "05":
                return "Maio";

            case "06":
                return "Junho";

            case "07":
                return "Julho";

            case "08":
                return "Agosto";

            case "09":
                return "Setembro";

            case "10":
                return "Outubro";

            case "11":
                return "Novembro";

            case "12":
                return "Dezembro";
        }
        return "São Nunca";
    }

    public static int modulo( int m, int n ){
        int mod =  m % n ;
        return ( mod < 0 ) ? mod + n : mod;
    }



}