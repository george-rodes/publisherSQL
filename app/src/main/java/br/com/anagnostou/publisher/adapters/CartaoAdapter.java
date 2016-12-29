package br.com.anagnostou.publisher.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Relatorio;
import br.com.anagnostou.publisher.telas.RelatorioActivity;
import br.com.anagnostou.publisher.utils.L;

/**
 * Created by George on 04/09/2016.
 */
public class CartaoAdapter extends RecyclerView.Adapter<CartaoAdapter.ItemViewHolder> {
    List<Relatorio> relatorios;
    Context context;

    public CartaoAdapter(List<Relatorio> relatorios, Context context) {
        this.relatorios = relatorios;
        this.context = context;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView data,publicacoes,horas,videos,revisitas,estudos;
        public ItemViewHolder(View iv) {
            super(iv);
            cardView = (CardView) iv.findViewById(R.id.cvRelatorios);
            data = (TextView) iv.findViewById(R.id.data);
            publicacoes = (TextView) iv.findViewById(R.id.publicacoes);
            horas = (TextView) iv.findViewById(R.id.horas);
            videos = (TextView) iv.findViewById(R.id.videos);
            revisitas = (TextView) iv.findViewById(R.id.revisitas);
            estudos = (TextView) iv.findViewById(R.id.estudos);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_cartao, viewGroup, false);
        ItemViewHolder pvh = new ItemViewHolder(v);
        return pvh;
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
                //L.m(nome+"/"+ano+"/"+mes);
                Intent intent = new Intent(context, RelatorioActivity.class);
                intent.putExtra("Origem", "CartaoAdapter");
                intent.putExtra("nome", nome );
                intent.putExtra("ano", ano );
                intent.putExtra("mes", mes );
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return relatorios.size();
    }

    private String naoMostraZero (int i){
        if (i == 0) return ""; else return String.valueOf(i);
   }


}
