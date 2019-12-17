package com.example.demo.googlecontroller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.googlemodel.Json;
import com.example.demo.googleservice.GoogleService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/google")
public class GoogleController {

	@Autowired
	GoogleService servicio;
	@Autowired
	Json solicitud;

	@PostMapping(value = "/subirImagen",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<Object> imagen(@RequestParam("file") MultipartFile file, @RequestPart("text") String text) throws IOException
	{
		Flux<Object> flujo = Flux.merge(Mono.just(servicio.googleStorage(file, text)),servicio.googleVision(file));
		Mono<List<Object>> salida = flujo.collectList();
		return salida.map(x -> x.get(0));
	}
}
