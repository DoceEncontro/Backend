package br.com.doceencontro.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.doceencontro.exception.exceptions.UsuarioNotFoundException;
import br.com.doceencontro.model.Convite;
import br.com.doceencontro.model.Evento;
import br.com.doceencontro.model.Usuario;
import br.com.doceencontro.model.dtos.ConviteDTO;
import br.com.doceencontro.model.dtos.ConviteResponseDTO;
import br.com.doceencontro.repository.ConviteRepository;
import br.com.doceencontro.repository.EventoRepository;

@Service
public class ConviteService {

	private ConviteRepository conviteRepository;

	private UsuarioService usuarioService;

	private EventoService eventoService;

	public ConviteService(ConviteRepository conviteRepository, EventoService eventoService,
			UsuarioService usuarioService) {
		this.conviteRepository = conviteRepository;
		this.usuarioService = usuarioService;
		this.eventoService = eventoService;
	}

	public List<Convite> findAll() {
		return conviteRepository.findAll();
	}

	public Convite findById(String id) {
		Optional<Convite> buscarConvite = conviteRepository.findById(id);

		if (buscarConvite.isEmpty()) {
			throw new RuntimeException("Convite não encontrado.");
		}

		return buscarConvite.get();
	}

	private ConviteDTO converterDto(Convite convite) {
		return new ConviteDTO(convite);
	}
	
	private List<ConviteResponseDTO> converterDtos(List<Convite> convites) {
		return convites.stream()
				.map(c -> new ConviteResponseDTO(c))
				.collect(Collectors.toList());
	}

	public ConviteDTO convidar(List<String> usuariosIds, String eventoId) {
		List<Usuario> usuarios = new ArrayList<Usuario>();

		Evento evento = eventoService.findById(eventoId);

		usuariosIds.forEach(usuarioId -> {
			try {
				if (!eventoService.isParticipando(evento, usuarioId) && !eventoService.verificarAutor(usuarioId, evento)) {
					usuarios.add(usuarioService.findById(usuarioId));
				}
			} catch (UsuarioNotFoundException e) {}
		});
		
		evento.getConvite().enviarConvite(usuarios);

		Evento eventodb = this.eventoService.salvar(evento);
		
		return converterDto(eventodb.getConvite());
	}

	public List<ConviteResponseDTO> listarConvitesUsuario(String destinatarioId) {
		return converterDtos(conviteRepository.findByDestinatariosId(destinatarioId));
	}
}
