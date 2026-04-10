package com.ofdun.jobfinder.features.resume.domain.chain;

import com.ofdun.jobfinder.features.clients.ai.AiClient;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmbeddingResumeHandler extends ResumeHandler {
    private final AiClient aiClient;

    @Override
    protected Optional<ResumeModel> execute(ResumeModel resume) {
        var embedding = aiClient.getEmbedding(
                resume.toString()
        );
        resume.setEmbedding(embedding);
        return Optional.of(resume);
    }
}
