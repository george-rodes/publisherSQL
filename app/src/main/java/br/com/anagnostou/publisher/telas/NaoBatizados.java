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

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;

public class NaoBatizados extends Fragment {
    View rootView;
    ListView listView;
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;

    public NaoBatizados() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_nao_batizados, container, false);
        listView = (ListView) rootView.findViewById(R.id.naoBatizadosListView);
        dbAdapter = new DBAdapter(rootView.getContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();

        cursor = dbAdapter.cursorNaoBatizados();

        if (cursor.getCount() > 0) {
            CursorAdapter listAdapter = new SimpleCursorAdapter(rootView.getContext(), R.layout.row,
                    cursor, new String[]{"nome", "familia"}, new int[]{R.id.nameTextView, R.id.familyTextView}, 0);
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
}