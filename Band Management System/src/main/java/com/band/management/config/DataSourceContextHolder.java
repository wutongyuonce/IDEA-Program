package com.band.management.config;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据源上下文持有者
 * 使用ThreadLocal存储当前线程使用的数据源类型
 * 
 * @author Band Management Team
 */
@Slf4j
public class DataSourceContextHolder {

    /**
     * 数据源类型枚举
     */
    public enum DataSourceType {
        ADMIN("admin"),
        BAND("band"),
        FAN("fan");

        private final String value;

        DataSourceType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    /**
     * 设置数据源类型
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        if (dataSourceType == null) {
            log.warn("数据源类型为空，使用默认数据源");
            return;
        }
        log.debug("切换数据源到: {}", dataSourceType.getValue());
        contextHolder.set(dataSourceType.getValue());
    }

    /**
     * 设置数据源类型（通过字符串）
     */
    public static void setDataSourceType(String dataSourceType) {
        if (dataSourceType == null || dataSourceType.isEmpty()) {
            log.warn("数据源类型为空，使用默认数据源");
            return;
        }
        String type = dataSourceType.toLowerCase();
        log.debug("切换数据源到: {}", type);
        contextHolder.set(type);
    }

    /**
     * 获取数据源类型
     */
    public static String getDataSourceType() {
        String dataSource = contextHolder.get();
        log.debug("当前数据源: {}", dataSource);
        return dataSource;
    }

    /**
     * 清除数据源类型
     */
    public static void clearDataSourceType() {
        log.debug("清除数据源设置");
        contextHolder.remove();
    }
}
