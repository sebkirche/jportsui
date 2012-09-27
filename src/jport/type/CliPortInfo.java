package jport.type;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jport.PortsConstants.EPortStatus;


/**
 * Information returned from any CLI port status command.
 *
 * @author sbaber
 */
public class CliPortInfo
{
    static
    {}

    final private String   fCi_name;
    final private String   fName;
    final private String   fVersionInstalled;
    final private String   fRevisionInstalled;
    final private String[] fVariants;

    /**
     *
     * @param name
     * @param versionInstalled
     * @param revisionInstalled
     * @param variants
     */
    public CliPortInfo
        ( final String   name
        , final String   versionInstalled
        , final String   revisionInstalled
        , final String[] variants
        )
    {
        fCi_name           = name.toLowerCase().intern();
        fName              = name;
        fVersionInstalled  = versionInstalled;
        fRevisionInstalled = revisionInstalled;
        fVariants          = variants;
    }

    public String   getCaseInsensitiveName() { return fCi_name; }
    public String   getName()                { return fName; }
    public String   getVersionInstalled()    { return fVersionInstalled; }
    public String   getRevisionInstalled()   { return fRevisionInstalled; }
    public String[] getVariants()            { return fVariants; }

    @Override final public boolean equals( final Object obj )
    {
        if( obj == this ) return true;

        if( obj instanceof CliPortInfo )
        {
            final CliPortInfo other = (CliPortInfo)obj;
            return this.fName.equals( other.fName )
                && this.fVersionInstalled.equals( other.fVersionInstalled )
                && this.fRevisionInstalled.equals( other.fRevisionInstalled );
        }

        return false;
    }

    @Override final public int hashCode()
    {
        int hash = 7;
        hash = 11 * hash + fRevisionInstalled.hashCode();
        hash = 11 * hash + fVersionInstalled.hashCode();
        hash = 11 * hash + fName.hashCode();
        return hash;
    }

    @Override public String toString()
    {
        return fName +'@'+ fVersionInstalled +'_'+ fRevisionInstalled;
    }
}
