package com.example.config;

import com.example.annotation.RefreshableRecord;

@RefreshableRecord(prefix = "app.config")
public record MyConfigRecord(
    String apiUrl, 
    int maxRetries
) {}
