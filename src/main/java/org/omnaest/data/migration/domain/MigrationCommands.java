package org.omnaest.data.migration.domain;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.data.migration.domain.MigrationCommand.Target;

public class MigrationCommands implements Iterable<MigrationCommand>
{
    private List<MigrationCommand> commands;

    private MigrationCommands(List<MigrationCommand> commands)
    {
        super();
        this.commands = commands;
    }

    public List<MigrationCommand> getCommands()
    {
        return this.commands;
    }

    public int size()
    {
        return this.commands.size();
    }

    public Stream<MigrationCommand> stream()
    {
        return this.commands.stream();
    }

    @Override
    public Iterator<MigrationCommand> iterator()
    {
        return this.commands.iterator();
    }

    public MigrationCommands forVersionRange(Version from, Version to)
    {
        return new MigrationCommands(this.forVersionAndAfter(from)
                                         .stream()
                                         .filter(command -> !command.getVersion()
                                                                    .hasParent(to))
                                         .collect(Collectors.toList()));
    }

    public MigrationCommands forVersionAndAfter(Version version)
    {
        return new MigrationCommands(this.commands.stream()
                                                  .filter(command -> command.getVersion()
                                                                            .isSameVersionAsOrAfter(version))
                                                  .collect(Collectors.toList()));
    }

    public MigrationCommands forVersionAndBefore(Version version)
    {
        return new MigrationCommands(this.commands.stream()
                                                  .filter(command -> command.getVersion()
                                                                            .isSameVersionAsOrBefore(version))
                                                  .collect(Collectors.toList()));
    }

    public MigrationCommands forVersion(Version version)
    {
        return this.forVersions(version);
    }

    public MigrationCommands forVersions(Version... versions)
    {
        return new MigrationCommands(this.commands.stream()
                                                  .filter(command -> command.getVersion()
                                                                            .isSameVersionAsAnyOf(Arrays.asList(versions)
                                                                                                        .stream()
                                                                                                        .collect(Collectors.toSet())))
                                                  .collect(Collectors.toList()));
    }

    public MigrationCommands forTarget(Target target)
    {
        return new MigrationCommands(this.commands.stream()
                                                  .filter(command -> command.getTarget()
                                                                            .equals(target))
                                                  .collect(Collectors.toList()));
    }

    public static MigrationCommands of(List<MigrationCommand> commands)
    {
        return new MigrationCommands(commands);
    }

    @Override
    public String toString()
    {
        return "MigrationCommands [commands=" + this.commands + "]";
    }

}
