package mack.sp.friendlyhand.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.model.Servico;

public class ServiceAdapter extends BaseAdapter {
    private final Context contexto;
    private ItemFilter filter = new ItemFilter();
    private List<Servico> servicos;
    public List<Servico> filtrados;

    public ServiceAdapter(Context contexto, List<Servico> servicos) {
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
        return servicos.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = LayoutInflater.from(contexto).inflate(R.layout.adapter_service, parent, false);

        Servico servico = servicos.get(position);

        TextView txtTitulo = (TextView) view.findViewById(R.id.txtTitulo);
        Drawable drawable = view.getResources().getDrawable(R.mipmap.ic_build_black_24dp);

        // Build = encanador; Broom = limpeza, flash = eletricidade, jardinagem = florist, pintura = paint

        // Mudar ícone
        if(servico.getCategoriaServico() != null) {
            switch (servico.getCategoriaServico()) {
                case LIMPEZA:
                    drawable = view.getResources().getDrawable(R.drawable.ic_broom);
                    break;
                case ENCANAMENTO:
                    drawable = view.getResources().getDrawable(R.mipmap.ic_build_black_24dp);
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
        }

        txtTitulo.setText(servico.getNome());
        txtTitulo.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

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
                ArrayList<Servico> listaFiltrada = new ArrayList<>();
                for (int i = 0; i < filtrados.size(); i++) {

                    // Comparar o título da notícia com o texto inserido e com o ID
                    if (filtrados.get(i).getNome().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        Servico servico = new Servico();
                        servico.setId(filtrados.get(i).getId());
                        servico.setNome(filtrados.get(i).getNome());
                        servico.setDescricao(filtrados.get(i).getDescricao());
                        servico.setPreco(filtrados.get(i).getPreco());
                        servico.setCategoriaServico(filtrados.get(i).getCategoriaServico());

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
            servicos = (ArrayList<Servico>) results.values;
            notifyDataSetChanged();
        }
    }
}
