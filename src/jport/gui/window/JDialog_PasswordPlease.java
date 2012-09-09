package jport.gui.window;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.UIManager;
import jport.common.Interfacing_.Targetable;
import jport.common.gui.FocusedButtonFactory;
import jport.gui.TheUiHolder;


/**
 * Present Administrator password request that only
 * calls back a Targetable lambda expression when user
 * types [ENTER] or clicks (OK).
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JDialog_PasswordPlease extends JDialog
    implements ActionListener
{
    static
    {}

    final private AbstractButton ab_Cancel = new JButton( "Cancel" );
    final private AbstractButton ab_Ok     = FocusedButtonFactory.create( "OK", "Use the password" );
    final private JPasswordField jPassword = new JPasswordField( 28 ); // char hiding when typing

    final private Targetable<String> fTargetable;

    /**
     *
     * @param cmd
     * @param adminPassword
     * @param targetable calls back with password
     */
    public JDialog_PasswordPlease
            ( final String cmd
            , final String adminPassword
            , final Targetable<String> targetable
            )
    {
        this
            ( TheUiHolder.INSTANCE.getMainFrame() // stay on top
            , cmd
            , adminPassword
            , targetable
            );
    }

    /**
     * @param owner parent window
     * @param cmd
     * @param adminPassword
     * @param targetable calls back with password
     */
    private JDialog_PasswordPlease
            ( final Window owner
            , final String cmd
            , final String adminPassword
            , final Targetable<String> targetable
            )
    {
        super
            ( owner
            , "Authentication"
            , ModalityType.APPLICATION_MODAL
            );

        if( targetable == null ) throw new NullPointerException();
        fTargetable = targetable;

        // inner north
        final JLabel jLabel_Cmd = new JLabel( "<HTML><FONT size=+0><I>An admin or root password is required to</I><BR><B>"+ cmd );

        // inner south
        jPassword.setText( adminPassword );
        jPassword.setFont( new Font( Font.MONOSPACED, Font.BOLD, 16 ) );
        jPassword.setSelectionStart( 0 );
        jPassword.setSelectionEnd( 99999 );
        jPassword.requestFocusInWindow(); // put insertion caret here

        // outer center
        final JPanel centerPanel = new JPanel( new BorderLayout( 20, 20 ) );
        centerPanel.add( jLabel_Cmd, BorderLayout.NORTH );
        centerPanel.add( jPassword , BorderLayout.SOUTH );

        // outer south
        final JPanel southPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        southPanel.setBorder( BorderFactory.createEmptyBorder( 10, 0, 0, 0 ) ); // T L B R
        southPanel.add( ab_Cancel );
        southPanel.add( Box.createHorizontalStrut( 15 ) );
        southPanel.add( ab_Ok );

        // outer west
        final JPanel westPanel = new JPanel(); // moves icon to top of west constraint
        final JLabel jLabel_Warn = new JLabel( "   ", UIManager.getIcon( "OptionPane.warningIcon" ), JLabel.LEFT ); // 3 spaces
        jLabel_Warn.setVerticalTextPosition( JLabel.BOTTOM );
        westPanel.add( jLabel_Warn );

        // assemble
        this.add( centerPanel, BorderLayout.CENTER );
        this.add( southPanel , BorderLayout.SOUTH );
        this.add( westPanel  , BorderLayout.WEST );

        ((JPanel)this.getContentPane()).setBorder( BorderFactory.createEmptyBorder( 20, 20, 10, 20 ) ); // T L B R

        this.pack();
        this.setLocationRelativeTo( owner );

        // listener
        ab_Cancel.addActionListener( this );
        ab_Ok    .addActionListener( this );
        jPassword.addActionListener( this );
    }

    @Override public void actionPerformed( ActionEvent e )
    {
        final Object obj = e.getSource(); // alias

        // javadoc recommended password security precautions
        final char[] chars = jPassword.getPassword(); // alias
        final String password = new String( chars );
        Arrays.fill( chars, (char)0 );

        //... if obj == ok, cancel, jPass
        this.dispose(); // close window since Future can not receive a reference to the JDialog

        if( obj != ab_Cancel )
        {
            fTargetable.target( password );
        }
    }

    /**
     * No way to make it use a JPasswordField?
     *
     * @param cmd
     * @param adminPassword
     * @return
     */
    static private String showPasswordInput( final String cmd, final String adminPassword )
    {
        final String response = JOptionPane.showInputDialog
                ( TheUiHolder.INSTANCE.getMainFrame()
                , "<HTML>An admin or root password is required to <BR><B>"+ cmd
                );
        return ( response != null ) ? response : "";
    }

    // TEST
//    static public void main( String[] args )
//    {
//        final Targetable<String> targetable = new Targetable<String>()
//                {   @Override public void target( String obj )
//                    {   System.out.println( "result="+ obj );
//                    }
//                };
//
//        JDialog jd = new JDialog_PasswordPlease( null, "Alpha Alpha Alpha Alpha Alpha <BR>Bravo Bravo <BR>Charlie", "*password*", targetable );
//        jd.setVisible( true );
//    }
}
