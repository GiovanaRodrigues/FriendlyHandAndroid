package mack.sp.friendlyhand.view.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.jansenfelipe.androidmask.MaskEditTextChangedListener;
import de.hdodenhof.circleimageview.CircleImageView;
import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.model.AvaliacaoCliente;
import mack.sp.friendlyhand.model.AvaliacaoPrestador;
import mack.sp.friendlyhand.model.CategoriaServico;
import mack.sp.friendlyhand.model.Prestador;
import mack.sp.friendlyhand.model.Servico;
import mack.sp.friendlyhand.model.Usuario;
import mack.sp.friendlyhand.tasks.HandlerTask;
import mack.sp.friendlyhand.tasks.TaskRest;
import mack.sp.friendlyhand.util.JsonParser;
import mack.sp.friendlyhand.util.RestUtil;

import static mack.sp.friendlyhand.model.CategoriaServico.ENCANAMENTO;
import static mack.sp.friendlyhand.model.CategoriaServico.JARDINAGEM;
import static mack.sp.friendlyhand.model.CategoriaServico.LIMPEZA;
import static mack.sp.friendlyhand.model.CategoriaServico.MARCENARIA;
import static mack.sp.friendlyhand.model.CategoriaServico.PINTURA;
import static mack.sp.friendlyhand.model.CategoriaServico.SERVICOS_ELETRICOS;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.APP_PREFS;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.FOTO;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.ID;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.IP;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.NOME;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.TIPOUSUARIO;

/**
 * Created by Giovana Rodrigues on 23/04/2018.
 */

public class ProfessionalViewServiceActivity extends AppCompatActivity {
    public final static String urlEditServico = "http://" + IP + "/friendlyhand/v1/prestador/";
    public final static String urlDeleteServico = "http://" + IP + "/friendlyhand/v1/servico/";

    private TextView txtTitulo, txtDescricao, txtCategoria, txtPreco, txtPrestador, txtTelefone;
    private CircleImageView imgPerfil;
    private RatingBar rtEstrelas;
    private LinearLayout linearLayout;
    private Button btnConfirmar;

