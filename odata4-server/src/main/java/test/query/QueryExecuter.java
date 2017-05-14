package test.query;


import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.ReflectiveAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.attribute.support.SimpleFunction;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.sdl.odata.api.ODataSystemException;
import com.sdl.odata.api.edm.model.EntityDataModel;
import com.sdl.odata.api.edm.model.EntitySet;
import com.sdl.odata.api.edm.model.StructuralProperty;
import com.sdl.odata.api.processor.query.AddOperator$;
import com.sdl.odata.api.processor.query.AndOperator$;
import com.sdl.odata.api.processor.query.ArithmeticCriteriaValue;
import com.sdl.odata.api.processor.query.ArithmeticOperator;
import com.sdl.odata.api.processor.query.ComparisonCriteria;
import com.sdl.odata.api.processor.query.ComparisonOperator;
import com.sdl.odata.api.processor.query.CompositeCriteria;
import com.sdl.odata.api.processor.query.CompositeOperator;
import com.sdl.odata.api.processor.query.ContainsMethodCriteria;
import com.sdl.odata.api.processor.query.CountOperation;
import com.sdl.odata.api.processor.query.Criteria;
import com.sdl.odata.api.processor.query.CriteriaFilterOperation;
import com.sdl.odata.api.processor.query.CriteriaValue;
import com.sdl.odata.api.processor.query.DivOperator$;
import com.sdl.odata.api.processor.query.EndsWithMethodCriteria;
import com.sdl.odata.api.processor.query.EqOperator$;
import com.sdl.odata.api.processor.query.ExpandOperation;
import com.sdl.odata.api.processor.query.GeOperator$;
import com.sdl.odata.api.processor.query.GtOperator$;
import com.sdl.odata.api.processor.query.JoinOperation;
import com.sdl.odata.api.processor.query.LeOperator$;
import com.sdl.odata.api.processor.query.LimitOperation;
import com.sdl.odata.api.processor.query.LiteralCriteriaValue;
import com.sdl.odata.api.processor.query.LtOperator$;
import com.sdl.odata.api.processor.query.MethodCriteria;
import com.sdl.odata.api.processor.query.ModOperator$;
import com.sdl.odata.api.processor.query.MulOperator$;
import com.sdl.odata.api.processor.query.NeOperator$;
import com.sdl.odata.api.processor.query.OrOperator$;
import com.sdl.odata.api.processor.query.OrderByOperation;
import com.sdl.odata.api.processor.query.OrderByProperty;
import com.sdl.odata.api.processor.query.PropertyCriteriaValue;
import com.sdl.odata.api.processor.query.QueryOperation;
import com.sdl.odata.api.processor.query.SelectByKeyOperation;
import com.sdl.odata.api.processor.query.SelectOperation;
import com.sdl.odata.api.processor.query.SelectPropertiesOperation;
import com.sdl.odata.api.processor.query.SkipOperation;
import com.sdl.odata.api.processor.query.StartsWithMethodCriteria;
import com.sdl.odata.api.processor.query.SubOperator$;
import com.sdl.odata.edm.model.EnumMemberImpl;
import com.sdl.odata.edm.model.StructuredTypeImpl;
import scala.math.BigDecimal;
import test.TableProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 */
public class QueryExecuter<T> {

    private final Class<T> type;
    private final EntityDataModel entityDataModel;
    private final QueryOperation operation;
    private final TableProvider tableProvider;

    private final Map<String, SimpleAttribute<T, Comparable>> attributes;
    private ConcurrentIndexedCollection<T> list;
    private List<Object> queryOptions = new ArrayList<>();

    public QueryExecuter(final Class<T> type,
                         final QueryOperation operation,
                         final TableProvider tableProvider,
                         final EntityDataModel entityDataModel) {
        this.type = type;
        this.entityDataModel = entityDataModel;
        this.operation = operation;
        this.tableProvider = tableProvider;

        attributes = DynamicIndexer.generateAttributesForPojo(type, entityDataModel);
        list = tableProvider.getIndexedCollectionForType(type);
    }

    public Collection<T> execute() {
        final Query<T> query = handleQueryOperation(operation);
        Collection<T> queryResult = null;
        if (query != null) {
            if (queryOptions.isEmpty()) {
                queryResult = StreamSupport
                        .stream(list.retrieve(query).spliterator(), false)
                        .collect(Collectors.toList());
            } else {
                queryResult = StreamSupport
                        .stream(list.retrieve(query, QueryFactory.queryOptions(queryOptions)).spliterator(), false)
                        .collect(Collectors.toList());
            }
        } else {
            queryResult = list;
        }
        return queryResult;
    }


