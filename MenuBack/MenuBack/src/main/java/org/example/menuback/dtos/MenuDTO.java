package org.example.menuback.dtos;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MenuDTO {

    private Long idMenu;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;



    public record MenuCADTO(
            Long idMenu,
            String nombre,
            String descripcion,
            BigDecimal precio

    ){}


}
