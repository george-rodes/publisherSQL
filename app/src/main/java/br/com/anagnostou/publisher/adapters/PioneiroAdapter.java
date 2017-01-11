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
import java.util.Locale;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.AnoDeServicoDoPioneiro;
import br.com.anagnostou.publisher.telas.PioneirosActivity;

public class PioneiroAdapter extends RecyclerView.Adapter<PioneiroAdapter.ItemViewHolder> {
    private List<AnoDeServicoDoPioneiro> pioneiros;
    private Context context;

    public PioneiroAdapter(List<AnoDeServicoDoPioneiro> pioneirosArray, Context context) {
        this.pioneiros = pioneirosArray;
        this.context = context;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nome, meses, horas, mediahoras, mediarequsito;

        ItemViewHolder(View iv) {
            super(iv);
            cardView = (CardView) iv.findViewById(R.id.cvPioneiros);
            nome = (TextView) iv.findViewById(R.id.nome);
            meses = (TextView) iv.findViewById(R.id.meses);
            horas = (TextView) iv.findViewById(R.id.horas);
            mediahoras = (TextView) iv.findViewById(R.id.mediahoras);
            mediarequsito = (TextView) iv.findViewById(R.id.mediarequsito);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_rv_pioneiros, viewGroup, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder h, int i) {
        //nome, meses, int totalhoras, float mediamensal, float mediarequisito, float revisitas, float estudos
        h.nome.setText(pioneiros.get(i).getNome());
        String str1 = "Total de Horas em " + pioneiros.get(i).getMeses() + " meses: ";
        h.meses.setText(str1);
        String str2 = "" + pioneiros.get(i).getTotalhoras();
        h.horas.setText(str2);
        String str3 = context.getString(R.string.media_momento) + String.format(Locale.getDefault(), "%.1f", pioneiros.get(i).getMediamensal());
        h.mediahoras.setText(str3);
        String str4 = context.getString(R.string.media_requsito_ponto) + String.format(Locale.getDefault(), "%.1f", pioneiros.get(i).getMediarequisito());
        h.mediarequsito.setText(str4);
        final String nome = pioneiros.get(i).getNome();
        h.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), PioneirosActivity.class);
                    intent.putExtra("nome", nome);
                    context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pioneiros.size();
    }




}
