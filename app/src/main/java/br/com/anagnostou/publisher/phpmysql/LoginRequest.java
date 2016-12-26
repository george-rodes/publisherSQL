package br.com.anagnostou.publisher.phpmysql;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;



public class LoginRequest extends StringRequest {
    public static String REGISTER_REQUEST_URL= "http://www.anagnostou.com.br/phptut/report_signin_app.php";
    private Map<String , String > params;

    public LoginRequest(String email,String password, Response.Listener<String> listener){
        super(Method.POST, REGISTER_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
    }

    @Override
    public Map<String ,String> getParams(){
        return params;
    }

}

