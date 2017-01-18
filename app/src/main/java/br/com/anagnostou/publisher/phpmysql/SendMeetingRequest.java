package br.com.anagnostou.publisher.phpmysql;


import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class SendMeetingRequest extends StringRequest {
    //public static String REGISTER_REQUEST_URL = "http://www.anagnostou.com.br/phptut/report_meetings_app.php";
    private Map<String, String> params;


    public SendMeetingRequest(String url, String data, String reuniao, String presentes, Response.Listener<String> listener) {
        super(Method.POST, url, listener, null);
        params = new HashMap<>();
        params.put("data", data);
        params.put("reuniao", reuniao);
        params.put("presentes", presentes);



    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
