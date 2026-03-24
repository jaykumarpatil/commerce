#!/bin/sh

set -e
# 1. Build the model
ollama create claude-high -f claude-high.modelfile

# 2. Set the OS-level memory limit for the GPU (Critical for 128k context)
sudo sysctl iogpu.wired_limit_mb=102400

# 3. Launch with the Claude Code bridge
OLLAMA_CONTEXT_LENGTH=131072 ollama launch claude --model claude-high