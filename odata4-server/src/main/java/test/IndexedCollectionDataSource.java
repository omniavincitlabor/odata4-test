package test;

import com.sdl.odata.api.processor.datasource.DataSource;

import java.util.List;

/**
 *
 */
public interface IndexedCollectionDataSource extends DataSource {

    List<Class<?>> getSuitableClasses();

    TableProvider getTableProvider();
}
