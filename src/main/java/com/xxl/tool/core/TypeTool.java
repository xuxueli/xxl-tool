package com.xxl.tool.core;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Type Tool / 类型工具：基于 {@link java.lang.reflect.Type} 判断类型之间的可赋值关系。
 *
 * <p>支持 {@link Class}、{@link ParameterizedType}、{@link GenericArrayType} 与 {@link WildcardType}。
 * 所有方法均为静态方法，不修改入参；类无状态，线程安全。
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class TypeTool {


    // ---------------------- type ----------------------

    /**
     * 判断右侧类型（{@code subType}）是否可赋值给左侧类型（{@code superType}），即
     * {@code subType} 是否为 {@code superType} 的子类型；两者相同或左侧为 {@link Object} 时恒为 {@code true}。
     *
     * @param superType 左侧（目标）类型，不能为 {@code null}
     * @param subType   右侧（来源）类型，不能为 {@code null}
     * @return {@code subType} 可赋值给 {@code superType} 时返回 {@code true}，否则返回 {@code false}
     * @throws IllegalArgumentException 任一参数为 {@code null} 时抛出
     */
    public static boolean isAssignable(Type superType, Type subType) {
        AssertTool.notNull(superType, "Left-hand side type must not be null");
        AssertTool.notNull(subType, "Right-hand side type must not be null");

        // all types are assignable to themselves and to class Object
        if (superType.equals(subType) || Object.class == superType) {
            return true;
        }

        if (superType instanceof Class) {
            Class<?> lhsClass = (Class<?>) superType;

            // just comparing two classes
            if (subType instanceof Class) {
                return ClassTool.isAssignable(lhsClass, (Class<?>) subType);
            }

            // parameterized types are only assignable to other parameterized types
            if (subType instanceof ParameterizedType) {
                Type rhsRaw = ((ParameterizedType) subType).getRawType();

                // a parameterized type is always assignable to its raw class type
                if (rhsRaw instanceof Class) {
                    return ClassTool.isAssignable(lhsClass, (Class<?>) rhsRaw);
                }
            } else if (lhsClass.isArray() && subType instanceof GenericArrayType) {
                Type rhsComponent = ((GenericArrayType) subType).getGenericComponentType();

                return isAssignable(lhsClass.getComponentType(), rhsComponent);
            }
        }

        // parameterized types are only assignable to other parameterized types and class types
        if (superType instanceof ParameterizedType) {
            if (subType instanceof Class) {
                Type lhsRaw = ((ParameterizedType) superType).getRawType();

                if (lhsRaw instanceof Class) {
                    return ClassTool.isAssignable((Class<?>) lhsRaw, (Class<?>) subType);
                }
            }
            else if (subType instanceof ParameterizedType) {
                return isAssignable((ParameterizedType) superType, (ParameterizedType) subType);
            }
        }

        if (superType instanceof GenericArrayType) {
            Type lhsComponent = ((GenericArrayType) superType).getGenericComponentType();

            if (subType instanceof Class) {
                Class<?> rhsClass = (Class<?>) subType;

                if (rhsClass.isArray()) {
                    return isAssignable(lhsComponent, rhsClass.getComponentType());
                }
            }
            else if (subType instanceof GenericArrayType) {
                Type rhsComponent = ((GenericArrayType) subType).getGenericComponentType();

                return isAssignable(lhsComponent, rhsComponent);
            }
        }

        if (superType instanceof WildcardType) {
            return isAssignable((WildcardType) superType, subType);
        }

        return false;
    }

}
