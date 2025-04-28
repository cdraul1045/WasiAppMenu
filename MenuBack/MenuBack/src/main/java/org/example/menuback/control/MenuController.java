package org.example.menuback.control;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.menuback.dtos.MenuDTO;
import org.example.menuback.mappers.MenuMapper;
import org.example.menuback.modelo.Menu;
import org.example.menuback.servicio.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/menus")
public class MenuController {

    private final IMenuService menuService;

    private final MenuMapper menuMapper;




    @GetMapping
    public ResponseEntity<List<MenuDTO>> findAll() {
        List<MenuDTO> list = menuMapper.toDTOs(menuService.findAll());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<MenuDTO> findById(@PathVariable("id") Long id) {
        Menu obj = menuService.findById(id);
        return ResponseEntity.ok(menuMapper.toDTO(obj));
    }

    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody MenuDTO.MenuCADTO dto) {
        MenuDTO obj = menuService.saveD(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getIdMenu()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuDTO> update(@Valid @RequestBody MenuDTO.MenuCADTO dto, @PathVariable("id") Long id) {
        MenuDTO obj = menuService.updateD(dto, id);
        return ResponseEntity.ok(obj);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pageable")
    public ResponseEntity<org.springframework.data.domain.Page<MenuDTO>> listPage(Pageable pageable){
        Page<MenuDTO> page = menuService.listaPage(pageable).map(e -> menuMapper.toDTO(e));
        return ResponseEntity.ok(page);
    }
}
