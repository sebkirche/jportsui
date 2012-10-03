package oz.zomg.jport.ports;

import static oz.zomg.jport.type.EPortMark.*;
//
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import oz.zomg.jport.PortConstants;
import oz.zomg.jport.TheApplication;
import oz.zomg.jport.common.Elemental.EElemental;
import oz.zomg.jport.common.Util;
import oz.zomg.jport.type.EPortMark;
import oz.zomg.jport.type.EPortStatus;
import oz.zomg.jport.type.Portable;
import oz.zomg.jport.type.Portable.Predicatable;


/**
 * Associates the user's action "marks" on a Port for eventually applying in the CLI.
 * Cross-cuts with PortsDeps.
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-nd/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US">
 * Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License</a>.</SMALL>
 */
public class PortsMarker
{
    static final private boolean DEBUG = false;

    static
    {}

    /**
     * Sparsely populated mutable information for the immutable Port object.
     * If keying by Portable ref, map must be re-keyed whenever PortIndex file parse occurs.
     */
    final private Map<Portable,EPortMark> fPort_to_MarkMap = new HashMap<Portable,EPortMark>();

    public PortsMarker() {}

    /**
     * Used after Post status change request command to clear any actually applied Marks.
     * Not associated with PortsCatalog in case an error kept the Apply from working
     * and the user's change requests needed to stick around.
     *
     * @param refreshedPortsCatalog from a newly loaded index which also references non-equitable, status updated ports
     */
    synchronized public void exchangeAudit( final PortsCatalog refreshedPortsCatalog )
    {
        final int size = fPort_to_MarkMap.size();
        if( size == 0 ) return; // nothing to do

        // avoids Iterator concurrent modification exception when .put(k,v)
        for( Map.Entry<Portable,EPortMark> entry : Util.createMapEntryArray( fPort_to_MarkMap ) )
        {
            final Portable prevPort = entry.getKey(); // alias
            final EPortMark mark = entry.getValue(); // alias

            final Portable refreshPort = refreshedPortsCatalog.getPortsInventory().equate( prevPort );
            if( refreshPort != null )
            {   // found
                switch( mark )
                {
                    case Conflicted           : // fall-thru
                    case Dependant_Deactivate : // fall-thru
                    case Dependant_Uninstall  : // fall-thru
                    case Dependency_Activate  : // fall-thru
                    case Dependency_Install   : // fall-thru
                    case Dependency_Upgrade   :
                            // machine marked
                            fPort_to_MarkMap.remove( prevPort );
                            break;

                    case Activate   : // fall-thru
                    case Deactivate : // fall-thru
                    case Install    : // fall-thru
                    case Uninstall  : // fall-thru
                    case Upgrade    :
                            if( mark.isApplicable( prevPort ) == false )
                            {   // the mark no longer applies
                                fPort_to_MarkMap.remove( prevPort );
                            }
                            else
                            {   // replace with the new port instance
                                fPort_to_MarkMap.put( refreshPort, mark );
                            }
                            break;
                }
            }
            else
            {   // may have been removed from PortsIndex when Obsoleted (or Upgraded from a revision?)
                fPort_to_MarkMap.remove( prevPort );
            }
        }
    }

    /**
     * Dependencies and dependants have been already resolved or Conflicted
     * due to the way BsdPort.setMark() works.
     *
     * @return non-sparse, immutable, inverse mapping from the ports mark collection in sorted order
     */
    synchronized public Map<EPortMark,SortedSet<Portable>> createInverseMultiMapping()
    {
        final Map<EPortMark,SortedSet<Portable>> inverseMap = new EnumMap<EPortMark, SortedSet<Portable>>( EPortMark.class );
        for( final EPortMark mark : EPortMark.VALUES )
        {
            inverseMap.put( mark, new TreeSet<Portable>() );
        }

        for( final Map.Entry<Portable,EPortMark> entry : fPort_to_MarkMap.entrySet() )
        {
            final Set<Portable> set = inverseMap.get( entry.getValue() );
            set.add( entry.getKey() );
        }

        return Collections.unmodifiableMap( inverseMap );
    }

    /**
     * Resets all user's pending Ports status change requests.
     */
    synchronized public void clearAll()
    {
        // blows up on .remove() -> for( final BsdPort port : fPort_to_MarkMap.keySet() )

        final Iterator<Portable> iter = fPort_to_MarkMap.keySet().iterator();
        while( iter.hasNext() == true )
        {
            final Portable port = iter.next();
            iter.remove();

            // notification only shows correct state when removed from Map
            TheApplication.INSTANCE.causeCrudNotification( EElemental.UPDATED, port );
        }

        fPort_to_MarkMap.clear(); // not required, but may GC map's internal bucket allocation
    }

    synchronized public int getMarkCount()
    {
        return fPort_to_MarkMap.entrySet().size();
    }

