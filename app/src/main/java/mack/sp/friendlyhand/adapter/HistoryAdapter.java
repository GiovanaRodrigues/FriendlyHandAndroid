package mack.sp.friendlyhand.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Date;
import java.util.List;

import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.model.Prestador;
import mack.sp.friendlyhand.model.Servico;
import mack.sp.friendlyhand.model.ServicoContratado;
import mack.sp.friendlyhand.util.RestUtil;

import static mack.sp.friendlyhand.model.CategoriaServico.ENCANAMENTO;
import static mack.sp.friendlyhand.model.CategoriaServico.JARDINAGEM;
import static mack.sp.friendlyhand.model.CategoriaServico.LIMPEZA;
import static mack.sp.friendlyhand.model.CategoriaServico.MARCENARIA;
import static mack.sp.friendlyhand.model.CategoriaServico.PINTURA;
import static mack.sp.friendlyhand.model.CategoriaServico.SERVICOS_ELETRICOS;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.IP;

public class HistoryAdapter extends BaseAdapter {
    public final static String urlServico = "http://" + IP + "/friendlyhand/v1/prestador/";
    private final Context contexto;
    private ItemFilter filter = new ItemFilter();
    private List<ServicoContratado> servicos;
    public List<ServicoContratado> filtrados;
    private Prestador prestador;

    public HistoryAdapter(Context contexto, List<ServicoContratado> servicos) {
        this.contexto = contexto;
        this.servicos = servicos;
        filtrados = servicos;
    }

    @Override
    public int getCount() {
        return servicos.size();
    }

    @Override
    public Object getItem(int position) {
        return servicos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return servicos.get(position).getId_servico_contratado();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = LayoutInflater.from(contexto).inflate(R.layout.adapter_history, parent, false);

        ServicoContratado servico = servicos.get(position);

        // Buscar dados do serviço
        new BuscarServicos(view, servico).execute();

        return view;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new ItemFilter();
        }
        return filter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            // Fazer filtragem das ongs pela actionbar

            if (constraint != null && constraint.length() > 0) {
                ArrayList<ServicoContratado> listaFiltrada = new ArrayList<>();
                for (int i = 0; i < filtrados.size(); i++) {

                    // Comparar o título da notícia com o texto inserido e com o ID
                    if (filtrados.get(i).getServico().getNome().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        ServicoContratado servico = new ServicoContratado();
                        servico.setId_servico_contratado(filtrados.get(i).getId_servico_contratado());
                        servico.setCliente(filtrados.get(i).getCliente());
                        servico.setPrestador(filtrados.get(i).getPrestador());
                        servico.setServico(filtrados.get(i).getServico());
                        servico.setData(filtrados.get(i).getData());
                        servico.setConcluido(filtrados.get(i).isConcluido());
                        servico.setConfirmado(filtrados.get(i).isConfirmado());

                        listaFiltrada.add(servico);
                    }
                }

                results.count = listaFiltrada.size();
                results.values = listaFiltrada;

            } else {
                results.count = filtrados.size();
                results.values = filtrados;
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            servicos = (ArrayList<ServicoContratado>) results.values;
            notifyDataSetChanged();
        }
    }

    // Buscar dados do serviço
    public class BuscarServicos extends AsyncTask<Void, Void, String> {
        private View view;
        private ServicoContratado servicoContratado;
        private ProgressDialog progresso;

        public BuscarServicos(View view, ServicoContratado servicoContratado) {
            this.view = view;
            this.servicoContratado = servicoContratado;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(contexto);
            progresso.setTitle(contexto.getResources().getString(R.string.aguarde));
            progresso.setMessage(contexto.getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;

            try {
                retorno = RestUtil.get(urlServico + servicoContratado.getId_prestador(), contexto);
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

                JSONObject usuarioJson = new JSONObject(retorno);
                prestador = form.fromJson(usuarioJson.toString(), Prestador.class);

            } catch (JSONException | RuntimeException erro) {
                erro.printStackTrace();
            }

            return retorno;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progresso.dismiss();

            if (prestador.getServicos().size() > 0 && !prestador.getServicos().isEmpty()) {
                for (int i = 0; i < prestador.getServicos().size(); i++) {
                    Servico servicoAtual = prestador.getServicos().get(i);
                    if (servicoAtual.getId() == servicoContratado.getId_servico()) {
                        TextView txtTitulo = (TextView) view.findViewById(R.id.txtTitulo);
                        TextView txtData = (TextView) view.findViewById(R.id.txtData);
                        ImageView imgHistorico = (ImageView) view.findViewById(R.id.imgHistory);
                        Drawable drawable = view.getResources().getDrawable(R.mipmap.ic_build_black_24dp);

                        // Build = encanador; Broom = limpeza, flash = eletricidade, jardinagem = florist, pintura = paint

                        // Mudar ícone
                        switch (servicoAtual.getCategoriaServico()) {
                            case ENCANAMENTO:
                                drawable = view.getResources().getDrawable(R.mipmap.ic_build_black_24dp);
                                break;
                            case LIMPEZA:
                                drawable = view.getResources().getDrawable(R.drawable.ic_broom);
                                break;
                            case PINTURA:
                                drawable = view.getResources().getDrawable(R.mipmap.ic_format_paint_black_24dp);
                                break;
                            case JARDINAGEM:
                                drawable = view.getResources().getDrawable(R.mipmap.ic_local_florist_black_24dp);
                                break;
                            case MARCENARIA:
                                drawable = view.getResources().getDrawable(R.drawable.ic_saw_hammer);
                                break;
                            case SERVICOS_ELETRICOS:
                                drawable = view.getResources().getDrawable(R.mipmap.ic_flash_on_black_24dp);
                                break;
                        }

                        txtTitulo.setText(servicoAtual.getNome());
                        txtData.setText(servicoContratado.getData());
                        imgHistorico.setImageDrawable(drawable);
                    }
                }
            }
        }
    }
}
