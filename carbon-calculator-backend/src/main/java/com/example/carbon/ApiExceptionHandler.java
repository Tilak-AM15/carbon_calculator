package com.example.carbon;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException ex) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status",400);
        body.put("error","Validation failed");
        body.put("details",ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField()+": "+e.getDefaultMessage()).toList());
        return ResponseEntity.badRequest().body(body);
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String,Object>> status(ResponseStatusException ex) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp",Instant.now().toString());
        body.put("status",ex.getStatusCode().value());
        body.put("error",ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}
