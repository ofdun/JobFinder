ARG BASE_IMAGE=jobfinder-microbench:base
FROM ${BASE_IMAGE}
ARG RUN_ID
LABEL com.ofdun.jobfinder.benchmark.run=${RUN_ID}
