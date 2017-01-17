package br.com.anagnostou.publisher.telas;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;


public class ListViewFragment extends Fragment {
    View rootView;
    ListView listView;
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;

    public ListViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String pesquisa = getArguments().getString("pesquisa");
        rootView = inflater.inflate(R.layout.fragment_padrao, container, false);
        listView = (ListView) rootView.findViewById(R.id.listView);
        dbAdapter = new DBAdapter(rootView.getContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(Calendar.getInstance().getTime());

        if (pesquisa != null) {
            if (pesquisa.contentEquals("Ancião") || pesquisa.contentEquals("Servo") || pesquisa.contentEquals("Publicador")) {
                cursor = dbAdapter.cursorPublicadorPorAnsepu(pesquisa);
            } else if (pesquisa.contentEquals("NaoBatizados")) {
                cursor = dbAdapter.cursorNaoBatizados();
            } else if (pesquisa.contentEquals("AnoBatismo")) {
                String anoini, anofim, mesini;
                int imesatual;
                imesatual = calendar.get(Calendar.MONTH) + 1;
                mesini = String.format(Locale.getDefault(), "%1$02d", imesatual); //pre
                anofim = Integer.toString(calendar.get(Calendar.YEAR));
                anoini = Integer.toString(calendar.get(Calendar.YEAR) - 1);
                cursor = dbAdapter.menosDeUmAnoDeBatismo(anoini, mesini, anofim);

            } else if (pesquisa.contentEquals("NaoRelataram")) {
                cursor = dbAdapter.naoRelatouMesPassado("" + anoNumero(), "" + mesNumero());
            } else if (pesquisa.contentEquals("Varoes")) {
                cursor = dbAdapter.cursorVaroesBatizados();
            } else if (pesquisa.contentEquals("Irregulares")) {
                String anoini, anofim, mesini, mesfim, mesini1, mesfim1;
                int imesatual;
                imesatual = calendar.get(Calendar.MONTH) + 1;
                if (imesatual >= 7 && imesatual <= 12) {
                    anoini = Integer.toString(calendar.get(Calendar.YEAR)); //2016
                    mesfim = Integer.toString(imesatual - 1); //11
                    mesini = Integer.toString(imesatual - 6); //6
                    cursor = dbAdapter.irregularesJaneiroDezembro(anoini, mesini, mesfim);

                } else if (imesatual == 1) {
                    anoini = Integer.toString(calendar.get(Calendar.YEAR) - 1); //2015
                    mesfim = "12"; //11
                    mesini = "7"; //6
                    cursor = dbAdapter.irregularesJaneiroDezembro(anoini, mesini, mesfim);

                } else if (imesatual >= 2 && imesatual <= 6) {
                    anoini = Integer.toString(calendar.get(Calendar.YEAR) - 1); //2015
                    anofim = Integer.toString(calendar.get(Calendar.YEAR)); //2016
                    int delta = 6 - imesatual;
                    mesini = Integer.toString(12 - delta);
                    mesfim = "12"; //constant
                    mesini1 = "01"; //constant
                    mesfim1 = Integer.toString(imesatual - 1);
                    cursor = dbAdapter.irregularesCruzaAno(anoini, mesini, mesfim, anofim, mesini1, mesfim1);
                }

            } else {
                cursor = dbAdapter.cursorPublicadorPorGrupo(pesquisa);
            }
        }

        if (cursor.getCount() > 0) {
            CursorAdapter listAdapter =
                    new SimpleCursorAdapter(
                            rootView.getContext(),
                            R.layout.row,
                            cursor,
                            new String[]{"nome", "familia"},
                            new int[]{R.id.nameTextView, R.id.familyTextView},
                            0);
            listView.setAdapter(listAdapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                cursor.moveToPosition(position);
                Intent intent = new Intent(view.getContext(), AtividadesActivity.class);
                intent.putExtra("nome", cursor.getString(cursor.getColumnIndex("nome")));
                startActivity(intent);
            }
        });
        return rootView;
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
