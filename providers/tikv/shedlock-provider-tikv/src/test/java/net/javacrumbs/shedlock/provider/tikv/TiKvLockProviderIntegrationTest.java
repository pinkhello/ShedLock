package net.javacrumbs.shedlock.provider.tikv;

import com.google.protobuf.ByteString;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.test.support.AbstractLockProviderIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.tikv.common.TiConfiguration;
import org.tikv.common.TiSession;
import org.tikv.raw.RawKVClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static net.javacrumbs.shedlock.provider.tikv.TikvContainer.PORT;

@Testcontainers
public class TiKvLockProviderIntegrationTest {

    @Container
    public static final TikvContainer container = new TikvContainer(PORT);

    static final String ENV = "test";

    @Nested
    class TiKv extends AbstractLockProviderIntegrationTest {

        private LockProvider lockProvider;

        private RawKVClient client;

        @BeforeEach
        public void createLockProvider() {
            TiConfiguration conf = TiConfiguration.createRawDefault(
                container.getContainerIpAddress() + ":" + container.getFirstMappedPort());
            client = TiSession.create(conf).createRawClient();
            lockProvider = new TiKvLockProvider(client, ENV);
        }


        @Override
        protected void assertUnlocked(String lockName) {
            assertThat(getLock(lockName)).isNull();
        }

        @Override
        protected void assertLocked(String lockName) {
            assertThat(getLock(lockName)).isNull();
        }

        @Override
        protected LockProvider getLockProvider() {
            return lockProvider;
        }

        private String getLock(String lockName) {
            ByteString key = ByteString.copyFromUtf8(TiKvLockProvider.buildKey(lockName, ENV));
            Optional<ByteString> result = client.get(key);
            return result.map(ByteString::toStringUtf8).orElse(null);
        }

    }
}
