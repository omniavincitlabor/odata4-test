package test;

import com.sdl.odata.api.edm.registry.ODataEdmRegistry;

/**
 *
 */
public abstract class EntityServiceRegistar {


    public EntityServiceRegistar(final ODataEdmRegistry oDataEdmRegistry) {
        registerEntities(oDataEdmRegistry);
    }

    public abstract void registerEntities(final ODataEdmRegistry oDataEdmRegistry);
}