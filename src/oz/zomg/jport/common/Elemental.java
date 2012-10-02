package oz.zomg.jport.common;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import oz.zomg.jport.common.Notification.ANotifier;
import oz.zomg.jport.common.Notification.NotificationListenable;


/**
 * Name space for Observer and Observable C.R.U.D. 
 * events on Collection or array elements.
 */
public class Elemental
{
    static public enum EElemental
            { CREATED     // element .add()
            , RETRIEVED   // element selected by user
            , UPDATED     // element changed
            , DELETED     // element .remove()
//...            , REMOVED_ALL // all elements .clear()
//...            , REBUILT     // flush and cold start from the backing collection
            ;
                    static final EElemental[] VALUES = EElemental.values();
            }

    private Elemental() {}

    /**
     * Cause elemental notifications to occur that would mutate listeners
     * from a "before" state into an "after" state.
     *
     * @param <E>
     * @param notifier
     * @param beforeSet
     * @param afterSet
     */
    static private <E> void differenceEngine( final Notifier<E> notifier, final Set<E> beforeSet, final Set<E> afterSet )
    {
        if( afterSet.equals( beforeSet ) == false )
        {
            final Set<E> includeSet = new HashSet<E>();
            for( final E element : afterSet )
            {
                if( beforeSet.contains( element ) == false )
                {
                    includeSet.add( element );
                }
            }

            final Set<E> excludeSet = new HashSet<E>();
            for( final E element : afterSet )
            {
                if( beforeSet.contains( element ) == false )
                {
                    excludeSet.add( element );
                }
            }

            for( final E element : includeSet )
            {
                notifier.causeNotification( EElemental.CREATED, element );
            }

            for( final E element : excludeSet )
            {
                notifier.causeNotification( EElemental.DELETED, element );
            }
        }
    }


    // ================================================================================
    /**
     * @param <E> element of class type
     */
    static public interface Listenable<E> extends NotificationListenable
    {
        abstract void notify( EElemental elemental, E obj );
    }


    // ================================================================================
    /**
     *
     * @param <E>  element of class type
     */
    static public class Notifier<E> extends ANotifier<Listenable<E>>
    {
        /** for automatic catch-up of listener when it subscribes with .add() */
        private E mLastRetreived;

        /**
         *
         * @param initialSelection usually a guard reference representing a NONE or UNKNOWN state.
         */
        public Notifier( final E initialSelection )
        {   // no-op can not be 'static final' because Generic type <E>
            super( new Listenable<E>() { @Override public void notify( EElemental elemental, E obj ) {} } );

            mLastRetreived = initialSelection;
        }

        /**
         * Subscribe to notifications.
         *
         * @param listenable Will catch-up / cold-start the new listener.
         */
        @Override synchronized public void addListener( final Listenable<E> listenable )
        {
            listenable.notify( EElemental.RETRIEVED, mLastRetreived );

            super.addListener( listenable );
        }

        /**
         *
         * @param elemental
         * @param obj
         */
        synchronized public void causeNotification( final EElemental elemental, final E obj )
        {
            if( elemental == EElemental.RETRIEVED )
            {   // remember for catching-up / cold-starting new listeners
                if( obj != mLastRetreived )
                {
                    mLastRetreived = obj; // continues below
                }
                else
                {   // no change, so do not notify listeners
                    return;
                }
            }

            final Enumeration<Listenable<E>> enumerati = createEnumeration();
            while( enumerati.hasMoreElements() == true )
            {
                enumerati.nextElement().notify( elemental, obj );
            }
        }

        synchronized E getLastRetrieved() { return mLastRetreived; }
    }
}
