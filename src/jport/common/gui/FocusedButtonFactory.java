package jport.common.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.AbstractButton;
import javax.swing.JButton;


/**
 * Mac users expect one "Default" button per Dialog window.
 *
 * @author sbaber
 */
public class FocusedButtonFactory
{
    private FocusedButtonFactory() {}

    static public AbstractButton create( final String text, final String tip )
    {
        final JButton jb = new JButton( text );
        jb.setToolTipText( ( tip != null && tip.isEmpty() == false ) ? tip : null );
        jb.setSelected( true );
        jb.setDefaultCapable( true );
        jb.setFocusable( true );
        jb.requestFocusInWindow(); // successfully hilites only if in Container?

        // accept [ENTER] or [CR] key as well as [SPACE BAR] but don't double fire with .isActionKey() by disabling the button
        jb.addKeyListener( new KeyAdapter() // anonymous class
                {   @Override public void keyReleased( KeyEvent e )
                    {   if( e.getKeyCode() == KeyEvent.VK_ENTER || e.isActionKey() == true )
                        {
                            jb.doClick(); // cause action in EDT first
                            jb.setEnabled( false ); // then debounce any follow ups
                        }
                    }
                } );

        return jb;
    }
}
