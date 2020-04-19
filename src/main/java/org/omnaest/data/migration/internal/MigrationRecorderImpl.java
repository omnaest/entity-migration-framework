package org.omnaest.data.migration.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.data.migration.MigrationRecorder;
import org.omnaest.data.migration.domain.EntityModel;
import org.omnaest.data.migration.domain.MigrationCommand;
import org.omnaest.data.migration.domain.MigrationCommand.Target;
import org.omnaest.data.migration.domain.MigrationCommands;
import org.omnaest.data.migration.domain.Version;
import org.omnaest.data.migration.domain.annotation.Column;
import org.omnaest.data.migration.domain.annotation.Entity;
import org.omnaest.data.migration.domain.annotation.MigrateFromColumn;
import org.omnaest.data.migration.domain.annotation.MigrateFromEntity;
import org.omnaest.data.migration.domain.annotation.MigrateFromEntityType;
import org.omnaest.data.migration.domain.annotation.MigrateFromField;
import org.omnaest.utils.BeanUtils;
import org.omnaest.utils.BeanUtils.BeanAnalyzer;
import org.omnaest.utils.BeanUtils.Property;
import org.omnaest.utils.ListUtils;
import org.omnaest.utils.MapUtils;
import org.omnaest.utils.SetUtils;

public class MigrationRecorderImpl implements MigrationRecorder
{
    @Override
    public <T> MigrationCommands generateMigrationChain(Class<T> domainType)
    {
        List<MigrationCommand> result = new ArrayList<>();

        BeanAnalyzer<T> analyzer = BeanUtils.analyze(domainType);

        Version version = this.determineVersion(analyzer);

        this.addPreviousMigrationCommands(result, analyzer);

        this.addEntityMigrationCommand(analyzer, version)
            .ifPresent(command -> result.add(command));

        Map<String, String> oldFieldNameToColumnName = analyzer.resolveTypeAnnotation(MigrateFromEntityType.class)
                                                               .findFirst()
                                                               .map(fromEntityType -> BeanUtils.analyze(fromEntityType.value())
                                                                                               .getProperties()
                                                                                               .collect(Collectors.toMap(property -> property.getName(),
                                                                                                                         property -> property.getAnnotations(Column.class)
                                                                                                                                             .findFirst()
                                                                                                                                             .map(column -> column.value())
                                                                                                                                             .orElse(property.getName()))))
                                                               .orElse(Collections.emptyMap());
        Map<String, String> oldColumnNameToFieldName = MapUtils.invertUnique(oldFieldNameToColumnName);

        List<Field> mappedFields = analyzer.getProperties()
                                           .map(property ->
                                           {
                                               Field field = new Field();

                                               // set new field name
                                               field.getTo()
                                                    .setName(property.getName());

                                               // set old column name
                                               Optional.ofNullable(oldFieldNameToColumnName.get(property.getName()))
                                                       .ifPresent(oldColumn ->
                                                       {
                                                           field.getFrom()
                                                                .setColumnName(oldColumn);
                                                       });

                                               // set new column name
                                               property.getAnnotations(Column.class)
                                                       .findFirst()
                                                       .map(column -> column.value())
                                                       .ifPresent(toColumn ->
                                                       {
                                                           field.getTo()
                                                                .setColumnName(toColumn);
                                                       });

                                               // get old column name and old field name 
                                               property.getAnnotations(MigrateFromColumn.class)
                                                       .findFirst()
                                                       .map(column -> column.value())
                                                       .ifPresent(fromColumn ->
                                                       {
                                                           //
                                                           field.getFrom()
                                                                .setColumnName(fromColumn);

                                                           //
                                                           Optional.ofNullable(oldColumnNameToFieldName.get(fromColumn))
                                                                   .ifPresent(oldFieldName ->
                                                                   {
                                                                       field.getFrom()
                                                                            .setName(oldFieldName);
                                                                   });
                                                       });

                                               // get old field name and old column name
                                               property.getAnnotations(MigrateFromField.class)
                                                       .findFirst()
                                                       .map(fromField -> fromField.value())
                                                       .ifPresent(fromField ->
                                                       {
                                                           field.getFrom()
                                                                .setName(fromField);

                                                           Optional.ofNullable(oldFieldNameToColumnName.get(fromField))
                                                                   .ifPresent(oldColumnName ->
                                                                   {
                                                                       field.getFrom()
                                                                            .setColumnName(oldColumnName);
                                                                   });
                                                       });

                                               // as fallback take current field name as new column name
                                               if (field.getTo()
                                                        .getColumnName() == null)
                                               {
                                                   field.getTo()
                                                        .setColumnName(field.getTo()
                                                                            .getName());
                                               }

                                               return field;
                                           })
                                           .collect(Collectors.toList());
        result.addAll(mappedFields.stream()
                                  .map(field ->
                                  {
                                      String from = field.getFrom()
                                                         .getColumnName();
                                      String to = field.getTo()
                                                       .getColumnName();
                                      return new MigrationCommand(Target.FIELD, from, to, version);
                                  })
                                  .collect(Collectors.toList()));

        Map<String, String> fromColumnNameToColumnName = mappedFields.stream()
                                                                     .filter(field -> field.getFrom()
                                                                                           .getColumnName() != null)
                                                                     .collect(Collectors.toMap(field -> field.getFrom()
                                                                                                             .getColumnName(),
                                                                                               field -> field.getTo()
                                                                                                             .getColumnName()));

        this.determineFromColumnNames(analyzer)
            .ifPresent(fromColumnNames ->
            {
                Set<String> toColumnNames = this.determineColumnNames(analyzer)
                                                .stream()
                                                .collect(Collectors.toSet());

                SetUtils.delta(fromColumnNames, toColumnNames)
                        .getRemoved()
                        .stream()
                        .filter(fromColumn -> !fromColumnNameToColumnName.containsKey(fromColumn))
                        .forEach(removedColumn ->
                        {
                            String to = null;
                            result.add(new MigrationCommand(Target.FIELD, removedColumn, to, version));
                        });
            });
        ;

        return MigrationCommands.of(result);
    }

