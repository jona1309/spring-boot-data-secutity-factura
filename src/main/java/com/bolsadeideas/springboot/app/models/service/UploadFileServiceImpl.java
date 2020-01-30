package com.bolsadeideas.springboot.app.models.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileServiceImpl implements IUploadFileService {
	private final Logger log = LoggerFactory.getLogger(UploadFileServiceImpl.class);
	private final static String UPLOADS_FOLDER = "uploads";

	@Override
	public Resource load(String fileName) throws MalformedURLException {
		Path pathFoto = this.getPath(fileName);
		log.info("pathFoto: " + pathFoto);
		Resource recurso = null;

		recurso = new UrlResource(pathFoto.toUri());
		if (!recurso.exists() && !recurso.isReadable()) {
			throw new RuntimeException("Error: no se puede cargar la imagen: " + pathFoto);
		}

		return recurso;
	}

	@Override
	public String copy(MultipartFile file) throws IOException {
		// Path directorioRecursos = Paths.get("src//main//resources//static//uploads");
		// String rootPath = directorioRecursos.toFile().getAbsolutePath();
		String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		Path rootPath = this.getPath(uniqueFilename);
		log.info("rootPath: " + rootPath);
		// byte[] bytes = foto.getBytes();
		// Path rutaCompleta = Paths.get(rootPath.concat(foto.getOriginalFilename()));
		// Files.write(rutaCompleta, bytes);
		Files.copy(file.getInputStream(), rootPath);

		return uniqueFilename;
	}

	@Override
	public boolean delete(String fileName) {
		Path rootPath = this.getPath(fileName);
		File archivo = rootPath.toFile();
		if(archivo.exists() && archivo.canRead()) {
			return archivo.delete();
		}
		return false;
	}

	public Path getPath(String fileName) {
		return Paths.get(UPLOADS_FOLDER).resolve(fileName).toAbsolutePath();
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(Paths.get(UPLOADS_FOLDER).toFile());
	}

	@Override
	public void init() throws IOException {
		Files.createDirectory(Paths.get(UPLOADS_FOLDER));	
	}
}
