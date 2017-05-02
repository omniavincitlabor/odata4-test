package test.query;

import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.parser.CountOption;
import com.sdl.odata.api.parser.ODataUriUtil;
import com.sdl.odata.api.parser.QueryOption;
import com.sdl.odata.api.processor.query.CountOperation;
import com.sdl.odata.api.processor.query.CriteriaFilterOperation;
import com.sdl.odata.api.processor.query.ExpandOperation;
import com.sdl.odata.api.processor.query.LimitOperation;
import com.sdl.odata.api.processor.query.OrderByOperation;
import com.sdl.odata.api.processor.query.QueryOperation;
import com.sdl.odata.api.processor.query.SelectByKeyOperation;
import com.sdl.odata.api.processor.query.SelectOperation;
import com.sdl.odata.api.processor.query.SelectPropertiesOperation;
import com.sdl.odata.api.processor.query.SkipOperation;
import com.sdl.odata.api.service.ODataRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.Iterator;

import java.util.List;

/**
 *
 */
public class QueryOptionBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(QueryOptionBuilder.class);

    private int limit = Integer.MAX_VALUE;
    private int skip = 0;
    private boolean count;
    private boolean includeCount;
    private List<String> propertyNames;

    public void build(final QueryOperation queryOperation, final ODataRequestContext requestContext)
            throws ODataException {
        buildFromOptions(ODataUriUtil.getQueryOptions(requestContext.getUri()));
        buildFromOperation(queryOperation);
    }

    public int getLimit() {
        return limit;
    }

    public int getSkip() {
        return skip;
    }

    public boolean isCount() {
        return count;
    }

    public boolean includeCount() {
        return includeCount;
    }

    public List<String> getPropertyNames() {
        return propertyNames;
    }

    private void buildFromOperation(final QueryOperation operation) throws ODataException {
        if (operation instanceof SelectOperation) {
            buildFromSelect((SelectOperation) operation);
        } else if (operation instanceof SelectByKeyOperation) {
            //ignore
        } else if (operation instanceof CriteriaFilterOperation) {
            //ignore
        } else if (operation instanceof LimitOperation) {
            buildFromLimit((LimitOperation) operation);
        } else if (operation instanceof CountOperation) {
            buildFromCount((CountOperation) operation);
        } else if (operation instanceof SkipOperation) {
            buildFromSkip((SkipOperation) operation);
        } else if (operation instanceof ExpandOperation) {
            //not supported for now
        } else if (operation instanceof OrderByOperation) {
            //not supported for now
        } else if (operation instanceof SelectPropertiesOperation) {
            buildFromSelectProperties((SelectPropertiesOperation) operation);
        } else {
            //ignore
        }
    }

    private void buildFromOptions(final scala.collection.immutable.List<QueryOption> queryOptions) {
        final Iterator<QueryOption> optIt = queryOptions.iterator();
        while (optIt.hasNext()) {
            final QueryOption opt = optIt.next();
            if (opt instanceof CountOption && ((CountOption) opt).value()) {
                includeCount = true;
                break;
            }
        }
    }

    private void buildFromSelectProperties(final SelectPropertiesOperation operation) throws ODataException {
        this.propertyNames = operation.getPropertyNamesAsJava();
        LOG.debug("Selecting properties: {}", propertyNames);
        buildFromOperation(operation.getSource());
    }

    private void buildFromLimit(final LimitOperation operation) throws ODataException {
        this.limit = operation.getCount();
        LOG.debug("Limit has been set to: {}", limit);
        buildFromOperation(operation.getSource());
    }

    private void buildFromSkip(final SkipOperation operation) throws ODataException {
        this.skip = operation.getCount();
        LOG.debug("Skip has been set to: {}", limit);
        buildFromOperation(operation.getSource());
    }

    private void buildFromCount(final CountOperation operation) throws ODataException {
        //Caution: if count was identified as Query Option it should not be counted as Operation
        if (!includeCount) {
            this.count = true;
            LOG.debug("Counting {} records", operation.getSource().entitySetName());
            buildFromOperation(operation.getSource());
        } else {
            LOG.debug("Count is mere query option");
        }
    }

    private void buildFromSelect(final SelectOperation selectOperation) {
        LOG.debug("Selecting all persons, no predicates needed");
    }


}
