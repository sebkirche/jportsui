package oz.zomg.jport.ports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import oz.zomg.jport.PortConstants;
import oz.zomg.jport.common.Util;
import oz.zomg.jport.type.EPortStatus;
import oz.zomg.jport.type.Portable;
import oz.zomg.jport.type.Portable.Predicatable;


/**
 * Ordered array of all installed with multiply versioned variants
 * and all not installed ports described in the "PortIndex" file.
 *
 * @author sbaber
 */
public class PortsInventory
{
    static
    {}

    /** All values in case-insensitive alphabetical order with descending version number tie-breakers. */
    final private Portable[] fAllPorts;

    PortsInventory()
    {
        fAllPorts = PortConstants.NO_PORTS;
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
     *
     * @param byStatus
     * @return in ascending alphabetical order with descending version number tie-breakers
     */
    public Portable[] filter( final EPortStatus byStatus )
    {
        return filter( new Predicatable() // anonymous class
                {   @Override public boolean evaluate( final Portable port )
                    {   return port.hasStatus( byStatus );
                    }
                } );
    }

    /**
     *
     * @param byPredicate
     * @return is reduced and in ascending alphabetical order with descending version number tie-breakers
     */
    public Portable[] filter( final Predicatable byPredicate )
    {
        final List<Portable> list = new ArrayList<Portable>( 32 );
        for( final Portable port : fAllPorts )
        {
            if( byPredicate.evaluate( port ) == true )
            {
                list.add( port );
            }
        }

        return Util.createArray( Portable.class, list );
    }

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
