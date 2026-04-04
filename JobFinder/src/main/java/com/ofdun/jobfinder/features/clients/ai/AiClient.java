package com.ofdun.jobfinder.features.clients.ai;

import java.util.List;

public interface AiClient {
    List<Double> getEmbedding(String content);
}
