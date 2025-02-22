package me.ddggdd135.slimeae.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReflectionUtils {
    /**
     * 使用反射调用对象的私有方法，并返回泛型类型的值。
     *
     * @param object        要调用方法的对象实例
     * @param methodName 私有方法的名称
     * @param args       调用方法时传递的参数，如果方法不需要参数，则传递空数组或null
     * @param <T>        返回值的泛型类型
     * @return 方法的返回值，如果方法返回void或调用失败，则返回null
     * @throws RuntimeException 如果反射调用失败
     */
    @Nullable public static <T> T invokePrivateMethod(
            @Nonnull Object object,
            @Nonnull String methodName,
            @Nonnull Class<?>[] parameterTypes,
            @Nonnull Object... args) {
        try {
            Class<?> clazz = object.getClass();

            while (true) {
                if (clazz.equals(Object.class)) {
                    throw new RuntimeException("No such method: " + methodName);
                }

                try {
                    // 获取Method对象
                    Method method = clazz.getDeclaredMethod(methodName, parameterTypes);

                    // 设置访问权限为true，允许访问私有方法
                    method.setAccessible(true);

                    // 调用方法
                    return (T) method.invoke(object, args);
                } catch (NoSuchMethodException ignored) {
                }

                clazz = clazz.getSuperclass();
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access to method: " + methodName, e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            } else {
                throw new RuntimeException("Invocation target exception in method: " + methodName, targetException);
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception in method invocation: " + methodName, e);
        }
    }

    @Nullable public static <T> T getField(@Nonnull Object object, @Nonnull String fieldName) {
        try {
            Field[] fields = getAllFields(object);
            Field field = null;
            for (Field f : fields) {
                if (f.getName().equals(fieldName)) {
                    field = f;
                    break;
                }
            }
            if (field == null) throw new NoSuchFieldException(fieldName);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void setField(@Nonnull Object object, @Nonnull String fieldName, @Nullable T value) {
        try {
            Field[] fields = getAllFields(object);
            Field field = null;
            for (Field f : fields) {
                if (f.getName().equals(fieldName)) {
                    field = f;
                    break;
                }
            }
            if (field == null) throw new NoSuchFieldException(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static Field[] getAllFields(@Nonnull Object object) {
        Class<?> clazz = object.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }
}
