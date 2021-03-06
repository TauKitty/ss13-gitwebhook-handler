package io.github.spair.controller;

import io.github.spair.service.config.ConfigService;
import io.github.spair.service.config.entity.HandlerConfig;
import io.github.spair.service.config.entity.HandlerConfigStatus;
import io.github.spair.service.github.GitHubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/config/rest")
public class ConfigRestController {

    private final String logName;

    private final ConfigService configService;
    private final GitHubRepository gitHubRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRestController.class);

    @Autowired
    public ConfigRestController(
            final ConfigService configService,
            final Environment env,
            final GitHubRepository gitHubRepository) {
        this.configService = configService;
        this.logName = env.getProperty("logging.file");
        this.gitHubRepository = gitHubRepository;
    }

    @GetMapping("/current")
    public HandlerConfig getCurrentConfig() {
        return configService.getConfig();
    }

    @PutMapping("/current")
    public void saveConfig(@RequestBody final HandlerConfig configuration) throws IOException {
        configService.importConfig(configuration);
    }

    @GetMapping("/file")
    public FileSystemResource downloadConfigFile(final HttpServletResponse response) {
        LOGGER.info("Configuration file downloaded");
        setResponseAsFile(response, ConfigService.CONFIG_NAME);
        return new FileSystemResource(configService.exportConfig());
    }

    @GetMapping("/log")
    public FileSystemResource downloadLogFile(final HttpServletResponse response) {
        LOGGER.info("Log file downloaded");
        setResponseAsFile(response, logName);
        return new FileSystemResource(new File(logName));

    }

    @PostMapping("/validation")
    public ResponseEntity validateConfig(@RequestBody final HandlerConfig config) {
        HandlerConfigStatus configStatus = configService.validateConfig(config);
        HttpStatus responseStatus = configStatus.allOk ? HttpStatus.OK : HttpStatus.NOT_ACCEPTABLE;
        return new ResponseEntity<>(configStatus, responseStatus);
    }

    @GetMapping("/repos/master")
    public String getMasterRepoInitStatus() {
        return gitHubRepository.getMasterRepoInitStatus().name();
    }

    @PutMapping("/repos/master")
    public void initializeMasterRepo() {
        gitHubRepository.initMasterRepository();
    }

    @DeleteMapping("/repos")
    public void cleanAllRepos() {
        gitHubRepository.cleanReposFolder();
    }

    private void setResponseAsFile(final HttpServletResponse response, final String fileName) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(fileName));
    }
}
