package mack.sp.friendlyhand.model;

import java.io.Serializable;

public class ServicoContratado implements Serializable{
    private int id_servico_contratado;
    private int id_prestador;
    private Prestador prestador;
    private int id_cliente;
    private Cliente cliente;
    private int id_servico;
    private Servico servico;
    private String data;
    private boolean confirmado;
    private boolean concluido;

    public int getId_servico_contratado() {
        return id_servico_contratado;
    }

    public void setId_servico_contratado(int id_servico_contratado) {
        this.id_servico_contratado = id_servico_contratado;
    }

    public int getId_prestador() {
        return id_prestador;
    }

    public void setId_prestador(int id_prestador) {
        this.id_prestador = id_prestador;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }

    public int getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public int getId_servico() {
        return id_servico;
    }

    public void setId_servico(int id_servico) {
        this.id_servico = id_servico;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }

    public boolean isConcluido() {
        return concluido;
    }

    public void setConcluido(boolean concluido) {
        this.concluido = concluido;
    }

    @Override
    public String toString() {
        return "ServicoContratado{" +
                "id_servico_contratado=" + id_servico_contratado +
                ", id_prestador=" + id_prestador +
                ", id_cliente=" + id_cliente +
                ", id_servico=" + id_servico +
                '}';
    }
}
