package test;

import com.sdl.odata.client.AbstractODataClientQuery;
import com.sdl.odata.client.api.ODataClientQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class MyBoundFunctionClientQuery extends AbstractODataClientQuery implements ObjectTypeAwareJsonProducer {

    private String boundEntityName;
    private String functionNameSpace;
    private String functionName;
    private Map<String, Object> functionParameterMap;

    public MyBoundFunctionClientQuery(final Builder builder) {
        setEntityType(builder.entityType);

        checkNotNull(builder.boundEntityName, "Bound Entity Name shouldn't be null");
        this.boundEntityName = builder.boundEntityName;
        this.functionNameSpace = builder.functionNameSpace;
        this.functionName = builder.functionName;
        this.functionParameterMap = builder.functionParameterMap;
    }

    protected String appendFunctionPath() {
        return this.functionName;
    }

    protected String generateFunctionParameters() {
        if (this.functionParameterMap == null || this.functionParameterMap.isEmpty()) {
            return "";
        }

        return "(" + this.functionParameterMap.entrySet().stream()
                .filter(entity -> entity.getValue() != null)
                .map(entry -> String.format("%s=%s", entry.getKey(), getStringValue(entry.getValue())))
                .collect(Collectors.joining(","))
                + ")";
    }

    protected String getFunctionName() {
        return functionName;
    }

    protected Map<String, Object> getFunctionParameterMap() {
        return functionParameterMap;
    }

    @Override
    public String getQuery() {
        try {
            return boundEntityName
                    + "/"
                    + functionNameSpace
                    + "."
                    + appendFunctionPath()
                    + URLEncoder.encode(generateFunctionParameters(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return boundEntityName
                    + "/"
                    + functionNameSpace
                    + "."
                    + appendFunctionPath()
                    + generateFunctionParameters();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MyBoundFunctionClientQuery that = (MyBoundFunctionClientQuery) o;

        if (!boundEntityName.equals(that.boundEntityName)) {
            return false;
        }
        if (!getFunctionName().equals(that.getFunctionName())) {
            return false;
        }
        if (!functionNameSpace.equals(that.functionNameSpace)) {
            return false;
        }
        if (getFunctionParameterMap() != null ? !getFunctionParameterMap().equals(that.getFunctionParameterMap())
                : that.getFunctionParameterMap() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = boundEntityName.hashCode();
        result = HASH * result + functionNameSpace.hashCode();
        result = HASH * result + getFunctionName().hashCode();
        result = HASH * result + getFunctionParameterMap().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("ODataClientQuery[%s]", getQuery());
    }

    /**
     * Builder for {@code ODataRequest} objects.
     */
    public static class Builder {

        private Class<?> entityType;
        private String functionName;
        private Map<String, Object> functionParameterMap;
        private String boundEntityName;
        private String functionNameSpace;

        public Builder withBoundEntityName(final String boundEntity) {
            this.boundEntityName = boundEntity;
            return this;
        }

        public Builder withNameSpace(final String nameSpace) {
            this.functionNameSpace = nameSpace;
            return this;
        }

        public Builder withEntityType(final Class<?> clazz) {
            this.entityType = clazz;
            return this;
        }

        public Builder withFunctionParameter(final String functionParameterName, final Object functionParameterValue) {
            if (this.functionParameterMap == null) {
                functionParameterMap = new LinkedHashMap<>();
            }
            this.functionParameterMap.put(functionParameterName, functionParameterValue);
            return this;
        }

        public Builder withFunctionName(final String name) {
            this.functionName = name;
            return this;
        }

        public ODataClientQuery build() {
            return new MyBoundFunctionClientQuery(this);
        }
    }
}