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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/google")
@Api(value = "Google Vision and Storage Microservice", description = "This API consumes two Google services: Vision and Storage")
public class GoogleController {

	@Autowired
	GoogleService servicio;
	@Autowired
	Json solicitud;
	
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "200 OK"),
			@ApiResponse(code = 500, message = "500 Bad Request")
	})

	@PostMapping(value = "/subirImagen",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiOperation(value = "Extract text from an Image and Storage it into a bucket", notes = "This is asynchronus and return true if an image was storage")
	public Mono<Object> imagen(@ApiParam(value = "A file with image extension to storage and analize its text", example = "image.jpeg") @RequestParam("file") MultipartFile file,
			@ApiParam(value = "A JSON with the text to find in the image", example = "{\"text\" : \"Google\"}") @RequestPart("text") String text) throws IOException
	{
		Flux<Object> flujo = Flux.merge(Mono.just(servicio.googleStorage(file, text)),servicio.googleVision(file));
		Mono<List<Object>> salida = flujo.collectList();
		return salida.map(x -> x.get(0));
	}
}
