package test;

import com.sdl.odata.api.edm.annotations.EdmProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public final class InheritanceAwareEdmUtil {

    private InheritanceAwareEdmUtil() { }

    public static Object getEdmPropertyValue(final Object entity,
                                             final String propertyName) throws IllegalAccessException {
        for (Field fld : getAllFields(new ArrayList<>(), entity.getClass())) {
            final EdmProperty ann = fld.getAnnotation(EdmProperty.class);
            if (ann != null && propertyName.equals(ann.name())) {
                fld.setAccessible(true);
                return fld.get(entity);
            }
        }
        throw new IllegalAccessException("No property " + propertyName +
                                         " in object of type " + entity.getClass().getName());
    }

    private static List<Field> getAllFields(final List<Field> fields,
                                            final Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
