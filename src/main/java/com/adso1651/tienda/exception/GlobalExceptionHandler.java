package com.adso1651.tienda.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.adso1651.tienda.dto.HttpGlobalResponseDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Reto extra: centraliza el manejo de errores para no repetir try/catch en cada controller.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Errores de negocio (400, 401, 403, 404, 409...) */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Void>> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(HttpGlobalResponseDTO.of(ex.getMessage(), null));
    }

    /** Falla la validación de un DTO (@NotBlank, @Email, @Positive...) → 400 con los errores por campo */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(HttpGlobalResponseDTO.of("Datos de entrada inválidos", errors));
    }

    /** JSON mal formado o con tipos incorrectos → 400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(HttpGlobalResponseDTO.of("El cuerpo de la petición es inválido o está mal formado", null));
    }

    /** Un parámetro de ruta/consulta con tipo incorrecto (ej: /products/abc) → 400 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(HttpGlobalResponseDTO.of("El parámetro '" + ex.getName() + "' tiene un valor inválido", null));
    }

    /** Red de seguridad: violaciones de unicidad / llaves foráneas que se escapen de los servicios → 409 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<HttpGlobalResponseDTO<Void>> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(HttpGlobalResponseDTO.of("La operación viola una restricción de datos (duplicado o registro en uso)", null));
    }
}
