package com.moretolearn;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/employees")
public class EmployeeController {

	@Autowired
	private EmployeeService service;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	Executor streamingExecutor;

	// Normal 
	@GetMapping(value = "/stream1", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Employee> streamEmployees1() {
		return service.loadEmployees();
	}

	//Emitter = push + async + events
	@GetMapping(value = "/stream2", produces = "application/x-ndjson")
	public ResponseEntity<ResponseBodyEmitter> streamEmployees2() {

		ResponseBodyEmitter emitter = new ResponseBodyEmitter();

		streamingExecutor.execute(() -> {
			transactionTemplate.executeWithoutResult(status -> {
				try (Stream<Employee> stream = service.loadEmployees1()) {
					stream.forEach(emp -> send(emp, emitter));
					emitter.complete();
				} catch (Exception e) {
					emitter.completeWithError(e);
				}
			});
		});

		return ResponseEntity.ok().header("Cache-Control", "no-cache").header("X-Accel-Buffering", "no")
				.contentType(MediaType.valueOf("application/x-ndjson")).body(emitter);
	}

	//Body = pull + sync + DB
	@GetMapping(value = "/stream3", produces = "application/x-ndjson")
	public ResponseEntity<StreamingResponseBody> streamEmployees3() {
		StreamingResponseBody body = outputStream -> {
			transactionTemplate.executeWithoutResult(status -> {
				try (Stream<Employee> stream = service.loadEmployees1()) {
					stream.forEach(emp -> writeNdjson(emp, outputStream));
				}
			});
		};
		return ResponseEntity.ok().header("Cache-Control", "no-cache").header("X-Accel-Buffering", "no")
				.contentType(MediaType.valueOf("application/x-ndjson")).body(body);
	}

	private void send(Employee emp, ResponseBodyEmitter emitter) {
		try {
			emitter.send(objectMapper.writeValueAsString(emp) + "\n", MediaType.TEXT_PLAIN);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void writeNdjson(Employee emp, OutputStream outputStream) {
		try {
			String json = objectMapper.writeValueAsString(emp);
			outputStream.write(json.getBytes());
			outputStream.write('\n');
			outputStream.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@GetMapping(value = "/stream4", produces = "application/x-ndjson")
	public ResponseEntity<ResponseBodyEmitter> streamEmployees4() {

	    ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L); // no timeout

	    streamingExecutor.execute(() -> {
	        try {
	            int page = 0;
	            int size = 1000; // small batches

	            while (true) {
	                List<Employee> batch = service.loadEmployeesPage(page, size);

	                if (batch.isEmpty()) {
	                    break;
	                }

	                for (Employee emp : batch) {
	                    emitter.send(toNdJson(emp));
	                }

	                page++;
	            }

	            emitter.complete();
	        } catch (Exception ex) {
	            emitter.completeWithError(ex);
	        }
	    });

	    return ResponseEntity.ok()
	            .header("Cache-Control", "no-cache")
	            .header("X-Accel-Buffering", "no")
	            .contentType(MediaType.valueOf("application/x-ndjson"))
	            .body(emitter);
	}
	
	private String toNdJson(Employee emp) throws JsonProcessingException {
	    return objectMapper.writeValueAsString(emp) + "\n";
	}
	

}
