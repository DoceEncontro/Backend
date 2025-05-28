package br.com.doceencontro.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.doceencontro.model.Usuario;
import br.com.doceencontro.model.dtos.ConviteResponseDTO;
import br.com.doceencontro.model.dtos.EventoResponseDTO;
import br.com.doceencontro.model.dtos.UsuarioDetailsDTO;
import br.com.doceencontro.model.dtos.UsuarioResponseDTO;
import br.com.doceencontro.service.ConviteService;
import br.com.doceencontro.service.UsuarioService;
import br.com.doceencontro.utils.EventoUtils;
import br.com.doceencontro.utils.IdToken;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@AllArgsConstructor
public class UsuarioController {

	private UsuarioService usuarioService;
	
	private ConviteService conviteService;
	
	@GetMapping
	public List<UsuarioResponseDTO> obterTodos() {
		return usuarioService.obterTodos();
	}
	
	@GetMapping("/find")
	public UsuarioDetailsDTO obterPorId() {
		return usuarioService.obterPorId(IdToken.get());
	}
	
	@GetMapping("/convites")
	public List<ConviteResponseDTO> listarConvitesUsuario() {
		return conviteService.listarConvitesUsuario(IdToken.get());
	}
	
	@GetMapping("/criados")
	public List<EventoResponseDTO> listarEventosCriados() {
		return usuarioService.listarEventosCriados(IdToken.get());
	}
	
	@GetMapping("/participados")
	public List<EventoResponseDTO> listarEventosParticipados() {
		return usuarioService.listarEventosParticipados(IdToken.get());
	}
	
	@GetMapping("/isParticipando/{eventoId}")
	public ResponseEntity<Void> verificarParticipacao(@PathVariable String eventoId) {
		return usuarioService.verificarParticipacao(eventoId, IdToken.get());
	}
	
	@PutMapping
	public UsuarioDetailsDTO editarUsuario(@RequestBody Usuario usuario) {
		return usuarioService.editarUsuario(IdToken.get(), usuario);
	}
	
	@DeleteMapping
	public String excluirUsuario() {
		return usuarioService.excluirUsuario(IdToken.get());
	}
	
	@DeleteMapping("/convites/{conviteId}")
	public String negarConvite(@PathVariable String conviteId) {
		return conviteService.negarConvite(conviteId, IdToken.get());
	}
	
}
