package jport.common;

import java.util.Enumeration;
import jport.common.Notification.ANotifier;
import jport.common.Notification.NotificationListenable;


/**
 * Name space class.
 *
 * @author sbaber
 */
public class Reset
{
    /** No Operation Listener para-lambda. */
    static final private Resetable _NO_OP = new Resetable() 
            {   @Override public void reset() // anonymous class
                {}
            };

    private Reset() {}

    // ================================================================================
    /**
     * Implementor can be .reset().
     * Not part of common.Interfacing because extends Listenable.
     *
     * @author sbaber
     */
    static public interface Resetable extends NotificationListenable
    {
        abstract public void reset();
    }


    // ================================================================================
    /**
     * Resets listeners.
     */
    static public class Reseter extends ANotifier<Resetable>
    {
        public Reseter()
        {
            super( _NO_OP );
        }

        /**
         * Called after a probe of Ports information to redisplay all contents in the GUI.
         */
        synchronized public void causeReset()
        {
            final Enumeration<Resetable> enumerati = createEnumeration();
            while( enumerati.hasMoreElements() == true )
            {
                enumerati.nextElement().reset();
            }
        }
    }
}
