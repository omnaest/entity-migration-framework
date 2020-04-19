package org.omnaest.data.migration.internal;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;
import org.omnaest.data.migration.MigrationRecorder;
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

public class MigrationRecorderImplTest
{
    private MigrationRecorder migrationRecorder = MigrationRecorder.newInstance();

    @Test
    public void testGenerateMigrationChain() throws Exception
    {
        MigrationCommands commands = this.migrationRecorder.generateMigrationChain(TestDomainV4.class);

        commands.forEach(command ->
        {
            System.out.println(command);
        });

        assertEquals(13, commands.size());

        Version versionV1 = this.migrationRecorder.determineVersion(TestDomainV1.class);
        Version versionV2 = this.migrationRecorder.determineVersion(TestDomainV2.class);
        Version versionV3 = this.migrationRecorder.determineVersion(TestDomainV3.class);
        Version versionV4 = this.migrationRecorder.determineVersion(TestDomainV4.class);

        assertEquals(versionV4, this.migrationRecorder.determineVersion(TestDomainV4.class));

        assertEquals(new MigrationCommand(Target.ENTITY, null, "TestDomainV1", versionV1), commands.forVersion(versionV1)
                                                                                                   .forTarget(Target.ENTITY)
                                                                                                   .stream()
                                                                                                   .findFirst()
                                                                                                   .get());
        assertEquals(new MigrationCommand(Target.ENTITY, "TestDomainV1", "TestDomainV2", versionV2), commands.forVersion(versionV2)
                                                                                                             .forTarget(Target.ENTITY)
                                                                                                             .stream()
                                                                                                             .findFirst()
                                                                                                             .get());
        assertEquals(new MigrationCommand(Target.ENTITY, "TestDomainV2", "TestDomainV3Custom", versionV3), commands.forVersion(versionV3)
                                                                                                                   .forTarget(Target.ENTITY)
                                                                                                                   .stream()
                                                                                                                   .findFirst()
                                                                                                                   .get());
        assertEquals(new MigrationCommand(Target.ENTITY, "TestDomainV3Custom", "TestDomainV4", versionV4), commands.forVersion(versionV4)
                                                                                                                   .forTarget(Target.ENTITY)
                                                                                                                   .stream()
                                                                                                                   .findFirst()
                                                                                                                   .get());

        assertEquals(Arrays.asList(new MigrationCommand(Target.FIELD, null, "nameV1", versionV1),
                                   new MigrationCommand(Target.FIELD, null, "versionV1", versionV1))
                           .stream()
                           .collect(Collectors.toSet()),
                     commands.forVersion(versionV1)
                             .forTarget(Target.FIELD)
                             .stream()
                             .collect(Collectors.toSet()));
        assertEquals(Arrays.asList(new MigrationCommand(Target.FIELD, "nameV1", "nameV2", versionV2),
                                   new MigrationCommand(Target.FIELD, "versionV1", "versionV2", versionV2))
                           .stream()
                           .collect(Collectors.toSet()),
                     commands.forVersion(versionV2)
                             .forTarget(Target.FIELD)
                             .stream()
                             .collect(Collectors.toSet()));
        assertEquals(Arrays.asList(new MigrationCommand(Target.FIELD, "nameV2", "nameV3Custom", versionV3),
                                   new MigrationCommand(Target.FIELD, "versionV2", "versionV3", versionV3))
                           .stream()
                           .collect(Collectors.toSet()),
                     commands.forVersion(versionV3)
                             .forTarget(Target.FIELD)
                             .stream()
                             .collect(Collectors.toSet()));
        assertEquals(Arrays.asList(new MigrationCommand(Target.FIELD, "nameV3Custom", "nameV4", versionV4),
                                   new MigrationCommand(Target.FIELD, "versionV3", null, versionV4),
                                   new MigrationCommand(Target.FIELD, null, "descriptionV4", versionV4))
                           .stream()
                           .collect(Collectors.toSet()),
                     commands.forVersion(versionV4)
                             .forTarget(Target.FIELD)
                             .stream()
                             .collect(Collectors.toSet()));

        assertEquals(3, commands.forVersionAndBefore(versionV1)
                                .size());
        assertEquals(6, commands.forVersionAndBefore(versionV2)
                                .size());
        assertEquals(9, commands.forVersionAndBefore(versionV3)
                                .size());

        assertEquals(13, commands.forVersionAndAfter(versionV1)
                                 .size());
        assertEquals(10, commands.forVersionAndAfter(versionV2)
                                 .size());
        assertEquals(7, commands.forVersionAndAfter(versionV3)
                                .size());

        assertEquals(Arrays.asList(versionV1, versionV2)
                           .stream()
                           .collect(Collectors.toSet()),
                     commands.forVersionRange(versionV1, versionV2)
                             .stream()
                             .map(MigrationCommand::getVersion)
                             .collect(Collectors.toSet()));
        assertEquals(Arrays.asList(versionV2, versionV3, versionV4)
                           .stream()
                           .collect(Collectors.toSet()),
                     commands.forVersionRange(versionV2, versionV4)
                             .stream()
                             .map(MigrationCommand::getVersion)
                             .collect(Collectors.toSet()));

    }

    public static class TestDomainV1
    {
        private String nameV1;
        private int    versionV1;

        public String getNameV1()
        {
            return this.nameV1;
        }

        public int getVersionV1()
        {
            return this.versionV1;
        }

    }

    @MigrateFromEntity("TestDomainV1")
    @MigrateFromEntityType(TestDomainV1.class)
    protected static class TestDomainV2
    {
        @MigrateFromColumn("nameV1")
        private String nameV2;

        @MigrateFromField("versionV1")
        private int versionV2;

        public String getNameV2()
        {
            return this.nameV2;
        }

        public int getVersionV2()
        {
            return this.versionV2;
        }

    }

    @Entity("TestDomainV3Custom")
    @MigrateFromEntityType(TestDomainV2.class)
    protected static class TestDomainV3
    {
        @Column("nameV3Custom")
        @MigrateFromField("nameV2")
        private String nameV3;

        @Column("versionV3")
        private int versionV2;

        public String getNameV3()
        {
            return this.nameV3;
        }

        public int getVersionV2()
        {
            return this.versionV2;
        }

    }

    @MigrateFromEntityType(TestDomainV3.class)
    protected static class TestDomainV4
    {
        @MigrateFromField("nameV3")
        private String nameV4;

        private String descriptionV4;

        public String getNameV4()
        {
            return this.nameV4;
        }

        public String getDescriptionV4()
        {
            return this.descriptionV4;
        }
    }
}
