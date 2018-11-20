package mack.sp.friendlyhand.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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

import de.hdodenhof.circleimageview.CircleImageView;
import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.model.AvaliacaoCliente;
import mack.sp.friendlyhand.model.AvaliacaoPrestador;
import mack.sp.friendlyhand.model.CategoriaServico;
import mack.sp.friendlyhand.model.Cliente;
import mack.sp.friendlyhand.model.Prestador;
import mack.sp.friendlyhand.model.Servico;
import mack.sp.friendlyhand.model.ServicoContratado;
import mack.sp.friendlyhand.model.Usuario;
import mack.sp.friendlyhand.tasks.HandlerTask;
import mack.sp.friendlyhand.tasks.TaskRest;
import mack.sp.friendlyhand.util.JsonParser;
import mack.sp.friendlyhand.util.RestUtil;

import static mack.sp.friendlyhand.view.activity.SuperClassActivity.APP_PREFS;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.ID;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.IP;

/**
 * Created by Giovana Rodrigues on 23/04/2018.
 */

public class ProfessionalRequestedActivity extends SuperClassActivity {
    private final String urlSolicitarServico = "http://" + IP + "/friendlyhand/v1/servicocontratado";
    public final static String urlServico = "http://" + IP + "/friendlyhand/v1/prestador/";
    public final static String urlCliente = "http://" + IP + "/friendlyhand/v1/cliente/";

    private TextView txtTitulo, txtDescricao, txtCategoria, txtPreco, txtData, txtNomeCliente, txtTelefone, txtRua, txtBairro, txtCidade;
    private RatingBar rtEstrelas;
    private Button btnConfirmar;
    private CircleImageView imgPerfil;
    private LinearLayout linearLayout;

