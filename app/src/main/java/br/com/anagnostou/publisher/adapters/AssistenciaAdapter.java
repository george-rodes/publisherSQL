package br.com.anagnostou.publisher.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.objetos.Assistencia;


public class AssistenciaAdapter extends RecyclerView.Adapter<AssistenciaAdapter.ItemViewHolder> {
    private List<Assistencia> assistencias;
    private Context context;

    public AssistenciaAdapter(List<Assistencia> assistencias, Context context) {
        this.assistencias = assistencias;
        this.context = context;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_rv_assistencia, viewGroup, false);
        return new ItemViewHolder(v);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView mes, reuniao, numero,presentes,media;
        ImageView seta;
        LinearLayout details;

        ItemViewHolder(View iv) {
            super(iv);
            cardView = (CardView) iv.findViewById(R.id.cvAssistencia);
            mes = (TextView) iv.findViewById(R.id.mes);
            reuniao = (TextView) iv.findViewById(R.id.reuniao);
            numero = (TextView) iv.findViewById(R.id.numero);
            presentes = (TextView) iv.findViewById(R.id.presentes);
            media = (TextView) iv.findViewById(R.id.mediaAssistencia);

            details = (LinearLayout) iv.findViewById(R.id.ll_hide_me_assist);
            details.setVisibility(View.GONE);
            seta = (ImageView) iv.findViewById(R.id.arrowAssist);

        }
    }



    @Override
    public void onBindViewHolder(final ItemViewHolder h, int i) {

        h.details.setVisibility(View.GONE);
        h.seta.setImageResource(R.drawable.ic_keyboard_arrow_down_black_48dp);

        String str1 = assistencias.get(i).getMes() + " " + assistencias.get(i).getAno() ;
        h.mes.setText(str1);
        h.reuniao.setText(assistencias.get(i).getReuniao());
        String str3 = ""+assistencias.get(i).getNumero();
        h.numero.setText(str3);
        String str4 = ""+assistencias.get(i).getTotal();
        h.presentes.setText(str4);
        String str2 = String.format(Locale.getDefault(), "%.1f", assistencias.get(i).getMedia());
        h.media.setText(str2);

        h.seta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (h.details.isShown()) {
                    h.details.setVisibility(View.GONE);
                    h.seta.setImageResource(R.drawable.ic_keyboard_arrow_down_black_48dp);
                } else {
                    h.details.setVisibility(View.VISIBLE);
                    h.seta.setImageResource(R.drawable.ic_keyboard_arrow_up_black_48dp);
                }
            }
        });

        // h.cardView.setOnClickListener(new View.OnClickListener() {
        /*h.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PioneirosActivity.class);
                intent.putExtra("nome", nome);
                context.startActivity(intent);
            }
        });*/

    }

    @Override
    public int getItemCount() {
        return assistencias.size();
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
