package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.dtos.HistoricoInmublesDTO;
import co.habitarinmobiliaria.middleware_service.entities.HistoricoInmubles;
import co.habitarinmobiliaria.middleware_service.repository.HistoricoInmublesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricoInmublesService {

    private final HistoricoInmublesRepository historicoInmublesRepository;

    /**
     * Guarda un único registro de histórico de inmueble.
     */
    @Transactional
    public HistoricoInmubles guardar(HistoricoInmublesDTO dto) {
        HistoricoInmubles entity = new HistoricoInmubles();
        entity.setCodigoNumerico(dto.getCodigoNumerico());
        entity.setClienteAsociado(dto.getClienteAsociado());
        entity.setEstadoCodigo(dto.getEstadoCodigo());
        entity.setFechaCreacion(OffsetDateTime.now());

        HistoricoInmubles saved = historicoInmublesRepository.save(entity);
        log.info("Histórico guardado con id={}", saved.getId());
        return saved;
    }

    /**
     * Guarda un lote de registros de histórico de inmuebles.
     */
    @Transactional
    public List<HistoricoInmubles> guardarLote(List<HistoricoInmublesDTO> dtos) {
        List<HistoricoInmubles> entities = dtos.stream().map(dto -> {
            HistoricoInmubles entity = new HistoricoInmubles();
            entity.setCodigoNumerico(dto.getCodigoNumerico());
            entity.setClienteAsociado(dto.getClienteAsociado());
            entity.setEstadoCodigo(dto.getEstadoCodigo());
            entity.setFechaCreacion(OffsetDateTime.now());
            return entity;
        }).toList();

        List<HistoricoInmubles> saved = historicoInmublesRepository.saveAll(entities);
        log.info("Lote de {} registros históricos guardados", saved.size());
        return saved;
    }

    /**
     * Obtiene todos los registros históricos.
     */
    @Transactional(readOnly = true)
    public List<HistoricoInmubles> obtenerTodos() {
        return historicoInmublesRepository.findAll();
    }

    /**
     * Obtiene registros históricos por cliente asociado.
     */
    @Transactional(readOnly = true)
    public List<HistoricoInmubles> obtenerPorClienteAsociado(Long clienteAsociado) {
        return historicoInmublesRepository.findByClienteAsociado(clienteAsociado);
    }
}
