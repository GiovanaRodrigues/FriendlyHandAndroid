package mack.sp.friendlyhand.view.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.adapter.ServiceAdapter;
import mack.sp.friendlyhand.model.AvaliacaoCliente;
import mack.sp.friendlyhand.model.AvaliacaoPrestador;
import mack.sp.friendlyhand.model.CategoriaServico;
import mack.sp.friendlyhand.model.Cliente;
import mack.sp.friendlyhand.model.Prestador;
import mack.sp.friendlyhand.model.Servico;
import mack.sp.friendlyhand.model.ServicoContratado;
import mack.sp.friendlyhand.tasks.HandlerTask;
import mack.sp.friendlyhand.tasks.TaskRest;
import mack.sp.friendlyhand.util.JsonParser;
import mack.sp.friendlyhand.util.RestUtil;

/**
 * Created by Giovana Rodrigues on 23/04/2018.
 */

public class EvaluationActivity extends SuperClassActivity {
    public final static String urlCliente = "http://" + IP + "/friendlyhand/v1/cliente/";
    public final static String urlPrestador = "http://" + IP + "/friendlyhand/v1/prestador/";

    public final static String urlAvaliacaoCliente = "http://" + IP + "/friendlyhand/v1/avaliacaoCliente/";
    public final static String urlAvaliacaoPrestador = "http://" + IP + "/friendlyhand/v1/avaliacaoPrestador/";

    private TextView txtCliente, txtServico, txtData, txtPreco;
    private EditText editComentario;
    private RatingBar ratingBar;
    private Button btnAvaliar;

    private ServicoContratado servico;
    private Prestador prestador;
    private Cliente cliente;
    private String usuario;
    private Long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        ActionBar actionBar = getSupportActionBar();

        // Adicionar botão voltar na ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle b = this.getIntent().getExtras();
        servico = (ServicoContratado) b.get("servicoAtual");

        iniciarComponentes();

        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        id = prefs.getLong(ID, 0);
        usuario = prefs.getString(TIPOUSUARIO, "cliente");

        if (usuario.equals("cliente")) {
            actionBar.setTitle(R.string.avaliar_prestador);
        } else {
            actionBar.setTitle(R.string.avaliar_cliente);
        }

        // Buscar informações do serviço e do prestador
        new BuscarServico().execute();

        // Buscar dados do cliente
        new BuscarCliente().execute();

        txtData.setText(Html.fromHtml(getResources().getString(R.string.data_servico, servico.getData())));

        // Mudar fonte do título
        txtServico.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf"));

        btnAvaliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usuario.equalsIgnoreCase("cliente")) {
                    avaliarPrestador();
                } else {
                    avaliarCliente();
                }
            }
        });
    }

    // ------------------------------------------------------- Métodos da classe

    private void iniciarComponentes() {
        txtCliente = (TextView) findViewById(R.id.txtCliente);
        txtServico = (TextView) findViewById(R.id.txtServico);
        txtData = (TextView) findViewById(R.id.txtData);
        txtPreco = (TextView) findViewById(R.id.txtPreco);
        editComentario = (EditText) findViewById(R.id.edit_comentario);

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnAvaliar = (Button) findViewById(R.id.btnAvaliar);
    }

    // Avaliar o cliente
    public void avaliarCliente() {
        AvaliacaoPrestador avaliacao = new AvaliacaoPrestador();
        avaliacao.setIdPrestador((int) (long) id);
        avaliacao.setIdCliente(servico.getId_cliente());
        avaliacao.setIdServicoContratado(servico.getId_servico_contratado());
        avaliacao.setComentario(editComentario.getText().toString().trim());
        avaliacao.setNota(ratingBar.getRating());

        JsonParser<AvaliacaoPrestador> parser = new JsonParser<>(AvaliacaoPrestador.class);
        new TaskRest(TaskRest.RequestMethod.POST, handlerTask, EvaluationActivity.this).execute(urlAvaliacaoPrestador, parser.fromObject(avaliacao));
    }

    // Avaliar o prestador
    private void avaliarPrestador() {
        AvaliacaoCliente avaliacao = new AvaliacaoCliente();
        avaliacao.setIdCliente((int) (long) id);
        avaliacao.setIdPrestador(servico.getId_prestador());
        avaliacao.setIdServicoContratado(servico.getId_servico_contratado());
        avaliacao.setComentario(editComentario.getText().toString().trim());
        avaliacao.setNota(ratingBar.getRating());

        JsonParser<AvaliacaoCliente> parser = new JsonParser<>(AvaliacaoCliente.class);
        new TaskRest(TaskRest.RequestMethod.POST, handlerTask, EvaluationActivity.this).execute(urlAvaliacaoCliente, parser.fromObject(avaliacao));
    }

    // ---------------------------------------------------- Métodos nativos

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

    // ----------------------------------------------------------- Métodos de persistência

    // Buscar dados do serviço
    public class BuscarServico extends AsyncTask<Void, Void, String> {
        private ProgressDialog progresso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(EvaluationActivity.this);
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            try {
                retorno = RestUtil.get(urlPrestador + servico.getId_prestador(), EvaluationActivity.this);
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
                        txtServico.setText(dadosServico.getNome());
                        txtPreco.setText(Html.fromHtml(getResources().getString(R.string.valor, String.format("%10.2f", dadosServico.getPreco()))));
                        txtData.setText(Html.fromHtml(getResources().getString(R.string.data_servico, servico.getData())));

                        if (usuario.equalsIgnoreCase("prestador")) {
                            new BuscarCliente().execute();
                        } else {
                            txtCliente.setText(Html.fromHtml(getResources().getString(R.string.prestador_servico, prestador.getNome())));
                        }
                    }
                }

                if (prestador.getAvaliacoes().size() > 0 && !prestador.getAvaliacoes().isEmpty()) {
                    AvaliacaoCliente avaliacaoCliente = null;

                    for (int i = 0; i < prestador.getAvaliacoes().size(); i++) {
                        AvaliacaoCliente avaliacao = prestador.getAvaliacoes().get(i);
                        if (avaliacao.getIdServicoContratado() == servico.getId_servico_contratado()) {
                            avaliacaoCliente = avaliacao;
                        }
                    }

                    if (avaliacaoCliente != null) {
                        if(!avaliacaoCliente.getComentario().equalsIgnoreCase("")) {
                            editComentario.setText(avaliacaoCliente.getComentario());
                        } else {
                            editComentario.setText(getResources().getString(R.string.nenhum_comentario));
                            editComentario.setTextColor(getResources().getColor(R.color.darkGray));
                            editComentario.setTypeface(editComentario.getTypeface(), Typeface.ITALIC);
                        }
                        // Tornar ediitext impossível de ser editado
                        editComentario.setTag(editComentario.getKeyListener());
                        editComentario.setKeyListener(null);

                        ratingBar.setRating((float) avaliacaoCliente.getNota());
                        ratingBar.setIsIndicator(true);

                        btnAvaliar.setVisibility(View.GONE);
                    } else {
                        btnAvaliar.setVisibility(View.VISIBLE);
                        avaliarPrestador();
                    }
                }
            } else {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(EvaluationActivity.this);
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
            progresso = new ProgressDialog(EvaluationActivity.this);
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            try {
                retorno = RestUtil.get(urlCliente + servico.getId_cliente(), EvaluationActivity.this);
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
                txtCliente.setText(Html.fromHtml(getResources().getString(R.string.cliente_servico, cliente.getNome())));
                if (usuario.equalsIgnoreCase("prestador")) {
                    if (cliente.getAvaliacoes().size() > 0 && !cliente.getAvaliacoes().isEmpty()) {
                        AvaliacaoPrestador avaliacaoPrestador = null;

                        for (int i = 0; i < cliente.getAvaliacoes().size(); i++) {
                            AvaliacaoPrestador avaliacao = cliente.getAvaliacoes().get(i);
                            if (avaliacao.getIdServicoContratado() == servico.getId_servico_contratado()) {
                                avaliacaoPrestador = avaliacao;
                            }
                        }

                        if (avaliacaoPrestador != null) {
                            if(!avaliacaoPrestador.getComentario().equalsIgnoreCase("")) {
                                editComentario.setText(avaliacaoPrestador.getComentario());
                            } else {
                                editComentario.setText(getResources().getString(R.string.nenhum_comentario));
                                editComentario.setTextColor(getResources().getColor(R.color.darkGray));
                                editComentario.setTypeface(editComentario.getTypeface(), Typeface.ITALIC);
                            }
                            // Tornar ediitext impossível de ser editado
                            editComentario.setTag(editComentario.getKeyListener());
                            editComentario.setKeyListener(null);

                            ratingBar.setRating((float) avaliacaoPrestador.getNota());
                            ratingBar.setIsIndicator(true);

                            btnAvaliar.setVisibility(View.GONE);
                        } else {
                            btnAvaliar.setVisibility(View.VISIBLE);
                            avaliarCliente();
                        }
                    }
                }
            } else {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(EvaluationActivity.this);
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

    // Concluir serviço
    public HandlerTask handlerTask = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(EvaluationActivity.this);
            alertDialog = alerta.create();

            View alertView = getLayoutInflater().inflate(R.layout.alert_success, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.sucesso_avaliacao));

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
            final AlertDialog.Builder alerta = new AlertDialog.Builder(EvaluationActivity.this);
            alertDialog = alerta.create();

            View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.erro_avaliacao));

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
