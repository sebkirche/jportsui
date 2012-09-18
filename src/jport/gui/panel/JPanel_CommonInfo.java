package jport.gui.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import jport.PortsConstants;
import jport.PortsConstants.EPortStatus;
import jport.TheApplication;
import jport.common.Elemental;
import jport.common.Elemental.EElemental;
import jport.common.GuiUtil_;
import jport.common.HttpUtil;
import jport.common.Interfacing_.Targetable;
import jport.common.StringsUtil_;
import jport.gui.AJLabel_PortInfo;
import jport.type.Portable;


/**
 * Shows general information about the Port in four HTML tabled JLabels.
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JPanel_CommonInfo extends JPanel
    implements
          Elemental.Listenable<Portable>
        , ActionListener
{
    static final private boolean _IS_SHOWING_VARIANT = true;
    static final private String  FONT_GRAY = "<FONT color=gray>";
    static final private String  FONT_OFF = "</FONT>";

    static
    {}

    final private boolean fIsAssignmentLocked;

    final private JLabel[] jLabels = new JLabel[ 4 ]; // nulls

    final private AbstractButton abHomepage = new JButton( "web" );

    /** Mutable for .actionPerformed() and follows table selection via .notify().  Must begin with 'null' */
    transient private Portable mAssignedPort = null;

    /**
     *
     * @param assignedPort Use Portable.NONE to signal dynamically driven by user's table selection.
     */
    public JPanel_CommonInfo( final Portable assignedPort )
    {
        super( new BorderLayout() );

        if( assignedPort == null ) throw new NullPointerException();

        fIsAssignmentLocked = assignedPort != Portable.NONE;

        this.setOpaque( false ); // otherwise messes up Mac-PLAF tab pit darkening

        for( int i = 0; i < jLabels.length; i++ )
        {   // init array, lambda expressions would be nicer
            final JLabel jLabel = new JLabel();
            jLabel.setBorder( BorderFactory.createEmptyBorder( GuiUtil_.GAP_PIXEL, GuiUtil_.GAP_PIXEL, GuiUtil_.GAP_PIXEL, GuiUtil_.GAP_PIXEL ) );
            jLabel.setVerticalAlignment( SwingConstants.TOP );
            jLabel.setHorizontalAlignment( SwingConstants.LEFT );
            jLabel.setOpaque( false ); // otherwise messes up Mac-PLAF tab pit darkening

            jLabels[ i ] = jLabel;
        }

        jLabels[ 3 ].setVisible( _IS_SHOWING_VARIANT );

        // needed to keep button small
        final JPanel centerPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        centerPanel.setOpaque( false );
        centerPanel.add( abHomepage );

        // assemble
        this.add( jLabels[ 0 ], BorderLayout.NORTH );
        this.add( jLabels[ 1 ], BorderLayout.WEST );
        this.add( jLabels[ 2 ], BorderLayout.EAST );
        this.add( jLabels[ 3 ], BorderLayout.SOUTH );
        this.add( centerPanel , BorderLayout.CENTER ); // fixes tooltip dead-zone

        // listener
        abHomepage.addActionListener( this );
        TheApplication.INSTANCE.getCrudNotifier().addListener( this ); // automatically calls .notify() and updates mAssignedPort conforming the view
    }

    /**
     *
     * @param port
     * @return status and version
     */
    private String getStatusVersText( final Portable port )
    {
        if( port.hasStatus( EPortStatus.INSTALLED ) == false )
        {   // Uninstalled
            return FONT_GRAY +"Status"+ FONT_OFF +" \t Uninstalled" +'\n'
                 + FONT_GRAY +"Latest Version"+ FONT_OFF +" \t "+ port.getLatestVersion() +'\n';
        }
        else
        {   // Installed
            final String obsolete = ( port.hasStatus( EPortStatus.OBSOLETE ) == true )
                    ? "OBSOLETE "
                    : " ";

            final String installed = ( port.hasStatus( EPortStatus.ACTIVE ) == true )
                    ? "<FONT color=blue>Installed & Active"+ FONT_OFF
                    : "<FONT color=purple>Installed & Inactive"+ FONT_OFF;

            final boolean isOutdated = port.hasStatus( EPortStatus.OUTDATED );

            return FONT_GRAY +"Status"+ FONT_OFF +" \t "+ obsolete + installed +'\n'
                 + ( ( isOutdated == false )
                            ? FONT_GRAY +"Installed Version"+ FONT_OFF +" \t "+ port.getVersionInstalled() +'\n'
                            : FONT_GRAY +"Installed Version"+ FONT_OFF +" \t "+"<FONT color=red><B>OUTDATED</B><BR>"+ port.getVersionInstalled() + FONT_OFF +'\n' )
                 + ( ( isOutdated == false )
                            ? FONT_GRAY +"Latest Version"+ FONT_OFF +" \t "+ port.getLatestVersion() +'\n'
                            : FONT_GRAY +"Latest Version"+ FONT_OFF +" \t <B>"+ port.getLatestVersion() +"</B>" +'\n' );
        }
    }

    /**
     *
     * @param port
     * @return tabbed for HTML table data and new line for HTML table rows
     */
    private String getCaptionText( final Portable port )
    {
        final String mark = ( port.isUnmarked() == true )
                ? ""
                : "<FONT color=#444444>"+ port.getMark() +"\u2192 "+ FONT_OFF; // dark-gray, right arrow

        return " \t <BIG>"+ mark +"<B>"+ port.getName() +"</B></BIG>\n"
              +" \t <FONT size=+1><I>"+ port.getShortDescription() +"</I>"+ FONT_OFF +'\n';
    }

    /**
     *
     * @param port
     * @return distribution and support
     */
    private String getDistroText( final Portable port )
    {
        final String categories  = StringsUtil_.concatenate( ", ", port.getCategories() ); // always at least one
        final String licenses    = ( port.getLicenses()   .length == 0 ) ? "Unknown" : StringsUtil_.concatenate( ", ", port.getLicenses() );
        final String maintainers = ( port.getMaintainers().length == 0 ) ? "Unknown" : StringsUtil_.concatenate( ", ", port.getMaintainers() );
        final String homepage    = ( port.getHomepage()   .isEmpty() )   ? "Unknown" : port.getHomepage();

        return FONT_GRAY +"Homepage</PRE> \t <U>"+ homepage    +"</U>"+'\n'
             + FONT_GRAY +"Maintainer</PRE> \t " + maintainers +'\n'
             + FONT_GRAY +"License</PRE> \t "    + licenses    +'\n'
             + FONT_GRAY +"Category</PRE> \t "   + categories  +'\n';
    }

    /**
     *
     * @param port
     * @return build variants
     */
    private String getVariantText( final Portable port )
    {
        if( port.getVariants().length != 0 )
        {   // has variants
            String modVariants = StringsUtil_.concatenate( ", ", StringsUtil_.sort( port.getVariants() ) );

            if( port.hasStatus( EPortStatus.INSTALLED ) == false )
            {   // NOT installed
                return FONT_GRAY +"Variants"+ FONT_OFF +" \t "+ modVariants +'\n';
            }
            else
            {   // IS installed
                for( final String installedVariants : StringsUtil_.sort( port.getVariantsInstalled() ) )
                {   // hilite installed
                    modVariants = modVariants.replace( installedVariants, "<FONT color=green><B><U>"+ installedVariants +"</U></B>"+ FONT_OFF );
                }

                return FONT_GRAY +"Variants"+ FONT_OFF +" \t "+ modVariants +'\n';
            }
        }
        else
        {
            return " \t \n";
        }
    }

    private void setPort( final Portable port )
    {
        if( port == Portable.NONE )
        {
            for( final JLabel jLabel : jLabels )
            {   // no text or tip
                jLabel.setText( null );
                jLabel.setToolTipText( null );
            }

            jLabels[ 0 ].setText( AJLabel_PortInfo.SELECT_PORT_TEXT );
        }
        else
        {   // HTML tables
            jLabels[ 0 ].setText( StringsUtil_.toHtmlTable( false, getCaptionText( port ) ) );
            jLabels[ 1 ].setText( StringsUtil_.toHtmlTable( false, getStatusVersText( port ) ) );
            jLabels[ 2 ].setText( StringsUtil_.toHtmlTable( false, getDistroText( port ) ) );
            jLabels[ 3 ].setText( StringsUtil_.toHtmlTable( false, getVariantText( port ) ) );

            // same tips
            final String tip = ( port.getShortDescription().equals( port.getLongDescription() ) == false )
                    ? port.provideTipText() // must be different else clutter
                    : null;
            for( final JLabel jLabel : jLabels )
            {
                jLabel.setToolTipText( tip );
            }

            abHomepage.setToolTipText( "<HTML>Browse to<BR>"+ port.getHomepage() );
        }

        final boolean hasHomepage = port.getHomepage().isEmpty() == false;
        abHomepage.setVisible( hasHomepage );
        abHomepage.setIcon( null );

        if( PortsConstants.IS_SHOWING_FAVICON == true )
        {
            jLabels[ 0 ].setIcon( null );

            if( hasHomepage == true )
            {
                HttpUtil.retrieveLogoConcurrently
                        ( port.getDomain()
                        , new Targetable<Image>() // anonymous class
                                {   @Override public void target( final Image image )
                                    {   // ignore any lag behind selection
                                        if( image != null && port.equals( mAssignedPort ) == true )
                                        {   setFavicon( image );
                                        }
                                    }
                                }
                        );
            }
        }
    }

    /**
     * EDT safe as may change parent container layout.
     *
     * @param image can be 'null'
     */
    private void setFavicon( final Image image )
    {
        if( SwingUtilities.isEventDispatchThread() == true )
        {   // Mac-PLAF not happy, changes to square button -> abHomepage.setIcon( new ImageIcon( image ) )
            final ImageIcon imageIcon = ( image != null )
                    ? new ImageIcon( image )
                    : null;
            jLabels[ 0 ].setIcon( imageIcon );
        }
        else
        {   SwingUtilities.invokeLater( new Runnable() // anonymous class
                    {   @Override public void run()
                        {   setFavicon( image );
                        }
                    } );
        }
    }

    /**
     * Follows main table selection or initializes the locked port assignment.
     *
     * @param elemental action
     * @param port of marking view
     */
    @Override public void notify( final EElemental elemental, final Portable port )
    {
        switch( elemental )
        {
            case RETRIEVED :
                {   if( fIsAssignmentLocked == false || mAssignedPort == null )
                    {
                        mAssignedPort = port;
                        setPort( port );
                    }
                }   break;

            case UPDATED :
                {   if( mAssignedPort == port )
                    {   // filtered out non-related updates
                        setPort( port ); break; // Mark may have changed
                    }
                }   break;
        }
    }

    @Override public void actionPerformed( final ActionEvent e )
    {
        if( e.getSource() == abHomepage )
        {   // could have used CLI -> port gohome $NAME
            HttpUtil.browseTo( mAssignedPort.getHomepage() );
        }
    }
}
