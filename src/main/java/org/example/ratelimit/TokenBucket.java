package org.example.ratelimit;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 轻量级令牌桶限流器，基于 {@link AtomicLong} 无锁实现。
 * <p>
 * 核心思想：按时间匀速补充令牌，每次请求 {@link #tryAcquire()} 消耗一个令牌。
 * 令牌余量以"下一次可通过的纳秒时间点"的形式存储，避免显式定时任务。
 *
 * <p>算法：
 * <pre>
 *   long now = nanoTime();
 *   long next = max(last, now) + intervalNanos;
 *   long waitNanos = next - now;
 *   if (waitNanos &lt;= maxBurstNanos) { accept; last = next; }
 *   else                              { reject; }
 * </pre>
 *
 * <p>支持突发：允许积累最多 {@code burst} 个令牌，超出部分拒绝。
 */
public class TokenBucket {

    /** 每生产一个令牌需要多少纳秒（= 1s / qps） */
    private final long intervalNanos;

    /** 允许的最大等待时间（纳秒）= burst × intervalNanos。大于此值则拒绝 */
    private final long maxBurstNanos;

    /** 下一次可通过的时间点（纳秒）。使用 AtomicLong 无锁更新 */
    private final AtomicLong nextFreeSlotNanos = new AtomicLong(0);

    /**
     * @param qps   稳态速率，每秒放行请求数
     * @param burst 突发容量（允许连续放行 N 个请求，随后按 qps 恢复）
     */
    public TokenBucket(double qps, int burst) {
        if (qps <= 0) {
            throw new IllegalArgumentException("qps 必须大于 0");
        }
        if (burst < 1) {
            throw new IllegalArgumentException("burst 必须 >= 1");
        }
        this.intervalNanos = (long) (1_000_000_000L / qps);
        // burst=N 代表可连续放行 N 次。第 N 次时 waitNanos = (N-1)*interval，
        // 第 N+1 次时 waitNanos = N*interval > 上限，故拒绝。
        this.maxBurstNanos = intervalNanos * (long) (burst - 1);
    }

    /**
     * 尝试获取一个令牌。
     * @return true 允许通过；false 被限流
     */
    public boolean tryAcquire() {
        long now = System.nanoTime();
        while (true) {
            long last = nextFreeSlotNanos.get();
            long base = Math.max(last, now);
            long next = base + intervalNanos;
            long waitNanos = base - now;  // 相对于 now，要等多久才到 base
            if (waitNanos > maxBurstNanos) {
                return false;
            }
            if (nextFreeSlotNanos.compareAndSet(last, next)) {
                return true;
            }
            // CAS 失败重试
        }
    }
}
