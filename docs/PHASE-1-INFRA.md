Phase 1 Infra: Foundation (INFRA-001)
Week 1 goals
- Integrate Elasticsearch (ES) 8.x container into the local stack.
- Convert Redis from a single instance to a 3-master Redis Cluster.
- Update central configuration with ES and Redis cluster endpoints.
- Validate infrastructure health: ES health endpoint, Redis cluster topology, and boot of all services.
- Align risk plan: start ES in Week 1 to mitigate architecture risk.

1) Elasticsearch integration
- Add ES 8.x container to the Docker Compose stack.
- Expose ports: 9200 (HTTP) and 9300 (transport).
- Mount data volume for persistence: /usr/share/elasticsearch/data (or a managed data path in Docker volumes).
- Configure JVM heap sizing (example: -Xms512m -Xmx1g; adjust for host RAM).
- Health checks: use GET http://localhost:9200/_cluster/health?pretty to verify cluster health.
- Expected outcome: ES is up, reachable, and healthy before dependent services start.

2) Redis Cluster
- Replace the single Redis with a 3-node Redis Cluster:
  - redis-node-1:6379
  - redis-node-2:6379
  - redis-node-3:6379
- Enable cluster mode across all nodes; ensure each node knows the others (cluster-enabled yes; cluster-config-file nodes.conf; cluster-node-timeout 5000).
- Update client libraries to auto-discover topology (or use a RedisClusterConfiguration with seed nodes).
- Validation: run cluster slots or a simple ping against all nodes to ensure cluster health.

3) Central configuration updates
- Add central configuration keys for ES and Redis cluster endpoints:
  - elasticsearch.url: http://localhost:9200
  - elasticsearch.tls: optional, if TLS is used
  - redis.cluster.nodes: redis-node-1:6379,redis-node-2:6379,redis-node-3:6379
- Ensure services fetch these values on startup (prefer a dynamic config mechanism or environment-based overrides).

4) Infrastructure health validation
- Start the entire stack with an incremental approach (e.g., docker-compose -f docker-compose.yml -f docker-compose.phase1.yml up -d).
- Verify ES health: curl -s http://localhost:9200/_cluster/health?pretty
- Verify Redis cluster: redis-cli -c -h localhost -p 6379 CLUSTER NODES
- Boot check: verify all 15 services boot without blocking errors; confirm no service crashes during startup.

5) Risk alignment notes
- Phase 1 risk alignment: move Elasticsearch integration to Week 1; document in architecture notes.
- Documentation: capture updated risk matrix and Phase 1 infra plan in repository docs.

Acceptance criteria
- Elasticsearch 8.x container is running and healthy on port 9200.
- Redis Cluster with 3 masters is reachable and reports cluster topology.
- Central config repository contains ES and Redis endpoints and is consumable at startup.
- All services boot successfully with the new infra changes.
- Phase 1 plan updated in the architecture/docs repository references this infra doc.

Owner recommendations
- Infra engineer A: Elasticsearch container config, heap sizing, health checks.
- Infra engineer B: Redis cluster setup, node wiring, client topology discovery.
- Platform engineer: central config integration and seed values propagation to services.

Notes
- This plan is scoped to local/dev/staging environments. Adapt for production with proper persistence, network isolation, and security policies.
