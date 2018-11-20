package mack.sp.friendlyhand.view.fragment;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import mack.sp.friendlyhand.adapter.ContractedServiceAdapter;
import mack.sp.friendlyhand.adapter.ServiceAdapter;
import mack.sp.friendlyhand.model.Prestador;
import mack.sp.friendlyhand.model.Servico;
import mack.sp.friendlyhand.model.ServicoContratado;
import mack.sp.friendlyhand.util.RestUtil;
import mack.sp.friendlyhand.view.activity.ProfessionalRequestedActivity;

import static android.content.Context.MODE_PRIVATE;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.APP_PREFS;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.ID;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.IP;

/**
 * Created by Giovana Rodrigues on 23/04/2018.
 */

public class ProfessionalConfirmedFragment extends ListFragment {
    public final static String urlServicosConfirmados = "http://" + IP + "/friendlyhand/v1/servicoscontratados/";

    private ContractedServiceAdapter adapter;
    private LinearLayout layoutVazio;
    private TextView txtVazio;
    private SwipeRefreshLayout refreshLayout;

    private Long id;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list, container, false);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.servicos_confirmados));

        layoutVazio = (LinearLayout) view.findViewById(R.id.layout_vazio);
        txtVazio = (TextView) view.findViewById(R.id.txtVazio);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);

        setHasOptionsMenu(true);

        return view;
    }

    // --------------------------------------------------------- Métodos Nativos

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

        final ListView listView = getListView();

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Verificar se o primeiro elemento visível é o primeiro elemento da listview
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();

                // Permitir que a lista atualize
                refreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);

            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
                refreshLayout.setRefreshing(false);
            }
        });

        new BuscarServicosConfirmados().execute();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.e("CLICOU", "CLICOU");
        ServicoContratado servico = (ServicoContratado) l.getItemAtPosition(position);

        Intent intent = new Intent(getActivity(), ProfessionalRequestedActivity.class);
        intent.putExtra("servicoAtual", servico);
        intent.putExtra("acao", "concluir");
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

    // --------------------------------------------------------- Métodos de busca

    // Buscar servicos confirmados
    public class BuscarServicosConfirmados extends AsyncTask<Void, Void, String> {
        public List<ServicoContratado> servicos = new ArrayList<>();
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
                retorno = RestUtil.get(urlServicosConfirmados + "Prestador/" + id + "?referencia=confirmado", getActivity());
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
                    JSONArray jsonArray = new JSONArray(retorno);
                    Log.e("ARRAY", "" + jsonArray.length());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject servicoJson = jsonArray.getJSONObject(i);
                        ServicoContratado servico = form.fromJson(servicoJson.toString(), ServicoContratado.class);

                        servicos.add(servico);
                    }
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

            if(servicos != null) {
                if (!servicos.isEmpty()) {
                    adapter = new ContractedServiceAdapter(getActivity(), servicos);
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
}
