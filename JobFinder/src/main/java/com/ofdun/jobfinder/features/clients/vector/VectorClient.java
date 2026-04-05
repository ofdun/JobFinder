package com.ofdun.jobfinder.features.clients.vector;

import io.qdrant.client.QdrantClient;

public interface VectorClient {
    QdrantClient getClient();

    String getCollectionName();
}
