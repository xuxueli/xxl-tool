package com.xxl.tool.test.http;

import com.xxl.tool.http.IPTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class IPToolTest {
    private static Logger logger = LoggerFactory.getLogger(IPToolTest.class);

    @Test
    public void isPortInUsed() {
        int port = 8080;
        logger.info("port = {}, result = {}", port, IPTool.isPortInUsed(port));
    }

    @Test
    public void isInvalidPort() {
        int port = 8080;
        logger.info("port = {}, result = {}", port, IPTool.isValidPort(port));
    }

    @Test
    public void getRandomPort() {
        logger.info("result = {}", IPTool.getRandomPort());
    }

    @Test
    public void getAvailablePort() {
        logger.info("result = {}", IPTool.getAvailablePort());
    }

    @Test
    public void getAvailablePort2() {
        logger.info("result = {}", IPTool.getAvailablePort(8080));
    }

    @Test
    public void getAvailablePortNotUsed() {
        logger.info("result = {}", IPTool.getAvailablePortNotUsed(8080));
    }

    @Test
    public void isLocalHost() {
        logger.info("result = {}", IPTool.isLocalHost("127.0.0.3"));
    }

    @Test
    public void isAnyHost() {
        logger.info("result = {}", IPTool.isAnyHost("127.0.0.3"));
    }

    @Test
    public void isValidLocalHost() {
        String host = "127.0.0.3";
        logger.info("host = {}, result = {}", host, IPTool.isValidLocalHost(host));
    }

    @Test
    public void toAddressString() {
        logger.info("result = {}", IPTool.toAddressString(new InetSocketAddress("localhost", 8089)));
    }

    @Test
    public void toAddress() {
        logger.info("result = {}", IPTool.toAddress("127.0.0.3:9999"));
    }

    @Test
    public void toAddress2() {
        logger.info("result = {}", IPTool.toAddress("127.0.0.3", 9999));
    }

    @Test
    public void isValidV4Address() {
        logger.info("result = {}", IPTool.isValidV4Address("127.0.0.3:9999"));
    }

    @Test
    public void isValidV4Address2() {
        logger.info("result = {}", IPTool.isValidV4Address(new InetSocketAddress("127.0.0.3", 9999).getAddress()));
    }

    @Test
    public void getIp() {
        logger.info("result = {}", IPTool.getIp());
    }

}
