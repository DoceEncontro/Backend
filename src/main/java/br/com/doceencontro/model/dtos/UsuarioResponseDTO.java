package br.com.doceencontro.model.dtos;

import br.com.doceencontro.model.Usuario;
import lombok.Data;

@Data
public class UsuarioResponseDTO {

	private String id;

	private String nome;

	public UsuarioResponseDTO(Usuario usuario) {
		this.id = usuario.getId();
		this.nome = usuario.getNome();
	}

}
