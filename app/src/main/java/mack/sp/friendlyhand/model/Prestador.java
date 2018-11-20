package mack.sp.friendlyhand.model;

import java.util.List;

/**
 * Created by Giovana Rodrigues on 23/04/2018.
 */

public class Prestador extends Usuario {
    private int id_prestador;
    private List<Servico> servicos;
    private List<ServicoContratado> servico_contratados;
    private List<AvaliacaoCliente> avaliacoes;

    public int getId() {
        return id_prestador;
    }

    public void setId(int id_prestador) {
        this.id_prestador = id_prestador;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos;
    }

    public List<ServicoContratado> getServicosContratados() {
        return servico_contratados;
    }

    public void setServicosContratados(List<ServicoContratado> servicosContratados) {
        this.servico_contratados = servico_contratados;
    }

    @Override
    public String toString() {
        return "Prestador{" +
                "id_prestador=" + id_prestador +
                ", servicos=" + servicos +
                ", servicosContratados=" + servico_contratados +
                '}';
    }

    public List<AvaliacaoCliente> getAvaliacoes() {
        return avaliacoes;
    }

    public void setAvaliacoes(List<AvaliacaoCliente> avaliacoes) {
        this.avaliacoes = avaliacoes;
    }
}
