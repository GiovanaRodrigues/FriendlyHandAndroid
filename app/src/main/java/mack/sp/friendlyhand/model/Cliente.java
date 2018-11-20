package mack.sp.friendlyhand.model;

import java.util.List;

/**
 * Created by Giovana Rodrigues on 23/04/2018.
 */

public class Cliente extends Usuario {
    private int id_cliente;
    private List<ServicoContratado> servico_contratados;
    private List<AvaliacaoPrestador> avaliacoes;

    public int getId() {
        return id_cliente;
    }

    public void setId(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public List<ServicoContratado> getServicosContratados() {
        return servico_contratados;
    }

    public void setServicosContratados(List<ServicoContratado> servicosContratados) {
        this.servico_contratados = servicosContratados;
    }

    public List<AvaliacaoPrestador> getAvaliacoes() {
        return avaliacoes;
    }

    public void setAvaliacoes(List<AvaliacaoPrestador> avaliacoes) {
        this.avaliacoes = avaliacoes;
    }
}
