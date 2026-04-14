package co.habitarinmobiliaria.middleware_service.repository;

import co.habitarinmobiliaria.middleware_service.entities.HistoricoInmubles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoInmublesRepository extends JpaRepository<HistoricoInmubles, Long> {

    List<HistoricoInmubles> findByClienteAsociado(Long clienteAsociado);

    List<HistoricoInmubles> findByEstadoCodigo(String estadoCodigo);
}
