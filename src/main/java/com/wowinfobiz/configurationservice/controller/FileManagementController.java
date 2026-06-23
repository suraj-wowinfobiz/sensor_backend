package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wowinfobiz.configurationservice.model.StoredFileEntity;
import com.wowinfobiz.configurationservice.service.FileStorageService;
import com.wowinfobiz.configurationservice.service.GenericResourceService;
import com.wowinfobiz.configurationservice.service.JsonMapperService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class FileManagementController {

    private final FileStorageService fileStorageService;
    private final GenericResourceService genericResourceService;
    private final JsonMapperService jsonMapperService;

    public FileManagementController(FileStorageService fileStorageService,
                                    GenericResourceService genericResourceService,
                                    JsonMapperService jsonMapperService) {
        this.fileStorageService = fileStorageService;
        this.genericResourceService = genericResourceService;
        this.jsonMapperService = jsonMapperService;
    }

    @PostMapping("/upload/sensor-config")
    public JsonNode uploadSensorConfig(@RequestParam("file") MultipartFile file) throws IOException {
        return fileStorageService.save("sensor-config", file);
    }

    @PostMapping("/upload/device-config")
    public JsonNode uploadDeviceConfig(@RequestParam("file") MultipartFile file) throws IOException {
        return fileStorageService.save("device-config", file);
    }

    @PostMapping("/upload/bulk-import")
    public JsonNode uploadBulkImport(@RequestParam("file") MultipartFile file) throws IOException {
        return fileStorageService.save("bulk-import", file);
    }

    @PostMapping("/upload/avatar")
    public JsonNode uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        return fileStorageService.save("avatar", file);
    }

    @PostMapping("/upload/document")
    public JsonNode uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        return fileStorageService.save("document", file);
    }

    @GetMapping("/download/template/{type}")
    public JsonNode downloadTemplate(@PathVariable String type) {
        ObjectNode payload = (ObjectNode) jsonMapperService.fromJson("{}");
        payload.put("type", type);
        payload.put("status", "template-requested");
        return genericResourceService.create("download-template", payload);
    }

    @GetMapping("/download/export/{id}")
    public JsonNode downloadExport(@PathVariable UUID id) {
        return genericResourceService.get("export-job", id);
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable UUID id) {
        StoredFileEntity file = fileStorageService.get(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.getFileName()).build().toString())
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getContent());
    }
}
