
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const RUN_ID = __ENV.RUN_ID || `order-load-${Date.now()}`;

const successfulOrders = new Counter('successful_orders');
const failedOrders = new Counter('failed_orders');

export const options = {
    scenarios: {
        order_creation: {
            executor: 'shared-iterations',
            vus: 5,
            iterations: 100,
            maxDuration: '2m',
        },
    },

    thresholds: {
        http_req_failed: ['rate<0.01'],
        checks: ['rate>0.99'],
    },

    summaryTrendStats: [
        'avg',
        'min',
        'med',
        'max',
        'p(90)',
        'p(95)',
        'p(99)',
    ],
};

export default function () {
    const idempotencyKey = `${RUN_ID}-vu-${__VU}-iter-${__ITER}`;

    const payload = JSON.stringify({
        userId: 1,
        items: [
            {
                productId: 3,
                quantity: 1,
            },
        ],
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Idempotency-Key': idempotencyKey,
        },
        tags: {
            endpoint: 'create_order',
        },
    };

    const response = http.post(
        `${BASE_URL}/api/orders`,
        payload,
        params
    );

    let responseBody = null;

    try {
        responseBody = response.json();
    } catch (_) {
        // Keep the test running if the response is not valid JSON.
    }

    const success = check(response, {
        'status is 201': (r) => r.status === 201,
        'response contains orderId': () =>
            responseBody !== null &&
            responseBody.orderId !== undefined,
        'initial order status is PENDING': () =>
            responseBody !== null &&
            responseBody.status === 'PENDING',
    });

    if (success) {
        successfulOrders.add(1);
    } else {
        failedOrders.add(1);
        console.error(
            `Order request failed. VU=${__VU}, ITER=${__ITER}, ` +
            `Status=${response.status}, Body=${response.body}`
        );
    }

    sleep(0.2);
}
