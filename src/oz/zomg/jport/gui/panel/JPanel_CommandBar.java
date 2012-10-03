package oz.zomg.jport.gui.panel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import oz.zomg.jport.PortConstants;
import oz.zomg.jport.TheApplication;
import oz.zomg.jport.common.Elemental;
import oz.zomg.jport.common.Elemental.EElemental;
import oz.zomg.jport.common.GuiUtil_;
import oz.zomg.jport.common.HttpUtil;
import oz.zomg.jport.common.gui.ModalDialogFactory;
import oz.zomg.jport.common.gui.ModalDialogFactory.EConfirmationChoices;
import oz.zomg.jport.gui.Commander;
import oz.zomg.jport.gui.Commander.ESearchWhere;
import oz.zomg.jport.gui.TheUiHolder;
import oz.zomg.jport.gui.window.JDialog_AboutApp;
import oz.zomg.jport.ports.PortsCliUtil;
import oz.zomg.jport.type.Portable;


/**
 * Application wide, user driven, main command functions.
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-nd/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US">
 * Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License</a>.</SMALL>
 */
@SuppressWarnings("serial")
public class JPanel_CommandBar extends JPanel
    implements
          ActionListener
        , FocusListener
        , Elemental.Listenable<Portable>
{
    final private Commander      fCommander;

    // ignored on Mac-PLAF are .setBackground() and .setContentAreaFilled()
    final private AbstractButton ab_Sync              = new JButton( "\u21BB Sync" ); // unicode clockwise arrow
    final private AbstractButton ab_MarkOutdated      = new JButton( "Mark \u2192 All Upgrades" ); // unicode right arrow
    final private AbstractButton ab_ApplyMarks        = new JButton( "\u221A Apply..." ); // unicode square root
    final private AbstractButton ab_MoreCommand       = new JButton( "More \u25BC" ); // unicode downward triangle, can not be HTML or will wreck BoxLayout
    final private AbstractButton ab_ClearSearch       = new JButton( "X" );

    final private JMenuItem      jItem_PortDetail     = new JMenuItem( "Details..." );
    final private JMenuItem      jItem_ResetMark      = new JMenuItem( "Reset Marks" );
    final private JMenuItem      jItem_ResetFilter    = new JMenuItem( "Reset Filters" );
    final private JMenuItem      jItem_ResetCache     = new JMenuItem( "Reset Logo Cache" );
    final private JMenuItem      jItem_MarkInactive   = new JMenuItem( "Mark \u2192 All Inactive" );
    final private JMenuItem      jItem_CleanInstalled = new JMenuItem( "Clean Ports..." );
    final private JMenuItem      jItem_UpgradeCli     = new JMenuItem( "Update MacPorts..." );
    final private JMenuItem      jItem_AboutApp       = new JMenuItem( "About "+ PortConstants.APP_NAME +"..." );

    final private JPopupMenu     jPop_MoreCmd         = new JPopupMenu();

    final private JComboBox      jCombo_LookIn        = new JComboBox( ESearchWhere.values() );
    final private JTextField     jField_Search        = new JTextField( 16 );


    /**
     * @param commander
     * @param hitTotalComponent created elsewhere for updating
     */
    public JPanel_CommandBar( final Commander commander, final Component hitTotalComponent )
    {
        fCommander = commander;

        this.setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) ); // btw- will not compress in FlowLayout -> .createHorizontalStrut( 2560 )
        this.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

        final String AB_SYNC_TIP = "<HTML>Refresh all loaded Ports information.<HR>"
                +"<I>Performs a sync operation only on the ports <BR>"
                +"tree of a MacPorts installation, pulling <BR>"
                +"in the latest revision available of the Portfiles <BR>"
                +"from the MacPorts rsync server.";
        ab_Sync             .setToolTipText( AB_SYNC_TIP );
        ab_MarkOutdated     .setToolTipText( "Marks all outdated Ports for upgrading" );
        ab_ApplyMarks       .setToolTipText( "Applies marked Port status change requests" );
        ab_MoreCommand      .setToolTipText( "Show other commands" );
        ab_ClearSearch      .setToolTipText( "Clear search text" );
        jItem_PortDetail    .setToolTipText( "Show Port details in a separate window" );
        jItem_ResetMark     .setToolTipText( "Remove all Port status change request marks" );
        jItem_ResetFilter   .setToolTipText( "Show all Ports without any filtering" );
        jItem_ResetCache    .setToolTipText( "Remove JPortUI project logos from your computer" );
        jItem_MarkInactive  .setToolTipText( "Marks all inactive Ports for removal" );
        jItem_CleanInstalled.setToolTipText( "<HTML>Clean installed Ports of any working,<BR>distribution, and log files" );
        jItem_UpgradeCli    .setToolTipText( "Have MacPorts self-update its CLI tools" );
        jItem_AboutApp      .setToolTipText( "Credits" );
        jCombo_LookIn       .setToolTipText( "Choose what Port information to search" );
        jField_Search       .setToolTipText( "<HTML>Type [ENTER] or [CR] to begin search.<BR>Use [+] to require each search term be present" );

        ab_Sync         .setEnabled( PortsCliUtil.HAS_PORT_CLI ); // only if ports bin file exists
        ab_ApplyMarks   .setEnabled( false );
        jItem_PortDetail.setEnabled( false );
        jItem_UpgradeCli.setEnabled( PortsCliUtil.HAS_PORT_CLI ); // only if ports bin file exists
        // leaving ab_MarkOutdated enabled because if any Ports are outdated, then we wont know until later in the Notifier Elemental from CLI recon

        for( final Component component : new Component[] // required for text field to gain focus at startup
                { ab_Sync
                , ab_MarkOutdated
                , ab_ApplyMarks
                , ab_MoreCommand
                , ab_ClearSearch
                , jCombo_LookIn
                }
           ) { component.setFocusable( false ); } // more like a lame-duh expression than a lambda expression

        jField_Search.setFont( new Font( Font.MONOSPACED, Font.BOLD, 16 ) );
        jField_Search.setHorizontalAlignment( JTextField.CENTER );
        jField_Search.requestFocusInWindow();

        ab_ClearSearch.setPreferredSize( new Dimension( 22, 22 ) ); // Mac-PLAF ignored .setMaximumSize() alone
        ab_ClearSearch.setMargin( GuiUtil_.ZERO_INSET );

        // needed so that text box doesn't over-expand
        JPanel searchPanel = new JPanel( new FlowLayout( FlowLayout.TRAILING, 1, 0 ) );
        searchPanel.add( jCombo_LookIn );
        searchPanel.add( new JLabel( "<HTML><BIG>\u26B2" ) ); // unicode character "neuter", looks sort of like Mac's magnify glass
        searchPanel.add( jField_Search );
        searchPanel.add( ab_ClearSearch );

        // assemble
        jPop_MoreCmd.add( jItem_PortDetail );
        jPop_MoreCmd.addSeparator();
        jPop_MoreCmd.add( jItem_ResetMark );
        jPop_MoreCmd.add( jItem_ResetFilter );
        jPop_MoreCmd.add( jItem_ResetCache );
        jPop_MoreCmd.addSeparator();
        jPop_MoreCmd.add( jItem_MarkInactive );
        jPop_MoreCmd.add( jItem_CleanInstalled );
        jPop_MoreCmd.add( jItem_UpgradeCli );
        jPop_MoreCmd.addSeparator();
        jPop_MoreCmd.add( jItem_AboutApp );

        this.add( ab_Sync );
        this.add( ab_MarkOutdated );
        this.add( ab_ApplyMarks );
        this.add( Box.createHorizontalStrut( 20 ) );
        this.add( ab_MoreCommand );
        this.add( Box.createHorizontalGlue() );
        this.add( hitTotalComponent ); // centered up
        this.add( Box.createHorizontalGlue() );
        this.add( searchPanel );

        // listeners
        for( final AbstractButton ab : GuiUtil_.getChildren( AbstractButton.class, jPop_MoreCmd, (Container)this ) )
        {   // popup menu items and panel's other ABs
            ab.addActionListener( this );
        }
