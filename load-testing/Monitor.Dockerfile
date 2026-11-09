FROM python:3.12-slim
COPY monitor.py /monitor.py
ENTRYPOINT ["python3", "/monitor.py"]
