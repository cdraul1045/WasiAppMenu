package org.example.menuback.servicio.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.menuback.dtos.MenuDTO;
import org.example.menuback.mappers.MenuMapper;
import org.example.menuback.modelo.Menu;
import org.example.menuback.modelo.Usuario;
import org.example.menuback.repositorio.ICrudGenericoRepository;
import org.example.menuback.repositorio.IMenuRepository;
import org.example.menuback.servicio.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;


@Service
@Transactional
@RequiredArgsConstructor

public class MenuServiceImp extends CrudGenericoServiceImp <Menu, Long> implements IMenuService {


    @Autowired
    private DataSource dataSource;

    private final IMenuRepository repo;
    private final MenuMapper menuMapper;



    @Override
    protected ICrudGenericoRepository<Menu, Long> getRepo() {
        return repo;
    }

    @Override
    public MenuDTO saveD(MenuDTO.MenuCADTO dto) {
        Menu menu = menuMapper.toEntityFromCADTO(dto);
        Menu menuGuardado = repo.save(menu);
        return menuMapper.toDTO(menuGuardado);
    }

    @Override
    public MenuDTO updateD(MenuDTO.MenuCADTO dto, Long id) {
        Menu menuExistente = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Menu no encontrado"));

        Menu menuActualizado = menuMapper.toEntityFromCADTO(dto);
        menuActualizado.setIdMenu(menuExistente.getIdMenu());

        Menu menuGuardado = repo.save(menuActualizado);
        return menuMapper.toDTO(menuGuardado);
    }



    public Page<Menu> listaPage(Pageable pageable) {
        return repo.findAll(pageable);
    }
}
