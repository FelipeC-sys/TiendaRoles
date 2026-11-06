package com.adso1651.tienda.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adso1651.tienda.filter.JwtValidationFilter;

@Configuration // Fábrica de beans
public class FilterConfig {

    @Bean
    FilterRegistrationBean<JwtValidationFilter> jwtFilter(JwtValidationFilter jwtValidationFilter) {
        // Contenedor de registro del bean para el filtro
        FilterRegistrationBean<JwtValidationFilter> registrationBean = new FilterRegistrationBean<>();

        // Le decimos a Spring cuál es el filtro con el que queremos trabajar
        registrationBean.setFilter(jwtValidationFilter);

        // Alcance: que revise todas las peticiones que entren a la API
        // (las rutas públicas se excluyen dentro del filtro, en shouldNotFilter)
        registrationBean.addUrlPatterns("/*");

        // Prioridad de ejecución: se ejecuta antes que los otros filtros internos
        registrationBean.setOrder(1);

        return registrationBean;
    }
}
