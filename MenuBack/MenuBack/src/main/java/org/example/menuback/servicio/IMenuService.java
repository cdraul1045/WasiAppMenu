package org.example.menuback.servicio;

import org.example.menuback.dtos.MenuDTO;
import org.example.menuback.modelo.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IMenuService extends ICrudGenericoService<Menu, Long>{
    
    MenuDTO saveD(MenuDTO.MenuCADTO dto);

    MenuDTO updateD(MenuDTO.MenuCADTO dto, Long id);

    Page<Menu> listaPage(Pageable pageable);
}
