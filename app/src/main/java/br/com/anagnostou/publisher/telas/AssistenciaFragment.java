package br.com.anagnostou.publisher.telas;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.adapters.AssistenciaAdapter;
import br.com.anagnostou.publisher.adapters.PioneiroAdapter;
import br.com.anagnostou.publisher.objetos.AnoDeServicoDoPioneiro;
import br.com.anagnostou.publisher.objetos.Assistencia;
import br.com.anagnostou.publisher.utils.L;


/**
 * Created by George on 16/01/2017.
 */

public class AssistenciaFragment extends Fragment {
    private int mes, ano, numero, total;
    private float media;
    private String reuniao;

    View rootView;
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    Cursor c;
    ArrayList<Assistencia> assistenciasArray;

    public AssistenciaFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_assistencia, container, false);
        dbAdapter = new DBAdapter(rootView.getContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        String anoini = "" + (anoDeServico() - 1);
        String anofim = "" + anoDeServico();


        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rvAssistencia);
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                llm.getOrientation());
        rv.addItemDecoration(mDividerItemDecoration);
        assistenciasArray = new ArrayList<>();
        c = dbAdapter.fetchGroupedAssistencia();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                assistenciasArray.add(new Assistencia(c.getString(0), c.getString(1), c.getString(2), c.getInt(3), c.getInt(4), c.getFloat(5)));
            }
        }
        RecyclerView.Adapter adapter = new AssistenciaAdapter(assistenciasArray, rootView.getContext());
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
        //comecar a considerar o novo ano de servico em Outubro
        if (mes > 9) return ano + 1;
        else return ano;
    }

}
