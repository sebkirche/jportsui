package jport;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jport.PortsConstants.EPortStatus;
import jport.common.Notification;
import jport.common.Notification.OneArgumentListenable;
import jport.type.CliPortInfo;


/**
 * Facility for driving progress bar when ports CLI status query is ongoing.
 *
 * @author sbaber
 */
public class ThePortsEchoer
{
    static final public ThePortsEchoer INSTANCE = new ThePortsEchoer();

    static
    {}

    final private Notification.Notifier<EPortStatus> fStatusNotifier = new Notification.Notifier<EPortStatus>();

    private ThePortsEchoer() {}

    public void addPortsEchoListener( OneArgumentListenable<EPortStatus> listenable )
    {
        fStatusNotifier.addListener( listenable );
    }

    public void removePortsEchoListener( OneArgumentListenable<EPortStatus> listenable )
    {
        fStatusNotifier.removeListener( listenable );
    }

    /**
     * Full accounting avoids asking for All ports or Uninstalled ports as
     * these are assumed from the "PortIndex" parsing.
     * Note: Inefficient but I do not know a way to get all status attributes for each installed port, see "man port"
     *
     * @return as reported by the CLI "port echo installed" all of which are type CliPort
     */
    Map<EPortStatus,Set<CliPortInfo>> cliAllStatus()
    {
        final Map<EPortStatus,Set<CliPortInfo>> status_to_InfoSet_Map = new EnumMap<EPortStatus, Set<CliPortInfo>>( EPortStatus.class );

        if( PortsCliUtil.HAS_PORT_CLI == false )
        {   // non-Ports environment, needs to be installed
            for( final EPortStatus statusEnum : EPortStatus.VALUES )
            {
                final Set<CliPortInfo> emptySet = Collections.emptySet();
                status_to_InfoSet_Map.put( statusEnum, emptySet );
            }
            return status_to_InfoSet_Map;
        }
        else
        {
            for( final EPortStatus statusEnum : EPortStatus.VALUES )
            {
                switch( statusEnum )
                {
                    case ALL         : // fall-thru
                    case UNINSTALLED :
                        {   // do not run CLI on these, too large/slow for a sanity check
                            final Set<CliPortInfo> emptySet = Collections.emptySet();
                            status_to_InfoSet_Map.put( statusEnum, emptySet );
                        }   break;

                    default :
                        {   fStatusNotifier.causeNotification( statusEnum );
                            status_to_InfoSet_Map.put( statusEnum, PortsCliUtil.cliEcho( statusEnum ) );
                            break;
                        }
                }
            }

            return status_to_InfoSet_Map;
        }
    }

     /**
     * Reverse the CLI Port status utility mapping to a more usable form.
     *
     * @param kvMap
     * @return
     */
    static public Map<CliPortInfo,Set<EPortStatus>> createInverseMultiMapping( final Map<EPortStatus,Set<CliPortInfo>> kvMap )
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
