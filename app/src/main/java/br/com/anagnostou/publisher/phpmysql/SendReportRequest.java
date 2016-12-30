package br.com.anagnostou.publisher.phpmysql;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class SendReportRequest extends StringRequest {
    //public static String REGISTER_REQUEST_URL = "http://www.anagnostou.com.br/phptut/report_signin_app.php";
    private Map<String, String> params;

    /** Problema com os acentos
     $nome = utf8_decode($nome );

     */

    public SendReportRequest(String email, String url, String nome, String ano, String mes, String modalidade,
                             String publicacoes, String videos,
                             String horas, String revisitas, String estudos, String entregue, Response.Listener<String> listener) {
        super(Method.POST, url, listener, null);
        params = new HashMap<>();
        params.put("email", email);
        params.put("nome", nome);
        params.put("ano", ano);
        params.put("mes", mes);
        params.put("modalidade", modalidade);
        params.put("publicacoes", publicacoes);
        params.put("videos", videos);
        params.put("horas", horas);
        params.put("revisitas", revisitas);
        params.put("estudos", estudos);
        params.put("entregue",entregue);

    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
