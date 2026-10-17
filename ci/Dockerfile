FROM docker:27.5.1-cli AS docker
FROM eclipse-temurin:25-jdk-noble
COPY --from=docker /usr/local/bin/docker /usr/local/bin/docker
RUN apt-get update && apt-get install -y --no-install-recommends git curl jq python3 tcpdump ca-certificates && apt-get clean
WORKDIR /workspace
ENV GRADLE_USER_HOME=/workspace/.ci-gradle
ENV JAVA_TOOL_OPTIONS=-Djava.net.preferIPv4Stack=true
ENTRYPOINT ["bash", "ci/inside.sh"]
