package org.omnaest.data.migration.domain;

import java.util.Set;

public class Version
{
    private String  version;
    private Version parentVersion;

    private Version(String version, Version parentVersion)
    {
        super();
        this.version = version;
        this.parentVersion = parentVersion;
    }

    public String getVersion()
    {
        return this.version;
    }

    public Version getParentVersion()
    {
        return this.parentVersion;
    }

    public boolean hasParent(Version parent)
    {
        return this.parentVersion != null && (this.parentVersion.equals(parent) || this.parentVersion.hasParent(parent));
    }

    public boolean isSameVersionAs(Version version)
    {
        return this.equals(version);
    }

    public static Version of(String version)
    {
        Version parentVersion = null;
        return of(version, parentVersion);
    }

    public static Version of(String version, Version parentVersion)
    {
        return new Version(version, parentVersion);
    }

    @Override
    public String toString()
    {
        return "Version [version=" + this.version + ", parentVersion=" + this.parentVersion + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
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
        Version other = (Version) obj;
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

    public boolean isSameVersionAsAnyOf(Set<Version> versions)
    {
        return versions.contains(this);
    }

    public boolean isSameVersionAsOrAfter(Version version)
    {
        return this.isSameVersionAs(version) || this.hasParent(version);
    }

    public boolean isSameVersionAsOrBefore(Version version)
    {
        return this.isSameVersionAs(version) || version.hasParent(this);
    }

}
