package org.omnaest.data.migration;

import org.omnaest.data.migration.domain.EntityModel;
import org.omnaest.data.migration.domain.MigrationCommands;
import org.omnaest.data.migration.domain.Version;
import org.omnaest.data.migration.internal.MigrationRecorderImpl;

/**
 * The {@link MigrationRecorder} generates a chain consisting of {@link MigrationCommands} which allow to replay all changes between two domain {@link Class}
 * types regarding column name and entity name changes.
 * 
 * @see #newInstance()
 * @author omnaest
 */
public interface MigrationRecorder
{
    public <T> MigrationCommands generateMigrationChain(Class<T> domainType);

    public <T> Version determineVersion(Class<T> domainType);

    public <T> Version determineVersion(EntityModel entityModel);

    public static MigrationRecorder newInstance()
    {
        return new MigrationRecorderImpl();
    }
}
