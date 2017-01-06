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

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;


public class NaoRelataram extends Fragment {
    View rootView;
    ListView lv;
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;
    View selectedItem;

    public NaoRelataram() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_nao_relataram, container, false);
        lv = (ListView) rootView.findViewById(R.id.naorelatouListView);
        dbAdapter = new DBAdapter(rootView.getContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        cursor = dbAdapter.naoRelatouMesPassado(""+anoNumero(), ""+mesNumero());
        if (cursor.getCount() > 0) {
            CursorAdapter listAdapter = new SimpleCursorAdapter(rootView.getContext(), R.layout.row,
                    cursor, new String[]{"nome", "familia"}, new int[]{R.id.nameTextView, R.id.familyTextView}, 0);
            lv.setAdapter(listAdapter);
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = view;
                view.setSelected(true);
                cursor.moveToPosition(position);
                //L.m(cursor.getString(cursor.getColumnIndex("nome")));
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