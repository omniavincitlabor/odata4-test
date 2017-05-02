package test;

import com.sdl.odata.api.ODataBadRequestException;
import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.annotations.EdmAction;
import com.sdl.odata.api.edm.annotations.EdmActionImport;
import com.sdl.odata.api.edm.annotations.EdmFunction;
import com.sdl.odata.api.edm.annotations.EdmFunctionImport;
import com.sdl.odata.api.edm.model.EntityDataModel;
import com.sdl.odata.api.edm.model.Operation;
import com.sdl.odata.api.parser.ODataUri;
import com.sdl.odata.api.parser.ODataUriUtil;
import com.sdl.odata.api.parser.TargetType;
import com.sdl.odata.api.processor.datasource.factory.DataSourceFactory;
import com.sdl.odata.api.processor.query.ODataQuery;
import com.sdl.odata.api.processor.query.strategy.QueryOperationStrategy;
import com.sdl.odata.api.service.ODataRequestContext;
import com.sdl.odata.parser.ODataUriParser;
import com.sdl.odata.processor.QueryModelBuilder;
import scala.Option;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public abstract class AbstractBoundOperation<BT, RT> implements Operation<RT> {

    @Override
    public RT doOperation(final ODataRequestContext requestContext,
                          final DataSourceFactory dataSourceFactory) throws ODataException {
        final EntityDataModel entityDataModel = requestContext.getEntityDataModel();

        final String uri = requestContext.getRequest().getUri();
        String baseUri = null;

        final EdmFunction function = this.getClass().getAnnotation(EdmFunction.class);
        if (function != null) {
            final String temp = getNameWithNamespace(function.namespace(), function.name());
            if (uri.contains(temp)) {
                baseUri = uri.split(temp)[0];
            }
        }

        final EdmAction action = this.getClass().getAnnotation(EdmAction.class);
        if (action != null) {
            final String temp = getNameWithNamespace(action.namespace(), action.name());
            if (uri.contains(temp)) {
                baseUri = uri.split(temp)[0];
            }
        }

        final EdmFunctionImport functionImport = this.getClass().getAnnotation(EdmFunctionImport.class);
        if (functionImport != null) {
            final String temp = getNameWithNamespace(functionImport.namespace(), functionImport.name());
            if (uri.contains(temp)) {
                baseUri = uri.split(temp)[0];
            }
        }

        final EdmActionImport actionImport = this.getClass().getAnnotation(EdmActionImport.class);
        if (action != null) {
            final String temp = getNameWithNamespace(actionImport.namespace(), actionImport.name());
            if (uri.contains(temp)) {
                baseUri = uri.split(temp)[0];
            }
        }

        baseUri = baseUri.substring(0, baseUri.length() - 1);
        final ODataUri reducedUri = new ODataUriParser(entityDataModel).parseUri(baseUri);

        final ODataRequestContext requestContext2 = requestContext.withUri(reducedUri);

        final Option<TargetType> targetTypeOption = ODataUriUtil.resolveTargetType(reducedUri, entityDataModel);
        if (!targetTypeOption.isDefined()) {
            throw new ODataBadRequestException("The target type could not be determined for this query: " +
                                               requestContext.getRequest().getUri());
        }

        final TargetType targetType = targetTypeOption.get();

        final ODataQuery query = new QueryModelBuilder(entityDataModel).build(requestContext2);

        final QueryOperationStrategy strategy = dataSourceFactory.getStrategy(requestContext2, query.operation(), targetType);
        final Object result = strategy.execute().getData();
        BT baseType = null;
        if (result instanceof List && !baseTypeIsCollection()) {
            baseType = ((List) result).isEmpty() ? null : (BT) ((List) result).get(0);
        } else if (baseTypeIsCollection() && !(result instanceof List)) {
            List temp = new ArrayList<>();
            temp.add(result);
            baseType = (BT) temp;
        } else {
            baseType = (BT) result;
        }

        return doOperation(requestContext, dataSourceFactory, baseType);
    }

    /**
     *
     * @return EntitySet Name as in <EntitySet Name="XXX" ...></EntitySet>
     *          or EntityType Name including Namespace !!!
     */
    public abstract String getBaseType();

    public abstract boolean baseTypeIsCollection();

    public abstract RT doOperation(final ODataRequestContext requestContext,
                                   final DataSourceFactory dataSourceFactory,
                                   final BT baseObject) throws ODataException;


    private String getNameWithNamespace(String namespace, String name) {
        if (namespace == null || namespace.trim().isEmpty()) {
            return name;
        }
        return namespace + "." + name;
    }
}
