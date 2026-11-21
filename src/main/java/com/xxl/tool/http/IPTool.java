package com.xxl.tool.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * IP 工具：本机地址探测与端口可用性判断
 *
 * <p>
 * 端口范围固定为 {@code [1, 65535]}；IPv4 判断基于正则，IPv6 仅在系统属性
 * {@code java.net.preferIPv6Addresses=true} 时参与本机地址选择。
 * 本类无状态（仅缓存本机地址），静态方法线程安全；本机地址探测失败时返回 {@code null}。
 * </p>
 *
 * <p>
 * 注意：{@link #isPortInUsed(int)} / {@link #getAvailablePort(int)} 通过临时绑定端口探测，
 * 探测与真正使用之间存在竞态，不能作为并发分配端口的强保证。
 * </p>
 */
public class IPTool {
    private static final Logger logger = LoggerFactory.getLogger(IPTool.class);

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


    // ---------------------- port ----------------------

    /**
     * port in used in-os or not
     *
     * <p>
     * 通过尝试绑定本地端口判断；绑定失败（包括无权限等其他 IO 原因）均视为已占用。
     * </p>
     *
     * @param port port to check，建议先经 {@link #isValidPort(int)} 校验
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
     * @return 端口位于 {@code [1, 65535]} 返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isValidPort(int port) {
        return port >= MIN_PORT && port <= MAX_PORT;
    }

    /**
     * get a random port in [30000, 39999]
     *
     * <p>
     * 仅生成随机数，不保证端口未被占用。
     * </p>
     *
     * @return a random port，范围 {@code [30000, 39999]}
     */
    public static int getRandomPort() {
        return RANDOM_PORT_START + ThreadLocalRandom.current().nextInt(RANDOM_PORT_RANGE);
    }

    /**
     * get a random & available port, synchronized
     *
     * <p>
     * 以随机端口为起点查找可用端口；进程内通过 {@code synchronized} 串行化，跨进程仍有竞态。
     * </p>
     *
     * @return a random & available port
     * @throws RuntimeException 在 {@code [1, 65535]} 范围内找不到可用端口时抛出
     */
    public static synchronized int getAvailablePort() {
        int randomPort = getRandomPort();
        return getAvailablePort(randomPort);
    }

    /**
     * get a available port, base assign port, synchronized
     *
     * <p>
     * 从指定端口开始向上查找至 65535，找不到再从 {@code port-1} 向下查找到 1。
     * </p>
     *
     * @param port default port，建议位于 {@code [1, 65535]}
     * @return a available port，尽可能接近入参
     * @throws RuntimeException 全范围无可用端口时抛出
     */
    public static synchronized int getAvailablePort(int port) {
        int portTmp = port;
        while (portTmp <= MAX_PORT) {
            if (isPortAvailable(portTmp)) {
                return portTmp;
            } else {
                portTmp++;
            }
        }
        portTmp = port - 1;
        while (portTmp >= MIN_PORT) {
            if (isPortAvailable(portTmp)) {
                return portTmp;
            } else {
                portTmp--;
            }
        }
        throw new RuntimeException("no available port.");
    }

    /**
     * check is available port
     *
     * @param port 待探测端口
     * @return 可成功绑定返回 {@code true}，否则 {@code false}
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------------- host ----------------------

    /**
     * host is localhost
     *
     * <p>
     * 匹配 {@code 127.x.x.x} 或忽略大小写的 {@code localhost}；IPv6 回环 {@code ::1} 不识别。
     * </p>
     *
     * @param host 主机名或地址，可为 {@code null}
     * @return 为本机回环地址返回 {@code true}；{@code null} 返回 {@code false}
     */
    public static boolean isLocalHost(String host) {
        return host != null && (LOCAL_IP_PATTERN.matcher(host).matches() || host.equalsIgnoreCase(LOCALHOST_KEY));
    }

    /**
     * host is any-host
     *
     * @param host 主机名或地址，可为 {@code null}
     * @return 精确等于 {@code 0.0.0.0} 返回 {@code true}
     */
    public static boolean isAnyHost(String host) {
        return ANYHOST_VALUE.equals(host);
    }

    /**
     * host is valid
     *
     * <p>
     * 判断是否为“非本机/非任意地址”的有效主机：{@code null}、空串、{@code localhost}、
     * {@code 0.0.0.0} 以及 {@code 127.*} 均返回 {@code false}。方法名保留历史命名，
     * 语义是“可作为对外通信地址的有效本地主机”。
     * </p>
     *
     * @param host 主机名或地址，可为 {@code null}
     * @return 非本机回环、非任意地址且非空时返回 {@code true}
     */
    public static boolean isValidLocalHost(String host) {
        return host != null
                && !host.isEmpty()
                && !host.equalsIgnoreCase(LOCALHOST_KEY)
                && !host.equals(ANYHOST_VALUE)
                && !host.startsWith("127.");
    }


    // ---------------------- address ----------------------

    /**
     * InetSocketAddress 2 address
     *
     * @param socketAddress InetSocketAddress，不能为 {@code null} 且必须已解析（{@code getAddress()} 非 {@code null}）
     * @return  address string，格式 {@code ip:port}；IPv6 地址不加方括号
     * @throws NullPointerException 地址未解析或入参为 {@code null} 时
     */
    public static String toAddressString(InetSocketAddress socketAddress) {
        return socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort();
    }

    /**
     * host and port to address string
     *
     * @param host  host，不能为 {@code null}
     * @param port  port
     * @return address string，格式 {@code ip:port}
     * @throws NullPointerException 主机名无法解析导致地址为空时
     */
    public static String toAddressString(String host, int port) {
        return toAddressString(toSocketAddress(host, port));
    }

    /**
     * address 2 InetSocketAddress
     *
     * <p>
     * 以第一个 {@code :} 分隔主机与端口；不含冒号时端口取 0。
     * <b>不支持 IPv6 字面量</b>（如 {@code ::1} 会解析失败）。
     * </p>
     *
     * @param address address string，格式 {@code host[:port]}，不能为 {@code null}
     * @return InetSocketAddress，非 {@code null}；主机名无法解析时地址处于未解析状态
     * @throws NumberFormatException 端口部分不是整数时
     */
    public static InetSocketAddress toSocketAddress(String address) {
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
     * @param host  host，不能为 {@code null}
     * @param port  port，需在 {@code [0, 65535]} 内
     * @return InetSocketAddress
     * @throws IllegalArgumentException 端口越界或主机名为空时（由 {@link InetSocketAddress} 抛出）
     */
    public static InetSocketAddress toSocketAddress(String host, int port) {
        return new InetSocketAddress(host, port);
    }

    /**
     * is valid ipv4 address.
     *
     * <p>
     * 仅做正则匹配，<b>要求形如 {@code x.x.x.x:port}</b>（必须带端口），不校验每段数值是否 ≤255；
     * 纯 IP（无端口）、IPv6、主机名均返回 {@code false}。
     * </p>
     *
     * @param address address to test，不能为 {@code null}
     * @return 匹配 {@code host:port} 形式时返回 {@code true}
     * @implNote Pattern matching only.
     */
    public static boolean isValidV4Address(String address) {
        return IPV4_ADDRESS_PATTERN.matcher(address).matches();
    }

    /**
     * is valid ipv4 InetAddress
     *
     * <p>
     * 排除 {@code null}、回环地址、{@code 0.0.0.0} 与 {@code 127.0.0.1}；
     * 其余地址按 {@code IPV4_IP_PATTERN} 匹配（该正则允许 3~5 段，历史实现）。
     * </p>
     *
     * @param address InetAddress，可为 {@code null}
     * @return 为可用的非回环 IPv4 地址时返回 {@code true}
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
     *
     * @return 系统属性 {@code java.net.preferIPv6Addresses} 为 {@code true} 时返回 {@code true}
     */
    private static boolean isPreferIPV6Address() {
        return Boolean.getBoolean("java.net.preferIPv6Addresses");
    }


    // ---------------------- ip tool ----------------------

    // local address store
    private static volatile InetAddress LOCAL_ADDRESS = null;

    /**
     * get ip address
     *
     * @return 本机首选非回环地址的字符串形式；无法探测时返回 {@code null}
     */
    public static String getIp(){
        InetAddress localAddress = getLocalAddress();
        return localAddress!=null?localAddress.getHostAddress():null;
    }

    /**
     * Find first valid IP from local network card
     *
     * <p>
     * 首次成功探测后缓存到静态字段（volatile），后续调用直接返回缓存；并发首次调用可能重复探测，
     * 但不影响结果正确性。探测失败时不缓存，下次调用会重试。
     * </p>
     *
     * @return first valid local IP；找不到可用地址时返回 {@code null}
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
     *
     * <p>
     * 优先遍历已启用且非回环/非虚拟网卡，要求地址可达（100ms）；失败后回退到
     * {@link InetAddress#getLocalHost()}；仍失败返回 {@code null}。
     * IPv6 地址仅在 {@code java.net.preferIPv6Addresses=true} 时被接受。
     * </p>
     *
     * @return 本机可用地址，可能为 {@code null}
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
     *
     * <p>
     * IPv6 仅在偏好 IPv6 时返回（并规范化 scope id）；否则按 IPv4 规则校验，不合法返回 {@code null}。
     * </p>
     *
     * @param address 待校验地址，可为 {@code null}
     * @return 可用地址；不满足条件时返回 {@code null}
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

}
