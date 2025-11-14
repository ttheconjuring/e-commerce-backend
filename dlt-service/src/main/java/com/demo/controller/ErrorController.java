package com.demo.controller;

import com.demo.model.DltMessageDTO;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/error")
@RequiredArgsConstructor
public class ErrorController {

    private final DltMessageService dltMessageService;

    @GetMapping("/check")
    public ResponseEntity<List<DltMessageDTO>> listErrors() {
        return ResponseEntity.ok(this.dltMessageService.retrieveAll());
    }

}
