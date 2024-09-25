package br.com.poison.core.util.extra;

import java.lang.reflect.Field;

public class ReflectionUtil {

    public static void setValue(String field, Class<?> clazz, Object instance, Object value) {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static Object getValue(String field, Class<?> clazz, Object instance) {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        while ((clazz != null) && (clazz != Object.class)) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    public static void setValue(String field, Object instance, Object value) {
        try {
            Field f = instance.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static Object getValue(String field, Object instance) {
        try {
            Field f = instance.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(instance);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static void setValue(Object instance, String fieldName, Object value) {
        try {
            Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    public static <T> T getValue(Object instance, String fieldName) {
        try {
            Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
