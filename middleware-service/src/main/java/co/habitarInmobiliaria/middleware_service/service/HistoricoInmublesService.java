package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.dtos.HistoricoInmublesDTO;
import co.habitarinmobiliaria.middleware_service.entities.HistoricoInmubles;
import co.habitarinmobiliaria.middleware_service.repository.HistoricoInmublesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricoInmublesService {

    private final HistoricoInmublesRepository historicoInmublesRepository;
    private final InmuebleMapperService inmuebleMapperService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Guarda un único registro de histórico de inmueble.
     */
    @Transactional
    public HistoricoInmubles guardar(HistoricoInmublesDTO dto) {
        HistoricoInmubles entity = new HistoricoInmubles();
        entity.setCodigoNumerico(normalizarCodigoOUrl(dto.getCodigoNumerico()));
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
            entity.setCodigoNumerico(normalizarCodigoOUrl(dto.getCodigoNumerico()));
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

    /**
     * Verificación simple de conectividad a la DB activa (Supabase/Postgres).
     */
    @Transactional(readOnly = true)
    public boolean verificarConexionDb() {
        Integer ok = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return ok != null && ok == 1;
    }

    /**
     * Acepta:
     * - Código directo (ej. 8116766, 6e0e775d, recXXXX)
     * - URL completa (ej. /casa-venta.../8116766 o /venta/6e0e775d)
     */
    private String normalizarCodigoOUrl(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            return limpio;
        }

        String extraido = inmuebleMapperService.extraerIdDeUrl(limpio);
        return (extraido != null && !extraido.isBlank()) ? extraido : limpio;
    }
}
