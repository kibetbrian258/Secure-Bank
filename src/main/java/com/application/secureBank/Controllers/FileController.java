package com.application.secureBank.Controllers;

import com.application.secureBank.Services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = {"http://localhost:4200", "https://secure-bank-sb.vercel.app"})
@RequiredArgsConstructor
@Tag(name = "Files", description = "API endpoints for file management")
@Slf4j
public class FileController {
    private final FileStorageService fileStorageService;

    @GetMapping("/{customerId}/{filename}")
    @Operation(
            summary = "Get file by path",
            description = "Retrieves a file from the file system by its path"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<byte[]> getFile(
            @PathVariable String customerId,
            @PathVariable String filename) {
        try {
            String filePath = customerId + "/" + filename;
            byte[] fileContent = fileStorageService.getFile(filePath);

            // Determine content type based on file extension
            String contentType = determineContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileContent);

        } catch (IOException e) {
            log.error("Error retrieving file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Determines content type based on file extension
     */
    private String determineContentType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }
}