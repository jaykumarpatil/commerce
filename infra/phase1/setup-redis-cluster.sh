#!/usr/bin/env bash
set -euo pipefail

echo "Waiting for Redis nodes to be ready..."
for host in redis-node-1 redis-node-2 redis-node-3; do
  docker exec "$host" sh -c 'until redis-cli -h 127.0.0.1 ping >/dev/null 2>&1; do sleep 1; done'
done

echo "Creating Redis cluster..."
# Use container hostnames as cluster nodes; 3 nodes, no replicas needed for baseline
docker exec redis-node-1 redis-cli --cluster create redis-node-1:6379 redis-node-2:6379 redis-node-3:6379 --cluster-replicas 0 --yes

echo "Redis cluster created."
