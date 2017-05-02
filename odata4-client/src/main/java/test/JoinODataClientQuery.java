package test;


import com.sdl.odata.client.AbstractODataClientQuery;
import com.sdl.odata.client.api.ODataClientQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 *
 */
public class JoinODataClientQuery extends AbstractODataClientQuery {

    private final ODataClientQuery left;
    private final ODataClientQuery right;
    private final String joinPropertyName;


    public JoinODataClientQuery(final Builder builder) {
        if (builder.entityType == null) {
            throw new IllegalArgumentException("EntityType shouldn't be null");
        }
        setEntityType(builder.entityType);

        left = builder.left;
        right = builder.right;
        joinPropertyName = builder.joinPropertyName;
    }

    @Override
    public String getQuery() {
        String temp = right.getQuery();
        String edmEntityName = right.getEdmEntityName();
        if (edmEntityName.contains("(")){
            edmEntityName = edmEntityName.split("\\(")[0];
        }
        temp = temp.substring(edmEntityName.length());
        temp = joinPropertyName + temp;
        try {
            if (URLDecoder.decode(temp, "UTF-8").equals(temp)) {
                temp = URLEncoder.encode(temp, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            //ignore
        }
        return left.getQuery() + "/" + temp;
    }

    public static class Builder {
        private Class<?> entityType;
        private ODataClientQuery left;
        private ODataClientQuery right;
        private String joinPropertyName;

        public Builder withEntityType(final Class<?> clazz) {
            this.entityType = clazz;
            return this;
        }

        public Builder withLeftQuery(final ODataClientQuery query) {
            this.left = query;
            return this;
        }

        public Builder withRightQuery(final ODataClientQuery query) {
            this.right = query;
            return this;
        }

        public Builder withJoinPropertyName(final String joinPropertyName) {
            this.joinPropertyName = joinPropertyName;
            return this;
        }

        public JoinODataClientQuery build() {
            return new JoinODataClientQuery(this);
        }
    }
}
