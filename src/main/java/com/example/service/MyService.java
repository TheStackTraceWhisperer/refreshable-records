package com.example.service;

import com.example.config.MyConfigRecord;
import com.example.config.RefreshableMyConfigRecord; // This is generated
import org.springframework.stereotype.Service;

@Service
public class MyService {

    private final RefreshableMyConfigRecord properties;

    public MyService(RefreshableMyConfigRecord properties) {
        this.properties = properties;
    }

    public void performAction() {
        // Calling .current() fetches the specific immutable Record instance
        // representing the state of configuration at this exact moment.
        MyConfigRecord currentConfig = properties.current();
        
        System.out.println("Connecting to: " + currentConfig.apiUrl());
        System.out.println("Max Retries: " + currentConfig.maxRetries());
    }
}
