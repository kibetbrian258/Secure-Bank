package com.application.secureBank.Config;

import com.application.secureBank.Services.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Configuration that runs on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppStartupConfig implements CommandLineRunner {

    private final FileStorageService fileStorageService;

    @Override
    public void run(String... args) {
        // Initialize required directories
        log.info("Initializing file storage directories...");
        fileStorageService.init();
    }
}