    private Long id;
    private Servico servico;
    private Prestador prestador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_service);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.informacoes_servico));

        // Adicionar botão voltar na ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle b = this.getIntent().getExtras();
        servico = (Servico) b.get("servicoAtual");

        inicializarComponentes();

        // Buscar dados do perfil do prestador
        new BuscarPerfil().execute();

        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        id = prefs.getLong(ID, 0);
        String nomePrestador = prefs.getString(NOME, "");
        String fotoPerfil = prefs.getString(FOTO, "");

        // Mudar fonte do título
        txtTitulo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf"));

        txtTitulo.setText(servico.getNome());
        if (servico.getCategoriaServico() != null) {
            if (servico.getCategoriaServico() == CategoriaServico.SERVICOS_ELETRICOS) {
                txtCategoria.setText(Html.fromHtml(getResources().getString(R.string.categoria, getResources().getString(R.string.servicos_eletricos))));
            } else {
                String categoria = servico.getCategoriaServico().toString().substring(0, 1).toUpperCase() + servico.getCategoriaServico().toString().substring(1).toLowerCase();
                txtCategoria.setText(Html.fromHtml(getResources().getString(R.string.categoria, categoria)));
            }
        }
        txtDescricao.setText(Html.fromHtml(getResources().getString(R.string.descricao, servico.getDescricao())));
        txtPreco.setText(Html.fromHtml(getResources().getString(R.string.valor, String.format("%10.2f", servico.getPreco()))));
        txtPrestador.setText(Html.fromHtml(getResources().getString(R.string.prestador_servico, nomePrestador)));

        if (!fotoPerfil.equals("")) {
            imgPerfil.setImageBitmap(stringToBitmap(fotoPerfil));
        }

        btnConfirmar.setVisibility(View.GONE);
    }

    // --------------------------------------------------------------------- Métodos da classe

    public void inicializarComponentes() {
        txtTitulo = (TextView) findViewById(R.id.txtTitulo);
        txtDescricao = (TextView) findViewById(R.id.txtDescricao);
        txtCategoria = (TextView) findViewById(R.id.txtCategoria);
        txtPreco = (TextView) findViewById(R.id.txtPreco);
        txtPrestador = (TextView) findViewById(R.id.txtPrestador);
        txtTelefone = (TextView) findViewById(R.id.txtTelefone);
        rtEstrelas = (RatingBar) findViewById(R.id.rtEstrelas);
        linearLayout = (LinearLayout) findViewById(R.id.avaliacao);

        imgPerfil = (CircleImageView) findViewById(R.id.img_profile);

        btnConfirmar = (Button) findViewById(R.id.btnConfirmar);
    }

    // Calcular média de avaliações do prestador
    private void calcularMediaAvaliacao() {
        List<AvaliacaoCliente> avaliacoes = prestador.getAvaliacoes();
        if (avaliacoes.size() > 0 && !avaliacoes.isEmpty()) {
            int nota = 0;

            for (int i = 0; i < avaliacoes.size(); i++) {
                AvaliacaoCliente avaliacao = avaliacoes.get(i);
                nota += avaliacao.getNota();
            }

            nota = nota / avaliacoes.size();
            rtEstrelas.setRating(nota);
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

    // Converter string para imagem
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

    //----------------------------------------------------------------------- Métodos nativos

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_service, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Finalizar activity quando clicar em voltar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                // Apresentar animação de passagem de direita para esquerda
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                return true;
            case R.id.edit:
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);

                View alertView = getLayoutInflater().inflate(R.layout.alert_new_service, null);

                final TextView txtTitulo = (TextView) alertView.findViewById(R.id.txtTitulo);
                final EditText editNome = (EditText) alertView.findViewById(R.id.edit_nome);
                final EditText editDescricao = (EditText) alertView.findViewById(R.id.edit_descricao);
                final EditText editValor = (EditText) alertView.findViewById(R.id.edit_valor);
                final Spinner spnCategoria = (Spinner) alertView.findViewById(R.id.spn_categoria);

                editNome.setText(servico.getNome());
                editDescricao.setText(servico.getDescricao());
                editValor.setText(String.valueOf(servico.getPreco()));

                final String[] categorias = {getResources().getString(R.string.limpeza),
                        getResources().getString(R.string.marcenaria), getResources().getString(R.string.servicos_eletricos),
                        getResources().getString(R.string.encanamento), getResources().getString(R.string.jardinagem),
                        getResources().getString(R.string.pintura)};

                ArrayAdapter<String> categoriasAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, categorias);
                categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnCategoria.setAdapter(categoriasAdapter);

                txtTitulo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf"));

                switch (servico.getCategoriaServico()) {
                    case LIMPEZA:
                        spnCategoria.setSelection(0);
                        break;
                    case MARCENARIA:
                        spnCategoria.setSelection(1);
                        break;
                    case SERVICOS_ELETRICOS:
                        spnCategoria.setSelection(2);
                        break;
                    case ENCANAMENTO:
                        spnCategoria.setSelection(3);
                        break;
                    case JARDINAGEM:
                        spnCategoria.setSelection(4);
                        break;
                    case PINTURA:
                        spnCategoria.setSelection(5);
                }

                alerta.setView(alertView);

                alerta.setPositiveButton(getResources().getString(R.string.confirmar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editNome.getEditableText().toString().trim().isEmpty() || editDescricao.getEditableText().toString().trim().isEmpty() || editValor.getEditableText().toString().trim().isEmpty()) {
                            Toast.makeText(ProfessionalViewServiceActivity.this, getResources().getString(R.string.preencha_todos_campos), Toast.LENGTH_LONG).show();
                        } else {
                            Servico novoServico = new Servico();
                            novoServico.setNome(editNome.getText().toString());
                            novoServico.setDescricao(editDescricao.getText().toString());
                            novoServico.setPreco(Double.parseDouble(editValor.getText().toString()));
                            switch (spnCategoria.getSelectedItemPosition()) {
                                case 0:
                                    novoServico.setCategoriaServico(LIMPEZA);
                                    break;
                                case 1:
                                    novoServico.setCategoriaServico(MARCENARIA);
                                    break;
                                case 2:
                                    novoServico.setCategoriaServico(SERVICOS_ELETRICOS);
                                    break;
                                case 3:
                                    novoServico.setCategoriaServico(ENCANAMENTO);
                                    break;
                                case 4:
                                    novoServico.setCategoriaServico(JARDINAGEM);
                                    break;
                                case 5:
                                    novoServico.setCategoriaServico(PINTURA);
                                    break;
                            }

                            JsonParser<Servico> parser = new JsonParser<>(Servico.class);
                            new TaskRest(TaskRest.RequestMethod.PUT, handlerTaskEdit, ProfessionalViewServiceActivity.this).execute(urlEditServico + id + "/servico/" + servico.getId(), parser.fromObject(novoServico));
                        }
                    }
                });

                alerta.setNegativeButton(getResources().getString(R.string.cancelar), null);

                alerta.show();

                return true;
            case R.id.delete:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.excluir_servico);
                alert.setMessage(R.string.excluir_servico_mensagem);

                alert.setPositiveButton(getResources().getString(R.string.sim), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new TaskRest(TaskRest.RequestMethod.DELETE, handlerTaskDelete, ProfessionalViewServiceActivity.this).execute(urlDeleteServico + servico.getId());
                    }
                });

                alert.setNegativeButton(getResources().getString(R.string.nao), null);

                alert.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();

        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    // ----------------------------------------------------------- Métodos de persistência de dados

    // Buscar programas pendentes
    public class BuscarPerfil extends AsyncTask<Void, Void, String> {
        private ProgressDialog progresso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(ProfessionalViewServiceActivity.this);
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            try {
                retorno = RestUtil.get(urlEditServico + id, ProfessionalViewServiceActivity.this);
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
                txtTelefone.setText(Html.fromHtml(getResources().getString(R.string.telefone, prestador.getCelular())));
                calcularMediaAvaliacao();
            } else {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalViewServiceActivity.this);
                alertDialog = alerta.create();

                View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(getResources().getString(R.string.erro_dados));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                        finish();

                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                }, 2500);

                alertDialog.show();
            }
        }
    }

    // Editar serviço
    public HandlerTask handlerTaskEdit = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalViewServiceActivity.this);
            alertDialog = alerta.create();

            View alertView = getLayoutInflater().inflate(R.layout.alert_success, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.sucesso_editar_servico));

            alertDialog.setView(alertView);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertDialog.dismiss();

                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
            }, 1500);

            alertDialog.show();
        }

        @Override
        public void onError(Exception erro) {
            Log.e("ERRO", erro.getMessage());
            if (erro.getMessage().contains("401")) {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalViewServiceActivity.this);
                alertDialog = alerta.create();

                View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(getResources().getString(R.string.erro_editar_servico));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();

                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                }, 1500);

                alertDialog.show();
            } else if (erro.getMessage().contains("200")) {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalViewServiceActivity.this);
                alertDialog = alerta.create();

                View alertView = getLayoutInflater().inflate(R.layout.alert_success, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(getResources().getString(R.string.sucesso_editar_servico));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();

                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                }, 1500);

                alertDialog.show();
            } else {
                Toast.makeText(ProfessionalViewServiceActivity.this, getResources().getString(R.string.erro_conexao), Toast.LENGTH_LONG).show();
            }
        }
    };

    // Deletar serviço
    public HandlerTask handlerTaskDelete = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalViewServiceActivity.this);
            alertDialog = alerta.create();

            View alertView = getLayoutInflater().inflate(R.layout.alert_success, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.sucesso_deletar_servico));

            alertDialog.setView(alertView);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertDialog.dismiss();

                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
            }, 1500);

            alertDialog.show();
        }

        @Override
        public void onError(Exception erro) {
            Log.e("ERRO", erro.getMessage());
            if (erro.getMessage().contains("401")) {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalViewServiceActivity.this);
                alertDialog = alerta.create();

                View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(getResources().getString(R.string.erro_deletar_servico));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();

                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                }, 1500);

                alertDialog.show();
            } else if (erro.getMessage().contains("200")) {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalViewServiceActivity.this);
                alertDialog = alerta.create();

                View alertView = getLayoutInflater().inflate(R.layout.alert_success, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(getResources().getString(R.string.sucesso_deletar_servico));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();

                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                }, 1500);

                alertDialog.show();
            } else {
                Toast.makeText(ProfessionalViewServiceActivity.this, getResources().getString(R.string.erro_conexao), Toast.LENGTH_LONG).show();
            }
        }
    };
}
