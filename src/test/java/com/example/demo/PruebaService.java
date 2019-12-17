package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.googlemodel.Salida;
import com.example.demo.googleservice.GoogleService;

import reactor.core.publisher.Mono;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GoogleCloudApplication.class)
@WebAppConfiguration
class PruebaService {
	
	@Autowired
	GoogleService googleService;

	@Test
	void testGoogleVision() throws IOException {
		
		File file = new File("./imageTest/profuturo.jpg");
		Path path = file.toPath();

		String contentType = Files.probeContentType(path);
		byte[] byesImage = Files.readAllBytes(path);
		
	    MultipartFile multipartFile = new MockMultipartFile("file",
	            file.getName(), contentType, byesImage);
	    
	    Mono<String> visionResponseTest = googleService.googleVision(multipartFile);
	    String responseString = visionResponseTest.block();

	    assertEquals(contentType, "image/jpeg");
	    assertTrue(responseString.contains("description"));
	
	}

	@Test
	void testGoogleStorage() throws IOException {
		File file = new File("./imageTest/ProFuturo.jpg");
		Path path = file.toPath();

		String contentType = Files.probeContentType(path);
		byte[] byesImage = Files.readAllBytes(path);
		
	    MultipartFile multipartFile = new MockMultipartFile("file",
	            file.getName(), contentType, byesImage);
	    
	    String text = "{\"text\" : \"ProFuturo\"}";
	    Salida storageResponseTest = googleService.googleStorage(multipartFile, text);
	    assertTrue(storageResponseTest.getIsSuccess());
	}
	
	@Test
	void testGoogleStorageLongText() throws IOException {
		File file = new File("./imageTest/ProFuturo.jpg");
		Path path = file.toPath();

		String contentType = Files.probeContentType(path);
		byte[] byesImage = Files.readAllBytes(path);
		
	    MultipartFile multipartFile = new MockMultipartFile("file",
	            file.getName(), contentType, byesImage);
	    
	    String text = "{\"text\" : \"ProFuturodvcxczxzd\"}";
	    Salida storageResponseTest = googleService.googleStorage(multipartFile, text);
	    assertEquals("La palabra escrita es MAYOR a la encontrada", storageResponseTest.getTextoEncontrado());
	}
	
	@Test
	void testGoogleStorageShortText() throws IOException {
		File file = new File("./imageTest/ProFuturo.jpg");
		Path path = file.toPath();

		String contentType = Files.probeContentType(path);
		byte[] byesImage = Files.readAllBytes(path);
		
	    MultipartFile multipartFile = new MockMultipartFile("file",
	            file.getName(), contentType, byesImage);
	    
	    String text = "{\"text\" : \"ProFu\"}";
	    Salida storageResponseTest = googleService.googleStorage(multipartFile, text);
	    assertEquals("La palabra escrita es MENOR a la encontrada", storageResponseTest.getTextoEncontrado());
	}
	
	@Test
	void testGoogleStorageEmptyText() throws IOException {
		File file = new File("./imageTest/ProFuturo.jpg");
		Path path = file.toPath();

		String contentType = Files.probeContentType(path);
		byte[] byesImage = Files.readAllBytes(path);
		
	    MultipartFile multipartFile = new MockMultipartFile("file",
	            file.getName(), contentType, byesImage);
	    
	    String text = "";
	    Salida storageResponseTest = googleService.googleStorage(multipartFile, text);
	    assertEquals("Falta imagen o texto", storageResponseTest.getTextoEncontrado());
	}
	
	@Test
	void testGoogleStorageBadStructure() throws IOException {
		File file = new File("./imageTest/ProFuturo.jpg");
		Path path = file.toPath();

		String contentType = Files.probeContentType(path);
		byte[] byesImage = Files.readAllBytes(path);
		
	    MultipartFile multipartFile = new MockMultipartFile("file",
	            file.getName(), contentType, byesImage);
	    
	    String text = "text:hola";
	    Salida storageResponseTest = googleService.googleStorage(multipartFile, text);
	    assertEquals("Estructura incorrecta", storageResponseTest.getTextoEncontrado());
	}
}