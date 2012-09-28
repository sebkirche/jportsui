package jport.gui.window;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jport.PortsConstants;
import jport.PortsConstants.EPortStatus;
import jport.TheApplication;
import jport.common.HttpUtil;
import jport.common.Reset.Resetable;
import jport.common.Util;
import jport.common.gui.FocusedButtonFactory;
import jport.gui.Commander;
import jport.gui.JProgress_Enum;
import jport.gui.JTabPane_Detail;
import jport.gui.JTabPane_Filter;
import jport.gui.PortFilterPredicates;
import jport.gui.panel.JPanel_CommandBar;
import jport.gui.panel.JPanel_Mark;
import jport.gui.table.TableModel_Port;
import jport.type.Portable;


/**
 * The primary, top-level window for the UI.
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JFrame_Main extends JFrame
    implements ChangeListener
{
    static
    {}

    public JFrame_Main
            ( final Commander            commander
            , final TableModel_Port      portsTable
            , final PortFilterPredicates compoundPredicate
            )
    {
        super( PortsConstants.APP_NAME +"  --  "+ PortsConstants.VERSION  );

        final JTabbedPane jTab_Filter = new JTabPane_Filter();
        final JTabbedPane jTab_Detail = new JTabPane_Detail();

        final JPanel markPortPanel = new JPanel_Mark();

        // JPanel_Mark has no need to manage an unrelated progress bar but it does have a BorderLayout.SOUTH available
        final JProgress_Enum<EPortStatus> jProgress_EchoPortStatus = new JProgress_Enum<EPortStatus>( false, EPortStatus.VALUES );
        jProgress_EchoPortStatus.setPreferredSize( new Dimension( 80, 15 ) ); // default width is 100+ pixels
        markPortPanel.add( jProgress_EchoPortStatus, BorderLayout.SOUTH ); // sneak a progress bar in here

        final JPanel detailPanel = new JPanel( new BorderLayout() );
        detailPanel.add( jTab_Detail, BorderLayout.CENTER );
        detailPanel.add( markPortPanel, BorderLayout.EAST );

        final JSplitPane jSplit_inventory_detail = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true, portsTable.getJScrollPane(), detailPanel );
        final JSplitPane jSplit_view_inv_detail  = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, false, jTab_Filter, jSplit_inventory_detail );

        jSplit_view_inv_detail.setDividerLocation( 232 );
        jSplit_view_inv_detail.setBorder( null ); // declutters
        jSplit_inventory_detail.setBorder( null );

        // north
        final Component northComponent = new JPanel_CommandBar( commander, compoundPredicate.getHitTotalComponent() );

        // center
        final Component centerComponent;
        if( Util.isOnMac() == false || PortsConstants.HAS_MAC_PORTS == true )
        {   //... allows testing on Windows but should not always
            centerComponent = jSplit_view_inv_detail;
        }
        else
        {   // no MacPorts installed
            northComponent.setVisible( false ); // hide command bar to reduce confusion

            final AbstractButton ab_BrowseMacPorts = FocusedButtonFactory.create
                    ( "<HTML><BIG><CENTER>JPortsUI requires MacPorts to be installed.<BR>Click to browse the MacPorts installer page."
                    , "http://www.macports.org/install.php"
                    );
            ab_BrowseMacPorts.setIcon( UIManager.getIcon( "OptionPane.errorIcon" ) );
            ab_BrowseMacPorts.addActionListener( new ActionListener()
                    {   @Override public void actionPerformed( ActionEvent e )
                        {   HttpUtil.browseTo( "http://www.macports.org/install.php" );
                        }
                    } );
            centerComponent = ab_BrowseMacPorts;
        }

        // south
        final Component southComponent = Box.createHorizontalStrut( 1200 ); //... helps .pack(), but should come from a Preference file

        // west
        final Component westComponent = Box.createHorizontalStrut( 10 );

        // east
        final Component eastComponent = Box.createVerticalStrut( 768 ); //... helps .pack(), but should come from a Preference file

        // assemble
        this.add( centerComponent, BorderLayout.CENTER );
        this.add( northComponent , BorderLayout.NORTH );
        this.add( southComponent , BorderLayout.SOUTH );
        this.add( westComponent  , BorderLayout.WEST );
        this.add( eastComponent  , BorderLayout.EAST );

        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        this.pack();

        // listener
        jTab_Filter.addChangeListener( this ); // user changed tabs

        TheApplication.INSTANCE.getResetNotifier().addListener( new Resetable() // anonymous class
                {   @Override public void reset()
                    {   // note: closing all Port Detail dialogs is automatic as they register themselves
                        jTab_Filter.setSelectedIndex( 0 );
                        jTab_Detail.setSelectedIndex( 0 );
                        portsTable.clearSelection();
                        portsTable.setTableSortByName();

                        // THUNK, can't set table rows until port snapshot parsing completes
                        portsTable.setRows( PortsConstants.NO_PORTS );
                        final Portable[] allPorts = TheApplication.INSTANCE.getPortsCatalog().getPortsInventory().getAllPorts().clone();
                        portsTable.setRows( allPorts );
                        jSplit_inventory_detail.setDividerLocation( 0.66F ); // needed the rows first
                    }
                } );

        TheApplication.INSTANCE.getEchoStatusNotifier().addListener( jProgress_EchoPortStatus );
    }

    /**
     * Clear search when filter tabs change.
     *
     * @param e status filter tab changed
     */
    @Override public void stateChanged( final ChangeEvent e )
    {
        //  reset filters like Synaptic does when tabs/views changed
//        fCompoundPredicate.setStatusFilter( Predicatable.ALL );
//        fCompoundPredicate.setHistoFilter( Predicatable.ALL );
//... reset search text box to "", status buttons to All, and all category tables to no selection?
//        fCompoundPredicate.setTextSearch( Predicatable.ALL );
    }
}
