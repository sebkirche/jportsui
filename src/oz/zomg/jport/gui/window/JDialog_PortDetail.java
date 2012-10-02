package oz.zomg.jport.gui.window;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import oz.zomg.jport.TheApplication;
import oz.zomg.jport.common.Reset.Resetable;
import oz.zomg.jport.common.Util;
import oz.zomg.jport.gui.TheUiHolder;
import oz.zomg.jport.gui.component.JTabPane_Detail;
import oz.zomg.jport.gui.panel.JPanel_Mark;
import oz.zomg.jport.type.Portable;


/**
 * Breaks out the Port details into a separate, heavy-weight, top-level window.
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JDialog_PortDetail extends JDialog
    implements
          Portable.Providable
        , Resetable
{
    static
    {}

    final private Portable fAssignedPort;

    public JDialog_PortDetail( final Portable assignedPort )
    {
        super // even though MODELESS which allows MainFrame to accept clicks, only 'null' parent will allow layering behind MainFrame
            ( TheUiHolder.INSTANCE.getMainFrame()
            , assignedPort.getName() +"  Details"
            , ModalityType.MODELESS
            );

        fAssignedPort = assignedPort;

        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE ); // default behavior is HIDE_ON_CLOSE

        // assemble
        this.add( new JTabPane_Detail( assignedPort ), BorderLayout.CENTER );
        this.add( new JPanel_Mark( assignedPort )    , BorderLayout.EAST );

        this.pack();
        this.setLocationByPlatform( true ); // cascades

        // listener
        TheApplication.INSTANCE.getResetNotifier().addListener( this );

        if( Util.isOnMac() == true )
        {   // register [CMD-W] as close window
            final KeyStroke ks = KeyStroke.getKeyStroke( KeyEvent.VK_W, java.awt.event.InputEvent.META_DOWN_MASK );
            final InputMap im = getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
            im.put( ks, ks ); // action could have been the String "CLOSE"
            getRootPane().getActionMap().put( ks, new AbstractAction() // anonymous class
                    {   @Override public void actionPerformed( ActionEvent e )
                        {   reset();
                        }
                    } );
        }
    }

    @Override public void reset()
    {
        TheApplication.INSTANCE.getResetNotifier().removeListener( this );

        this.dispose();
    }

    @Override public Portable providePort() { return fAssignedPort; }
}
