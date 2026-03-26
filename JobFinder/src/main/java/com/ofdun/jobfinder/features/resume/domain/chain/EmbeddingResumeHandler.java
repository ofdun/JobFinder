package com.ofdun.jobfinder.features.resume.domain.chain;

import com.ofdun.jobfinder.features.clients.ai.AiClient;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingResumeHandler extends ResumeHandler {
    AiClient aiClient;

    @Override
    protected ResumeModel execute(ResumeModel resume) {
        var embedding = aiClient.getEmbedding(
                resume.toString()
        );
        resume.setEmbedding(embedding);
        return resume;
    }
}
