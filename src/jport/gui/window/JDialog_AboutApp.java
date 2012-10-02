package jport.gui.window;

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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jport.PortConstants;
import jport.TheApplication;
import jport.TheOsBinaries;
import jport.common.HttpUtil;
import jport.common.ImageUtil_;
import jport.common.Interfacing_.Targetable;
import jport.common.StringsUtil_;
import jport.gui.TheUiHolder;
import jport.type.EPortStatus;
import jport.type.Portable;
import jport.type.Portable.Predicatable;


/**
 * Show an Application Information dialog box with
 * native bin or Installed port domain browse buttons.
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JDialog_AboutApp extends JDialog
{
    static final private int _MAX_PIXEL_SIZE = 48;

    static
    {}

    final private Font fFont = new Font( Font.SANS_SERIF, Font.PLAIN, 9 );
    final private JPanel fContentPanel;

    public JDialog_AboutApp()
    {
        super
            ( TheUiHolder.INSTANCE.getMainFrame() // stay on top
            , PortConstants.APP_NAME +" designed and coded by Stephen Baber"
            , ModalityType.APPLICATION_MODAL
            );

        this.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        this.setLayout( new FlowLayout() );
        this.setSize( 832, 624 );
        this.setLocationByPlatform( true );

        fContentPanel = (JPanel)this.getContentPane();
        fContentPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

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

            fContentPanel.add( ab );
            fContentPanel.add( Box.createHorizontalStrut( 5 ) );
            fContentPanel.validate();
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
}
