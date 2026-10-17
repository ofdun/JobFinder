ARG BASE_IMAGE=jobfinder-load-base:local
FROM ${BASE_IMAGE}
ARG RUN_ID
LABEL com.ofdun.jobfinder.load.run=${RUN_ID}
