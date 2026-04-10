FROM ollama/ollama:0.19.0-rc1

ENV OLLAMA_HOST=0.0.0.0:11434

RUN ollama serve & \
    sleep 5 && \
    ollama pull mxbai-embed-large && \
    pkill ollama
