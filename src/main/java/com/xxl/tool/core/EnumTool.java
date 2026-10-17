package com.xxl.tool.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用枚举工具类：按枚举类路径或包名列表反射枚举项，并转换为 {@link EnumItemVO} 列表。
 *
 * <p>仅处理实现了 {@link IEnum} 的枚举；查询失败（类不存在、非枚举、未实现 {@link IEnum}）时
 * 返回空列表而不抛出异常。所有方法均为静态方法，不修改入参；类无状态（仅持有日志对象），线程安全。
 *
 * @author xuxueli 2015-10-01
 */
public class EnumTool {
    private static final Logger logger = LoggerFactory.getLogger(EnumTool.class);


    /**
     * 根据枚举名，查询获取枚举项数据
     *
     * <p>依次按 {@code 包名 + "." + 枚举名} 拼装类路径并尝试加载，返回第一个非空结果；
     * 包名列表为空、枚举名为空白或全部包均未找到时返回空列表。
     *
     * @param packageList 枚举包名列表，可为 {@code null} 或空集合
     * @param enumName    枚举名，可为 {@code null} 或空白
     * @return 枚举项列表，未找到时返回空列表，不会返回 {@code null}
     */
    public static List<EnumItemVO> getEnumItemList(List<String> packageList, String enumName) {
        if (CollectionTool.isEmpty(packageList) || StringTool.isBlank(enumName)) {
            return new ArrayList<>();
        }

        for (String packageName : packageList) {

            // find each package
            String enumClass = packageName + "." + enumName;
            List<EnumItemVO> enumItemVOList = getEnumItemList(enumClass);

            // valid data
            if (CollectionTool.isNotEmpty(enumItemVOList)) {
                return enumItemVOList;
            }
        }

        return new ArrayList<>();
    }

    /**
     * 根据枚举 Class路径，查询枚举项数据
     *
     * <p>通过 {@link Class#forName(String)} 加载类；类路径为空白、类不存在、目标不是枚举或未实现
     * {@link IEnum} 时返回空列表。类初始化失败等其他错误不在捕获范围内，会向上抛出。
     *
     * @param enumClass 枚举 Class路径（全限定类名），可为 {@code null} 或空白
     * @return 枚举项列表，未找到或不满足条件时返回空列表，不会返回 {@code null}
     */
    public static List<EnumItemVO> getEnumItemList(String enumClass) {
        if (StringTool.isBlank(enumClass)) {
            return new ArrayList<>();
        }
        try {
            // match enum
            Class<?> clazz = Class.forName(enumClass);
            if (clazz.isEnum() && IEnum.class.isAssignableFrom(clazz)) {

                // process enum item
                List<EnumItemVO> enumItemVOList = new ArrayList<>();

                //noinspection unchecked
                for (IEnum e : ((Class<? extends IEnum>) clazz).getEnumConstants()) {
                    enumItemVOList.add(new EnumItemVO(e.getCode(), e.getTitle()));
                }

                return enumItemVOList;
            }
        } catch (ClassNotFoundException ignored) {
            logger.debug("EnumTool.getEnumItemList error, enumClass invalid:{}", enumClass, ignored);
        }
        return new ArrayList<>();
    }


    /**
     * 枚举项视图对象：承载枚举的编码、展示名称与可选说明。
     */
    public static class EnumItemVO {

        /**
         * 枚举编码
         */
        private int code;

        /**
         * 枚举展示名称
         */
        private String title;

        /**
         * 枚举详细说明（可选）
         */
        private String desc;

        /**
         * 构造空枚举项
         */
        public EnumItemVO() {
        }

        /**
         * 构造枚举项
         *
         * @param code  枚举编码
         * @param title 枚举展示名称
         */
        public EnumItemVO(int code, String title) {
            this.code = code;
            this.title = title;
        }

        /**
         * 构造枚举项
         *
         * @param code  枚举编码
         * @param title 枚举展示名称
         * @param desc  枚举详细说明，可为 {@code null}
         */
        public EnumItemVO(int code, String title, String desc) {
            this.code = code;
            this.title = title;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

    }

    /**
     * 通用枚举接口
     *
     * @author xuxueli 2015-10-01
     */
    public interface IEnum {

        /**
         * 枚举编码
         */
        int getCode();

        /**
         * 枚举展示名称
         */
        String getTitle();

        /**
         * 枚举详细说明（可选）
         */
        default String getDesc() {
            return null;
        }

    }

}
