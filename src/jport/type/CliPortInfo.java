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
    final private String[] fVariants;

    /**
     *
     * @param variants
     * @param name
     * @param versionInstalled
     */
    public CliPortInfo
        ( final String   name
        , final String   versionInstalled
        , final String[] variants
        )
    {
        fCi_name          = name.toLowerCase().intern();
        fName             = name;
        fVersionInstalled = versionInstalled;
        fVariants         = variants;
    }

    public String   getCaseInsensitiveName() { return fCi_name; }
    public String   getName()                { return fName; }
    public String   getVersionInstalled()    { return fVersionInstalled; }
    public String[] getVariants()            { return fVariants; }

    @Override final public boolean equals( final Object obj )
    {
        if( obj == this ) return true;

        if( obj instanceof CliPortInfo )
        {
            final CliPortInfo other = (CliPortInfo)obj;
            return this.fName.equals( other.fName ) && this.fVersionInstalled.equals( other.fVersionInstalled );
        }

        return false;
    }

    @Override final public int hashCode()
    {
        return this.fName.hashCode() + 17 * this.fVersionInstalled.hashCode();
    }

    @Override public String toString()
    {
        return fName +'@'+ fVersionInstalled;
    }

    /**
     * Reverse the CLI Port status utility mapping to a more usable form.
     *
     * @param kvMap
     * @return
     */
    static public Map<CliPortInfo,Set<EPortStatus>> reverseMultiMapping( final Map<EPortStatus,Set<CliPortInfo>> kvMap )
    {
        final Map<CliPortInfo,Set<EPortStatus>> invMap = new HashMap<CliPortInfo, Set<EPortStatus>>();

        for( final Map.Entry<EPortStatus,Set<CliPortInfo>> entry : kvMap.entrySet() )
        {
            final Set<CliPortInfo> cpiSet = entry.getValue(); // alias
            final EPortStatus statusEnum = entry.getKey(); // alias

            if( cpiSet != null )
            {   // values maybe 'null' but keys can not be
                for( final CliPortInfo cpiKey : cpiSet )
                {
                    if( invMap.containsKey( cpiKey ) == false )
                    {   // new key
                        final Set<EPortStatus> valueSet = EnumSet.of( statusEnum ); // mutable with single element
                        invMap.put( cpiKey, valueSet );
                    }
                    else
                    {   // seen the inverse key before
                        final Set<EPortStatus> valueSet = invMap.get( cpiKey );
                        valueSet.add( statusEnum );
                    }
                }
            }
        }

        // replacing 'null' value Sets with Collections.emptySet() does not have to be done because 'null' keys are prohibited
        return invMap;
    }
}
