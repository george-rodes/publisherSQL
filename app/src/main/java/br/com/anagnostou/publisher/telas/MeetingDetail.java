package br.com.anagnostou.publisher.telas;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import br.com.anagnostou.publisher.DBAdapter;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.utils.Utilidades;

public class MeetingDetail extends AppCompatDialogFragment implements View.OnClickListener {
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    View view;
    Button button;
    String reuniao, data;
    TextView assistencia,assistenciaLabel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = getArguments().getString("ano") + "-" + Utilidades.mesNomeParaNumero(getArguments().getString("mes"));

        reuniao = getArguments().getString("reuniao");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate( R.layout.dialog_meeting_detail,container,false );
        assistencia = (TextView) view.findViewById(R.id.assistencia);
        assistenciaLabel = (TextView) view.findViewById(R.id.assistenciaLabel);
        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);
        dbAdapter = new DBAdapter(view.getContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getWritableDatabase();
        preencheCampos();
        return view;
    }


    @Override
    public void onClick(View view) {
        dismiss();
    }

    private String traducaoReuniao(String str) {
        if (str.contentEquals("Reunião durante a semana")) {
            return "midweek";
        } else return "weekend";

    }

    private void preencheCampos(){
        assistenciaLabel.setText(reuniao);
        assistencia.setText(dbAdapter.assistenciaDoMes(traducaoReuniao(reuniao), data));

    }

}
