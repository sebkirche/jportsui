package oz.zomg.jport.gui.window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import oz.zomg.jport.PortConstants;
import oz.zomg.jport.TheApplication;
import oz.zomg.jport.TheOsBinaries;
import oz.zomg.jport.common.HttpUtil;
import oz.zomg.jport.common.ImageUtil_;
import oz.zomg.jport.common.Interfacing_.Targetable;
import oz.zomg.jport.common.StringsUtil_;
import oz.zomg.jport.common.gui.FocusedButtonFactory;
import oz.zomg.jport.gui.TheUiHolder;
import oz.zomg.jport.type.EPortStatus;
import oz.zomg.jport.type.Portable;
import oz.zomg.jport.type.Portable.Predicatable;


/**
 * Show an Application Information dialog box with
 * native bin or Installed port domain browse buttons.
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * Creative Commons Attribution-ShareAlike 3.0 Unported License</a>.</SMALL>
 */
@SuppressWarnings("serial")
public class JDialog_AboutApp extends JDialog
    implements ActionListener
{
    static final private int _MAX_PIXEL_SIZE = 48;

    static final private String _COPYRIGHT_NOTICE = "<HTML><CENTER>"
            +"<IMG src=\"http://i.creativecommons.org/l/by-sa/3.0/80x15.png\"><BR>"
            +"<SMALL>This work is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License</SMALL>"
            ;

    static
    {}

    final private Font fFont = new Font( Font.SANS_SERIF, Font.PLAIN, 9 );
    final private JPanel fPopulatePanel = new JPanel( new FlowLayout() );

    public JDialog_AboutApp()
    {
        super
            ( TheUiHolder.INSTANCE.getMainFrame() // stay on top
            , PortConstants.APP_NAME +"  --  Java based, graphical user interface to MacPorts 2.0+"
            , ModalityType.APPLICATION_MODAL
            );

        this.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        this.setLayout( new BorderLayout( 20, 20 ) );
        this.setSize( 1024, 624 );
        this.setLocationByPlatform( true );

        // determine unique domains
        final Map<String,Set<Portable>> orderedDomain_to_PortSet_Map = new TreeMap<String,Set<Portable>>();

        final Predicatable predicate = new Predicatable() // para-lambda expression
                {   @Override public boolean evaluate( final Portable port )
                    {   return port.hasStatus( EPortStatus.INSTALLED ) == true || TheOsBinaries.INSTANCE.has( port.getName() ) == true;
                    }
                };

        final Portable[] ports = TheApplication.INSTANCE.getPortsCatalog().getPortsInventory().filter( predicate );
        for( final Portable port : ports )
        {   // installed or Native
            final String domain = port.getDomain();
            if( orderedDomain_to_PortSet_Map.containsKey( domain ) == false )
            {
                orderedDomain_to_PortSet_Map.put( domain, new TreeSet<Portable>() );
            }

            final Set<Portable> orderedSet = orderedDomain_to_PortSet_Map.get( domain );
            orderedSet.add( port );
        }

        orderedDomain_to_PortSet_Map.remove( "" ); // sometimes the domain is unknown
        orderedDomain_to_PortSet_Map.remove( "http://www.darwinsys.com" ); // natively installed

        // fetch images from cache or in another I/O thread
        for( final Map.Entry<String,Set<Portable>> entry : orderedDomain_to_PortSet_Map.entrySet() )
        {
            final String domain = entry.getKey(); // alias
            final Set<Portable> portSet = entry.getValue(); // alias

            HttpUtil.retrieveLogoConcurrently
                    ( domain
                    , new Targetable<Image>() // anonymous class
                            {   @Override public void target( Image image )
                                {   if( image != null && image.getWidth( null ) >= 16 )
                                    {   // ".ICO" image may be usable
                                        addDomainButton( domain, image, portSet );
                                    }
                                }
                            }
                    );
        }

        // north
        final JLabel jLabel = new JLabel( _COPYRIGHT_NOTICE );
        jLabel.setHorizontalAlignment( JLabel.CENTER );

        // south
        final AbstractButton ab = FocusedButtonFactory.create( "Copyright 2012 by Stephen Baber", "Originally designed and coded in NetBeans IDE 7.2 on JDK 1.6.0_35" );
        ab.addActionListener( this );

        // sub-assemble
        final JPanel jPanel = new JPanel( new BorderLayout() );
        jPanel.add( jLabel, BorderLayout.NORTH );
        jPanel.add( ab, BorderLayout.SOUTH );

        // assemble
        this.add( fPopulatePanel, BorderLayout.CENTER );
        this.add( jPanel, BorderLayout.SOUTH );
    }

    /**
     * Live adds a browse button with domain "favicon".
     * No duplicates possible.
     *
     * @param domain
     * @param image
     * @param portSet
     */
    private void addDomainButton
            ( final String domain
            , final Image image
            , final Set<Portable> portSet
            )
    {
        if( SwingUtilities.isEventDispatchThread() == true )
        {
            final Icon icon = new ImageIcon( ImageUtil_.reduceImage( image, _MAX_PIXEL_SIZE ) );

            final AbstractButton ab = new JButton( domain, icon );
            ab.setToolTipText( StringsUtil_.htmlTabularize( 6, " ", " ", portSet ) );
            ab.setFont( fFont );
            ab.setBorderPainted( false );
            ab.setFocusPainted( false );
            ab.setContentAreaFilled( false );
            ab.addActionListener( new ActionListener() // anonymous class
                    {   @Override public void actionPerformed( ActionEvent e )
                        {   HttpUtil.browseTo( domain );
                        }
                    } );

            fPopulatePanel.add( ab );
            fPopulatePanel.add( Box.createHorizontalStrut( 5 ) );
            fPopulatePanel.validate();
        }
        else
        {
            SwingUtilities.invokeLater( new Runnable() // anonymous class
                    {   @Override public void run()
                        {   addDomainButton( domain, image, portSet );
                        }
                    } );
        }
    }

//... animate icons from logo cache

    @Override public void actionPerformed( ActionEvent e )
    {
        this.dispose();
    }
}
