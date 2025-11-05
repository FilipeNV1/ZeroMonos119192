import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
  stages: [ 
    // ramp up from 0 to 20 VUs over the next 5 seconds
    { duration: '5s', target: 20 },
    // run 20 VUs over the next 10 seconds
    { duration: '30s', target: 20 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    http_req_failed: ['rate<0.02'],   // less than 2% of requests should fail
    checks: ['rate>0.99'],             // more than 99% of checks should pass
  },
};

const BASE_URL = 'http://localhost:8080';
const municipalities = [
  'Aveiro', 'Lisboa', 'Porto', 'Coimbra', 'Braga',
  'Faro', 'Évora', 'Viseu', 'Leiria', 'Setúbal'
];

export default function () {
  const municipality = municipalities[(__VU + __ITER) % municipalities.length];
  const dayOffset = (__VU * 100 + __ITER) % 30; // Vary dates within a month

  // create booking
  const createPayload = JSON.stringify({
    description: `Load test booking ${__VU}-${__ITER}`,
    municipality: municipality,
    date: `2025-12-${String(1 + dayOffset).padStart(2, '0')}T${String(10 + (__VU % 10)).padStart(2, '0')}:00:00`
  });

  const createParams = {
    headers: { 'Content-Type': 'application/json' },
  };

  const createResponse = http.post(`${BASE_URL}/api/bookings`, createPayload, createParams);
  
  check(createResponse, {
    'booking created successfully': (r) => r.status === 201,
    'response has token': (r) => r.json('token') !== null,
  }) || errorRate.add(1);

  const token = createResponse.json('token');

  sleep(1);

  // get booking by token
  if (token) {
    const getResponse = http.get(`${BASE_URL}/api/bookings/${token}`);
    
    check(getResponse, {
      'get booking successful': (r) => r.status === 200,
      'booking has correct token': (r) => r.json('token') === token,
    }) || errorRate.add(1);
  }

  sleep(1);

  // get all bookings
  const getAllResponse = http.get(`${BASE_URL}/api/bookings`);
  
  check(getAllResponse, {
    'get all bookings successful': (r) => r.status === 200,
    'response is array': (r) => Array.isArray(r.json()),
  }) || errorRate.add(1);

  sleep(1);
}