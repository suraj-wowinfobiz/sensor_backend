package com.wowinfobiz.configurationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wowinfobiz.configurationservice.model.StoredFileEntity;
import com.wowinfobiz.configurationservice.repository.StoredFileRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    private final StoredFileRepository storedFileRepository;
    private final JsonMapperService jsonMapperService;

    public FileStorageService(StoredFileRepository storedFileRepository, JsonMapperService jsonMapperService) {
        this.storedFileRepository = storedFileRepository;
        this.jsonMapperService = jsonMapperService;
    }

    public JsonNode save(String fileType, MultipartFile file) throws IOException {
        StoredFileEntity entity = new StoredFileEntity();
        entity.setFileType(fileType);
        entity.setFileName(file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename());
        entity.setContentType(file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType());
        entity.setContent(file.getBytes());

        StoredFileEntity saved = storedFileRepository.save(entity);
        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("id", saved.getId().toString());
        response.put("fileType", saved.getFileType());
        response.put("fileName", saved.getFileName());
        response.put("contentType", saved.getContentType());
        return response;
    }

    public StoredFileEntity get(UUID id) {
        return storedFileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("File not found"));
    }
}
