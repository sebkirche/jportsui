package jport;

import java.util.Arrays;
import java.util.Set;
import jport.type.Portable;


/**
 * Alphabetized array of all installed with multiply versioned variants
 * and all not installed ports described in the "PortIndex" file.
 *
 * @author sbaber
 */
public class PortsInventory
{
    static
    {}

    /** All values in alphabetical order. */
    final private Portable[] fAllPorts;

    PortsInventory()
    {
        fAllPorts = PortsConstants.NO_PORTS;
    }

    /**
     *
     * @param allPortSet values to array and sort by name and version
     */
    PortsInventory( final Set<Portable> allPortSet  )
    {
        fAllPorts = allPortSet.toArray( new Portable[ allPortSet.size() ] );
        Arrays.sort( fAllPorts );
    }

    /**
     *
     * @return in ascending alphabetical order with descending version number tie-breakers
     */
    public Portable[] getAllPorts() { return fAllPorts; }
    
    /**
     * Look up a matching new port by case-insensitive name and version.
     *
     * @param otherPort from a previous catalog
     * @return Portable.NONE if not found
     */
    Portable equate( final Portable otherPort )
    {
        final int index = Arrays.binarySearch( fAllPorts, otherPort );
        return ( index >= 0 )
                ? fAllPorts[ index ]
                : Portable.NONE;
    }
}
