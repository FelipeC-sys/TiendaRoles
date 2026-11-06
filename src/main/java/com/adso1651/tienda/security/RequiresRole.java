package com.adso1651.tienda.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.adso1651.tienda.enums.RoleEnum;

// Define donde quiero usar mi anotación: en métodos y también a nivel de clase (reto extra)
@Target({ ElementType.METHOD, ElementType.TYPE })
// RUNTIME: disponible en tiempo de ejecución, en todo momento (esto es necesario para que el interceptor lo lea)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    RoleEnum[] value();
}
