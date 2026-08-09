package io.github.prakash.kqe.operator.kubernetes.service;

import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexStorageService {
    private static final Path INDEX_ROOT = Path.of("/index");

    public void delete(KafkaQueryEngine resource) {

        String engineId = resource.getSpec().getEngineId();
        String indexPathString = resource.getStatus().getIndexPath();

        Path indexPath = Path.of(indexPathString)
                .toAbsolutePath()
                .normalize();
        log.info(
                "Deleting index for KQE {}: {}",
                engineId,
                indexPath);

        try {
            if (Files.exists(indexPath)) {
                FileUtils.deleteDirectory(indexPath.toFile());

                log.info(
                        "Successfully deleted index for KQE {}",
                        engineId);
            } else {
                log.info(
                        "Index directory does not exist for KQE {}",
                        engineId);
            }

        } catch (IOException e) {
            log.error(
                    "Failed to delete index for KQE {}: {}",
                    engineId,
                    indexPath,
                    e);

            throw new IllegalStateException(
                    "Failed to delete index directory " + indexPath,
                    e);
        }
    }
}