    @Override
    public <T> Version determineVersion(Class<T> domainType)
    {
        BeanAnalyzer<T> analyzer = BeanUtils.analyze(domainType);
        return this.determineVersion(analyzer);
    }

    @Override
    public <T> Version determineVersion(EntityModel entityModel)
    {
        return this.determineVersion(entityModel.getEntityName(), entityModel.getColumnNames(), null);
    }

    private <T> Optional<List<String>> determineFromColumnNames(BeanAnalyzer<T> analyzer)
    {
        return analyzer.resolveTypeAnnotation(MigrateFromEntityType.class)
                       .findFirst()
                       .map(annoation -> annoation.value())
                       .map(type -> this.determineColumnNames(BeanUtils.analyze(type)));
    }

    private <T> Optional<MigrationCommand> addEntityMigrationCommand(BeanAnalyzer<T> analyzer, Version version)
    {
        Function<? super String, ? extends MigrationCommand> fromEntityNameToMigrationCommandMapper = fromEntityName ->
        {
            String from = fromEntityName;
            String to = this.determineEntityName(analyzer);
            return new MigrationCommand(Target.ENTITY, from, to, version);
        };

        Optional<MigrationCommand> result1 = analyzer.resolveTypeAnnotation(MigrateFromEntity.class)
                                                     .findFirst()
                                                     .map(annotation -> annotation.value())
                                                     .map(fromEntityNameToMigrationCommandMapper);
        Optional<MigrationCommand> result2 = analyzer.resolveTypeAnnotation(MigrateFromEntityType.class)
                                                     .findFirst()
                                                     .map(annotation -> annotation.value())
                                                     .map(fromType -> this.determineEntityName(BeanUtils.analyze(fromType)))
                                                     .map(fromEntityNameToMigrationCommandMapper);

        Optional<MigrationCommand> result3 = Optional.of(fromEntityNameToMigrationCommandMapper.apply(null));

        return Stream.of(result1, result2, result3)
                     .filter(Optional<MigrationCommand>::isPresent)
                     .map(Optional<MigrationCommand>::get)
                     .findFirst();
    }

    private <T> String determineEntityName(BeanAnalyzer<T> analyzer)
    {
        return analyzer.resolveTypeAnnotation(Entity.class)
                       .findFirst()
                       .map(annotation -> annotation.value())
                       .orElse(analyzer.getType()
                                       .getSimpleName());
    }

    private <T> Version determineVersion(BeanAnalyzer<T> analyzer)
    {
        List<String> columnNames = this.determineColumnNames(analyzer);
        return this.determineVersion(this.determineEntityName(analyzer), columnNames, this.determineParentVersion(analyzer));
    }

    private <T> List<String> determineColumnNames(BeanAnalyzer<T> analyzer)
    {
        return analyzer.getProperties()
                       .map(this::determineColumnName)
                       .collect(Collectors.toList());
    }

    private <T> String determineColumnName(Property<T> property)
    {
        return property.getAnnotations(Column.class)
                       .findFirst()
                       .map(Column::value)
                       .orElse(property.getName());
    }

    private <T> Version determineParentVersion(BeanAnalyzer<T> analyzer)
    {
        return analyzer.resolveTypeAnnotation(MigrateFromEntityType.class)
                       .findFirst()
                       .map(fromEntityType -> fromEntityType.value())
                       .map(fromType ->
                       {
                           BeanAnalyzer<?> analyze = BeanUtils.analyze(fromType);
                           return this.determineVersion(analyze);
                       })
                       .orElse(null);
    }

    private Version determineVersion(String entityName, List<String> columnNames, Version parentVersion)
    {
        return Version.of("" + ListUtils.addToNew(columnNames, entityName)
                                        .hashCode(),
                          parentVersion);
    }

    private <T> void addPreviousMigrationCommands(List<MigrationCommand> result, BeanAnalyzer<T> analyzer)
    {
        result.addAll(analyzer.resolveTypeAnnotation(MigrateFromEntityType.class)
                              .findAny()
                              .map(migrateFromEntityType -> this.generateMigrationChain(migrateFromEntityType.value())
                                                                .getCommands())
                              .orElse(Collections.emptyList()));
    }

    private static class Field
    {
        private FieldMeta from = new FieldMeta();
        private FieldMeta to   = new FieldMeta();

        public FieldMeta getFrom()
        {
            return this.from;
        }

        public FieldMeta getTo()
        {
            return this.to;
        }

        @Override
        public String toString()
        {
            return "Field [from=" + this.from + ", to=" + this.to + "]";
        }

    }

    private static class FieldMeta
    {
        private String name;
        private String columnName;

        public String getName()
        {
            return this.name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getColumnName()
        {
            return this.columnName;
        }

        public void setColumnName(String columnName)
        {
            this.columnName = columnName;
        }

        @Override
        public String toString()
        {
            return "FieldMeta [name=" + this.name + ", columnName=" + this.columnName + "]";
        }

    }

}