    private Query<T> handleQueryOperation(final QueryOperation operation) {
        if (operation instanceof JoinOperation) {
            final String entitySetNameLeft = ((JoinOperation) operation).getLeftSource().entitySetName();
            final EntitySet entitySetLeft = entityDataModel.getEntityContainer().getEntitySet(entitySetNameLeft);
            final Class<?> javaTypeLeft = entityDataModel.getType(entitySetLeft.getTypeName()).getJavaType();



            final String joinPropName = ((JoinOperation) operation).getJoinPropertyName();
            final List<StructuralProperty> properties = ((StructuredTypeImpl) entityDataModel.getType(javaTypeLeft))
                    .getStructuralProperties();
            final Optional<StructuralProperty> optional = properties
                    .stream()
                    .filter(prop -> prop.getName().equals(joinPropName))
                    .findFirst();

            final QueryExecuter<?> qbLeft = new QueryExecuter(javaTypeLeft,
                    ((JoinOperation) operation).getLeftSource(),
                    tableProvider,
                    entityDataModel);

            final Collection<?> left = qbLeft.execute();

            if (optional.isPresent()) {
                final SimpleAttribute att = ReflectiveAttribute
                        .forField(javaTypeLeft,
                                optional.get().getJavaField().getType(),
                                optional.get().getJavaField().getName());

                list.clear();
                for (Object o : left) {
                    if (Collection.class.isAssignableFrom(optional.get().getJavaField().getType())) {
                        list.addAll((Collection<T>) att.getValue(o, null));
                    } else {
                        list.add((T) att.getValue(o, null));
                    }
                }
            }

            return handleQueryOperation(((JoinOperation) operation).getRightSource());
        } else if (operation instanceof SelectOperation) {
            return QueryFactory.all(type);
        } else if (operation instanceof SelectByKeyOperation) {
            //
            final Query<T> source = handleQueryOperation(((SelectByKeyOperation) operation).getSource());
            final SelectByKeyOperation selectByKeyOperation = (SelectByKeyOperation) operation;
            final Map<String, Object> keys = selectByKeyOperation.getKeyAsJava();
            Query<T> last = null;
            for (String key : keys.keySet()) {
                Comparable value = null;
                if (keys.get(key) instanceof BigDecimal) {
                    value = ((BigDecimal) keys.get(key)).doubleValue();
                }
                if (keys.get(key) instanceof Comparable) {
                    value = (Comparable) keys.get(key);
                }
                final Query<T> query = QueryFactory.equal(attributes.get(key), value);
                if (last == null) {
                    last = query;
                } else {
                    last = QueryFactory.and(last, query);
                }
            }
            return QueryFactory.and(source, last);
        } else if (operation instanceof CriteriaFilterOperation) {
            final Criteria criteria = ((CriteriaFilterOperation) operation).getCriteria();
            final Query<T> source = handleQueryOperation(((CriteriaFilterOperation) operation).getSource());
            return QueryFactory.and(source, handleCriteria(criteria));
        } else if (operation instanceof LimitOperation) {
            return handleQueryOperation(((LimitOperation) operation).getSource());
        } else if (operation instanceof CountOperation) {
            return handleQueryOperation(((CountOperation) operation).getSource());
        } else if (operation instanceof SkipOperation) {
            return handleQueryOperation(((SkipOperation) operation).getSource());
        } else if (operation instanceof ExpandOperation) {
            return handleQueryOperation(((ExpandOperation) operation).getSource());
        } else if (operation instanceof OrderByOperation) {
            for (OrderByProperty prop : ((OrderByOperation) operation).getOrderByPropertiesAsJava()) {
                if (prop.getDirection().toString().equals("ASC")) {
                    queryOptions.add(QueryFactory.orderBy(QueryFactory.ascending(getAttributeForProperty(prop.getPropertyName()))));
                } else {
                    queryOptions.add(QueryFactory.orderBy(QueryFactory.descending(getAttributeForProperty(prop.getPropertyName()))));
                }
            }
            return handleQueryOperation(((OrderByOperation) operation).getSource());
        } else if (operation instanceof SelectPropertiesOperation) {
            return handleQueryOperation(((SelectPropertiesOperation) operation).getSource());
        } else {
            throw new ODataSystemException("Unsupported query operation: " + operation);
        }
    }

