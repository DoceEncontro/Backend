package br.com.doceencontro.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.doceencontro.exception.exceptions.EventoNotFoundException;
import br.com.doceencontro.exception.exceptions.ForbiddenException;
import br.com.doceencontro.exception.exceptions.NotAutorException;
import br.com.doceencontro.exception.exceptions.NotConvidadoException;
import br.com.doceencontro.model.Chat;
import br.com.doceencontro.model.Convite;
import br.com.doceencontro.model.Endereco;
import br.com.doceencontro.model.Evento;
import br.com.doceencontro.model.Tipo;
import br.com.doceencontro.model.Usuario;
import br.com.doceencontro.model.dtos.EventoDetailsDTO;
import br.com.doceencontro.model.dtos.EventoRequestDTO;
import br.com.doceencontro.model.dtos.EventoResponseDTO;
import br.com.doceencontro.repository.EnderecoRepository;
import br.com.doceencontro.repository.EventoRepository;
import br.com.doceencontro.utils.EventoUtils;
import jakarta.transaction.Transactional;

@Service
public class EventoService {

	private EventoRepository eventoRepository;

	private EnderecoRepository enderecoRepository;

	private UsuarioService usuarioService;

	public EventoService(EventoRepository eventoRepository, UsuarioService usuarioService,
			EnderecoRepository enderecoRepository) {
		this.eventoRepository = eventoRepository;
		this.usuarioService = usuarioService;
		this.enderecoRepository = enderecoRepository;
	}

	public List<Evento> findAll() {
		return eventoRepository.findAll();
	}

	public Evento findById(String id) {
		Optional<Evento> buscarEvento = eventoRepository.findById(id);

		if (buscarEvento.isEmpty()) {
			throw new EventoNotFoundException();
		}

		return buscarEvento.get();
	}

	public Evento salvar(Evento evento) {
		return eventoRepository.save(evento);
	}

	private EventoDetailsDTO converterParticipantesDto(Evento evento) {
		return new EventoDetailsDTO(evento);
	}

	private EventoResponseDTO converterDto(Evento evento) {
		return new EventoResponseDTO(evento);
	}

	private List<EventoResponseDTO> converterDtos(List<Evento> evento) {
		return evento.stream().map(e -> new EventoResponseDTO(e)).collect(Collectors.toList());
	}

	public List<EventoResponseDTO> obterTodos() {
		return converterDtos(eventoRepository.findAll());
	}

	public EventoDetailsDTO obterPorId(String id) {
		Evento buscarEvento = findById(id);

		return converterParticipantesDto(buscarEvento);
	}

	public EventoResponseDTO registrarEvento(String organizadorId, EventoRequestDTO eventoDTO) {
		Usuario buscarOrganizador = usuarioService.findById(organizadorId);

		Endereco novoEndereco = new Endereco(null, eventoDTO.local(), eventoDTO.estado(), eventoDTO.cidade(),
				eventoDTO.rua(), eventoDTO.numero(), null);

		Evento novoEvento = new Evento(null, eventoDTO.titulo(), eventoDTO.descricao(),
				Tipo.fromString(eventoDTO.tipo()), eventoDTO.data(), true, novoEndereco, buscarOrganizador,
				new ArrayList<Usuario>(), null, null,
				null, null);

		novoEndereco.setEvento(novoEvento);
		novoEvento.setConvite(new Convite(novoEvento));
		novoEvento.setChat(new Chat(novoEvento));

		novoEvento.addParticipante(buscarOrganizador);
		novoEvento.getChat().adicionarParticipante(buscarOrganizador);

		return new EventoResponseDTO(eventoRepository.save(novoEvento));
	}

	public EventoResponseDTO editarEvento(String eventoId, EventoRequestDTO eventoDTO, String autorId) {
		Evento buscarEvento = findById(eventoId);

		if (!EventoUtils.verificarAutor(autorId, buscarEvento)) {
			throw new NotAutorException();
		}

		Evento eventoEditado = eventoRepository.save(buscarEvento.editar(eventoDTO));

		return converterDto(eventoEditado);
	}

	@Transactional
	public String excluirEvento(String eventoId, String autorId) {
		Evento buscarEvento = findById(eventoId);

		if (!EventoUtils.verificarAutor(autorId, buscarEvento)) {
			throw new NotAutorException();
		}

		eventoRepository.excluir(buscarEvento.getId());
		enderecoRepository.excluir(buscarEvento.getEndereco().getId());

		return "Evento excluído com sucesso.";
	}

	public String participar(String eventoId, String usuarioId) {
		Evento buscarEvento = findById(eventoId);
		Usuario buscarUsuario = usuarioService.findById(usuarioId);

		Optional<Usuario> buscarConvidado = buscarEvento.getConvite().getDestinatarios().stream()
				.filter(c -> c.getId().equals(usuarioId))
				.findFirst();

		if (buscarConvidado.isEmpty()) {
			throw new NotConvidadoException();
		}

		buscarEvento.addParticipante(buscarUsuario);

		buscarEvento.getChat().adicionarParticipante(buscarUsuario);

		buscarEvento.getConvite().getDestinatarios().remove(buscarUsuario);

		eventoRepository.save(buscarEvento);

		return "Usuário adicionado com sucesso";
	}

	public String removerParticipacao(String eventoId, String usuarioId) {
		Evento buscarEvento = findById(eventoId);
		Usuario buscarUsuario = usuarioService.findById(usuarioId);

		if (EventoUtils.verificarAutor(usuarioId, buscarEvento)) {
			throw new ForbiddenException("Organizadores não podem retirar a participação.");
		}
		EventoUtils.garantirParticipacao(buscarEvento, usuarioId);

		buscarEvento.removerParticipante(buscarUsuario);

		buscarEvento.getChat().removerParticipante(buscarUsuario);

		eventoRepository.save(buscarEvento);

		return "Participação removida com sucesso.";
	}

	public List<EventoResponseDTO> listarAtivos(String usuarioId) {
		return converterDtos(eventoRepository.listarEventosAtivos(usuarioId));
	}

	public List<String> tiposDeEvento() {
		Tipo[] tipos = Tipo.values();
		List<String> tiposString = new ArrayList<String>();
		for (Tipo tipo : tipos) {
			tiposString.add(tipo.toString());
		}
		return tiposString;
	}

	@Transactional
	public String desativarEvento(String eventoId, String autorId) {
		Evento buscarEvento = findById(eventoId);

		if (!EventoUtils.verificarAutor(autorId, buscarEvento)) {
			throw new NotAutorException();
		}

		buscarEvento.setAtivo(false);
		eventoRepository.save(buscarEvento);

		return "Evento desativado com sucesso.";
	}

}
