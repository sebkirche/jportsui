package jport.gui;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import jport.common.Notification.OneArgumentListenable;
import jport.common.Util;


/**
 * Enum driven progress bar that listens to Notification.OneArgumentListenable<Enum>
 *
 * @param <E>
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JProgress_Enum<E extends Enum<E>> extends JProgressBar
    implements OneArgumentListenable<E>
{
    static
    {}

    final private E[] fEnums;

    /**
     *
     * @param isLabeled shows enum.toString()
     * @param enumClass the full enumeration in ordinal sequence
     */
    public JProgress_Enum( final boolean isLabeled, final Class<E> enumClass )
    {
        this( isLabeled, enumClass.getEnumConstants() );
    }

    /**
     *
     * @param isLabeled
     * @param enums can be a smaller subset of the entire enumeration or follow a non-ordinal sequence
     */
    public JProgress_Enum( final boolean isLabeled, final E... enums )
    {
        super( 0, enums.length );

        fEnums = enums;
        setStringPainted( isLabeled );
        setVisible( false );
    }

    /**
     * Swing EDT Safe.
     *
     * @param here
     */
    @Override public void listen( final E here )
    {
        if( SwingUtilities.isEventDispatchThread() == true )
        {
            final int index = Util.indexOfIdentity( here, fEnums );
            if( index == Util.INVALID_INDEX ) throw new IllegalArgumentException();

            setString( here.toString() );
            setValue( index );
            setVisible( index != fEnums.length - 1 );
        }
        else
        {
            SwingUtilities.invokeLater( new Runnable()
                    {   @Override public void run()
                        {   listen( here );
                        }
                    } );
        }
    }
}
