package jport.gui;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
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
import jport.ports.PortsCliUtil;
import jport.type.EPortMark;
import jport.type.EPortStatus;
import jport.type.Portable;
import jport.type.Portable.Predicatable;


/**
 * Does commands issued by the user.
 *
 * @author sbaber
 */
public class Commander
{
    /**
     * For Search JComboBox or Check boxes.
     */
    @SuppressWarnings("unchecked")
    static public enum ESearchWhere
            { Name         { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getName() ); } }
            , Descr_Name   { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getName() +' '+ port.getShortDescription() +' '+ port.getLongDescription() ); }
                             @Override public String toString() { return "Description & Name"; } }
            , Category     { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getCategories() ); } }
            , Maintainer   { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getMaintainers() ); } }
            , Dependencies { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getFullDependencies() ); } }
            , Licenses     { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getLicenses() ); } }
            , Variants     { @Override public boolean doesMatch( final SearchTerm2 searchTerm, final Portable port ) { return searchTerm.doesMatch( port.getVariants() ); } }
            ;
                    abstract public boolean doesMatch( final SearchTerm2<?> searchTerm, final Portable port );
            }

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
     * Change request all ports that need updating to Upgrade.
     * Dependencies will be assumed as resolved by MacPorts.
     */
    public void markOutdatedPorts()
    {
        final Portable[] outdatedPorts = TheApplication.INSTANCE.getPortsCatalog().getPortsInventory().filter( EPortStatus.OUTDATED );
        for( final Portable port : outdatedPorts )
        {
            port.setMark( EPortMark.Upgrade );
        }

        TheUiHolder.INSTANCE.setTableSortByMark();
    }

    public void markInactivePorts()
    {
        final Portable[] inactivePorts = TheApplication.INSTANCE.getPortsCatalog().getPortsInventory().filter( EPortStatus.INACTIVE );
        for( final Portable port : inactivePorts )
        {
            port.setMark( EPortMark.Uninstall );
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
    public void confirmApplyMarks()
    {
        final JDialog modalDialog = new JDialog_ApplyMarks();
        modalDialog.setVisible( true );
    }

    /**
     * Ask MacPorts to change all Ports with status request changes.
     *
     * @param isSimulated
     * @param mark_to_PortSet_Map
     */
    public void applyMarksToPorts( final boolean isSimulated, final Map<EPortMark,? extends Set<Portable>> mark_to_PortSet_Map )
    {
        new ProcessCommand_ApplyMarks( isSimulated, mark_to_PortSet_Map ).passwordedExecute();
    }

    /**
     * Updates the MacPorts CLI software itself, and performs a Port tree rsync as a side-effect.
     */
    public void updateMacPortsItself()
    {
        new ProcessCommand_UpdateMacPortsItself().passwordedExecute();
    }

    /**
     * Reload catalog index, status, and modification dates.
     */
    public void syncPorts()
    {
        new ProcessCommand_Sync().passwordedExecute();
    }

    /**
     * Cleans all installed Ports of distribution files, working files, and logs.
     * Was supposed to also Removes all inactive Ports.
     */
    public void cleanInstalled()
    {
        new ProcessCommand_Clean().passwordedExecute();
    }

    // ================================================================================
    private class ProcessCommand_ApplyMarks extends AProcessCommand
    {
        final private boolean fIsSimulated;
        final private Map<EPortMark,? extends Set<Portable>> fMark_to_PortSet_Map;

        ProcessCommand_ApplyMarks( final boolean isSimulated, final Map<EPortMark,? extends Set<Portable>> mark_to_PortSet_Map )
        {
            super( "Apply Marks", "<HTML>"+ PortsCliUtil.getApplyMarksCli( isSimulated, mark_to_PortSet_Map ).replace( " ; ", " ; <BR>" ) );

            fIsSimulated = isSimulated;
            fMark_to_PortSet_Map = mark_to_PortSet_Map;
        }

        @Override public Thread provideExecutingCommandLineInterfaceThread( final Listener listener )
        {
            return PortsCliUtil.cliApplyMarks
                    ( Commander.this.vAdminPassword
                    , fIsSimulated
                    , fMark_to_PortSet_Map
                    , listener
                    );
        }
    }

    // ================================================================================
    private class ProcessCommand_UpdateMacPortsItself extends AProcessCommand
    {
        ProcessCommand_UpdateMacPortsItself()
        {
            super( "Update MacPorts Tool", "sudo port -v selfupdate" );
        }

        @Override public Thread provideExecutingCommandLineInterfaceThread( final Listener listener )
        {
            return PortsCliUtil.cliUpdateMacPortsItself( Commander.this.vAdminPassword, listener );
        }
    }

    // ================================================================================
    private class ProcessCommand_Sync extends AProcessCommand
    {
        ProcessCommand_Sync()
        {
            super( "Update Port Info", "sudo port -v sync" );
        }

        @Override public Thread provideExecutingCommandLineInterfaceThread( final Listener listener )
        {
            return PortsCliUtil.cliSyncUpdate( Commander.this.vAdminPassword, listener );
        }
    }

    // ================================================================================
    private class ProcessCommand_Clean extends AProcessCommand
    {
        ProcessCommand_Clean()
        {
            super( "Clean Installed Ports", "sudo port -u -p clean --all installed" );
        }

        @Override public Thread provideExecutingCommandLineInterfaceThread( final Listener listener )
        {
            return PortsCliUtil.cliCleanInstalled( Commander.this.vAdminPassword, listener );
        }
    }

    // ================================================================================
    /**
     * Abstract base class for an administrator password driven CLI Port command.
     */
    private abstract class AProcessCommand
        implements
              Cliable
            , Targetable<String>
            , OneArgumentListenable<Integer>
    {
        final private String fTitle;
        final private String fCommandText;

        AProcessCommand( final String title )
        {
            fTitle = title;
            fCommandText = title;
        }

        AProcessCommand( final String title, final String commandText )
        {
            fTitle = title;
            fCommandText = commandText;
        }

        /**
         *
         * @param obj password fired at the command from the password dialog
         */
        @Override public void target( String obj )
        {
            Commander.this.vAdminPassword = obj;
            if( obj.isEmpty() == false )
            {
                TheUiHolder.INSTANCE.goDark();
                final JDialog processDialog = new JDialog_ProcessStream
                        ( fTitle
                        , (Cliable)this
                        , (OneArgumentListenable<Integer>)this
                        );

                // superimposed to the right of the NORTH border error label, kinda strange that
                final JLabel jLabel_cmd = new JLabel( fCommandText );
                jLabel_cmd.setHorizontalAlignment( JLabel.RIGHT );

                processDialog.add( jLabel_cmd, BorderLayout.NORTH );
                processDialog.setVisible( true );
            }
            // else cancelled
        }

        /**
         *
         * @param resultCode returned from Cliable process
         */
        @Override public void listen( final Integer resultCode )
        {
            if( resultCode == 0 )
            {   // implies a Port command ran successfully
                TheApplication.INSTANCE.probeUpdate(); // *BLOCKS*
            }
            else
            {   // recover from CLI error but don't reset UI. For ex. Wifi is off, but clearing the user's marks would be antisocial.
                SwingUtilities.invokeLater( new Runnable()
                        {   @Override public void run()
                            {   TheApplication.INSTANCE.deprang();
                            }
                        } );
            }
        }

        void passwordedExecute()
        {
            final JDialog passwordDialog = new JDialog_PasswordPlease
                    ( fTitle
                    , fCommandText
                    , Commander.this.vAdminPassword
                    , (Targetable<String>)this
                    );
            passwordDialog.setVisible( true );
        }
    }
}
