package com.ofdun.jobfinder.features.clients.ai.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OllamaResponse(
   String model,
   List<List<Double>> embeddings,
   @JsonProperty("total_duration") Long totalDuration,
   @JsonProperty("load_duration") Long loadDuration,
   @JsonProperty("prompt_eval_count") Long promptEvalCount
) {}
