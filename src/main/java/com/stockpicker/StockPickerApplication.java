package com.stockpicker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.stockpicker.config.StockPickerProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(StockPickerProperties.class)
public class StockPickerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockPickerApplication.class, args);
    }
}
