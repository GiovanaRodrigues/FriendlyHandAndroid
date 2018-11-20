package mack.sp.friendlyhand.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Giovana Rodrigues on 23/04/2018.
 */

public class Servico implements Serializable{
    private int id_servico;
    private String nome;
    private Prestador prestador;
    private int id_prestador;
    private String descricao;
    private double preco;
    private CategoriaServico categoria_servico;

    public int getId() {
        return id_servico;
    }

    public void setId(int id) {
        this.id_servico = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public CategoriaServico getCategoriaServico() {
        return categoria_servico;
    }

    public void setCategoriaServico(CategoriaServico categoriaServico) {
        this.categoria_servico = categoriaServico;
    }

    @Override
    public String toString() {
        return "Servico{" +
                "id_servico=" + id_servico +
                ", nome='" + nome + '\'' +
                ", prestador=" + prestador +
                ", descricao='" + descricao + '\'' +
                ", preco=" + preco +
                ", categoriaServico=" + categoria_servico +
                '}';
    }

    public int getId_prestador() {
        return id_prestador;
    }

    public void setId_prestador(int id_prestador) {
        this.id_prestador = id_prestador;
    }
}
