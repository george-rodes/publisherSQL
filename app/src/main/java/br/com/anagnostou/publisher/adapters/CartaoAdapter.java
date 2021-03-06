package br.com.anagnostou.publisher.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Relatorio;
import br.com.anagnostou.publisher.telas.AtividadesActivity;
import br.com.anagnostou.publisher.telas.LoginActivity;
import br.com.anagnostou.publisher.telas.RelatorioActivity;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

import static android.content.Context.MODE_PRIVATE;
import static br.com.anagnostou.publisher.MainActivity.DEFAULT;
import static br.com.anagnostou.publisher.MainActivity.SP_AUTHENTICATED;
import static br.com.anagnostou.publisher.MainActivity.SP_SPNAME;

public class CartaoAdapter extends RecyclerView.Adapter<CartaoAdapter.ItemViewHolder> {
    private List<Relatorio> relatorios;
    private Context context;

    public CartaoAdapter(List<Relatorio> relatorios, Context context) {
        this.relatorios = relatorios;
        this.context = context;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView data, publicacoes, horas, videos, revisitas, estudos;

        ItemViewHolder(View iv) {
            super(iv);
            cardView = (CardView) iv.findViewById(R.id.cvRelatorios);
            data = (TextView) iv.findViewById(R.id.data);
            publicacoes = (TextView) iv.findViewById(R.id.publicacoes);
            horas = (TextView) iv.findViewById(R.id.horas);
            videos = (TextView) iv.findViewById(R.id.videos);
            revisitas = (TextView) iv.findViewById(R.id.revisitas);
            estudos = (TextView) iv.findViewById(R.id.estudos);
            //j
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_cartao, viewGroup, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder h, int i) {
        h.data.setText(relatorios.get(i).getMes() + "/" + relatorios.get(i).getAno());
        h.publicacoes.setText(naoMostraZero(relatorios.get(i).getPublicacoes()));
        h.horas.setText(naoMostraZero(relatorios.get(i).getHoras()));
        h.videos.setText(naoMostraZero(relatorios.get(i).getVideos()));
        h.revisitas.setText(naoMostraZero(relatorios.get(i).getRevisitas()));
        h.estudos.setText(naoMostraZero(relatorios.get(i).getEstudos()));
        final String nome = relatorios.get(i).getNome();
        final int ano = relatorios.get(i).getAno();
        final int mes = relatorios.get(i).getMes();
        h.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Modo OFFLINE adicionado
                if (areWeAuthenticated()) {
                    Intent intent = new Intent(context, RelatorioActivity.class);
                    intent.putExtra("Origem", "CartaoAdapter");
                    intent.putExtra("nome", nome);
                    intent.putExtra("ano", ano);
                    intent.putExtra("mes", mes);
                    context.startActivity(intent);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return relatorios.size();
    }

    private String naoMostraZero(int i) {
        if (i == 0) return "";
        else return String.valueOf(i);
    }

    private boolean areWeAuthenticated() {
        SharedPreferences sp = context.getSharedPreferences(SP_SPNAME, MODE_PRIVATE);
        if (!sp.getString(SP_AUTHENTICATED, DEFAULT).equals("authenticated")
                && (Utilidades.isOnline((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)))) {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            return false;
        } else return true;
    }

    private void dialogoNoInternet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.sem_conexao_internet);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
//uuu
}
