package com.xxl.tool.core;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Class Tool
 *
 * @author xuxueli 2024-01-21 05:03:10
 */
public class TypeTool {


    // ---------------------- type ----------------------

    /**
     * Check if the right-hand side type may be assigned to the left-hand side type
     *
     * @param superType     super type
     * @param subType       the sub type that should be assigned to the target type
     * @return              true if subType is assignable to superType
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