    private Query<T> handleCriteria(final Criteria criteria) {
        if (criteria instanceof CompositeCriteria) {
            final CompositeOperator operator = ((CompositeCriteria) criteria).getOperator();
            final Criteria left = ((CompositeCriteria) criteria).getLeft();
            final Criteria right = ((CompositeCriteria) criteria).getRight();

            final Query<T> query1 = handleCriteria(left);
            final Query<T> query2 = handleCriteria(right);
            if (operator instanceof AndOperator$) {
                return QueryFactory.and(query1, query2);
            }
            if (operator instanceof OrOperator$) {
                return QueryFactory.or(query1, query2);
            }
        }
        if (criteria instanceof MethodCriteria) {
            if (criteria instanceof ContainsMethodCriteria) {
                final Object rawAttribute = handleCriteriaValue(((ContainsMethodCriteria) criteria).getProperty());
                final Object rawValue = handleCriteriaValue(((ContainsMethodCriteria) criteria).getStringLiteral());
                if (rawValue instanceof CharSequence) {
                    final SimpleAttribute<T, CharSequence> attribute = (SimpleAttribute<T, CharSequence>) rawAttribute;
                    final CharSequence value = (CharSequence) rawValue;
                    return QueryFactory.contains(attribute, value);
                }
            }
            if (criteria instanceof EndsWithMethodCriteria) {
                final Object rawAttribute = handleCriteriaValue(((EndsWithMethodCriteria) criteria).getProperty());
                final Object rawValue = handleCriteriaValue(((EndsWithMethodCriteria) criteria).getStringLiteral());
                if (rawValue instanceof CharSequence) {
                    final SimpleAttribute<T, CharSequence> attribute = (SimpleAttribute<T, CharSequence>) rawAttribute;
                    final CharSequence value = (CharSequence) rawValue;
                    return QueryFactory.endsWith(attribute, value);
                }
            }
            if (criteria instanceof StartsWithMethodCriteria) {
                final Object rawAttribute = handleCriteriaValue(((StartsWithMethodCriteria) criteria).getProperty());
                final Object rawValue = handleCriteriaValue(((StartsWithMethodCriteria) criteria).getStringLiteral());
                if (rawValue instanceof CharSequence) {
                    final SimpleAttribute<T, CharSequence> attribute = (SimpleAttribute<T, CharSequence>) rawAttribute;
                    final CharSequence value = (CharSequence) rawValue;
                    return QueryFactory.startsWith(attribute, value);
                }
            }
        }
        if (criteria instanceof ComparisonCriteria) {
            final ComparisonOperator operator = ((ComparisonCriteria) criteria).getOperator();
            final CriteriaValue left = ((ComparisonCriteria) criteria).getLeft();
            final CriteriaValue right = ((ComparisonCriteria) criteria).getRight();

            final SimpleAttribute<T, Comparable> rawAttribute = (SimpleAttribute<T, Comparable>) handleCriteriaValue(left);
            final Object rawValue = handleCriteriaValue(right);
            Comparable value = null;
            if (rawValue instanceof BigDecimal) {
                value = ((BigDecimal) rawValue).doubleValue();
            }
            if (rawValue instanceof Comparable) {
                value = (Comparable) rawValue;
            }

            if (operator instanceof LeOperator$) {
                return QueryFactory.lessThanOrEqualTo(rawAttribute, value);
            }
            if (operator instanceof GeOperator$) {
                return QueryFactory.greaterThanOrEqualTo(rawAttribute, value);
            }
            if (operator instanceof LtOperator$) {
                return QueryFactory.lessThan(rawAttribute, value);
            }
            if (operator instanceof GtOperator$) {
                return QueryFactory.greaterThan(rawAttribute, value);
            }
            if (operator instanceof EqOperator$) {
                return QueryFactory.equal(rawAttribute, value);
            }
            if (operator instanceof NeOperator$) {
                return QueryFactory.not(QueryFactory.equal(rawAttribute, value));
            }
            return null;
        }
        return null;
    }

