package mack.sp.friendlyhand.view.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.jansenfelipe.androidmask.MaskEditTextChangedListener;
import de.hdodenhof.circleimageview.CircleImageView;
import mack.sp.friendlyhand.R;
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

public class ClientRequestActivity extends SuperClassActivity {
    private final String urlSolicitarServico = "http://" + IP + "/friendlyhand/v1/servicocontratado";

    private TextView txtTitulo, txtDescricao, txtCategoria, txtPreco, txtPrestador, txtTelefone;
    private Button btnConfirmar;
    private RatingBar rtEstrelas;
    private CircleImageView imgPerfil;
    private LinearLayout linearLayout;

    private Servico servico;
    private ServicoContratado servicoContratado;
    private Prestador prestador;
    private Long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_service);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.solicitar_servico));

        // Adicionar botão voltar na ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle b = this.getIntent().getExtras();
        servico = (Servico) b.get("servicoAtual");
        prestador = servico.getPrestador();

        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        id = prefs.getLong(ID, 0);

        inicializarComponentes();

        //new BuscarAvaliacoes().execute();
        calcularMediaAvaliacao();

        // Mudar fonte do título
        txtTitulo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf"));

        txtTitulo.setText(servico.getNome());
        txtDescricao.setText(Html.fromHtml(getResources().getString(R.string.descricao, servico.getDescricao())));
        txtPreco.setText(Html.fromHtml(getResources().getString(R.string.valor, String.format("%10.2f", servico.getPreco()))));
        txtPrestador.setText(Html.fromHtml(getResources().getString(R.string.nome_usuario, servico.getPrestador().getNome())));
        txtTelefone.setText(Html.fromHtml(getResources().getString(R.string.telefone, servico.getPrestador().getCelular())));
        if (servico.getCategoriaServico() != null) {
            if (servico.getCategoriaServico() == CategoriaServico.SERVICOS_ELETRICOS) {
                txtCategoria.setText(Html.fromHtml(getResources().getString(R.string.categoria, getResources().getString(R.string.servicos_eletricos))));
            } else {
                String categoria = servico.getCategoriaServico().toString().substring(0, 1).toUpperCase() + servico.getCategoriaServico().toString().substring(1).toLowerCase();
                txtCategoria.setText(Html.fromHtml(getResources().getString(R.string.categoria, categoria)));
            }
        }

        if (servico.getPrestador().getFoto() != null && !servico.getPrestador().getFoto().equals("")) {
            imgPerfil.setImageBitmap(stringToBitmap(servico.getPrestador().getFoto()));
        }

        btnConfirmar.setText(getResources().getString(R.string.solicitar));

        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(ClientRequestActivity.this);

                final View alertView = getLayoutInflater().inflate(R.layout.alert_request, null);

                alerta.setView(alertView);

                final TextView txtTitulo = (TextView) alertView.findViewById(R.id.txtTitulo);
                final EditText editData = (EditText) alertView.findViewById(R.id.edit_data);

                MaskEditTextChangedListener maskData = new MaskEditTextChangedListener("##/##/####", editData);
                editData.addTextChangedListener(maskData);

                // Mudar fonte do título
                txtTitulo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf"));

                alerta.setPositiveButton(getResources().getString(R.string.enviar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editData.getText().toString().trim().isEmpty()) {
                            String[] data = editData.getText().toString().split("/");
                            int mes = Integer.parseInt(data[1]) - 1;
                            Calendar calendarHoje = Calendar.getInstance();
                            Calendar calendarServico = Calendar.getInstance();
                            calendarServico.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data[0]));
                            calendarServico.set(Calendar.MONTH, mes);
                            calendarServico.set(Calendar.YEAR, Integer.parseInt(data[2]));

                            Date dataServico = calendarServico.getTime();

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                            if (calendarServico.before(calendarHoje) || calendarServico.get(Calendar.YEAR) > calendarHoje.get(Calendar.YEAR) + 1) {
                                dialog.dismiss();
                                final AlertDialog alertDialog;
                                final AlertDialog.Builder alerta = new AlertDialog.Builder(ClientRequestActivity.this);
                                alertDialog = alerta.create();

                                View alertView = getLayoutInflater().inflate(R.layout.alert_erro, null);

                                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                                calendarHoje.set(Calendar.YEAR, calendarHoje.get(Calendar.YEAR) + 1);
                                txtMensagem.setText(Html.fromHtml(getResources().getString(R.string.data_invalida, sdf.format(calendarHoje.getTime()))));

                                alertDialog.setView(alertView);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertDialog.dismiss();
                                    }
                                }, 3500);

                                alertDialog.show();
                            } else {
                                dialog.dismiss();

                                servicoContratado = new ServicoContratado();
                                servicoContratado.setData(sdf.format(dataServico));
                                servicoContratado.setId_servico(servico.getId());
                                servicoContratado.setId_prestador(prestador.getId());
                                servicoContratado.setId_cliente((int) (long) id);

                                JsonParser<ServicoContratado> parser = new JsonParser<>(ServicoContratado.class);
                                new TaskRest(TaskRest.RequestMethod.POST, handlerTask, ClientRequestActivity.this).execute(urlSolicitarServico, parser.fromObject(servicoContratado));
                            }
                        }
                    }
                });

                alerta.setNegativeButton(getResources().getString(R.string.cancelar), null);

                alerta.show();
            }
        });
    }

    // ------------------------------------------------------- Métodos da classe
    public void inicializarComponentes() {
        txtTitulo = (TextView) findViewById(R.id.txtTitulo);
        txtDescricao = (TextView) findViewById(R.id.txtDescricao);
        txtCategoria = (TextView) findViewById(R.id.txtCategoria);
        txtPreco = (TextView) findViewById(R.id.txtPreco);
        txtPrestador = (TextView) findViewById(R.id.txtPrestador);
        txtTelefone = (TextView) findViewById(R.id.txtTelefone);
        rtEstrelas = (RatingBar) findViewById(R.id.rtEstrelas);

        imgPerfil = (CircleImageView) findViewById(R.id.img_profile);

        linearLayout = (LinearLayout) findViewById(R.id.avaliacao);

        btnConfirmar = (Button) findViewById(R.id.btnConfirmar);
    }

    // Calcular a média de avaliações do prestador
    private void calcularMediaAvaliacao() {
        List<AvaliacaoCliente> avaliacoes = prestador.getAvaliacoes();
        if (avaliacoes.size() > 0 || !avaliacoes.isEmpty()) {
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

    // ------------------------------------------------------- Métodos de busca

    // Validar Login
    public HandlerTask handlerTask = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(ClientRequestActivity.this);
            alertDialog = alerta.create();

            View alertView = getLayoutInflater().inflate(R.layout.alert_success, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.sucesso_solicitar_servico));

            alertDialog.setView(alertView);
            alertDialog.setCancelable(false);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    alertDialog.dismiss();
                    finish();

                    // Apresentar animação de passagem da esquerda para direita
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
            }, 1500);

            alertDialog.show();
        }

        @Override
        public void onError(Exception erro) {
            Log.e("ERRO", erro.getMessage());
            if (erro.getMessage().contains("401")) {
                Toast.makeText(ClientRequestActivity.this, getResources().getString(R.string.erro_cadastro), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ClientRequestActivity.this, getResources().getString(R.string.erro_conexao), Toast.LENGTH_LONG).show();
            }
        }
    };

    // ------------------------------------------------------- Métodos nativos
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
}
