package br.com.anagnostou.publisher.phpmysql;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class AssistenciaRequest extends StringRequest {
    private static String ASSISTENCIA_REQUEST_URL = "http://www.anagnostou.com.br/phptut/json_assistencia.php";
    private Map<String, String> params;
//id is the max _id on my local database

    public AssistenciaRequest(String id, Response.Listener<String> listener) {
        super(Method.POST, ASSISTENCIA_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("id", id);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

}
