package jport.gui;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JLabel;
import jport.PortsCliUtil;
import jport.PortsConstants.EPortMark;
import jport.PortsConstants.EPortStatus;
import jport.PortsConstants.ESearchWhere;
import jport.TheApplication;
import jport.common.CliUtil.Listener;
import jport.common.Elemental.EElemental;
import jport.common.Interfacing_.Targetable;
import jport.common.Notification.OneArgumentListenable;
import jport.common.SearchTerm2;
import jport.gui.window.JDialog_ApplyMarks;
import jport.gui.window.JDialog_PasswordPlease;
import jport.gui.window.JDialog_PortDetail;
import jport.gui.window.JDialog_ProcessStream;
import jport.gui.window.JDialog_ProcessStream.Cliable;
import jport.type.Portable;
import jport.type.Portable.Predicatable;


/**
 * Does commands issued by the user.
 *
 * @author sbaber
 */
public class Commander
{
    /** If ports failed then don't reset UI. For ex. Wifi is off, but clearing the user's marks would be antisocial. */
    static final private OneArgumentListenable<Integer> _RESULT_CODE_LISTENABLE = new OneArgumentListenable<Integer> () // para-lambda anonymous class
            {   @Override public void listen( final Integer resultCode )
                {   if( resultCode == 0 )
                    {   TheApplication.INSTANCE.probeUpdate(); // *BLOCKS*
                    }
                }
            };

    static
    {}

    /** Only retain during this runtime. */
    transient volatile private String vAdminPassword = "";

    Commander() {}

    /**
     * Convert the main Port table selection or deselection into a C.[R.]U.D. event.
     *
     * @param port
     * @return successfully selected
     */
    public boolean selectPort( final Portable port )
    {
        if( port != null )
        {
            TheApplication.INSTANCE.causeCrudNotification( EElemental.RETRIEVED, port );
            return true;
        }

        return false;
    }

    /**
     * Show a modeless dialog window with the selected Port's detail info.
     */
    public void openSelectionDetails()
    {
        final Portable port = TheUiHolder.INSTANCE.getSelectedPort();
        if( port != null && port != Portable.NONE )
        {
            selectPort( port );
            final JDialog detailsDialog = new JDialog_PortDetail( port );
            detailsDialog.setVisible( true );
        }
    }

    /**
     * Resets all user's pending Ports status change requests to unmarked.
     */
    public void clearAllMarks()
    {
        TheApplication.INSTANCE.getPortsMarker().clearAll();
    }

    /**
     * Change all ports that need updating to Upgrade.
     * Dependencies will be assumed as resolved by MacPorts.
     */
    public void markOutdatedPorts()
    {
        for( final Portable port : TheApplication.INSTANCE.getPortsCatalog().getAllPorts() )
        {
            if( port.hasStatus( EPortStatus.OUTDATED ) == true )
            {
                port.setMark( EPortMark.Upgrade );
            }
        }

        TheUiHolder.INSTANCE.setTableSortByMark();
    }

    /**
     * 
     * @param searchText
     * @param searchWhere
     */
    public void doDirectedTextSearch( final String searchText, final ESearchWhere searchWhere )
    {
        final SearchTerm2<String> searchTerm = new SearchTerm2<String>( searchText, true );
        final Portable.Predicatable predicate;
        if( searchTerm.isEmptyTerm() == false )
        {
            predicate = new Portable.Predicatable() // anonymous class
                    {   @Override public boolean evaluate( final Portable port )
                        {   return searchWhere.doesMatch( searchTerm, port );
                        }
                    };
        }
        else
        {   // no search needed
            predicate = Predicatable.ANY;
        }

        TheUiHolder.INSTANCE.getPortFilterPredicate().setTextSearch( predicate );
    }

    /**
     * Confirm with user for MacPorts to change all Ports with status request changes.
     */
    public void applyMarks()
    {
        final JDialog modalDialog = new JDialog_ApplyMarks();
        modalDialog.setVisible( true );
    }

    /**
     * Ask MacPorts to change all Ports with status request changes.
     *
     * @param isSimulated
     * @param map
     */
    public void applyMarksToPorts( final boolean isSimulated, final Map<EPortMark,? extends Set<Portable>> map )
    {
        final String cliCmd = "<HTML>"+ PortsCliUtil.getApplyMarksCli( isSimulated, map ).replace( " ; ", " ; <BR>" );
        final JDialog passwordDialog = new JDialog_PasswordPlease
                ( cliCmd
                , vAdminPassword
                , new Targetable<String>() // anonymous class
                        {   @Override public void target( String obj )
                            {   vAdminPassword = obj;
                                if( vAdminPassword.isEmpty() == false )
                                {
                                    TheUiHolder.INSTANCE.goDark();
                                    final JDialog processDialog = new JDialog_ProcessStream
                                            ( "Applying Marks..."
                                            , new Cliable() // anonymous class
                                                    {   @Override public Thread provideExecutingCommandLineInterfaceThread( final Listener listener )
                                                        {   return PortsCliUtil.cliApplyMarks( vAdminPassword, isSimulated, map, listener );
                                                        }
                                                    }
                                            , _RESULT_CODE_LISTENABLE
                                            );
                                    processDialog.add( new JLabel( cliCmd ), BorderLayout.NORTH );
                                    processDialog.setVisible( true );
                                }
                                // else cancelled
                            }
                        }
                );
        passwordDialog.setVisible( true );
    }

    /**
     * Updates the MacPorts CLI software itself, and performs a Port tree rsync.
     */
    public void updateMacPortsItself()
    {
        final String cliCmd = "sudo port -d selfupdate";
        final JDialog passwordDialog = new JDialog_PasswordPlease
                ( cliCmd
                , vAdminPassword
                , new Targetable<String>() // anonymous class
                        {   @Override public void target( String obj )
                            {   vAdminPassword = obj;
                                if( vAdminPassword.isEmpty() == false )
                                {
                                    TheUiHolder.INSTANCE.goDark();
                                    final JDialog processDialog = new JDialog_ProcessStream
                                            ( cliCmd
                                            , new Cliable() // anonymous class
                                                    {   @Override public Thread provideExecutingCommandLineInterfaceThread( final Listener listener )
                                                        {   return PortsCliUtil.cliUpdateMacPortsItself( vAdminPassword, listener );
                                                        }
                                                    }
                                            , _RESULT_CODE_LISTENABLE
                                            );
                                    processDialog.setVisible( true );
                                }
                                // else cancelled
                            }
                        }
                );
        passwordDialog.setVisible( true );
    }

    /**
     * Reload catalog index, status, and modification dates.
     */
    public void syncPorts()
    {
        final String cliCmd = "sudo port sync -v";
        final JDialog passwordDialog = new JDialog_PasswordPlease
                ( cliCmd
                , vAdminPassword
                , new Targetable<String>() // anonymous class
                        {   @Override public void target( String obj )
                            {   vAdminPassword = obj;
                                if( vAdminPassword.isEmpty() == false )
                                {
                                    TheUiHolder.INSTANCE.goDark();
                                    final JDialog processDialog = new JDialog_ProcessStream
                                            ( cliCmd
                                            , new Cliable() // anonymous class
                                                    {   @Override public Thread provideExecutingCommandLineInterfaceThread( final Listener listener )
                                                        {   return PortsCliUtil.cliSyncUpdate( vAdminPassword, listener );
                                                        }
                                                    }
                                            , _RESULT_CODE_LISTENABLE
                                            );
                                    processDialog.setVisible( true );
                                }
                                // else cancelled
                            }
                        }
                );
        passwordDialog.setVisible( true );
    }
}
