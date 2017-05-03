package com.evolutionarylabs.nanofrm.dto;

/**
 * Created by cleberzanella on 02/05/17.
 */
public class Pessoa implements IPessoa {

    private int id;
    private String nome;

    private DateTimeAudit dateAudit;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public DateTimeAudit getDateAudit() {
        return dateAudit;
    }

    public void setDateAudit(DateTimeAudit dateAudit) {
        this.dateAudit = dateAudit;
    }
}
