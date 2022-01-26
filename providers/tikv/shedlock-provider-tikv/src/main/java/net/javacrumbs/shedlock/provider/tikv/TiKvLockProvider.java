package net.javacrumbs.shedlock.provider.tikv;

import com.google.protobuf.ByteString;
import net.javacrumbs.shedlock.core.AbstractSimpleLock;
import net.javacrumbs.shedlock.core.ClockProvider;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.support.LockException;
import net.javacrumbs.shedlock.support.annotation.NonNull;
import org.tikv.common.TiSession;
import org.tikv.raw.RawKVClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static net.javacrumbs.shedlock.support.Utils.getHostname;
import static net.javacrumbs.shedlock.support.Utils.toIsoString;

public class TiKvLockProvider implements LockProvider {

    /**
     * KEY PREFIX
     */
    private static final String KEY_PREFIX = "shedlock";

    /**
     * ENV DEFAULT
     */
    private static final String ENV_DEFAULT = "default";

    private RawKVClient client;

    private final String env;

    public TiKvLockProvider(@NonNull RawKVClient client) {
        this(client, ENV_DEFAULT);
    }

    public TiKvLockProvider(@NonNull RawKVClient client, @NonNull String env) {
        this.client = client;
        this.env = env;
    }

    @Override
    @NonNull
    public Optional<SimpleLock> lock(LockConfiguration lockConfiguration) {
        long expireTime = getMsUntil(lockConfiguration.getLockAtMostUntil());
        String key = buildKey(lockConfiguration.getName(), this.env);

        // compare redis nx 只在键不存在时，才对键进行设置操作 px
        client.put();

        return Optional.empty();
    }

    private static final class TiKvLock extends AbstractSimpleLock {

        private final String key;

        private final RawKVClient client;

        private TiKvLock(@NonNull String key,
                         @NonNull RawKVClient client,
                         @NonNull LockConfiguration lockConfiguration) {
            super(lockConfiguration);
            this.key = key;
            this.client = client;
        }

        @Override
        protected void doUnlock() {
            long keepLockFor = getMsUntil(lockConfiguration.getLockAtLeastUntil());
            ByteString kk = ByteString.copyFromUtf8(key);
            client
            if (keepLockFor <= 0) {
                // compare redis del
                try {
                    client.delete(kk);
                } catch (Exception e) {
                    throw new LockException("Can not remove node", e);
                }
            } else {
                // compare redis xx 只在键已经存在时，才对键进行设置操作。  px
                client.compareAndSet();
                client.putIfAbsent(kk, ByteString.copyFromUtf8(buildValue()), keepLockFor);
            }
        }
    }



    private static long getMsUntil(Instant instant) {
        return Duration.between(ClockProvider.now(), instant).toMillis();
    }
//
    static String buildKey(String lockName, String env) {
        return String.format("%s:%s:%s", KEY_PREFIX, env, lockName);
    }
//
    private static String buildValue() {
        return String.format("ADDED:%s@%s", toIsoString(ClockProvider.now()), getHostname());
    }


}
