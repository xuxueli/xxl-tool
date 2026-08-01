package com.xxl.tool.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用枚举工具类
 *
 * @author xuxueli 2015-10-01
 */
public class EnumTool {
    private static final Logger logger = LoggerFactory.getLogger(EnumTool.class);


    /**
     * 根据枚举名，查询获取枚举项数据
     *
     * @param packageList 枚举包名列表
     * @param enumName    枚举名
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
     * @param enumClass 枚举 Class路径
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
     * 通用枚举项，提取枚举项数据列表
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

        public EnumItemVO() {
        }

        public EnumItemVO(int code, String title) {
            this.code = code;
            this.title = title;
        }

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
