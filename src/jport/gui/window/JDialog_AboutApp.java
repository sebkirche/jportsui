package jport.gui.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jport.PortsConstants;
import jport.common.CachedUriContent;
import jport.common.ImageUtil_;
import jport.common.UriContent;
import jport.gui.TheUiHolder;


/**
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JDialog_AboutApp extends JDialog
{
    public JDialog_AboutApp()
    {
        super
            ( TheUiHolder.INSTANCE.getMainFrame() // stay on top
            , PortsConstants.APP_NAME +" designed and coded by Stephen Baber"
            , ModalityType.APPLICATION_MODAL
            );

        this.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );

        final UriContent[] ucs = CachedUriContent.dumpContent();
        final int sqr = (int)Math.sqrt( ucs.length );

        if( sqr > 0 )
        {
            this.setLayout( new GridLayout( sqr, sqr ) );
            ((JPanel)this.getContentPane()).setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

            int i = 0;
            int j = 0;
            while( i < sqr*sqr && j < ucs.length )
            {
                final UriContent uc = ucs[ j ];
                j++;

                final Image image = ImageUtil_.parseImage( uc.fUri.getRawPath(), uc.fContentBytes );
                if( image != null )
                {
                    this.add( new JLabel( new ImageIcon( image ) ) );
                    i++;
                }
            }
        }
        else
        {
            this.add( Box.createRigidArea( new Dimension( 256, 256 ) ), BorderLayout.CENTER );
        }

        this.pack();
        this.setLocationByPlatform( true );
    }

//... animate icons from logo cache
}
