package com.adso1651.tienda.enums;

public enum RoleEnum {
    ADMIN(1L),
    CLIENTE(2L),
    VENDEDOR(3L);

    private final Long id;

    RoleEnum(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
