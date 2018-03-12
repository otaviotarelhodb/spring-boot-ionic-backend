package com.otaviotarelho.curso.dto;

import java.io.Serializable;

import com.otaviotarelho.curso.domain.Cidade;

public class CidadeDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer id;
	private String nome;
	
	public CidadeDTO() {
		// TODO Auto-generated constructor stub
	}
	
	public CidadeDTO(Cidade cidade) {
		this.id = cidade.getId();
		this.nome = cidade.getNome();
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	
}
