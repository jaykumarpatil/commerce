import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const latencyTrend = new Trend('latency');

const BASE_URL = __ENV.BASE_URL || 'https://api.staging.example.com';
const USERS = __ENV.USERS || 1000;
const DURATION = __ENV.DURATION || '10m';

const thresholds = {
  http_req_duration: ['p(95)<500', 'p(99)<1000'],
  http_req_failed: ['rate<0.01'],
  errors: ['rate<0.05'],
};

export const options = {
  scenarios: {
    smoke: {
      executor: 'constant-vus',
      vus: 10,
      duration: '2m',
    },
    load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: USERS * 0.3 },
        { duration: '5m', target: USERS * 0.5 },
        { duration: '3m', target: USERS },
        { duration: '5m', target: USERS },
        { duration: '2m', target: 0 },
      ],
      gracefulRampDown: '1m',
    },
    stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: USERS * 2 },
        { duration: '5m', target: USERS * 3 },
        { duration: '5m', target: USERS * 4 },
        { duration: '2m', target: 0 },
      ],
    },
    spike: {
      executor: 'spike',
      baseVUs: USERS,
      spikes: [
        { duration: '30s', target: USERS * 10 },
        { duration: '5m', target: USERS },
        { duration: '30s', target: USERS * 10 },
        { duration: '5m', target: USERS },
      ],
    },
    soak: {
      executor: 'constant-vus',
      vus: USERS,
      duration: DURATION,
    },
  },
  thresholds: thresholds,
};

const categories = ['electronics', 'clothing', 'home', 'sports'];
const products = Array.from({ length: 100 }, (_, i) => `prod-${i + 1}`);

let authToken = '';

export function setup() {
  const loginRes = http.post(`${BASE_URL}/api/auth/login`, {
    username: 'testuser@example.com',
    password: 'Test123!@#',
  });
  
  if (loginRes.status === 200) {
    authToken = loginRes.json('accessToken');
  }
  
  return { token: authToken };
}

export default function (data) {
  const token = data.token;
  
  const endpoints = [
    { method: 'GET', path: '/api/products', weight: 30 },
    { method: 'GET', path: '/api/categories', weight: 20 },
    { method: 'GET', path: `/api/products/${products[Math.floor(Math.random() * products.length)]}`, weight: 15 },
    { method: 'GET', path: `/api/products?category=${categories[Math.floor(Math.random() * categories.length)]}`, weight: 10 },
    { method: 'POST', path: '/api/cart/items', weight: 10 },
    { method: 'GET', path: '/api/cart', weight: 5 },
    { method: 'GET', path: '/api/search?q=laptop', weight: 5 },
    { method: 'POST', path: '/api/checkout', weight: 5 },
  ];
  
  const selected = weightedRandom(endpoints);
  
  let res;
  const start = Date.now();
  
  switch (selected.method) {
    case 'GET':
      res = http.get(`${BASE_URL}${selected.path}`, {
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'X-Correlation-ID': `k6-${Date.now()}`,
        },
      });
      break;
    case 'POST':
      if (selected.path.includes('cart')) {
        res = http.post(`${BASE_URL}${selected.path}`, 
          JSON.stringify({
            productId: products[Math.floor(Math.random() * products.length)],
            quantity: Math.floor(Math.random() * 3) + 1,
          }),
          {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json',
            },
          }
        );
      } else if (selected.path.includes('checkout')) {
        res = http.post(`${BASE_URL}${selected.path}`,
          JSON.stringify({
            cartId: `cart-${__VU}-${__ITER}`,
            shippingAddress: { street: '123 Test St' },
          }),
          {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json',
            },
          }
        );
      }
      break;
  }
  
  latencyTrend.add(Date.now() - start);
  
  const success = check(res, {
    'status is 200 or 201 or 204': (r) => [200, 201, 204].includes(r.status),
    'response time < 500ms': (r) => r.timings.duration < 500,
    'has content-type': (r) => r.headers['Content-Type'] !== undefined,
  });
  
  errorRate.add(!success);
  
  sleep(Math.random() * 2 + 0.5);
}

function weightedRandom(items) {
  const totalWeight = items.reduce((sum, item) => sum + item.weight, 0);
  let random = Math.random() * totalWeight;
  
  for (const item of items) {
    random -= item.weight;
    if (random <= 0) {
      return item;
    }
  }
  return items[items.length - 1];
}

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'summary.json': JSON.stringify(data),
  };
}

function textSummary(data, options) {
  const { metrics } = data;
  
  let summary = '\n';
  summary += '='.repeat(80) + '\n';
  summary += '  LOAD TEST SUMMARY\n';
  summary += '='.repeat(80) + '\n\n';
  
  summary += `Duration: ${formatDuration(data.state.testRunDurationMs)}\n`;
  summary += `Total Requests: ${metrics.http_reqs.values.count}\n`;
  summary += `Failed Requests: ${metrics.http_req_failed.values.passes}\n`;
  summary += `Error Rate: ${(metrics.errors.values.rate * 100).toFixed(2)}%\n\n`;
  
  summary += 'Response Times:\n';
  summary += `  Average: ${metrics.http_req_duration.values.avg.toFixed(2)}ms\n`;
  summary += `  p50: ${metrics.http_req_duration.values['p(50)'].toFixed(2)}ms\n`;
  summary += `  p95: ${metrics.http_req_duration.values['p(95)'].toFixed(2)}ms\n`;
  summary += `  p99: ${metrics.http_req_duration.values['p(99)'].toFixed(2)}ms\n`;
  summary += `  Max: ${metrics.http_req_duration.values.max.toFixed(2)}ms\n\n`;
  
  summary += 'Throughput:\n';
  summary += `  Requests/sec: ${metrics.http_reqs.values.rate.toFixed(2)}\n\n`;
  
  summary += '='.repeat(80) + '\n';
  
  return summary;
}

function formatDuration(ms) {
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  
  if (hours > 0) {
    return `${hours}h ${minutes % 60}m`;
  } else if (minutes > 0) {
    return `${minutes}m ${seconds % 60}s`;
  }
  return `${seconds}s`;
}
