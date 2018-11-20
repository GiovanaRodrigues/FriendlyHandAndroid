package mack.sp.friendlyhand.view.fragment;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import mack.sp.friendlyhand.model.ServicoContratado;
import mack.sp.friendlyhand.model.Usuario;
import mack.sp.friendlyhand.tasks.HandlerTask;
import mack.sp.friendlyhand.tasks.TaskRest;
import mack.sp.friendlyhand.util.JsonParser;
import mack.sp.friendlyhand.util.RestUtil;
import mack.sp.friendlyhand.view.activity.ClientRequestActivity;
import mack.sp.friendlyhand.view.activity.InicialActivity;
import mack.sp.friendlyhand.view.activity.LoginActivity;
import mack.sp.friendlyhand.view.activity.MainActivityProfessional;
import mack.sp.friendlyhand.view.activity.ProfessionalViewServiceActivity;

import static android.content.Context.MODE_PRIVATE;
import static mack.sp.friendlyhand.model.CategoriaServico.ENCANAMENTO;
import static mack.sp.friendlyhand.model.CategoriaServico.JARDINAGEM;
import static mack.sp.friendlyhand.model.CategoriaServico.LIMPEZA;
import static mack.sp.friendlyhand.model.CategoriaServico.MARCENARIA;
import static mack.sp.friendlyhand.model.CategoriaServico.PINTURA;
import static mack.sp.friendlyhand.model.CategoriaServico.SERVICOS_ELETRICOS;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.APP_PREFS;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.EMAIL;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.FOTO;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.ID;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.IP;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.NOME;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.SENHA;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.TIPOUSUARIO;

/**
 * Created by Giovana Rodrigues on 23/04/2018.
 */

public class ProfessionalServicesFragment extends ListFragment {
    public final static String urlNovoServico = "http://" + IP + "/friendlyhand/v1/prestador/";

    private ServiceAdapter adapter;
    private LinearLayout layoutVazio;
    private TextView txtVazio;
    private FloatingActionButton fabNovoServico;
    private SwipeRefreshLayout refreshLayout;

