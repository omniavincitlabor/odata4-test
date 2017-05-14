package test;

import java.time.Period;

/**
 *
 */
public interface ObjectTypeAwareJsonProducer {

    default String getStringValue(final Object value) {
        if (value instanceof String) {
            return String.format("'%s'", ((String) value).replaceAll("'", "''"));
        } else if (value instanceof Period) {
            return String.format("duration'%s'", value.toString());
        } else {
            return value != null ? value.toString() : null;
        }
    }
}
