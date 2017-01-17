package br.com.anagnostou.publisher.telas;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.adapters.PioneiroAdapter;
import br.com.anagnostou.publisher.objetos.AnoDeServicoDoPioneiro;


public class PioneirosFragment extends Fragment {
    View rootView;
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    Cursor c;
    ArrayList<AnoDeServicoDoPioneiro> pioneirosArray;


    public PioneirosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pioneiros, container, false);
        dbAdapter = new DBAdapter(rootView.getContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        String anoini = "" + (anoDeServico() - 1);
        String anofim = "" + anoDeServico();


        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rvFragmentPioneiros);
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                llm.getOrientation());
        rv.addItemDecoration(mDividerItemDecoration);


        pioneirosArray = new ArrayList<>();
        c = dbAdapter.cursorPioneiros(anoini, anofim);

        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                //_id 0,nome 1,meses2,horas3,mediahoras4, mediarevisitas5, mediaestudos6
                //c AnoDeServicoDoPioneiro( nome, meses, int totalhoras, float mediamensal, float mediarequisito, float revisitas, float estudos)
                pioneirosArray.add(new AnoDeServicoDoPioneiro(
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getFloat(4),
                        mediasRequisito(c.getInt(2), c.getInt(3)),
                        c.getFloat(5),
                        c.getFloat(6)));
            }
        }

        RecyclerView.Adapter adapter = new PioneiroAdapter(pioneirosArray, rootView.getContext());
        rv.setAdapter(adapter);


        return rootView;
    }

    private int anoDeServico() {
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

    public float mediasRequisito(int mes, int soma) {
        int mesesrestantes = 12 - mes;
        return (float) (840 - soma) / mesesrestantes;
    }

}

/*

if (cursor.getCount() > 0) {
            CursorAdapter listAdapter = new SimpleCursorAdapter(rootView.getContext(), R.layout.row_pioneiros,
                    cursor, new String[]{"nome", "familia"}, new int[]{R.id.nameTextView, R.id.familyTextView}, 0);
            pioneirosListView.setAdapter(listAdapter);

        }

        pioneirosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                cursor.moveToPosition(position);
                Intent intent = new Intent(view.getContext(), PioneirosActivity.class);
                intent.putExtra("nome", cursor.getString(cursor.getColumnIndex("nome")));
                startActivity(intent);
            }
        });


 */
/*
if (cursor.getCount() > 0) {
            CursorAdapter listAdapter = new SimpleCursorAdapter(rootView.getContext(), R.layout.row_pioneiros,
                    cursor,
                    new String[]{"nome","meses","horas","mediahoras","mediarevisitas","mediaestudos"},
                    new int[]{R.id.nome, R.id.meses, R.id.horas,R.id.mediahoras,R.id.mediarevisitas,R.id.mediaestudos}, 0);
            pregadoresListView.setAdapter(listAdapter);

        }

        pregadoresListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                cursor.moveToPosition(position);
                Intent intent = new Intent(view.getContext(), PioneirosActivity.class);
                intent.putExtra("nome", cursor.getString(cursor.getColumnIndex("nome")));
                startActivity(intent);
            }
        });



 */