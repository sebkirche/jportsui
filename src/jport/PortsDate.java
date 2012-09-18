package jport;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import jport.common.StringsUtil_;
import jport.common.Util;
import jport.type.Portable;


/**
 * Tracks mutable rsync directory dates of individual Ports.
 *
 * @author sbaber
 */
class PortsDate
{
    /** Applies mutable information to an immutable object. */
    final private Map<Portable,Long> fPort_to_EpochMap = new HashMap<Portable,Long>();

    /** Bi-directional mapping. */
    private Map<Long,Set<Portable>> mEpoch_to_PortsMap = null;

    static
    {}

    /**
     * Generally, a slow operation.
     *
     * @param portCatalog 
     */
    PortsDate( final PortsCatalog portCatalog )
    {
        if( PortsConstants.HAS_MAC_PORTS == false ) return; // devel

        final long startMillisec = System.currentTimeMillis();
        final File portsPath = new File( PortsConstants.PORTS_PATH );

// putting ports array in directory order only 20ms (10%) faster but array sorting takes 100ms making it 25% slower overall
//            final Portable[] copy = ports.clone();
//            Arrays.sort( copy, new Comparator<Portable>() // anonymous class
//                    {   @Override public int compare( Portable o1, Portable o2 )
//                        {   return o1.getPortDirectory().compareTo( o2.getPortDirectory() );
//                        }
//                    } );

        //... ends up double checking some of the inodes
        final Portable[] allPorts = portCatalog.getPortsInventory().getAllPorts();
        for( final Portable port : allPorts )
        {   // none of the mod dates are = 0L
            final File filePath = new File( portsPath, port.getPortDirectory() +"/Portfile" );
            fPort_to_EpochMap.put( port, filePath.lastModified() ); // auto-box

// different date with the "files" dir modification, not sure what it implies
//            File filePath = new File( portsPath, port.getPortDirectory() +"/files" );
//            filePath = ( filePath.exists() == true )
//                    ? filePath
//                    : new File( portsPath, port.getPortDirectory() +"/Portfile" );
//            if( filePath.exists() == false ) System.err.println( "FNF="+ filePath );
        }

//        System.out.println( PortsDate.class.getSimpleName() +".init ms="+ ( System.currentTimeMillis() - startMillisec ) );
    }

    long getModificationEpoch( final Portable port )
    {
        final Long epochLong = fPort_to_EpochMap.get( port );
        return ( epochLong != null ) ? epochLong.longValue() : -1L;
    }

    /**
     *
     * @param port
     * @return
     */
    String getModificationDate( final Portable port )
    {
        return StringsUtil_.getDateString( getModificationEpoch( port) );
    }

    /**
     *
     * @return sequential epochs to Port Sets where keys are a NavigableSet
     */
    private Map<Long,Set<Portable>> getInverseMultiMapping()
    {
        if( mEpoch_to_PortsMap == null )
        {   // lazy instantiate
            mEpoch_to_PortsMap = Util.createInverseMultiMapping( true, false, fPort_to_EpochMap );
        }

        return mEpoch_to_PortsMap; // Collections.unmodifiableMap( ... ) fubars (NavigableSet<Long>) cast
    }

    /**
     *
     * @return epoch of last "PortFile" modification time
     */
    public long getLastSyncEpoch()
    {
        final Map<Long,Set<Portable>> map = getInverseMultiMapping();
        final NavigableSet<Long> navSet = (NavigableSet<Long>)map.keySet();
        return navSet.last().longValue();
    }
}
