package com.balgoorm.balgoorm_backend.ide.controller;

import com.balgoorm.balgoorm_backend.ide.model.dto.request.CodeRunRequest;
import com.balgoorm.balgoorm_backend.ide.model.dto.response.CodeRunResponse;
import com.balgoorm.balgoorm_backend.ide.service.IdeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ide")
public class IdeController {

    private final IdeService ideService;

    @PostMapping("/run")
    public ResponseEntity codeRun(@RequestBody CodeRunRequest executeRequest) throws IOException, InterruptedException {

        CodeRunResponse response = ideService.codeRun(executeRequest);

        return ResponseEntity.ok(response);
    }


}
