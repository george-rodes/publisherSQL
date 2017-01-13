package br.com.anagnostou.publisher.telas;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.anagnostou.publisher.R;
import br.com.anagnostou.publisher.phpmysql.LoginRequest;
import br.com.anagnostou.publisher.utils.L;
import br.com.anagnostou.publisher.utils.Utilidades;

import static br.com.anagnostou.publisher.MainActivity.SP_AUTHENTICATED;
import static br.com.anagnostou.publisher.MainActivity.SP_SPNAME;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etEmail;
    EditText etPassword;
    Button btnLogin;
    Button btnDesisto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnDesisto = (Button) findViewById(R.id.btnDesisto );
        btnLogin.setOnClickListener(this);
        btnDesisto.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        //L.m("id:" + id);
        if (id == R.id.btnLogin) {
            final String email = etEmail.getText().toString();
            final String password = etPassword.getText().toString();
            if (!email.isEmpty() && !password.isEmpty()) {
                if (Utilidades.isEmailValid(email)) {
                    requestLogin(email, password);
                } else dialogoEmailInvalido();
            } else dialogoCamposVazios();
        } else if (id==R.id.btnDesisto){
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }
    }

    private void requestLogin(final String email, String password) {
        final SharedPreferences sp = getSharedPreferences( SP_SPNAME, MODE_PRIVATE);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arrayJSON = new JSONArray(response);
                    if (arrayJSON.length() > 0) {
                        JSONObject jsonObject = arrayJSON.getJSONObject(0);
                        if (!jsonObject.getString("result").isEmpty()
                                && jsonObject.getString("result").contentEquals("authenticated")) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(SP_AUTHENTICATED, "authenticated" );
                            editor.putString("email", email );
                            editor.apply();
                            voltaParaCasa();
                        }  naoLogouNoServidor (jsonObject.getString("result"));
                    } else dialogoServidor();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
        queue.add(new LoginRequest(email, password, responseListener));
    }

    private void voltaParaCasa() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void dialogoServidor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(R.string.servidor_nao_respondeu);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void naoLogouNoServidor(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(str);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogoEmailInvalido() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(R.string.email_invalido);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void dialogoCamposVazios() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(R.string.falta_email_senha);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
