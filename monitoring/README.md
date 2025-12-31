# Observability Stack - Prometheus, Grafana & Jaeger

This directory contains the configuration for the observability stack used to monitor the MS Lotosia microservices architecture.

## Services

### Prometheus (Port 9090)
- **Purpose**: Metrics collection and storage
- **URL**: http://localhost:9090
- **Configuration**: `prometheus.yml`
- **Scrapes**: All Spring Boot services, Redis, PostgreSQL, and Jaeger

### Grafana (Port 3000)
- **Purpose**: Metrics visualization and dashboards
- **URL**: http://localhost:3000
- **Credentials**: admin / admin (configurable via env vars)
- **Dashboards**: Pre-configured JVM metrics dashboard

### Jaeger (Port 16686)
- **Purpose**: Distributed tracing
- **URL**: http://localhost:16686
- **Protocol**: OpenTelemetry (OTLP)

## Configuration Files

### `prometheus.yml`
- Defines scrape targets for all services
- Configures scrape intervals (5s for Spring Boot, 15s default)
- Includes job configurations for each microservice

### `grafana/provisioning/datasources/prometheus.yml`
- Automatically configures Prometheus as Grafana datasource
- No manual setup required

### `grafana/provisioning/dashboards/dashboard.yml`
- Configures dashboard provisioning from files

### `grafana/dashboards/jvm-metrics.json`
- Pre-built dashboard showing JVM metrics for all services
- Includes memory usage, HTTP requests, threads, and GC metrics

## Spring Boot Configuration

All Java services have been configured to expose metrics via Spring Boot Actuator:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0
```

## Usage

### Starting the Stack
```bash
docker-compose up prometheus grafana jaeger
```

### Accessing Services
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Jaeger**: http://localhost:16686

### Viewing Metrics
1. **Grafana**: Import the JVM Metrics dashboard or create custom dashboards
2. **Prometheus**: Query metrics directly (e.g., `jvm_memory_used_bytes{job="identity-service"}`)
3. **Jaeger**: View distributed traces when tracing is implemented

## Metrics Available

### JVM Metrics
- Memory usage (heap, non-heap)
- Thread counts (live, daemon)
- Garbage collection statistics
- Class loading information

### HTTP Metrics
- Request count and rates
- Response time percentiles (50th, 95th, 99th)
- HTTP status codes

### Application Metrics
- Custom business metrics
- Database connection pools
- Cache hit/miss ratios

### Infrastructure Metrics
- Redis metrics
- PostgreSQL metrics
- Container resource usage

## Adding Custom Metrics

### Spring Boot (Java)
```java
@Autowired
private MeterRegistry meterRegistry;

public void recordCustomMetric() {
    Counter.builder("custom_operation_total")
        .tag("service", "my-service")
        .register(meterRegistry)
        .increment();
}
```

### Node.js
```javascript
const promClient = require('prom-client');
const register = new promClient.Registry();

const customCounter = new promClient.Counter({
  name: 'custom_operation_total',
  help: 'Custom operation counter',
  registers: [register]
});
```

## Environment Variables

### Grafana
- `GRAFANA_ADMIN_USER`: Admin username (default: admin)
- `GRAFANA_ADMIN_PASSWORD`: Admin password (default: admin)

### pgAdmin (existing)
- `PGADMIN_DEFAULT_EMAIL`: Admin email
- `PGADMIN_DEFAULT_PASSWORD`: Admin password

## Troubleshooting

### Metrics Not Appearing
1. Check service health: `curl http://localhost:8080/actuator/health`
2. Verify metrics endpoint: `curl http://localhost:8080/actuator/prometheus`
3. Check Prometheus targets: http://localhost:9090/targets

### Grafana Login Issues
- Default credentials: admin/admin
- Check environment variables if customized

### Jaeger Not Receiving Traces
- Ensure services have tracing enabled
- Check OTLP endpoint configuration
- Verify network connectivity

## Next Steps

1. **Implement Tracing**: Add OpenTelemetry to services for distributed tracing
2. **Custom Dashboards**: Create service-specific dashboards
3. **Alerting**: Configure Prometheus alerts for critical metrics
4. **Log Aggregation**: Add ELK stack for centralized logging
5. **Performance Monitoring**: Add APM tools like New Relic or DataDog
