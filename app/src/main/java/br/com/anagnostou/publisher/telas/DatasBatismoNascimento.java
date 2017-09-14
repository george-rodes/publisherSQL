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

/**
 * Created by George on 07/09/2017.
 */

public class DatasBatismoNascimento extends AppCompatDialogFragment implements View.OnClickListener {
    View view;
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;
    Button button;
    String nome;
    TextView datas;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nome = getArguments().getString("nome");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_detalhe_batismo, container, false);
        datas = (TextView) view.findViewById(R.id.datas);
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

    private void preencheCampos(){
        //datas.setText("Batismo de " + nome);
        datas.setText(dbAdapter.dataBatismoNascimento(nome));
    }

}
