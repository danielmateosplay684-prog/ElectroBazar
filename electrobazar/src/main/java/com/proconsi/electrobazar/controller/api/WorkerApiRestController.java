package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.Worker;
import com.proconsi.electrobazar.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerApiRestController {

    private final WorkerService workerService;

    @GetMapping
    public ResponseEntity<List<Worker>> getAll() {
        return ResponseEntity.ok(workerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Worker> getById(@PathVariable Long id) {
        return workerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Worker> create(@RequestBody Worker worker) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workerService.save(worker));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Worker> update(@PathVariable Long id, @RequestBody Worker worker) {
        worker.setId(id);
        return ResponseEntity.ok(workerService.save(worker));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Optional<Worker> worker = workerService.login(username, password);

        if (worker.isPresent()) {
            return ResponseEntity.ok(worker.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        }
    }
}
