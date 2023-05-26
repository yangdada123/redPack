package com.cyb.ybmobileredis.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * <p>åç§°ï¼IdWorker.java</p>
 * <p>æè¿°ï¼åå¸å¼èªå¢é¿ID</p>
 * <pre>
 *     Twitterç SnowflakeãJAVAå®ç°æ¹æ¡
 * </pre>
 * æ ¸å¿ä»£ç ä¸ºå¶IdWorkerè¿ä¸ªç±»å®ç°ï¼å¶åçç»æå¦ä¸ï¼æåå«ç¨ä¸ä¸ª0è¡¨ç¤ºä¸ä½ï¼ç¨âåå²å¼é¨åçä½ç¨ï¼
 * 1||0---0000000000 0000000000 0000000000 0000000000 0 --- 00000 ---00000 ---000000000000
 * å¨ä¸é¢çå­ç¬¦ä¸²ä¸­ï¼ç¬¬ä¸ä½ä¸ºæªä½¿ç¨ï¼å®éä¸ä¹å¯ä½ä¸ºlongçç¬¦å·ä½ï¼ï¼æ¥ä¸æ¥ç41ä½ä¸ºæ¯«ç§çº§æ¶é´ï¼
 * ç¶å5ä½datacenteræ è¯ä½ï¼5ä½æºå¨IDï¼å¹¶ä¸ç®æ è¯ç¬¦ï¼å®éæ¯ä¸ºçº¿ç¨æ è¯ï¼ï¼
 * ç¶å12ä½è¯¥æ¯«ç§åçå½åæ¯«ç§åçè®¡æ°ï¼å èµ·æ¥åå¥½64ä½ï¼ä¸ºä¸ä¸ªLongåã
 * è¿æ ·çå¥½å¤æ¯ï¼æ´ä½ä¸æç§æ¶é´èªå¢æåºï¼å¹¶ä¸æ´ä¸ªåå¸å¼ç³»ç»åä¸ä¼äº§çIDç¢°æï¼ç±datacenteråæºå¨IDä½åºåï¼ï¼
 * å¹¶ä¸æçè¾é«ï¼ç»æµè¯ï¼snowflakeæ¯ç§è½å¤äº§ç26ä¸IDå·¦å³ï¼å®å¨æ»¡è¶³éè¦ã
 * <p>
 * 64ä½ID (42(æ¯«ç§)+5(æºå¨ID)+5(ä¸å¡ç¼ç )+12(éå¤ç´¯å ))
 *
 * @author Polim
 */
public class IdWorker {

    private final static long twepoch = 1288834974657L;
    private final static long workerIdBits = 5L;
    private final static long datacenterIdBits = 5L;
    private final static long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final static long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    private final static long sequenceBits = 12L;
    private final static long workerIdShift = sequenceBits;
    private final static long datacenterIdShift = sequenceBits + workerIdBits;
    private final static long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final static long sequenceMask = -1L ^ (-1L << sequenceBits);
    private static long lastTimestamp = -1L;
    private long sequence = 0L;
    private final long workerId;
    private final long datacenterId;
    public IdWorker(){
        this.datacenterId = getDatacenterId(maxDatacenterId);
        this.workerId = getMaxWorkerId(datacenterId, maxWorkerId);
    }

    public IdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        long nextId = ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift) | sequence;

        return nextId;
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * <p>
     * è·å maxWorkerId
     * </p>
     */
    protected static long getMaxWorkerId(long datacenterId, long maxWorkerId) {
        StringBuffer mpid = new StringBuffer();
        mpid.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (!name.isEmpty()) {
            /*
             * GET jvmPid
             */
            mpid.append(name.split("@")[0]);
        }
        /*
         * MAC + PID ç hashcode è·å16ä¸ªä½ä½
         */
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

    /**
     * <p>
     * æ°æ®æ è¯idé¨å
     * </p>
     */
    protected static long getDatacenterId(long maxDatacenterId) {
        long id = 0L;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                id = ((0x000000FF & (long) mac[mac.length - 1])
                        | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                id = id % (maxDatacenterId + 1);
            }
        } catch (Exception e) {
            System.out.println(" getDatacenterId: " + e.getMessage());
        }
        return id;
    }
}

æµè¯ï¼
public static void main(String[] args) {
        IdWorker idWorker = new IdWorker(0,0);
        for (int i = 0; i <2600 ; i++) {
        System.out.println(idWorker.nextId());
        }
        }