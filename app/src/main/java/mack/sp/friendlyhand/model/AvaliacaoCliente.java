package mack.sp.friendlyhand.model;

import java.io.Serializable;

/**
 * Created by Giovana Rodrigues on 26/05/2018.
 */

public class AvaliacaoCliente implements Serializable{
    private int id;
    private int id_servico_contratado;
    private int id_cliente;
    private int id_prestador;
    private double nota;
    private String comentario;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdServicoContratado() {
        return id_servico_contratado;
    }

    public void setIdServicoContratado(int idServicoContratado) {
        this.id_servico_contratado = idServicoContratado;
    }

    public int getIdCliente() {
        return id_cliente;
    }

    public void setIdCliente(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public int getIdPrestador() {
        return id_prestador;
    }

    public void setIdPrestador(int id_prestador) {
        this.id_prestador = id_prestador;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