    synchronized public boolean isUnmarked( final Portable port )
    {
        return fPort_to_MarkMap.containsKey( port ) == false;
    }

    /**
     *
     * @param port generally immutable
     * @return 'null' if not marked
     */
    synchronized public EPortMark getMark( final Portable port )
    {
        return fPort_to_MarkMap.get( port );
    }

    /**
     * Overwrites a previous value.
     *
     * @param port
     * @param mark if 'null' then unsets the state by removing from the map
     */
    synchronized public void setMark( final Portable port, final EPortMark mark )
    {
        final boolean isPresent = fPort_to_MarkMap.containsKey( port );

        if( mark == null )
        {   // unmark will delete markings
            if( isPresent == true ) dissolveDeps( port );
            fPort_to_MarkMap.remove( port );
        }
        else if( isPresent == true && fPort_to_MarkMap.get( port ) == mark )
        {   // no change to notify listeners of
            return;
        }
        else
        {   // add or change marking
            fPort_to_MarkMap.put( port, mark );
            resolveDeps( port, mark );
        }

        // mutable information ASSOCIATED with the Port has changed
        TheApplication.INSTANCE.causeCrudNotification( EElemental.UPDATED, port );
    }

    public void unmark( final Portable port )
    {
        setMark( port, null );
    }

    /**
     * Roll-back deps to unmarked.
     * Needs to be smarter so that other marked ports deps are considered. //...
     *
     * @param onPort to be unmarked
     */
    private void dissolveDeps( final Portable onPort )
    {
        final Portable[] depPorts = gatherDeps( onPort, onPort.getMark() );
        for( final Portable depPort : depPorts )
        {
//... reference count check if some other dependant port of the depport is status=installed or marked=install
            final EPortMark depMark = depPort.getMark();
            if( depMark != null )
            {
                switch( depMark )
                {
                    case Dependency_Upgrade   : // fall-thru
                    case Dependency_Install   : // fall-thru
                    case Dependency_Activate  : // fall-thru
                    case Dependant_Uninstall  : // fall-thru
                    case Dependant_Deactivate :
                        {   fPort_to_MarkMap.remove( depPort );
                            TheApplication.INSTANCE.causeCrudNotification( EElemental.UPDATED, depPort );
                        }   break;
                }
            }
        }
    }

    /**
     * Simulate what Ports CLI will do.
     *
     * @param onPort
     * @param parentMark onPort's mark
     */
    private void resolveDeps( final Portable onPort, final EPortMark parentMark )
    {
        final Portable[] depPorts = gatherDeps( onPort, parentMark );
        for( final Portable depPort : depPorts )
        {
            final EPortMark presentDepMark = fPort_to_MarkMap.get( depPort ); // expected to be 'null' as not presently marked
            final EPortMark targetDepMark;
            switch( parentMark )
            {
                case Upgrade  :  // fall-thru
                case Install  :  // fall-thru
                case Activate :
                    {   targetDepMark = // sequential check logic
                                  ( depPort.hasStatus( EPortStatus.OUTDATED ) )    ? EPortMark.Dependency_Upgrade
                                : ( depPort.hasStatus( EPortStatus.INACTIVE ) )    ? EPortMark.Dependency_Activate
                                : ( depPort.hasStatus( EPortStatus.UNINSTALLED ) ) ? EPortMark.Dependency_Install
                                                                                   : null;
                    }   break;

                case Uninstall :
//... reference count check if some other dependant port of the depport is status=installed or marked=install -> PortDeps.hasDependency()...
                    {   targetDepMark = ( depPort.hasStatus( EPortStatus.INACTIVE ) )
                                ? EPortMark.Dependant_Deactivate
                                : EPortMark.Dependant_Uninstall;
                    }   break;

                case Deactivate :
//... reference count check if some other dependant port of the depport is status=activated or marked=activate
                    {   targetDepMark = EPortMark.Dependant_Deactivate;
                    }   break;

                default:
                        throw new IllegalArgumentException(); // not expected
            }

            if( targetDepMark != presentDepMark )
            {
                if( fPort_to_MarkMap.containsKey( depPort ) == false )
                {   // not marked
                    fPort_to_MarkMap.put( depPort, targetDepMark );
                }
                else
                {   // contained so Dep is already marked but different
                    EPortMark remark = Conflicted;
                    switch( presentDepMark )
                    {
                        case Conflicted : break; // ultimately needs to be Unmarked to reset

                        case Upgrade : case Install : case Activate :
                        case Uninstall : case Deactivate :
                            {   final EPortMark otherMark = EPortMark.VALUES[ 1 + presentDepMark.ordinal() ]; // code stench
                                if( targetDepMark == otherMark ) remark = targetDepMark;
                                // else Conflicted
                            }   break;

                        case Dependency_Upgrade : case Dependency_Install : case Dependency_Activate :
                        case Dependant_Uninstall : case Dependant_Deactivate :
                            {   final EPortMark nonDepMark = presentDepMark.getNonDepMark();
                                if( targetDepMark == nonDepMark ) remark = targetDepMark;
                                // else Conflicted
                            }   break;
                    }

                    fPort_to_MarkMap.put( depPort, remark );
                }

                // update UI
                TheApplication.INSTANCE.causeCrudNotification( EElemental.UPDATED, depPort );
            }
            // else same mark
        }
    }

