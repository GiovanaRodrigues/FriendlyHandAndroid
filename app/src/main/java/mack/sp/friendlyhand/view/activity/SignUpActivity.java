package mack.sp.friendlyhand.view.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import br.com.jansenfelipe.androidmask.MaskEditTextChangedListener;
import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.model.Cliente;
import mack.sp.friendlyhand.model.Endereco;
import mack.sp.friendlyhand.model.Prestador;
import mack.sp.friendlyhand.model.Usuario;
import mack.sp.friendlyhand.tasks.HandlerTask;
import mack.sp.friendlyhand.tasks.HandlerTaskAdapter;
import mack.sp.friendlyhand.tasks.TaskRest;
import mack.sp.friendlyhand.util.JsonParser;
import mack.sp.friendlyhand.util.RestUtil;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static mack.sp.friendlyhand.view.activity.LoginActivity.APP_PREFS;
import static mack.sp.friendlyhand.view.activity.LoginActivity.EMAIL;
import static mack.sp.friendlyhand.view.activity.LoginActivity.SENHA;

/**
 * Created by Giovana Rodrigues on 19/04/2018.
 */

public class SignUpActivity extends SuperClassActivity {
    private EditText editNome, editCpf, editData, editCelular, editEmail, editSenha,
            editCep, editRua, editNumero, editBairro, editCidade, editEstado;
    private RadioButton rbtCliente;
    private Button btnCadastro;

    private Cliente cliente;
    private Prestador prestador;

