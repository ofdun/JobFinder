package com.ofdun.jobfinder.features.clients.vector.impl;

import com.ofdun.jobfinder.features.clients.vector.VectorClient;
import io.qdrant.client.QdrantClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BasicVectorClient implements VectorClient {
    private final QdrantClient client;
    private final String collectionName;
}