    /**
     *
     * @param onPort
     * @param parentMark of onPort
     * @return
     */
    static public Portable[] gatherDeps( final Portable onPort, final EPortMark parentMark )
    {
        switch( parentMark )
        {
            case Uninstall : // dependants that will be released if Uninstalled
                    return _reducer
                            ( onPort.getDependants()
                            , new Predicatable()
                                    {   @Override public boolean evaluate( Portable port )
                                        {   return port.hasStatus( EPortStatus.INSTALLED );
                                        }
                                    }
                            );

            case Deactivate : // dependants that will be released if Deactivated
                    return _reducer
                            ( onPort.getDependants()
                            , new Predicatable()
                                    {   @Override public boolean evaluate( Portable port )
                                        {   return port.hasStatus( EPortStatus.ACTIVE );
                                        }
                                    }
                            );

            case Install : case Activate : // dependencies that will be activated if Installed or Activated
                    return _reducer
                            ( onPort.getFullDependencies()
                            , new Predicatable()
                                    {   @Override public boolean evaluate( Portable port )
                                        {   return port.hasStatus( EPortStatus.ACTIVE ) == false;
//? shouldn'tthis be INACTIVE or UNINSTALLED ?
                                        }
                                    }
                            );

            case Upgrade : // dependencies that will be Upgraded
                    return _reducer
//? clue user in that these may be rebuilt!                           ( RefsUtil.concatenate( onPort.getFullDependencies(), onPort.getDependants() )
                            ( onPort.getFullDependencies()
                            , new Predicatable()
                                    {   @Override public boolean evaluate( Portable port )
                                        {   return port.hasStatus( EPortStatus.OUTDATED );
                                        }
                                    }
                            );

            default: return PortConstants.NO_PORTS; // unexpected
        }
    }

    static private Portable[] _reducer( final Portable[] unresolvedDeps, final Predicatable predicate )
    {
        // Upgrade combines two arrays, in the unlikely scenario, ensure no duplicates
        final Set<Portable> portSet = new HashSet<Portable>( unresolvedDeps.length );

        for( final Portable port : unresolvedDeps )
        {
            if( predicate.evaluate( port ) == true )
            {
                portSet.add( port );
            }
        }

        return ( portSet.isEmpty() == false )
                ? portSet.toArray( new Portable[ portSet.size() ] )
                : PortConstants.NO_PORTS;
    }

    /**
     * for CLI apply command where Sets are in alphabetical order
     *
     * @param resolvedMap
     * @return also Conflicts, ie. something being installed and uninstalled simultaneously
     */
    @Deprecated
    static private Map<EPortMark,Set<Portable>> buildConflictMap( final Map<EPortMark,Set<Portable>> resolvedMap )
    {
        final Set<Portable> addSet = new HashSet<Portable>();
        addSet.addAll( resolvedMap.get( Upgrade ) );
        addSet.addAll( resolvedMap.get( Install ) );
        addSet.addAll( resolvedMap.get( Activate ) );
        addSet.addAll( resolvedMap.get( Dependency_Upgrade ) );
        addSet.addAll( resolvedMap.get( Dependency_Install ) );
        addSet.addAll( resolvedMap.get( Dependency_Activate ) );

        final Set<Portable> releaseSet = new HashSet<Portable>();
        releaseSet.addAll( resolvedMap.get( Uninstall ) );
        releaseSet.addAll( resolvedMap.get( Deactivate ) );
        releaseSet.addAll( resolvedMap.get( Dependant_Uninstall ) );
        releaseSet.addAll( resolvedMap.get( Dependant_Deactivate ) );

        // conflict pass
        final Set<Portable> conflictSet = resolvedMap.get( Conflicted ); // alias
        for( final Portable port : addSet )
        {
            if( releaseSet.contains( port ) == true ) conflictSet.add( port );
        }

        for( final Portable port : releaseSet )
        {
            if( addSet.contains( port ) == true ) conflictSet.add( port );
        }

        // copy map into alphabetical order
        final Map<EPortMark,Set<Portable>> map = new EnumMap<EPortMark, Set<Portable>>( EPortMark.class );
        for( final EPortMark mark : EPortMark.VALUES )
        {
            final Set<Portable> set = resolvedMap.get( mark );
            map.put( mark, new TreeSet<Portable>( set ) );
        }

        return Collections.unmodifiableMap( map );
    }
}
