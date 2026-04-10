FROM ollama/ollama:0.19.0-rc1

COPY init.sh /init.sh
RUN chmod +x /init.sh

ENTRYPOINT ["/init.sh"]