    private Prestador prestador;
    private Long id;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_services, container, false);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.meus_servicos));

        layoutVazio = (LinearLayout) view.findViewById(R.id.layout_vazio);
        txtVazio = (TextView) view.findViewById(R.id.txtVazio);
        fabNovoServico = (FloatingActionButton) view.findViewById(R.id.fab);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);

        setHasOptionsMenu(true);

        return view;
    }

    // --------------------------------------------------------------------- Métodos nativos
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        id = prefs.getLong(ID, 0);

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Verificar se o primeiro elemento visível é o primeiro elemento da listview
                int topRowVerticalPosition = (getListView() == null || getListView().getChildCount() == 0) ? 0 : getListView().getChildAt(0).getTop();

                // Permitir que a lista atualize
                refreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);

            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
                refreshLayout.setRefreshing(false);
            }
        });

        new BuscarServicos().execute();

        fabNovoServico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                criarNovoServico();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new BuscarServicos().execute();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.e("CLICOU", "CLICOU");
        Servico servico = (Servico) l.getItemAtPosition(position);

        Intent intent = new Intent(getActivity(), ProfessionalViewServiceActivity.class);
        intent.putExtra("servicoAtual", servico);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    // Menu buscar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (adapter != null) {
            inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_search, menu);
            MenuItem menuItem = menu.findItem(R.id.search);

            SearchView searchView = (SearchView) menuItem.getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);

                    return false;
                }
            });
        }
    }

    // ------------------------------------------------------------------ Métodos da classe
    // Criar novo servico
    public void criarNovoServico() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());

        View alertView = getActivity().getLayoutInflater().inflate(R.layout.alert_new_service, null);

        final TextView txtTitulo = (TextView) alertView.findViewById(R.id.txtTitulo);
        final EditText editNome = (EditText) alertView.findViewById(R.id.edit_nome);
        final EditText editDescricao = (EditText) alertView.findViewById(R.id.edit_descricao);
        final EditText editValor = (EditText) alertView.findViewById(R.id.edit_valor);
        final Spinner spnCategoria = (Spinner) alertView.findViewById(R.id.spn_categoria);

        final String[] categorias = {getResources().getString(R.string.limpeza),
                getResources().getString(R.string.marcenaria), getResources().getString(R.string.servicos_eletricos),
                getResources().getString(R.string.encanamento), getResources().getString(R.string.jardinagem),
                getResources().getString(R.string.pintura)};

        ArrayAdapter<String> categoriasAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, categorias);
        categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategoria.setAdapter(categoriasAdapter);

        txtTitulo.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lobster-Regular.ttf"));

        alerta.setView(alertView);

        alerta.setPositiveButton(getResources().getString(R.string.confirmar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editNome.getEditableText().toString().trim().isEmpty() || editDescricao.getEditableText().toString().trim().isEmpty() || editValor.getEditableText().toString().trim().isEmpty()) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.preencha_todos_campos), Toast.LENGTH_LONG).show();
                } else {
                    Servico servico = new Servico();
                    servico.setNome(editNome.getText().toString());
                    servico.setDescricao(editDescricao.getText().toString());
                    servico.setPreco(Double.parseDouble(editValor.getText().toString()));
                    switch (spnCategoria.getSelectedItemPosition()) {
                        case 0:
                            servico.setCategoriaServico(LIMPEZA);
                            break;
                        case 1:
                            servico.setCategoriaServico(MARCENARIA);
                            break;
                        case 2:
                            servico.setCategoriaServico(SERVICOS_ELETRICOS);
                            break;
                        case 3:
                            servico.setCategoriaServico(ENCANAMENTO);
                            break;
                        case 4:
                            servico.setCategoriaServico(JARDINAGEM);
                            break;
                        case 5:
                            servico.setCategoriaServico(PINTURA);
                    }

                    JsonParser<Servico> parser = new JsonParser<>(Servico.class);
                    new TaskRest(TaskRest.RequestMethod.POST, handlerTask, getActivity()).execute(urlNovoServico + id + "/servico", parser.fromObject(servico));

                    if (prestador == null) {
                        new TaskRest(TaskRest.RequestMethod.POST, handlerTask, getActivity()).execute(urlNovoServico + id + "/servico", parser.fromObject(servico));
                    }
                }
            }
        });

        alerta.setNegativeButton(getResources().getString(R.string.cancelar), null);

        alerta.show();
    }

    // ------------------------------------------------------------------ Métodos de persistência

    // Buscar servicos do prestador
    public class BuscarServicos extends AsyncTask<Void, Void, String> {
        private ProgressDialog progresso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(getActivity());
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            try {
                retorno = RestUtil.get(urlNovoServico + id, getActivity());
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
                if (!prestador.getServicos().isEmpty()) {
                    adapter = new ServiceAdapter(getActivity(), prestador.getServicos());
                    adapter.notifyDataSetChanged();

                    getListView().setAdapter(adapter);

                    layoutVazio.setVisibility(View.GONE);
                    getListView().setVisibility(View.VISIBLE);
                } else {
                    layoutVazio.setVisibility(View.VISIBLE);
                    txtVazio.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lobster-Regular.ttf"));

                    getListView().setVisibility(View.GONE);
                }
            } else {
                layoutVazio.setVisibility(View.VISIBLE);
                txtVazio.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lobster-Regular.ttf"));

                getListView().setVisibility(View.GONE);
            }
            getActivity().invalidateOptionsMenu();
        }
    }

    // Cadastrar novo serviço
    public HandlerTask handlerTask = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
            alertDialog = alerta.create();

            View alertView = getActivity().getLayoutInflater().inflate(R.layout.alert_success, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.sucesso_novo_servico));

            alertDialog.setView(alertView);
            alertDialog.setCancelable(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertDialog.dismiss();
                    onStart();
                }
            }, 1500);

            alertDialog.show();
        }

        @Override
        public void onError(Exception erro) {
            Log.e("ERRO", erro.getMessage());
            if (erro.getMessage().contains("401")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.erro_cadastro), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.erro_conexao), Toast.LENGTH_LONG).show();
            }
        }
    };
}
