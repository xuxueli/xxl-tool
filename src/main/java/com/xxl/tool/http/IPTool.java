package com.xxl.tool.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * ip tool
 */
public class IPTool {
    private static Logger logger = LoggerFactory.getLogger(IPTool.class);

    // ipv4 address pattern
    private static final Pattern IPV4_ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");
    private static final Pattern IPV4_IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
    // local ip pattern
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    // localhost
    private static final String LOCALHOST_KEY = "localhost";
    private static final String LOCALHOST_VALUE = "127.0.0.1";
    // anyhost
    private static final String ANYHOST_VALUE = "0.0.0.0";
    // valid port range is [1, 65535]
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    // random port range is [30000, 39999]
    private static final int RANDOM_PORT_START = 30000;
    private static final int RANDOM_PORT_RANGE = 10000;
    // store the used port, the set used only on the synchronized method.
    private static final BitSet USED_PORT = new BitSet(65536);

    // ----------- port -----------

    /**
     * port in used in-os or not
     *
     * @param port port to check
     * @return true if it's occupied
     */
    public static boolean isPortInUsed(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            // continue
        }
        return true;
    }

    /**
     * is valid port
     *
     * @param port port to test
     * @return true if invalid
     * @implNote Numeric comparison only.
     */
    public static boolean isValidPort(int port) {
        return port >= MIN_PORT && port <= MAX_PORT;
    }

    /**
     * get a random port in [30000, 39999]
     *
     * @return
     */
    public static int getRandomPort() {
        return RANDOM_PORT_START + ThreadLocalRandom.current().nextInt(RANDOM_PORT_RANGE);
    }

    /**
     * get a random & available port, synchronized
     *
     * @return
     */
    public static synchronized int getAvailablePort() {
        int randomPort = getRandomPort();
        return getAvailablePort(randomPort);
    }

    /**
     * get a available port base the given port, synchronized
     *
     * @param port
     * @return
     */
    public static synchronized int getAvailablePort(int port) {
        // valid
        int invalidPort = -1;
        if (!isValidPort(port)) {
            return invalidPort;
        }

        // check available
        for (int i = port; i < MAX_PORT; i++) {
            if (isPortAvailable(i)) {
                return i;
            }
        }
        return invalidPort;
    }

    /**
     * get a available port base the given port, synchronized + check-repetition
     *
     * @param port
     * @return
     */
    public static synchronized int getAvailablePortNotUsed(int port) {
        // valid
        int invalidPort = -1;
        if (!isValidPort(port)) {
            return invalidPort;
        }

        // check available
        for (int i = port; i < MAX_PORT; i++) {
            if (USED_PORT.get(i)) {
                continue;
            }
            if (isPortAvailable(i)) {
                USED_PORT.set(i);
                return i;
            }
        }
        return invalidPort;
    }

    /**
     * check is available port
     *
     * @param port
     * @return
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ----------- host -----------

    /**
     * host is localhost
     *
     * @param host
     * @return
     */
    public static boolean isLocalHost(String host) {
        return host != null && (LOCAL_IP_PATTERN.matcher(host).matches() || host.equalsIgnoreCase(LOCALHOST_KEY));
    }

    /**
     * host is any-host
     *
     * @param host
     * @return
     */
    public static boolean isAnyHost(String host) {
        return ANYHOST_VALUE.equals(host);
    }

    /**
     * host is valid
     *
     * @param host
     * @return
     */
    public static boolean isValidLocalHost(String host) {
        return host != null
                && !host.isEmpty()
                && !host.equalsIgnoreCase(LOCALHOST_KEY)
                && !host.equals(ANYHOST_VALUE)
                && !host.startsWith("127.");
    }

    // ----------- address -----------

    /**
     * InetSocketAddress 2 address
     *
     * @param address
     * @return
     */
    public static String toAddressString(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    /**
     * address 2 InetSocketAddress
     *
     * @param address
     * @return
     */
    public static InetSocketAddress toAddress(String address) {
        int i = address.indexOf(':');
        String host;
        int port;
        if (i > -1) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
            port = 0;
        }
        return new InetSocketAddress(host, port);
    }

    /**
     * host and port to InetSocketAddress
     *
     * @param host
     * @param port
     * @return
     */
    public static InetSocketAddress toAddress(String host, int port) {
        return new InetSocketAddress(host, port);
    }

    /**
     * is valid ipv4 address.
     *
     * @param address address to test
     * @return true if invalid
     * @implNote Pattern matching only.
     */
    public static boolean isValidV4Address(String address) {
        return IPV4_ADDRESS_PATTERN.matcher(address).matches();
    }

    /**
     * is valid ipv4 address.
     *
     * @param address
     * @return
     */
    public static boolean isValidV4Address(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }

        String name = address.getHostAddress();
        return (name != null
                && IPV4_IP_PATTERN.matcher(name).matches()
                && !ANYHOST_VALUE.equals(name)
                && !LOCALHOST_VALUE.equals(name));
    }

    /**
     * is prefer ipv6 address
     */
    private static boolean isPreferIPV6Address() {
        return Boolean.getBoolean("java.net.preferIPv6Addresses");
    }

    /**
     * normalize the ipv6 Address, convert scope name to scope id.
     *
     * e.g.
     * convert
     * fe80:0:0:0:894:aeec:f37d:23e1%en0
     * to
     * fe80:0:0:0:894:aeec:f37d:23e1%5
     * <p>
     * The %5 after ipv6 address is called scope id.
     * see java doc of {@link Inet6Address} for more details.
     *
     * @param address the input address
     * @return the normalized address, with scope id converted to int
     */
    private static InetAddress normalizeV6Address(Inet6Address address) {
        String addr = address.getHostAddress();
        int i = addr.lastIndexOf('%');
        if (i > 0) {
            try {
                return InetAddress.getByName(addr.substring(0, i) + '%' + address.getScopeId());
            } catch (UnknownHostException e) {
                // ignore
                logger.debug("Unknown IPV6 address: ", e);
            }
        }
        return address;
    }

    // ----------- ip tool -----------

    // local address store
    private static volatile InetAddress LOCAL_ADDRESS = null;

    /**
     * get ip address
     *
     * @return String
     */
    public static String getIp(){
        InetAddress localAddress = getLocalAddress();
        return localAddress!=null?localAddress.getHostAddress():null;
    }

    /**
     * Find first valid IP from local network card
     *
     * @return first valid local IP
     */
    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        if (localAddress != null) {
            LOCAL_ADDRESS = localAddress;
        }
        return localAddress;
    }

    /**
     * find first InetAddress from local network card
     */
    private static InetAddress getLocalAddress0() {

        // 1、choose NetworkInterface first
        try {
            // find all NetworkInterface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        // find network
                        NetworkInterface network = interfaces.nextElement();
                        if (network.isLoopback() || network.isVirtual() || !network.isUp()) {
                            continue;
                        }
                        // first address
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            try {
                                InetAddress addressItem = toValidAddress(addresses.nextElement());
                                if (addressItem != null) {
                                    try {
                                        if(addressItem.isReachable(100)){
                                            return addressItem;
                                        }
                                    } catch (IOException e) {
                                        // ignore
                                    }
                                }
                            } catch (Throwable e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }

        // 2、getLocalHost
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            InetAddress addressItem = toValidAddress(localAddress);
            if (addressItem != null) {
                return addressItem;
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }

        // 3、getLocalAddressV6
        return null;
    }

    /**
     * valid InetAddress
     */
    private static InetAddress toValidAddress(InetAddress address) {
        if (address instanceof Inet6Address) {
            Inet6Address v6Address = (Inet6Address) address;
            if (isPreferIPV6Address()) {
                return normalizeV6Address(v6Address);
            }
        }
        if (isValidV4Address(address)) {
            return address;
        }
        return null;
    }

}
