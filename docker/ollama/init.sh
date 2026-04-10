#!/bin/sh

ollama serve &

echo "Waiting for Ollama to start..."

until ollama list > /dev/null 2>&1; do
  sleep 1
done

echo "Ollama started"

if ! ollama list | grep -q "mxbai-embed-large"; then
  echo "Pulling model mxbai-embed-large..."
  ollama pull mxbai-embed-large
else
  echo "Model already exists"
fi

wait
