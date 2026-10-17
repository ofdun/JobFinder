FROM eclipse-temurin:25-jdk-noble
WORKDIR /benchmark
COPY src/ src/
RUN javac -d classes src/com/ofdun/jobfinder/benchmark/VacancyBenchmark.java && java -cp classes com.ofdun.jobfinder.benchmark.VacancyBenchmark --verify
ENTRYPOINT ["java", "-Xms128m", "-Xmx128m", "-XX:+UseSerialGC", "-XX:ActiveProcessorCount=1", "-Xbatch", "-cp", "classes", "com.ofdun.jobfinder.benchmark.VacancyBenchmark"]