    private ServicoContratado servico;
    private Cliente cliente;
    private Prestador prestador;
    private Long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_requested);

        Bundle b = this.getIntent().getExtras();
        servico = (ServicoContratado) b.get("servicoAtual");
        String acao = (String) b.get("acao");

        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        id = prefs.getLong(ID, 0);

        ActionBar actionBar = getSupportActionBar();
        if (!servico.isConfirmado()) {
            actionBar.setTitle(getResources().getString(R.string.confirmar_servico));
        } else if (!servico.isConcluido()) {
            actionBar.setTitle(getResources().getString(R.string.concluir_servico));
        }

        // Adicionar botão voltar na ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        inicializarComponentes();

        // Mudar fonte do título
        txtTitulo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf"));

        // Buscar dados do serviço
        new BuscarServico().execute();

        // Buscar dados do cliente
        new BuscarCliente().execute();

        if (acao.equalsIgnoreCase("confirmar")) {
            if (!servico.isConfirmado()) {
                btnConfirmar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        servico.setConfirmado(true);
                        JsonParser<ServicoContratado> parser = new JsonParser<>(ServicoContratado.class);
                        new TaskRest(TaskRest.RequestMethod.PUT, handlerTaskConfirmar, ProfessionalRequestedActivity.this).execute(urlSolicitarServico, parser.fromObject(servico));
                    }
                });
            } else {
                btnConfirmar.setVisibility(View.GONE);
            }

        } else if (acao.equalsIgnoreCase("concluir")) {
            if (!servico.isConcluido()) {
                btnConfirmar.setText(R.string.concluir_servico);
                btnConfirmar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        servico.setConcluido(true);
                        JsonParser<ServicoContratado> parser = new JsonParser<>(ServicoContratado.class);
                        new TaskRest(TaskRest.RequestMethod.PUT, handlerTaskConcluir, ProfessionalRequestedActivity.this).execute(urlSolicitarServico, parser.fromObject(servico));
                    }
                });
            } else {
                btnConfirmar.setVisibility(View.GONE);
            }
        }
    }

    // --------------------------------------------------------- Métodos da classe

    public void inicializarComponentes() {
        txtTitulo = (TextView) findViewById(R.id.txtTitulo);
        txtDescricao = (TextView) findViewById(R.id.txtDescricao);
        txtCategoria = (TextView) findViewById(R.id.txtCategoria);
        txtPreco = (TextView) findViewById(R.id.txtPreco);
        txtNomeCliente = (TextView) findViewById(R.id.txtNomeCliente);
        txtTelefone = (TextView) findViewById(R.id.txtTelefone);
        txtData = (TextView) findViewById(R.id.txtData);
        txtRua = (TextView) findViewById(R.id.txtRua);
        txtBairro = (TextView) findViewById(R.id.txtBairro);
        txtCidade = (TextView) findViewById(R.id.txtCidade);
        rtEstrelas = (RatingBar) findViewById(R.id.rtEstrelas);

        imgPerfil = (CircleImageView) findViewById(R.id.img_profile);

        linearLayout = (LinearLayout) findViewById(R.id.avaliacao);

        btnConfirmar = (Button) findViewById(R.id.btnConfirmar);
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

    // Calcular média das avaliações do cliente
    private void calcularMediaAvaliacao() {
        List<AvaliacaoPrestador> avaliacoes = cliente.getAvaliacoes();
        if (avaliacoes.size() > 0 || !avaliacoes.isEmpty()) {
            int nota = 0;

            for (int i = 0; i < avaliacoes.size(); i++) {
                AvaliacaoPrestador avaliacao = avaliacoes.get(i);
                nota += avaliacao.getNota();
            }

            nota = nota / avaliacoes.size();
            rtEstrelas.setRating(nota);
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

    // -------------------------------------------------------- Métodos nativos
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();

        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
        }
        return super.onOptionsItemSelected(item);
    }

    // -------------------------------------------------------------- Métodos de persistência

    // Buscar dados do serviço
    public class BuscarServico extends AsyncTask<Void, Void, String> {
        private ProgressDialog progresso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(ProfessionalRequestedActivity.this);
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            try {
                retorno = RestUtil.get(urlServico + id, ProfessionalRequestedActivity.this);
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

            Servico dadosServico = null;

            if (prestador != null) {
                if (prestador.getServicos().size() > 0 && !prestador.getServicos().isEmpty()) {
                    for (int i = 0; i < prestador.getServicos().size(); i++) {
                        Servico servicoAtual = prestador.getServicos().get(i);
                        if (servicoAtual.getId() == servico.getId_servico()) {
                            dadosServico = servicoAtual;
                        }
                    }

                    if (dadosServico != null) {
                        txtTitulo.setText(dadosServico.getNome());
                        if (dadosServico.getCategoriaServico() == CategoriaServico.SERVICOS_ELETRICOS) {
                            txtCategoria.setText(Html.fromHtml(getResources().getString(R.string.categoria, getResources().getString(R.string.servicos_eletricos))));
                        } else {
                            String categoria = dadosServico.getCategoriaServico().toString().substring(0, 1).toUpperCase() + dadosServico.getCategoriaServico().toString().substring(1).toLowerCase();
                            txtCategoria.setText(Html.fromHtml(getResources().getString(R.string.categoria, categoria)));
                        }

                        txtDescricao.setText(Html.fromHtml(getResources().getString(R.string.descricao, dadosServico.getDescricao())));
                        txtPreco.setText(Html.fromHtml(getResources().getString(R.string.valor, String.format("%10.2f", dadosServico.getPreco()))));
                        txtData.setText(Html.fromHtml(getResources().getString(R.string.data_servico, servico.getData())));
                    }
                }
            } else {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalRequestedActivity.this);
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

    // Buscar dados do cliente
    public class BuscarCliente extends AsyncTask<Void, Void, String> {
        private ProgressDialog progresso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(ProfessionalRequestedActivity.this);
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            try {
                retorno = RestUtil.get(urlCliente + servico.getId_cliente(), ProfessionalRequestedActivity.this);
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
                if (cliente.getFoto() != null) {
                    if (!cliente.getFoto().equals("")) {
                        imgPerfil.setImageBitmap(stringToBitmap(cliente.getFoto()));
                    }
                }
                txtNomeCliente.setText(Html.fromHtml(getResources().getString(R.string.nome_usuario, cliente.getNome())));
                txtTelefone.setText(Html.fromHtml(getResources().getString(R.string.telefone, cliente.getCelular())));
                txtRua.setText(getResources().getString(R.string.endereco_rua,
                        cliente.getEndereco().getRua() + ", " + cliente.getEndereco().getNumero()));
                txtBairro.setText(getResources().getString(R.string.endereco_bairro, cliente.getEndereco().getBairro()));
                txtCidade.setText(getResources().getString(R.string.endereco_cidade,
                        cliente.getEndereco().getCidade() + " - " + cliente.getEndereco().getUf()));

                // Calcular média de avaliação do cliente
                calcularMediaAvaliacao();
            }
        }
    }

    // Confirmar serviço
    public HandlerTask handlerTaskConfirmar = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalRequestedActivity.this);
            alertDialog = alerta.create();

            View alertView = getLayoutInflater().inflate(R.layout.alert_success, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.sucesso_confirmar_servico));

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

        @Override
        public void onError(Exception erro) {
            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalRequestedActivity.this);
            alertDialog = alerta.create();

            View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.erro_confirmar_servico));

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
    };

    // Concluir serviço
    public HandlerTask handlerTaskConcluir = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalRequestedActivity.this);
            alertDialog = alerta.create();

            View alertView = getLayoutInflater().inflate(R.layout.alert_success, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.sucesso_concluir_servico));

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

        @Override
        public void onError(Exception erro) {
            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(ProfessionalRequestedActivity.this);
            alertDialog = alerta.create();

            View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.erro_concluir_servico));

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
    };
}