    private final String urlCliente = "http://" + IP + "/friendlyhand/v1/cliente";
    private final String urlPrestador = "http://" + IP + "/friendlyhand/v1/prestador";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.cadastro));

        // Adicionar botão voltar na ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Iniciar componentes da tela
        iniciarComponentes();

        // Adicionar máscaras aos campos
        adicionarMascaras();

        // Tornar a senha visível e trocar o ícone
        editSenha.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Drawables: 0 = Left, 1 = Top, 2 = Right, 3 = Bottom

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editSenha.getRight() - editSenha.getCompoundDrawables()[2].getBounds().width())) {
                        Drawable[] drawables = editSenha.getCompoundDrawables();
                        Bitmap bitmap = ((BitmapDrawable) drawables[2]).getBitmap();
                        Bitmap bitmap2 = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_visibility_black_24dp)).getBitmap();

                        if (bitmap == bitmap2) {
                            editSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_visibility_off_black_24dp, 0);
                            editSenha.setInputType((InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
                        } else {
                            editSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_visibility_black_24dp, 0);
                            editSenha.setInputType(TYPE_CLASS_TEXT);
                        }

                        return true;
                    }
                }
                return false;
            }
        });

        // Método de pesquisa
        StrictMode.ThreadPolicy politicaDeAcesso = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(politicaDeAcesso);

        editCep.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    buscarCep(editCep.getText().toString().replace("-", ""));
                }
            }
        });

        btnCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarCampos();
            }
        });
    }

    // -------------------------------------------------------------- Métodos da classe
    // Iniciar componentes da tela
    public void iniciarComponentes() {
        // Dados cliente
        rbtCliente = (RadioButton) findViewById(R.id.rbt_cliente);
        editNome = (EditText) findViewById(R.id.edit_nome);
        editCpf = (EditText) findViewById(R.id.edit_cpf);
        editData = (EditText) findViewById(R.id.edit_data);
        editCelular = (EditText) findViewById(R.id.edit_celular);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editSenha = (EditText) findViewById(R.id.edit_senha);

        // Dados endereço
        editCep = (EditText) findViewById(R.id.edit_cep);
        editRua = (EditText) findViewById(R.id.edit_rua);
        editNumero = (EditText) findViewById(R.id.edit_numero);
        editBairro = (EditText) findViewById(R.id.edit_bairro);
        editCidade = (EditText) findViewById(R.id.edit_cidade);
        editEstado = (EditText) findViewById(R.id.edit_estado);

        // Botão de cadastro
        btnCadastro = (Button) findViewById(R.id.btnSignUp);
    }

    // Adicionar máscaras aos campos
    public void adicionarMascaras() {
        MaskEditTextChangedListener maskCpf = new MaskEditTextChangedListener("###.###.###-##", editCpf);
        MaskEditTextChangedListener maskCel = new MaskEditTextChangedListener("(##)#####-####", editCelular);
        MaskEditTextChangedListener maskData = new MaskEditTextChangedListener("##/##/####", editData);
        MaskEditTextChangedListener maskCep = new MaskEditTextChangedListener("#####-###", editCep);

        editCpf.addTextChangedListener(maskCpf);
        editCelular.addTextChangedListener(maskCel);
        editData.addTextChangedListener(maskData);
        editCep.addTextChangedListener(maskCep);
    }

    // Verificar se todos os campos foram preenchidos
    public void validarCampos() {
        if (editNome.getEditableText().toString().trim().isEmpty() || editCpf.getEditableText().toString().trim().isEmpty() ||
                editData.getEditableText().toString().trim().isEmpty() || editCelular.getEditableText().toString().trim().isEmpty() ||
                editEmail.getEditableText().toString().trim().isEmpty() || editSenha.getEditableText().toString().trim().isEmpty() ||
                editCep.getEditableText().toString().trim().isEmpty() || editRua.getEditableText().toString().trim().isEmpty() ||
                editNumero.getEditableText().toString().trim().isEmpty() || editBairro.getEditableText().toString().trim().isEmpty() ||
                editCidade.getEditableText().toString().trim().isEmpty() || editEstado.getEditableText().toString().trim().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.preencha_todos_campos), Toast.LENGTH_LONG).show();
        } else {
            String[] data = editData.getText().toString().split("/");
            int mes = Integer.parseInt(data[1]) - 1;
            Calendar calendarHoje = Calendar.getInstance();
            Calendar calendarServico = Calendar.getInstance();
            calendarServico.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data[0]));
            calendarServico.set(Calendar.MONTH, mes);
            calendarServico.set(Calendar.YEAR, Integer.parseInt(data[2]));

            if (calendarServico.get(Calendar.YEAR) > calendarHoje.get(Calendar.YEAR) - 18) {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(SignUpActivity.this);
                alertDialog = alerta.create();

                View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(Html.fromHtml(getResources().getString(R.string.data_nascimento_invalida)));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();

                        // Focar no campo de data de nascimento
                        editData.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editData, InputMethodManager.SHOW_IMPLICIT);

                    }
                }, 2500);

                alertDialog.show();
            } else {
                if (rbtCliente.isChecked()) {
                    cadastrarCliente();
                } else {
                    cadastrarPrestador();
                }
            }
        }
    }

    // Buscar os dados vis JSON
    public void buscarCep(String numeroCep) {
        String response = makeRequest("http://viacep.com.br/ws/" + numeroCep + "/json/");

        try {
            JSONObject json = new JSONObject(response);
            String logradouro = json.getString("logradouro");
            String bairro = json.getString("bairro");
            String cidade = json.getString("localidade");
            String estado = json.getString("uf");

            editRua.setText(logradouro);
            editBairro.setText(bairro);
            editCidade.setText(cidade);
            editEstado.setText(estado);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Fazer requisição
    private String makeRequest(String enderecoUrl) {
        HttpURLConnection conexao = null;
        URL url;
        String resposta = "";

        try {
            url = new URL(enderecoUrl);
            conexao = (HttpURLConnection) url.openConnection();
            resposta = readStream(conexao.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conexao.disconnect();
        }
        return resposta;
    }

    // Retornar os dados
    private String readStream(InputStream in) {
        BufferedReader leitor = null;
        StringBuilder builder = new StringBuilder(); //Evitar vazamento de memória

        try {
            leitor = new BufferedReader(new InputStreamReader(in));
            String line = null;

            while ((line = leitor.readLine()) != null) {
                builder.append(line + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (leitor != null) {
                try {
                    leitor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return builder.toString();
    }

    // Cadastrar cliente
    public void cadastrarCliente() {
        cliente = new Cliente();
        cliente.setNome(editNome.getText().toString());
        cliente.setCpf(editCpf.getText().toString());
        cliente.setCelular(editCelular.getText().toString());
        cliente.setEmail(editEmail.getText().toString());
        cliente.setSenha(editSenha.getText().toString());
        cliente.setDataNascimento(editData.getText().toString());

        Endereco endereco = new Endereco();

        int cep = Integer.parseInt(editCep.getText().toString().replace("-", ""));
        String rua = editRua.getText().toString();
        int numero = Integer.parseInt(editNumero.getText().toString());
        String bairro = editBairro.getText().toString();
        String cidade = editCidade.getText().toString();
        String estado = editEstado.getText().toString();

        endereco.setCep(cep);
        endereco.setRua(rua);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setUf(estado);

        cliente.setEndereco(endereco);

        JsonParser<Cliente> parser = new JsonParser<>(Cliente.class);
        new TaskRest(TaskRest.RequestMethod.POST, handlerTask, this).execute(urlCliente, parser.fromObject(cliente));
    }

    // Cadastrar prestador
    public void cadastrarPrestador() {
        prestador = new Prestador();
        prestador.setNome(editNome.getText().toString());
        prestador.setCpf(editCpf.getText().toString());
        prestador.setCelular(editCelular.getText().toString());
        prestador.setEmail(editEmail.getText().toString());
        prestador.setSenha(editSenha.getText().toString());
        prestador.setDataNascimento(editData.getText().toString());

        Endereco endereco = new Endereco();

        int cep = Integer.parseInt(editCep.getText().toString().replace("-", ""));
        String rua = editRua.getText().toString();
        int numero = Integer.parseInt(editNumero.getText().toString());
        String bairro = editBairro.getText().toString();
        String cidade = editCidade.getText().toString();
        String estado = editEstado.getText().toString();

        endereco.setCep(cep);
        endereco.setRua(rua);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setUf(estado);

        prestador.setEndereco(endereco);

        JsonParser<Prestador> parser = new JsonParser<>(Prestador.class);
        new TaskRest(TaskRest.RequestMethod.POST, handlerTask, this).execute(urlPrestador, parser.fromObject(prestador));
    }

    // -------------------------------------------------------------- Métodos de persistência de dados
    // Validar Login
    public HandlerTask handlerTask = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            if (rbtCliente.isChecked()) {
                SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(TIPOUSUARIO, "cliente");
                editor.putString(EMAIL, cliente.getEmail());
                editor.putString(SENHA, cliente.getSenha());
                editor.commit();


                finish();
                startActivity(new Intent(SignUpActivity.this, MainActivityClient.class));
                // Apresentar animação de passagem da esquerda para direita
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            } else {
                SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(TIPOUSUARIO, "prestador");
                editor.putString(EMAIL, prestador.getEmail());
                editor.putString(SENHA, prestador.getSenha());
                editor.commit();

                finish();
                startActivity(new Intent(SignUpActivity.this, MainActivityProfessional.class));

                // Apresentar animação de passagem da esquerda para direita
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        }

        @Override
        public void onError(Exception erro) {
            Log.e("ERRO", erro.getMessage());
            if (erro.getMessage().contains("401")) {
                Toast.makeText(SignUpActivity.this, getResources().getString(R.string.erro_cadastro), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SignUpActivity.this, getResources().getString(R.string.erro_conexao), Toast.LENGTH_LONG).show();
            }
        }
    };

    // -------------------------------------------------------------- Métodos nativos

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

        startActivity(new Intent(SignUpActivity.this, InicialActivity.class));

        // Apresentar animação de passagem da esquerda para direita
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    // Finalizar activity quando clicar em voltar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                startActivity(new Intent(SignUpActivity.this, InicialActivity.class));

                // Apresentar animação de passagem de direita para esquerda
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
