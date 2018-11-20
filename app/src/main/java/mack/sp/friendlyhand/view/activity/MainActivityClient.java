package mack.sp.friendlyhand.view.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.model.Cliente;
import mack.sp.friendlyhand.model.Prestador;
import mack.sp.friendlyhand.util.RestUtil;
import mack.sp.friendlyhand.view.fragment.ClientConfirmedFragment;
import mack.sp.friendlyhand.view.fragment.ClientHistoryFragment;
import mack.sp.friendlyhand.view.fragment.ClientRequestedFragment;
import mack.sp.friendlyhand.view.fragment.ProfileFragment;
import mack.sp.friendlyhand.view.fragment.ClientSearchFragment;

import static mack.sp.friendlyhand.view.activity.LoginActivity.APP_PREFS;
import static mack.sp.friendlyhand.view.activity.LoginActivity.EMAIL;
import static mack.sp.friendlyhand.view.activity.LoginActivity.SENHA;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.FOTO;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.ID;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.IP;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.NOME;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.TIPOUSUARIO;

/**
 * Created by Giovana Rodrigues on 19/04/2018.
 */

public class MainActivityClient extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private Cliente cliente;

    private final String urlCliente = "http://" + IP + "/friendlyhand/v1/login/cliente?email=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_client);

        createActionBar();
        new LogarCliente().execute();
    }

    // Clicar no botão de "hambúrguer" e abrir o menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Criando o menu lateral
    public void createActionBar() {
        navigationView = (NavigationView) findViewById(R.id.nav_menu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                updateActionBar();
                invalidateOptionsMenu();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Colocar botão de menu
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Colocar nome no header
        View headerView = navigationView.getHeaderView(0);
        TextView navNome = (TextView) headerView.findViewById(R.id.txtNome);
        TextView navEmail = (TextView) headerView.findViewById(R.id.txtEmail);
        ImageView navImg = (ImageView) headerView.findViewById(R.id.imgPerfil);

        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        String nome = prefs.getString(NOME, "");
        String email = prefs.getString(EMAIL, "");
        String foto = prefs.getString(FOTO, "");

        navNome.setText(nome);
        navEmail.setText(email);
        if (!foto.equals("")) {
            navImg.setImageBitmap(stringToBitmap(foto));
        }

        // Acionar o fragment de busca de serviços
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.getMenu().getItem(1).setChecked(true);
        navigationView.getMenu().getItem(2).setChecked(false);
        navigationView.getMenu().getItem(3).setChecked(false);
        navigationView.getMenu().getItem(4).setChecked(false);
        drawerLayout.closeDrawers();
        getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new ClientSearchFragment()).commit();

        // Mudar página ao clicar no item
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_perfil:
                        navigationView.getMenu().getItem(0).setChecked(true);
                        navigationView.getMenu().getItem(1).setChecked(false);
                        navigationView.getMenu().getItem(2).setChecked(false);
                        navigationView.getMenu().getItem(3).setChecked(false);
                        navigationView.getMenu().getItem(4).setChecked(false);
                        drawerLayout.closeDrawers();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_main, new ProfileFragment()).commit();
                        return true;

                    case R.id.nav_buscar:
                        navigationView.getMenu().getItem(0).setChecked(false);
                        navigationView.getMenu().getItem(1).setChecked(true);
                        navigationView.getMenu().getItem(2).setChecked(false);
                        navigationView.getMenu().getItem(3).setChecked(false);
                        navigationView.getMenu().getItem(4).setChecked(false);
                        drawerLayout.closeDrawers();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_main, new ClientSearchFragment()).commit();
                        return true;

                    case R.id.nav_solicitados:
                        navigationView.getMenu().getItem(0).setChecked(false);
                        navigationView.getMenu().getItem(1).setChecked(false);
                        navigationView.getMenu().getItem(2).setChecked(true);
                        navigationView.getMenu().getItem(3).setChecked(false);
                        navigationView.getMenu().getItem(4).setChecked(false);

                        drawerLayout.closeDrawers();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_main, new ClientRequestedFragment()).commit();
                        return true;

                    case R.id.nav_confirmados:
                        navigationView.getMenu().getItem(0).setChecked(false);
                        navigationView.getMenu().getItem(1).setChecked(false);
                        navigationView.getMenu().getItem(2).setChecked(false);
                        navigationView.getMenu().getItem(3).setChecked(true);
                        navigationView.getMenu().getItem(4).setChecked(false);
                        drawerLayout.closeDrawers();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_main, new ClientConfirmedFragment()).commit();
                        return true;

                    case R.id.nav_historico:
                        navigationView.getMenu().getItem(0).setChecked(false);
                        navigationView.getMenu().getItem(1).setChecked(false);
                        navigationView.getMenu().getItem(2).setChecked(false);
                        navigationView.getMenu().getItem(3).setChecked(false);
                        navigationView.getMenu().getItem(4).setChecked(true);

                        drawerLayout.closeDrawers();
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_main, new ClientHistoryFragment()).commit();
                        return true;
                    case R.id.nav_logout:
                        AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivityClient.this);
                        alerta.setTitle(R.string.sair);
                        alerta.setMessage(R.string.mensagem);

                        alerta.setPositiveButton(getResources().getString(R.string.sim), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Validar o logout
                                SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();

                                editor.putLong(ID, 0);
                                editor.putString(NOME, "");
                                editor.putString(EMAIL, "");
                                editor.putString(SENHA, "");
                                editor.putString(FOTO, "");
                                editor.commit();

                                Intent resultado = new Intent(MainActivityClient.this, InicialActivity.class);
                                resultado.putExtra("saiu", true);
                                startActivity(resultado);

                                dialog.dismiss();
                                finish();

                                // Apresentar animação de passagem da direita para esquerda
                                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            }
                        });

                        alerta.setNegativeButton(getResources().getString(R.string.nao), null);

                        alerta.show();

                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    public void updateActionBar() {
        // Colocar nome no header
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView navNome = (TextView) headerView.findViewById(R.id.txtNome);
            TextView navEmail = (TextView) headerView.findViewById(R.id.txtEmail);
            ImageView navImg = (ImageView) headerView.findViewById(R.id.imgPerfil);

            SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
            String nome = prefs.getString(NOME, "");
            String email = prefs.getString(EMAIL, "");
            String foto = prefs.getString(FOTO, "");

            navNome.setText(nome);
            navEmail.setText(email);
            if (!foto.equals("")) {
                navImg.setImageBitmap(stringToBitmap(foto));
            }
        }
    }

    private Bitmap stringToBitmap(String stringFoto) {
        Bitmap foto = null;

        try {
            byte[] encodeByte = Base64.decode(stringFoto, Base64.DEFAULT);
            foto = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
        }

        return foto;
    }

    // -------------------------------------------------- Métodos de busca de dados
    // Realizar login do cliente
    public class LogarCliente extends AsyncTask<Void, Void, String> {
        private ProgressDialog progresso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(MainActivityClient.this);
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
            String email = prefs.getString(EMAIL, "");
            String senha = prefs.getString(SENHA, "");

            Log.e("USUARIO", email + " - " + senha);

            try {
                retorno = RestUtil.get(urlCliente + email + "&senha=" + senha, MainActivityClient.this);
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
                editor.putString(FOTO, cliente.getFoto());
                editor.commit();

                // Atualizar menu lateral
                View headerView = navigationView.getHeaderView(0);
                TextView navNome = (TextView) headerView.findViewById(R.id.txtNome);
                TextView navEmail = (TextView) headerView.findViewById(R.id.txtEmail);
                ImageView navImg = (ImageView) headerView.findViewById(R.id.imgPerfil);

                navNome.setText(cliente.getNome());
                navEmail.setText(cliente.getEmail());
                if (cliente.getFoto() != null && !cliente.getFoto().equals("")) {
                    navImg.setImageBitmap(stringToBitmap(cliente.getFoto()));
                }

            } else {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivityClient.this);
                alertDialog = alerta.create();

                View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(getResources().getString(R.string.erro_login));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();

                        // Validar o logout
                        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putLong(ID, 0);
                        editor.putString(NOME, "");
                        editor.putString(EMAIL, "");
                        editor.putString(SENHA, "");
                        editor.putString(FOTO, "");
                        editor.commit();

                        finish();
                        startActivity(new Intent(MainActivityClient.this, InicialActivity.class));

                        // Apresentar animação de passagem da esquerda para direita
                        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    }
                }, 2500);

                alertDialog.show();
            }
        }
    }
}
