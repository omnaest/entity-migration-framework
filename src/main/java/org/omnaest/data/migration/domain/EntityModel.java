package org.omnaest.data.migration.domain;

import java.util.List;

/**
 * Meta description of an entity like its name and its column names
 * 
 * @see #of(String, List)
 * @author omnaest
 */
public class EntityModel
{
    private String       entityName;
    private List<String> columnNames;

    private EntityModel(String entityName, List<String> columnNames)
    {
        super();
        this.entityName = entityName;
        this.columnNames = columnNames;
    }

    public static EntityModel of(String entityName, List<String> columnNames)
    {
        return new EntityModel(entityName, columnNames);
    }

    public String getEntityName()
    {
        return this.entityName;
    }

    public List<String> getColumnNames()
    {
        return this.columnNames;
    }

    @Override
    public String toString()
    {
        return "EntityModel [entityName=" + this.entityName + ", columnNames=" + this.columnNames + "]";
    }

}
