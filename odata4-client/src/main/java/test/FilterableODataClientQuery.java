package test;

import com.sdl.odata.api.edm.annotations.EdmEntitySet;
import com.sdl.odata.api.edm.annotations.EdmSingleton;
import com.sdl.odata.client.AbstractODataClientQuery;
import com.sdl.odata.client.api.exception.ODataClientRuntimeException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sdl.odata.util.ReferenceUtil.isNullOrEmpty;
import static com.sdl.odata.util.edm.EntityDataModelUtil.pluralize;
import static java.lang.String.format;

/**
 *
 */
public class FilterableODataClientQuery extends AbstractODataClientQuery {

    private static final String EXPAND_PREFIX = "$expand=";
    private static final String FILTER_PREFIX = "$filter=";

    private final List<Filter> filterList;
    private final List<String> expandParameters;
    private final List<EntityKey> entityKeys;

    private boolean isSingletonEntity;


    public FilterableODataClientQuery(final Builder builder) {
        if (builder.entityType == null) {
            throw new IllegalArgumentException("EntityType shouldn't be null");
        }
        setEntityType(builder.entityType);
        setEntityKey(builder.entityKeys == null || builder.entityKeys.isEmpty()
                             ? null
                             : builder.entityKeys.get(0).keyValue);

        this.entityKeys = builder.entityKeys;
        this.filterList = builder.filterList;
        this.expandParameters = builder.expandParameters;
    }

    public String getQuery() {
        final StringBuilder query = new StringBuilder();
        query.append(getEdmEntityName());
        if (!isSingletonEntity()) {
            query.append(generateParameters());
        }
        try {
            return URLEncoder.encode(query.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return query.toString();
        }
    }

    private StringBuilder generateParameters() {
        final StringBuilder parameters = new StringBuilder();
        if (filterList == null && expandParameters == null) {
            return parameters;
        }
        parameters.append('?');
        int filterParameterCounter = 0;
        if (filterList != null && !filterList.isEmpty()) {
            parameters.append(FILTER_PREFIX);

            for (Filter filter : filterList) {
                if (filter.quote) {
                    parameters.append(String.format("%s %s '%s'", filter.property, filter.operation.oDataOperation, filter.value));
                } else {
                    parameters.append(String.format("%s %s %s", filter.property, filter.operation.oDataOperation, filter.value));
                }
                if (++filterParameterCounter < filterList.size()) {
                    parameters.append(" and ");
                }
            }

            if (expandParameters != null) {
                parameters.append("&");
            }
        }

        if (expandParameters != null) {
            parameters.append(EXPAND_PREFIX);
            Iterator iterator = expandParameters.iterator();
            parameters.append(String.format("%s", iterator.next()));
            while (iterator.hasNext()) {
                parameters.append(String.format(",%s", iterator.next()));
            }
        }
        return parameters;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FilterableODataClientQuery that = (FilterableODataClientQuery) o;

        return Objects.equals(getEntityType(), that.getEntityType()) &&
               Objects.equals(filterList, that.filterList) &&
               Objects.equals(expandParameters, that.expandParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntityType(), filterList, expandParameters);
    }


    public String getEdmEntityName() {
        final EdmEntitySet edmEntitySet = getEntityType().getAnnotation(EdmEntitySet.class);

        if (edmEntitySet != null) {
            String entitySetName = edmEntitySet.name();
            if (isNullOrEmpty(entitySetName)) {
                entitySetName = edmEntitySet.value();
                if (isNullOrEmpty(entitySetName)) {
                    // Use automatically pluralized simple name of entity type if
                    // no name for the entity set is specified
                    entitySetName = pluralize(getEntityType().getSimpleName());
                }
            }
            return appendEntityKeySuffix(entitySetName);
        } else {
            // Check for Singleton entity in the container
            final EdmSingleton singleton = getEntityType().getAnnotation(EdmSingleton.class);

            if (singleton == null) {
                throw new ODataClientRuntimeException(
                        "There is no an odata endpoint for provided class. " +
                        "@EdmEntitySet or @EdmSingleton annotation is not present on this type");
            }
            isSingletonEntity = true;
            String entityName = getEntityType().getSimpleName();

            if (isNullOrEmpty(entityName)) {
                entityName = singleton.value();
            }
            return entityName;
        }
    }

    private String appendEntityKeySuffix(final String entityName) {
        if (entityKeys != null && !entityKeys.isEmpty()) {
            final String temp = entityKeys.stream().map(EntityKey::toString).collect(Collectors.joining(","));
            return format("%s(%s)", entityName, temp);
        }
        return entityName;
    }

    protected boolean isSingletonEntity() {
        return isSingletonEntity;
    }

    /**
     * Builder for {@code ODataRequest} objects.
     */
    public static class Builder {

        private Class<?> entityType;
        private List<String> expandParameters;
        //Using LinkedHashMap to preserve filter parameters insertion order.
        private List<Filter> filterList;
        private List<EntityKey> entityKeys;

        public Builder withEntityType(final Class<?> clazz) {
            this.entityType = clazz;
            return this;
        }

        public Builder withFilter(final Filter filter) {
            if (this.filterList == null) {
                filterList = new ArrayList<>();
            }
            this.filterList.add(filter);
            return this;
        }

        public Builder withExpandParameters(final String expandParameter) {
            if (this.expandParameters == null) {
                expandParameters = new ArrayList<>();
            }
            this.expandParameters.add(expandParameter);
            return this;
        }

        public Builder withEntityKey(final String entityKeyValue, final boolean quoteIt) {
            if (this.entityKeys == null) {
                entityKeys = new ArrayList<>();
            }
            this.entityKeys.clear();
            this.entityKeys.add(new EntityKey(entityKeyValue, quoteIt));
            return this;
        }

        public Builder andEntityKey(final String entityKeyValue, final boolean quoteIt) {
            this.entityKeys.add(new EntityKey(entityKeyValue, quoteIt));
            return this;
        }

        public FilterableODataClientQuery build() {
            return new FilterableODataClientQuery(this);
        }
    }

    public static class Filter {
        private final String property;
        private final FilterOperation operation;
        private final String value;
        private final boolean quote;

        public Filter(final String property, final FilterOperation operation, final String value, final boolean quote) {
            this.property = property;
            this.operation = operation;
            this.value = value;
            this.quote = quote;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Filter filter = (Filter) o;
            return quote == filter.quote &&
                   Objects.equals(property, filter.property) &&
                   operation == filter.operation &&
                   Objects.equals(value, filter.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(property, operation, value, quote);
        }
    }

    public enum FilterOperation {
        EQUALS("eq"),
        IS_NOT_EQUAL_TO("ne"),
        IS_LESS_THAN("lt"),
        IS_GREATER_THAN("gt"),
        IS_LESS_OR_EQUAL_TO("le"),
        IS_GREATER_OR_EQUAL_TO("ge");

        private final String oDataOperation;

        FilterOperation(final String oDataOperation) {
            this.oDataOperation = oDataOperation;
        }
    }

    public static class EntityKey {
        private final String keyValue;
        private final boolean quote;

        public EntityKey(final String keyValue, final boolean quote) {
            this.keyValue = keyValue;
            this.quote = quote;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final EntityKey entityKey = (EntityKey) o;
            return quote == entityKey.quote &&
                   Objects.equals(keyValue, entityKey.keyValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyValue, quote);
        }

        @Override
        public String toString() {
            if (quote) {
                return format("'%s'", keyValue);
            } else {
                return format("%s", keyValue);
            }
        }
    }
}