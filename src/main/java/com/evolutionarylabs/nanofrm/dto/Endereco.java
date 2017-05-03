package com.evolutionarylabs.nanofrm.dto;

/**
 * Created by cleberzanella on 02/05/17.
 */
public class Endereco {

    private int id;
    private boolean principal;
    private String logradouro;
    private int numero;
    private int pessoaId;

    private DateTimeAudit dateAudit;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getPessoaId() {
        return pessoaId;
    }

    public void setPessoaId(int pessoaId) {
        this.pessoaId = pessoaId;
    }

    public DateTimeAudit getDateAudit() {
        return dateAudit;
    }

    public void setDateAudit(DateTimeAudit dateAudit) {
        this.dateAudit = dateAudit;
    }
}