//        ab_Sync.addActionListener( this );
//        ab_MarkOutdated.addActionListener( this );
//        ab_Apply.addActionListener( this );
//        ab_Reset.addActionListener( this );
//        ab_Details.addActionListener( this );
//        ab_ClearSearch.addActionListener( this );
//        jItem_Detail.addActionListener( this );
//        jItem_ResetAll   .addActionListener( this );
//        jItem_ResetFilter.addActionListener( this );
//        jItem_ResetMark .addActionListener( this );
//        jItem_Upgrade   .addActionListener( this );
//        jItem_About   .addActionListener( this );

        jCombo_LookIn.addActionListener( this );
        jField_Search.addActionListener( this );

        jField_Search.addFocusListener( this );

        TheApplication.INSTANCE.getCrudNotifier().addListener( this );
    }

    /**
     * Set up by user interaction with GUI Components.
     */
    private void doDirectedTextSearch()
    {
        final String searchText = jField_Search.getText();
        final ESearchWhere searchWhereEnum = (ESearchWhere)jCombo_LookIn.getSelectedItem();

        fCommander.doDirectedTextSearch( searchText, searchWhereEnum );
    }

    /**
     * Reset text filter.
     */
    private void clearTextSearch()
    {
        jField_Search.setText( "" );
        doDirectedTextSearch();
    }

    @Override public void notify( final EElemental elemental, final Portable port )
    {
        switch( elemental )
        {
            case RETRIEVED :
                    jItem_PortDetail.setEnabled( port != Portable.NONE );
                    break;

            case UPDATED :
                    final boolean isMarked = TheApplication.INSTANCE.getPortsMarker().getMarkCount() > 0;
                    ab_ApplyMarks.setEnabled( isMarked );
                    break;
        }
    }

    @Override public void actionPerformed( ActionEvent e )
    {
        final Object obj = e.getSource();
        if( obj instanceof AbstractButton )
        {
            final AbstractButton ab = (AbstractButton)obj;
            if( ab == ab_Sync )
            {
                fCommander.syncPorts();
            }
            else if( ab == ab_MarkOutdated )
            {
                fCommander.markOutdatedPorts();
            }
            else if( ab == ab_ApplyMarks )
            {
                fCommander.confirmApplyMarks();
            }
            else if( ab == ab_ClearSearch )
            {
                clearTextSearch();
            }
            else if( ab == ab_MoreCommand )
            {
                jPop_MoreCmd.show( ab_MoreCommand, 0, ab_MoreCommand.getHeight() );
            }
            else if( ab == jItem_MarkInactive )
            {
                fCommander.markInactivePorts();
            }
            else if( ab == jItem_PortDetail )
            {
                fCommander.openSelectionDetails();
            }
            else if( ab == jItem_ResetMark )
            {
                fCommander.clearAllMarks();
            }
            else if( ab == jItem_ResetFilter )
            {
                clearTextSearch();
                TheUiHolder.INSTANCE.causeReset();
            }
            else if( ab == jItem_ResetCache )
            {
                HttpUtil.clearCache();
            }
            else if( ab == jItem_CleanInstalled )
            {
                fCommander.cleanInstalled();
            }
            else if( ab == jItem_UpgradeCli )
            {
                fCommander.updateMacPortsItself();
            }
            else if( ab == jItem_AboutApp )
            {
                if( true )
                {
                    new JDialog_AboutApp().setVisible( true );
                }
                else
                {
                    ModalDialogFactory.showConfirmation
                            ( EConfirmationChoices.OK
                            , TheUiHolder.INSTANCE.getMainFrame()
                            , PortConstants.APP_NAME +" Credits"
                            , "<HTML>UI designed and coded by Stephen Baber<BR><SMALL><BR>MacPorts availble @ http://www.macports.org/"
                            );
                }
            }
        }
        else if( obj instanceof JComboBox )
        {
            doDirectedTextSearch();
        }
        else if( obj instanceof JTextField )
        {
            doDirectedTextSearch();
        }
    }

    @Override public void focusGained( FocusEvent e )
    {
        jField_Search.select( 0, 9999 ); // select all
    }

    @Override public void focusLost( FocusEvent e )
    {
        jField_Search.select( 9999, 9999 ); // deselect
    }
}
