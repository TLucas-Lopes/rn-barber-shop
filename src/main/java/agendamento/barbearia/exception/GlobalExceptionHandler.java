package agendamento.barbearia.exception;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 - validação @Valid no body (DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {

        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(" | "));

        ApiError body = build(HttpStatus.BAD_REQUEST, "Validation error", msg, req);
        log.warn("400 VALIDATION path={} msg={}", req.getRequestURI(), msg);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private String formatFieldError(FieldError fe) {
        String field = fe.getField();
        String defaultMessage = fe.getDefaultMessage();
        Object rejected = fe.getRejectedValue();
        return field + ": " + (defaultMessage != null ? defaultMessage : "inválido") +
                (rejected != null ? " (valor=" + rejected + ")" : "");
    }

    // 400 - JSON malformado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ApiError body = build(HttpStatus.BAD_REQUEST, "Malformed JSON", "JSON inválido no body.", req);
        log.warn("400 JSON_MALFORMED path={} err={}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 - validação em params (se usar @Validated + @RequestParam etc.)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        ApiError body = build(HttpStatus.BAD_REQUEST, "Constraint violation", ex.getMessage(), req);
        log.warn("400 CONSTRAINT path={} msg={}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 - param obrigatório faltando (?data=...)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String msg = "Parâmetro obrigatório ausente: " + ex.getParameterName();
        ApiError body = build(HttpStatus.BAD_REQUEST, "Missing parameter", msg, req);
        log.warn("400 MISSING_PARAM path={} msg={}", req.getRequestURI(), msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 - tipo errado em param/path (ex: id=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Parâmetro inválido: " + ex.getName();
        ApiError body = build(HttpStatus.BAD_REQUEST, "Type mismatch", msg, req);
        log.warn("400 TYPE_MISMATCH path={} param={} value={}", req.getRequestURI(), ex.getName(), ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 409 - conflito de banco (unique, FK, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ApiError body = build(HttpStatus.CONFLICT, "Database conflict",
                "Conflito no banco (registro duplicado ou inválido).", req);

        log.warn("409 DATA_INTEGRITY path={} err={}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // erros que você já lança (ResponseStatusException)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        String reason = ex.getReason();
        String msg = (reason != null && !reason.isBlank()) ? reason : status.getReasonPhrase();

        ApiError body = build(status, "Request error", msg, req);

        if (status.is4xxClientError()) {
            log.warn("{} RESPONSE_STATUS path={} msg={}", status.value(), req.getRequestURI(), msg);
        } else {
            log.error("{} RESPONSE_STATUS path={} msg={}", status.value(), req.getRequestURI(), msg, ex);
        }

        return ResponseEntity.status(status).body(body);
    }

    // fallback final (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        ApiError body = build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Erro inesperado. Tente novamente.", req);

        log.error("500 UNEXPECTED path={} err={}", req.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiError build(HttpStatus status, String error, String message, HttpServletRequest req) {
        return new ApiError(
                OffsetDateTime.now(),
                status.value(),
                error,
                message,
                req.getRequestURI()
        );
    }
}
