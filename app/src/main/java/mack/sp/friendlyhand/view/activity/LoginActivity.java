package mack.sp.friendlyhand.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.adapter.ServiceAdapter;
import mack.sp.friendlyhand.model.Cliente;
import mack.sp.friendlyhand.model.Prestador;
import mack.sp.friendlyhand.model.Servico;
import mack.sp.friendlyhand.tasks.HandlerTask;
import mack.sp.friendlyhand.tasks.TaskRest;
import mack.sp.friendlyhand.util.JsonParser;
import mack.sp.friendlyhand.util.RestUtil;

import static android.text.InputType.TYPE_CLASS_TEXT;

/**
 * Created by Giovana Rodrigues on 19/04/2018.
 */

public class LoginActivity extends SuperClassActivity {
    private EditText editEmail, editSenha;
    private Button btnLogin;
    private RadioButton rbtCliente;
    private LinearLayout linearLayout;

    private Cliente cliente;
    private String stringEmail, stringSenha;
    private final String urlCliente = "http://" + IP + "/friendlyhand/v1/login/cliente?email=";
    private final String urlPrestador = "http://" + IP + "/friendlyhand/v1/login/prestador?email=";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.NoActionBarTheme);
        setContentView(R.layout.activity_login);

        rbtCliente = (RadioButton) findViewById(R.id.rbt_cliente);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editSenha = (EditText) findViewById(R.id.edit_senha);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        Drawable background = linearLayout.getBackground();
        background.setAlpha(215);

        // Tornar a senha visível e trocar o ícone
        editSenha.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Drawables: 0 = Left, 1 = Top, 2 = Right, 3 = Bottom

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editSenha.getRight() - editSenha.getCompoundDrawables()[2].getBounds().width())) {
                        Drawable[] drawables = editSenha.getCompoundDrawables();
                        Bitmap bitmap = ((BitmapDrawable) drawables[2]).getBitmap();
                        Bitmap bitmap2 = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_visibility_white_24dp)).getBitmap();

                        if (bitmap == bitmap2) {
                            editSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_visibility_off_white_24dp, 0);
                            editSenha.setInputType((InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
                        } else {
                            editSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_visibility_white_24dp, 0);
                            editSenha.setInputType(TYPE_CLASS_TEXT);
                        }

                        return true;
                    }
                }
                return false;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarCampos();
            }
        });
    }

    private void validarCampos() {
        stringEmail = editEmail.getText().toString().trim();
        stringSenha = editSenha.getText().toString().trim();

        if (stringEmail.isEmpty() || stringSenha.isEmpty()) {
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.preencha_todos_campos), Toast.LENGTH_LONG).show();
        } else {
            if (rbtCliente.isChecked()) {
                new LogarCliente().execute();
            } else {
                new LogarPrestador().execute();
            }
        }
    }

    // Realizar login do cliente
    public class LogarCliente extends AsyncTask<Void, Void, String> {
        private ProgressDialog progresso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(LoginActivity.this);
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            try {
                retorno = RestUtil.get(urlCliente + editEmail.getText().toString().trim() + "&senha=" + editSenha.getText().toString().trim(), LoginActivity.this);
                Log.e("RETORNO", retorno);
            } catch (IOException | RuntimeException erro) {
                erro.printStackTrace();
            }

            try {
                GsonBuilder formatter = new GsonBuilder();
                formatter.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });

                Gson form = formatter.create();

                if (retorno != null && !retorno.equals("")) {
                    JSONObject usuarioJson = new JSONObject(retorno);
                    cliente = form.fromJson(usuarioJson.toString(), Cliente.class);
                }
                Log.e("CLIENTE", cliente.toString());

            } catch (JSONException | RuntimeException erro) {
                erro.printStackTrace();
            }

            return retorno;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progresso.dismiss();

            if (cliente != null) {
                SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putLong(ID, cliente.getId());
                editor.putString(TIPOUSUARIO, "cliente");
                editor.putString(NOME, cliente.getNome());
                editor.putString(EMAIL, cliente.getEmail());
                editor.putString(SENHA, cliente.getSenha());
                if (cliente.getFoto() != null) {
                    if (!cliente.getFoto().equals("")) {
                        editor.putString(FOTO, cliente.getFoto());
                    }
                }
                editor.commit();

                finish();
                startActivity(new Intent(LoginActivity.this, MainActivityClient.class));

                // Apresentar animação de passagem da esquerda para direita
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            } else {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(LoginActivity.this);
                alertDialog = alerta.create();

                View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(getResources().getString(R.string.erro_login));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                    }
                }, 2500);

                alertDialog.show();
            }
        }
    }

    // Realizar login do cliente
    public class LogarPrestador extends AsyncTask<Void, Void, String> {
        private Prestador prestador;
        private ProgressDialog progresso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(LoginActivity.this);
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            Log.e("AQUI", "Entrou");

            try {
                retorno = RestUtil.get(urlPrestador + editEmail.getText().toString().trim() + "&senha=" + editSenha.getText().toString().trim(), LoginActivity.this);
                Log.e("RETORNO", retorno);
            } catch (IOException | RuntimeException erro) {
                erro.printStackTrace();
            }

            try {
                GsonBuilder formatter = new GsonBuilder();
                formatter.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });

                Gson form = formatter.create();

                if (retorno != null && !retorno.equals("")) {
                    JSONObject usuarioJson = new JSONObject(retorno);
                    prestador = form.fromJson(usuarioJson.toString(), Prestador.class);
                }

            } catch (JSONException | RuntimeException erro) {
                erro.printStackTrace();
            }

            return retorno;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progresso.dismiss();

            if (prestador != null) {
                SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putLong(ID, prestador.getId());
                editor.putString(TIPOUSUARIO, "prestador");
                editor.putString(NOME, prestador.getNome());
                editor.putString(EMAIL, prestador.getEmail());
                editor.putString(SENHA, prestador.getSenha());
                if (prestador.getFoto() != null) {
                    if (!prestador.getFoto().equals("")) {
                        editor.putString(FOTO, prestador.getFoto());
                    }
                }
                editor.commit();

                finish();
                startActivity(new Intent(LoginActivity.this, MainActivityProfessional.class));

                // Apresentar animação de passagem da esquerda para direita
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            } else {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(LoginActivity.this);
                alertDialog = alerta.create();

                View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(getResources().getString(R.string.erro_login));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                    }
                }, 2500);

                alertDialog.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

        startActivity(new Intent(LoginActivity.this, InicialActivity.class));

        // Apresentar animação de passagem da esquerda para direita
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}
