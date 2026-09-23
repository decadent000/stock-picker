package com.stockpicker.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpicker.config.StockPickerProperties;
import com.stockpicker.model.AuctionSnapshot;

import org.springframework.stereotype.Component;

@Component
public class AuctionSnapshotStore {

    private final ObjectMapper objectMapper;
    private final Path dataDir;

    public AuctionSnapshotStore(ObjectMapper objectMapper, StockPickerProperties properties) {
        this.objectMapper = objectMapper;
        this.dataDir = Paths.get(properties.getDataDir());
    }

    public void save(LocalDate date, List<AuctionSnapshot> snapshots) {
        try {
            Files.createDirectories(dataDir);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file(date).toFile(), snapshots);
        } catch (IOException e) {
            throw new IllegalStateException("保存竞价快照失败: " + e.getMessage(), e);
        }
    }

    public List<AuctionSnapshot> load(LocalDate date) {
        Path path = file(date);
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(path.toFile(), new TypeReference<List<AuctionSnapshot>>() { });
        } catch (IOException e) {
            throw new IllegalStateException("读取竞价快照失败: " + e.getMessage(), e);
        }
    }

    private Path file(LocalDate date) {
        return dataDir.resolve("auction-" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + ".json");
    }
}
