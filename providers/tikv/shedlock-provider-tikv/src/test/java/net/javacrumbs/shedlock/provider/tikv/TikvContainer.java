package net.javacrumbs.shedlock.provider.tikv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;

public class TikvContainer extends FixedHostPortGenericContainer<TikvContainer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TikvContainer.class);

    /**
     * TIKV IMAGE NAME
     */
    public static final String TIKV_IMAGE_NAME = "pingcap/tikv";

    final static int PORT = 2379;

    /**
     *
     * docker pull pingcap/tikv
     *
     * @param hostPort host port
     *
     * @see <a href="https://github.com/pingcap/tidb-docker-compose"> tikv docker-compose repo</a>
     */
    public TikvContainer(int hostPort) {
        super(TIKV_IMAGE_NAME);
        this.withFixedExposedPort(hostPort, PORT)
            .withExposedPorts(PORT)
            .withLogConsumer(frame -> LOGGER.info(frame.getUtf8String()));
    }
}
