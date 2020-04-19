package org.omnaest.data.migration.domain;

public class MigrationCommand
{
    private Target target;

    private String from;
    private String to;

    private Version version;

    public static enum Target
    {
        ENTITY, FIELD
    }

    public MigrationCommand(Target target, String from, String to, Version version)
    {
        super();
        this.target = target;
        this.from = from;
        this.to = to;
        this.version = version;
    }

    public Version getVersion()
    {
        return this.version;
    }

    public Target getTarget()
    {
        return this.target;
    }

    public String getFrom()
    {
        return this.from;
    }

    public String getTo()
    {
        return this.to;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.from == null) ? 0 : this.from.hashCode());
        result = prime * result + ((this.target == null) ? 0 : this.target.hashCode());
        result = prime * result + ((this.to == null) ? 0 : this.to.hashCode());
        result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        MigrationCommand other = (MigrationCommand) obj;
        if (this.from == null)
        {
            if (other.from != null)
            {
                return false;
            }
        }
        else if (!this.from.equals(other.from))
        {
            return false;
        }
        if (this.target != other.target)
        {
            return false;
        }
        if (this.to == null)
        {
            if (other.to != null)
            {
                return false;
            }
        }
        else if (!this.to.equals(other.to))
        {
            return false;
        }
        if (this.version == null)
        {
            if (other.version != null)
            {
                return false;
            }
        }
        else if (!this.version.equals(other.version))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "MigrationCommand [target=" + this.target + ", from=" + this.from + ", to=" + this.to + ", version=" + this.version + "]";
    }

}
