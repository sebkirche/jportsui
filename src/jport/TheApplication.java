package jport;

import javax.swing.SwingUtilities;
import jport.common.Elemental;
import jport.common.Elemental.EElemental;
import jport.common.Elemental.Listenable;
import jport.common.Notification.Notifiable;
import jport.common.Reset.Resetable;
import jport.common.Reset.Reseter;
import jport.gui.TheUiHolder;
import jport.type.Portable;


/**
 * Manages the current PortsCatalog collection and mutable information as a singleton.
 *
 * @author sbaber
 */
public class TheApplication
{
    /** Static because doesn't require the INSTANCE to be finished constructing. */
    static final private Reseter _RESET_CATALOG_NOTIFIER = new Reseter();

    /** Manages the current PortsCatalog collection and mutable information as a singleton. */
    static final public TheApplication INSTANCE = new TheApplication();

    static
    {}

    final private Elemental.Notifier<Portable> fPortElementNotifier = new Elemental.Notifier<Portable>( Portable.NONE );
    final private PortsMarker fPortsMarker = new PortsMarker();

    volatile private PortsCatalog vCurrentPortsCatalog = PortsCatalog.NONE;

    /**
     * Singleton constructor.
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

        // continue in this thread with each Port status CLI requests
//dead, pity that   ->     PortsCliUtil.cliAllStatus();

        if( TheUiHolder.isReady() == true )
        {   // from the Swing thread, reload port table, clear selection etc.
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
    static public Notifiable<Resetable> getResetNotifier() { return _RESET_CATALOG_NOTIFIER; }

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
        _RESET_CATALOG_NOTIFIER.causeReset();
    }
}
