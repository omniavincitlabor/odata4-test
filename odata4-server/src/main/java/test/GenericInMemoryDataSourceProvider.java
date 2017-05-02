package test;

import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.model.EntityDataModel;
import com.sdl.odata.api.parser.TargetType;
import com.sdl.odata.api.processor.datasource.DataSource;
import com.sdl.odata.api.processor.datasource.DataSourceProvider;
import com.sdl.odata.api.processor.datasource.ODataDataSourceException;
import com.sdl.odata.api.processor.query.QueryOperation;
import com.sdl.odata.api.processor.query.QueryResult;
import com.sdl.odata.api.processor.query.strategy.QueryOperationStrategy;
import com.sdl.odata.api.service.ODataRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.query.QueryExecuter;
import test.query.QueryOptionBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * This is an example data source provide that uses in memory structures to demonstrate how to provide
 * entities, storing and querying capabilities to the OData v4 framework.
 */
public class GenericInMemoryDataSourceProvider implements DataSourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GenericInMemoryDataSourceProvider.class);

    private final IndexedCollectionDataSource dataSource;

    public GenericInMemoryDataSourceProvider(final IndexedCollectionDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean isSuitableFor(ODataRequestContext oDataRequestContext, String entityType) throws ODataDataSourceException {
        return dataSource.getSuitableClasses().contains(oDataRequestContext.getEntityDataModel().getType(entityType).getJavaType());
    }

    @Override
    public DataSource getDataSource(ODataRequestContext oDataRequestContext) {
        return dataSource;
    }

    @Override
    public QueryOperationStrategy getStrategy(ODataRequestContext oDataRequestContext, QueryOperation queryOperation, TargetType targetType) throws ODataException {
        EntityDataModel entityDataModel = oDataRequestContext.getEntityDataModel();
        Class<?> targetJavaType = oDataRequestContext.getEntityDataModel().getType(targetType.typeName()).getJavaType();

        QueryOptionBuilder builder = new QueryOptionBuilder();
        builder.build(queryOperation, oDataRequestContext);

        QueryExecuter<?> qe = new QueryExecuter(targetJavaType,
                                                queryOperation,
                                                dataSource.getTableProvider(),
                                                entityDataModel);

        final QueryResult finalResult = prepareQueryResult(builder, qe.execute());
        return () -> finalResult;
    }

    private <T>QueryResult prepareQueryResult(final QueryOptionBuilder builder,
                                              final Collection<T> rawResult) {
        Collection<T> queryResult = rawResult;

        QueryResult result = null;

        long count = 0;
        if (builder.isCount() || builder.includeCount()) {
            count = queryResult.size();
            LOG.debug("Counted {} persons matching query", count);

            if (builder.isCount()) {
                result = QueryResult.from(count);
            }
        }

        queryResult = queryResult.stream()
                                 .limit(builder.getLimit())
                                 .skip(builder.getSkip())
                                 .collect(Collectors.toList());

        if (result == null) {
            if (builder.getPropertyNames() != null && !builder.getPropertyNames().isEmpty()) {
                try {
                    LOG.debug("Selecting {} properties of person", builder.getPropertyNames());
                    result = QueryResult.from(InheritanceAwareEdmUtil.getEdmPropertyValue(queryResult.stream().findFirst().get(), builder.getPropertyNames().get(0)));
                } catch (IllegalAccessException e) {
                    LOG.error(e.getMessage(), e);
                    result = QueryResult.from(Collections.emptyList());
                }
            }
        }

        if (result == null) {
            result = QueryResult.from(queryResult);
            if (builder.includeCount()) {
                result = result.withCount(count);
            }
        }

        return result;
    }
}