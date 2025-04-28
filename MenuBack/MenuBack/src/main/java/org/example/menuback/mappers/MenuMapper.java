package org.example.menuback.mappers;


import org.example.menuback.dtos.MenuDTO;

import org.example.menuback.modelo.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuMapper extends GenericMapper<MenuDTO, Menu>{


    Menu toEntityFromCADTO(MenuDTO.MenuCADTO menucrearDTO);
}
