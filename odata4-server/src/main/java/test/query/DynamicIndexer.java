package test.query;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.ReflectiveAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.query.QueryFactory;
import com.sdl.odata.api.edm.model.EntityDataModel;
import com.sdl.odata.api.edm.model.StructuralProperty;
import com.sdl.odata.edm.model.StructuredTypeImpl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Methods to generate attributes dynamically for fields in a POJO, and to create IndexedCollections
 * configured dynamically to index these attributes.
 */
public class DynamicIndexer {

    public static final Map<String,Class> PRIMITIVES = new HashMap<>();

    static {
        PRIMITIVES.put("int", Integer.class );
        PRIMITIVES.put("long", Long.class );
        PRIMITIVES.put("double", Double.class );
        PRIMITIVES.put("float", Float.class );
        PRIMITIVES.put("bool", Boolean.class );
        PRIMITIVES.put("char", Character.class );
        PRIMITIVES.put("byte", Byte.class );
        PRIMITIVES.put("void", Void.class );
        PRIMITIVES.put("short", Short.class );
    }

    /**
     * Generates attributes dynamically for the fields declared in the given POJO class.
     * <p/>
     * Implementation is currently limited to generating attributes for Comparable fields (String, Integer etc.).
     *
     * @param pojoClass A POJO class
     * @param <O> Type of the POJO class
     * @return Attributes for fields in the POJO
     */
    public static <O> Map<String, SimpleAttribute<O, Comparable>> generateAttributesForPojo(
            final Class<O> pojoClass, final EntityDataModel entityDataModel) {
        final Map<String, SimpleAttribute<O, Comparable>> generatedAttributes = new LinkedHashMap<>();
        for (Field field : pojoClass.getDeclaredFields()) {
            if (Comparable.class.isAssignableFrom(field.getType()) || field.getType().isPrimitive()) {
                @SuppressWarnings({"unchecked"})
                final Class<Comparable> fieldType = (Class<Comparable>) field.getType();
                final String fieldName = getODataFieldName(pojoClass, field, entityDataModel);
                if (PRIMITIVES.containsKey(field.getType().getTypeName())
                    && Number.class.isAssignableFrom(PRIMITIVES.get(field.getType().getTypeName()))) {
                    generatedAttributes.put(fieldName, QueryFactory.attribute(fieldName, object -> {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        try {
                            return ((Number) field.get(object)).doubleValue();
                        } catch (IllegalAccessException e) {
                            return null;
                        }
                    }));
                } else {
                    generatedAttributes.put(fieldName, ReflectiveAttribute.forField(pojoClass, fieldType, field.getName()));
                }

            }
        }
        return generatedAttributes;
    }


    private static String getODataFieldName(final Class<?> pojoClass,
                                            final Field field,
                                            final EntityDataModel entityDataModel) {
        if (entityDataModel.getType(pojoClass) instanceof StructuredTypeImpl ) {
            final List<StructuralProperty> properties = ((StructuredTypeImpl) entityDataModel.getType(pojoClass))
                    .getStructuralProperties();
            final Optional<StructuralProperty> optional = properties
                    .stream()
                    .filter(structuralProperty -> structuralProperty.getJavaField().equals(field))
                    .findFirst();
            if (optional.isPresent()) {
                return optional.get().getName();
            }
        }
        return field.getName();
    }

    /**
     * Creates an IndexedCollection and adds NavigableIndexes for the given attributes.
     *
     * @param attributes Attributes for which indexes should be added
     * @param <O> Type of objects stored in the collection
     * @return An IndexedCollection configured with indexes on the given attributes.
     */
    public static <O> IndexedCollection<O> newAutoIndexedCollection(
            final Iterable<Attribute<O, ? extends Comparable>> attributes) {
        final IndexedCollection<O> autoIndexedCollection = new ConcurrentIndexedCollection<O>();
        for (Attribute<O, ? extends Comparable> attribute : attributes) {
            // Add a NavigableIndex...
            @SuppressWarnings("unchecked")
            final NavigableIndex<? extends Comparable, O> index = NavigableIndex.onAttribute(attribute);
            autoIndexedCollection.addIndex(index);
        }
        return autoIndexedCollection;
    }

    /**
     * Private constructor, not used.
     */
    DynamicIndexer() {
    }
}