    private Object handleCriteriaValue(final CriteriaValue criteriaValue) {
        if (criteriaValue instanceof ArithmeticCriteriaValue) {
            final ArithmeticOperator arithmeticOperator = ((ArithmeticCriteriaValue) criteriaValue).getOperator();

            final CriteriaValue left = ((ArithmeticCriteriaValue) criteriaValue).getLeft();
            final CriteriaValue right = ((ArithmeticCriteriaValue) criteriaValue).getRight();

            final Object rawLeft = handleCriteriaValue(left);
            final Object rawRight = handleCriteriaValue(right);

            if ((rawLeft instanceof SimpleAttribute && rawRight instanceof BigDecimal)
                    || (rawLeft instanceof SimpleAttribute && rawRight instanceof SimpleAttribute)) {

                final SimpleAttribute<T, Comparable> rawAttribute = (SimpleAttribute<T, Comparable>) rawLeft;

                if (arithmeticOperator instanceof AddOperator$) {
                    return QueryFactory.attribute(object -> {
                        final Double number1 = (Double) rawAttribute.getValue((T) object, null);
                        Double number2 = null;
                        if (rawRight instanceof BigDecimal) {
                            number2 = ((BigDecimal) rawRight).doubleValue();
                        } else {
                            number2 = (Double) ((SimpleAttribute<T, Comparable>) rawRight)
                                    .getValue((T) object, null);
                        }
                        return number1 + number2;
                    });
                }
                if (arithmeticOperator instanceof DivOperator$) {
                    return QueryFactory.attribute(object -> {
                        final Double number1 = (Double) rawAttribute.getValue((T) object, null);
                        Double number2 = null;
                        if (rawRight instanceof BigDecimal) {
                            number2 = ((BigDecimal) rawRight).doubleValue();
                        } else {
                            number2 = (Double) ((SimpleAttribute<T, Comparable>) rawRight)
                                    .getValue((T) object, null);
                        }
                        return number1 / number2;
                    });
                }
                if (arithmeticOperator instanceof ModOperator$) {
                    return QueryFactory.attribute(object -> {
                        final Double number1 = (Double) rawAttribute.getValue((T) object, null);
                        Double number2 = null;
                        if (rawRight instanceof BigDecimal) {
                            number2 = ((BigDecimal) rawRight).doubleValue();
                        } else {
                            number2 = (Double) ((SimpleAttribute<T, Comparable>) rawRight)
                                    .getValue((T) object, null);
                        }
                        return number1 % number2;
                    });
                }
                if (arithmeticOperator instanceof MulOperator$) {
                    return QueryFactory.attribute(object -> {
                        final Double number1 = (Double) rawAttribute.getValue((T) object, null);
                        Double number2 = null;
                        if (rawRight instanceof BigDecimal) {
                            number2 = ((BigDecimal) rawRight).doubleValue();
                        } else {
                            number2 = (Double) ((SimpleAttribute<T, Comparable>) rawRight)
                                    .getValue((T) object, null);
                        }
                        return number1 * number2;
                    });
                }
                if (arithmeticOperator instanceof SubOperator$) {
                    return QueryFactory.attribute(object -> {
                        final Double number1 = (Double) rawAttribute.getValue((T) object, null);
                        Double number2 = null;
                        if (rawRight instanceof BigDecimal) {
                            number2 = ((BigDecimal) rawRight).doubleValue();
                        } else {
                            number2 = (Double) ((SimpleAttribute<T, Comparable>) rawRight)
                                    .getValue((T) object, null);
                        }
                        return number1 - number2;
                    });
                }
            }
        }
        if (criteriaValue instanceof PropertyCriteriaValue) {
            final String field = ((PropertyCriteriaValue) criteriaValue).getPropertyName();
            return getAttributeForProperty(field);
        }
        if (criteriaValue instanceof LiteralCriteriaValue) {
            return ((LiteralCriteriaValue) criteriaValue).getValue();
        }
        return null;
    }

    private SimpleAttribute getAttributeForProperty(final String field) {
        if (field.contains(".")) {
            final String[] fields = field.split("\\.");
            SimpleAttribute baseAttribute = (SimpleAttribute) attributes.get(fields[0]);
            for (int i = 1; i < fields.length; i++) {
                final Map<String, SimpleAttribute<?, Comparable>> attributes = DynamicIndexer
                        .generateAttributesForPojo(baseAttribute.getAttributeType(), entityDataModel);
                baseAttribute = createNestedAttribute(baseAttribute, attributes.get(fields[i]));
            }
            return baseAttribute;
        } else {
            return attributes.get(field);
        }
    }

    private SimpleAttribute createNestedAttribute(final SimpleAttribute baseAttribute, final SimpleAttribute innerAttribute) {
        return QueryFactory.attribute(
                baseAttribute.getObjectType(),
                innerAttribute.getAttributeType(),
                "dynamic",
                (SimpleFunction<? extends Object, ? extends Object>) object -> {
                    if (object == null) {
                        return null;
                    }
                    final Object temp = baseAttribute.getValue(object, null);
                    if (temp == null) {
                        return null;
                    }
                    return innerAttribute.getValue(temp, null);
                });
    }


}
