package bespoken.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpk on 11/10/16.
 */
public class ReflectionUtil {
    public static List<Field> allFieldsForClass (Class<?> clazz) {
        Field [] fieldArray = clazz.getDeclaredFields();
        List<Field> fields = new ArrayList<Field>();
        for (Field field : fieldArray) {
            //Ignore static fields
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            fields.add(field);
        }
        if (clazz.getSuperclass() != null) {
            fields.addAll(allFieldsForClass(clazz.getSuperclass()));
        }
        return fields;
    }

    public static Field getField (Class<?> clazz, String fieldName) {
        List<Field> fields = allFieldsForClass(clazz);
        for (Field field : fields) {
            //Logger.info("Field: " + field.getName());
            if (field.getName().equals(fieldName)) {
                //Logger.info("Match");
                return field;
            }
        }
        return null;
    }

    public static Object get(Object object, String fieldName) {
        Field field = ReflectionUtil.getField(object.getClass(), fieldName);
        return get(object, field);
    }

    public static Object get(Object object, Field field) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
