package co.habitarinmobiliaria.middleware_service.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "historico_inmubles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoInmubles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_numerico_inmueble")
    private String codigoNumerico;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    @Column(name = "cliente_asociado")
    private Long clienteAsociado;

    @Column(name = "estado_codigo")
    private String estadoCodigo;
}
