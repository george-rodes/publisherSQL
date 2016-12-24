package br.com.anagnostou.publisher.phpmysql;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by George on 23/12/2016.
 */

public class TTrelatorioRequest extends StringRequest {
    public static String REGISTER_REQUEST_URL; //= "http://www.anagnostou.com.br/phptut/json_report.php";
    private Map<String , String > params;

    public TTrelatorioRequest(String username, Response.Listener<String> listener){
        super(Method.POST, REGISTER_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("username", username);
    }

    @Override
    public Map<String ,String> getParams(){
        return params;
    }

}
