package com.example.demo.googleservice;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.googlemodel.Features;
import com.example.demo.googlemodel.Image;
import com.example.demo.googlemodel.Json;
import com.example.demo.googlemodel.Requests;
import com.example.demo.googlemodel.Salida;
import com.example.demo.googlemodel.TextInput;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BucketGetOption;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;

import reactor.core.publisher.Mono;

@Service
public class GoogleService {
	Properties prop = new Properties();
	String configProp = "config.properties";

	public Mono<String> urlImagen(Json recibe) throws IOException {
		Logger logger = Logger.getLogger(GoogleService.class.getName());

		InputStream input = GoogleService.class.getClassLoader().getResourceAsStream(configProp);
		if (input == null) {
			logger.log(null, "NO HAY PROPIEDADES PARA EL PROYECTO");
		}
		prop.load(input);
		String key = prop.getProperty("API_KEY");
		String direccion = prop.getProperty("API_BASE_URL");
		String specific = prop.getProperty("API_BASE_URI");

		WebClient.Builder builder = WebClient.builder().baseUrl(direccion).defaultHeader(HttpHeaders.CONTENT_TYPE,
				MediaType.APPLICATION_JSON_VALUE);

		WebClient webClient = builder.build();
		return webClient.post().uri(specific + "?key={llave}", key).body(BodyInserters.fromValue(recibe)).exchange()
				.flatMap(x -> {
					if (!x.statusCode().is2xxSuccessful())
						return Mono.just(direccion + " Called. Error 4xx: " + x.statusCode() + "\n");
					return x.bodyToMono(String.class);
				});
	}

	public Mono<String> googleVision(MultipartFile file) throws IOException {
		Json solicitud = new Json();
		byte[] imagenString = toBytes(file);
		if (imagenString == null) {
			throw new ServerException("El archivo debe ser una imagen");
		}

		String encodedString = Base64.getEncoder().encodeToString(imagenString);

		Image imagen = new Image();
		imagen.setContent(encodedString);

		InputStream input = GoogleService.class.getClassLoader().getResourceAsStream(configProp);
		prop.load(input);
		String googleType = prop.getProperty("API_GOOGLE_TYPE");

		String type = googleType;
		Features caracteristicas = new Features();
		caracteristicas.setType(type);

		Requests request = new Requests();
		ArrayList<Features> features = new ArrayList<>();
		features.add(caracteristicas);
		request.setFeatures(features);
		request.setImage(imagen);

		ArrayList<Requests> peticiones = new ArrayList<>();
		peticiones.add(request);
		solicitud.setRequests(peticiones);

		return urlImagen(solicitud);

	}
	
	public Salida googleStorage(MultipartFile file, String text) throws IOException {
		Salida salida = new Salida();
		if (text != null && !text.contentEquals("") && !text.isEmpty()) {
			byte[] imagenString = toBytes(file);
			if (imagenString == null) {
				throw new ServerException("El archivo debe ser una imagen");
			}

			InputStream input = GoogleService.class.getClassLoader().getResourceAsStream(configProp);
			prop.load(input);
			String bucketName = prop.getProperty("API_BUCKET");
			String blobName = prop.getProperty("API_BLOB");
			Storage storage1 = StorageOptions.getDefaultInstance().getService();
			Blob blob;
			Bucket bucket = storage1.get(bucketName, BucketGetOption.fields(Storage.BucketField.values()));

			Mono<String> vision = null;
			vision = googleVision(file);
			if (bucket == null) {
				bucket = storage1.create(BucketInfo.newBuilder(bucketName).setStorageClass(StorageClass.COLDLINE)
						.setLocation("asia").build());
				blob = bucket.create(blobName, imagenString);
			} else {
				bucket = bucket.toBuilder().setVersioningEnabled(true).build().update();
				blob = bucket.create(blobName, imagenString);
			}

			try {

				Gson gson = new Gson();
				TextInput jsonInput = gson.fromJson(text, TextInput.class);

				String[] respuestaVision = getValue(vision).split("\"description\": \"");
				String[] textoEncontrado = respuestaVision[1].split("\"");
				Boolean found = textoEncontrado[0].contains(jsonInput.getText());

				int lenght2 = jsonInput.getText().length(); // mayor o menor a 4
				int lenght = textoEncontrado[0].length() - 2; // 4
				String limpieza = textoEncontrado[0].substring(0, lenght);

				salida.setTextoRequerido(limpieza);
				salida.setRutaImagen(blob.getMediaLink());
				salida.setIsSuccess(found);

				if (lenght == lenght2)
					salida.setTextoEncontrado(jsonInput.getText());
				else if (lenght > lenght2) {
					salida.setTextoEncontrado("La palabra escrita es MENOR a la encontrada");
					salida.setIsSuccess(false);
				} else {
					salida.setTextoEncontrado("La palabra escrita es MAYOR a la encontrada");
					salida.setIsSuccess(false);
				}
			} catch (Exception e) {
				salida.setTextoRequerido("");
				salida.setRutaImagen("");
				salida.setIsSuccess(false);
				salida.setTextoEncontrado("Estructura incorrecta");
			}

		} else{
			salida.setTextoRequerido("");
			salida.setRutaImagen("");
			salida.setIsSuccess(false);
			salida.setTextoEncontrado("Falta imagen o texto");
		}
		return salida;

	}

	public byte[] toBytes(MultipartFile file) throws IOException {
		String fileName = file.getOriginalFilename();
		byte[] imagenString = null;
		if (fileName.contains(".")) {
			final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
			String[] allowedExt = { "jpg", "jpeg", "png", "gif" };
			for (String s : allowedExt) {
				if (extension.equals(s)) {
					imagenString = file.getBytes();
				}
			}
		}
		return imagenString;
	}

	public String getValue(Mono<String> mono1) {
		return mono1.block();
	}

}
