package test;

import com.googlecode.cqengine.ConcurrentIndexedCollection;

/**
 *
 */
public interface TableProvider {


    <X> ConcurrentIndexedCollection<X> getIndexedCollectionForType(final Class<X> type);
}
