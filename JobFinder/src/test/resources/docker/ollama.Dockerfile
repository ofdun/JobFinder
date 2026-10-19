FROM ollama/ollama:0.19.0-rc1

ENV OLLAMA_HOST=0.0.0.0:11434

COPY src/test/resources/docker/prepare-ollama.sh /tmp/prepare-ollama.sh
RUN sh /tmp/prepare-ollama.sh
