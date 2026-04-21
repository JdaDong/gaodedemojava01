package org.example.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 令牌桶算法单元测试（T11）。
 */
class TokenBucketTest {

    @Test
    void should_allow_burst_then_reject_extra() {
        // qps=10, burst=3 → 初始可连续放行 3 次，第 4 次触限
        TokenBucket bucket = new TokenBucket(10, 3);

        assertTrue(bucket.tryAcquire(), "第 1 次应通过");
        assertTrue(bucket.tryAcquire(), "第 2 次应通过");
        assertTrue(bucket.tryAcquire(), "第 3 次（突发上限）应通过");
        assertFalse(bucket.tryAcquire(), "第 4 次超出突发应被拒");
    }

    @Test
    void should_refill_token_after_waiting() throws InterruptedException {
        // qps=100 → 每 10ms 补充一个令牌；burst=1
        TokenBucket bucket = new TokenBucket(100, 1);
        assertTrue(bucket.tryAcquire());
        assertFalse(bucket.tryAcquire(), "紧接着的第 2 次应被拒");

        Thread.sleep(15);   // 等待超过补充间隔
        assertTrue(bucket.tryAcquire(), "等待补充后应重新通过");
    }

    @Test
    void should_reject_when_qps_invalid() {
        try {
            new TokenBucket(0, 1);
        } catch (IllegalArgumentException e) {
            return; // 预期
        }
        throw new AssertionError("qps=0 应抛 IllegalArgumentException");
    }
}
