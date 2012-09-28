package jport;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import jport.PortsConstants.EPortStatus;
import jport.common.Elemental;
import jport.common.Elemental.EElemental;
import jport.common.Elemental.Listenable;
import jport.common.Notification;
import jport.common.Notification.Notifiable;
import jport.common.Notification.OneArgumentListenable;
import jport.common.Reset.Resetable;
import jport.common.Reset.Reseter;
import jport.gui.TheUiHolder;
import jport.type.CliPortInfo;
import jport.type.Portable;


/**
 * Manages the current PortsCatalog collection and mutable information as a singleton.
 *
 * @author sbaber
 */
public class TheApplication
{
    /** Manages the current PortsCatalog collection and mutable information as a singleton. */
    static final public TheApplication INSTANCE = new TheApplication();

    static
    {}

    /** Notifies when "PortIndex" file has been parsed and Port status queried. */
    final private Reseter fCatalogResetNotifier = new Reseter();

    /** Facility for driving progress bar when ports CLI echo Ports status query is ongoing. */
    final private Notification.Notifier<EPortStatus> fEchoStatusNotifier = new Notification.Notifier<EPortStatus>();

    /** Get notifications of individual  */
    final private Elemental.Notifier<Portable> fPortElementNotifier = new Elemental.Notifier<Portable>( Portable.NONE );

    final private PortsMarker fPortsMarker = new PortsMarker();

    volatile private PortsCatalog vCurrentPortsCatalog = PortsCatalog.NONE;

    /**
     * Singleton constructor.
     * Needs to be fast as many Threads are waiting on TheApplication.INSTANCE being non-null!
     */
    private TheApplication() {}

    /**
     *
     * @return facility for letting the user indicate desired port status change requests
     */
    public PortsMarker getPortsMarker() { return fPortsMarker; }

    /**
     *
     * @return instance managing the current contents of the "PortIndex" file
     */
    public PortsCatalog getPortsCatalog() { return vCurrentPortsCatalog; }

    /**
     * Reload catalog index, status, and modification dates.
     * The time stamp of the current catalog is the refresh time.
     * Completely refreshes the GUI in regards to the status of all known Ports.
     * Enables the main table for selections.
     */
    synchronized public void probeUpdate()
    {
        // complete parsing "PortsIndex" in this thread
        vCurrentPortsCatalog = new PortsCatalog( vCurrentPortsCatalog );

        //  since I/O bound, fetch the port folder modification dates from the file system in a seperate thread
        new Thread
                ( new Runnable() // anonymous class
                        {   @Override public void run()
                            {   vCurrentPortsCatalog.scanDates();
                            }
                        }
                , TheApplication.class.getCanonicalName()
                ).start();

        // from the Swing thread, reload port table, clear selection etc.
        if( TheUiHolder.isReady() == true )
        {
            SwingUtilities.invokeLater( new Runnable()
                    {   @Override public void run()
                        {   TheApplication.INSTANCE.causeReset();
                            deprang();
                        }
                    } );
        }
    }

    /**
     * Unmark any applied changes from CLI or JPortsUI.
     * Turn the JTable back on.
     */
    public void deprang()
    {
        SwingUtilities.invokeLater( new Runnable()
                {   @Override public void run()
                    {   getPortsMarker().exchangeAudit( vCurrentPortsCatalog );
                        TheUiHolder.INSTANCE.goLive();
                    }
                } );
    }

    /**
     * Subscribe to Ports Catalog reset notifications.
     *
     * @return just the listener .add() & .remove() aspect of the interface
     */
    public Notifiable<Resetable> getResetNotifier() { return fCatalogResetNotifier; }

    /**
     * Subscribe to Ports Catalog reset notifications.
     *
     * @return just the listener .add() & .remove() aspect of the interface
     */
    public Notifiable<OneArgumentListenable<EPortStatus>> getEchoStatusNotifier() { return fEchoStatusNotifier; }

    /**
     * Subscribe to Port elemental notifications.
     *
     * @return just the listener .add() & .remove() aspect of the interface
     */
    public Notifiable<Listenable<Portable>> getCrudNotifier() { return fPortElementNotifier; }

    /**
     * Sends message to all C.R.U.D. listeners
     *
     * @param elemental
     * @param port
     */
    public void causeCrudNotification( final EElemental elemental, final Portable port )
    {
        fPortElementNotifier.causeNotification( elemental, port );
    }

    public void causeReset()
    {
        fCatalogResetNotifier.causeReset();
    }

    /**
     * Full accounting avoids asking for All ports or Uninstalled ports as
     * these are assumed from the "PortIndex" parsing.
     * Note: Inefficient but I do not know a way to get all status attributes for each installed port, see "man port"
     *
     * @return as reported by the CLI "port echo installed" all of which are type CliPort
     */
    Map<EPortStatus,Set<CliPortInfo>> cliEchoAllStatus()
    {
        final Map<EPortStatus,Set<CliPortInfo>> status_to_InfoSet_Map = new EnumMap<EPortStatus, Set<CliPortInfo>>( EPortStatus.class );

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
                    {   fEchoStatusNotifier.causeNotification( statusEnum );
                        final Set<CliPortInfo> cpiSet = PortsCliUtil.cliEcho( statusEnum );
                        status_to_InfoSet_Map.put( statusEnum, cpiSet );
                        break;
                    }
            }
        }

        return status_to_InfoSet_Map;
    }
}
