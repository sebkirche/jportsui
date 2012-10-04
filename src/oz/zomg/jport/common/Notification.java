package oz.zomg.jport.common;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Name space class for
 * mostly awesome Notifications.
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * Creative Commons Attribution-ShareAlike 3.0 Unported License</a>.</SMALL>
 */
public class Notification
{
    /** Name space class. */
    private Notification() {}


    // ================================================================================
    /**
     * Allows exposing only the subscription aspect of the Notification implementation.
     *
     * @param <L> listener of class type will be inferred
     */
    static public interface Notifiable<L extends NotificationListenable>
    {
        abstract void addListener   ( final L listenable );
        abstract void removeListener( final L listenable );
    }


    // ================================================================================
    /**
     * Abstract base class that manages Listeners but does not cause notifications.
     * Your derived class will invoke listener notification via an Enumeration visitor.
     * Notification method can not be declared 'abstract' because the actual .causeNotification()
     * method has various parameters.
     * Weakly referenced so does not CPU/mem leak when no
     * other Strongly reachable objects refer to the listener.
     * Thread safe.
     *
     * @param <L> class type will be inferred
     */
    static abstract public class ANotifier<L extends NotificationListenable>
        implements Notifiable<L>
    {
        static final private Reference<?>[] NO_REFS = new Reference[ 0 ];
        static final private NoArgumentListenable[] NO_LISTENERS = new NoArgumentListenable[ 0 ];

        static final private Enumeration<?> EMPTY_ENUMERATION = new Enumeration<Object>() // anonymous class
                {   @Override public boolean hasMoreElements() { return false; }
                    @Override public Object nextElement() { throw new NoSuchElementException(); }
                };

        static
        {}


        /** Used to avoid checking for 'null' in .causeNotification() visitor. */
        final private L fNoOperationListener;

        /** WARNING! non-ref'd anonymous classes will get GC'd out of a WeakHashMap. */
        volatile private Reference<L>[] vWeakListeners = null;

        /** Non-Garbage Collected hard refs to Listeners. */
        volatile private NotificationListenable[] vNoGcListeners = null;


        /**
         *
         * @param noOperationListener Used to avoid checking for 'null' in .causeNotification() visitor in case a Listener was weakly GC'd.
         */
        @SuppressWarnings("unchecked")
        public ANotifier( final L noOperationListener )
        {
            if( noOperationListener == null ) throw new NullPointerException();

            fNoOperationListener = noOperationListener;
        }

        /**
         * Subscribe to notifications.
         *
         * @param listenable
         */
        @SuppressWarnings("unchecked")
        @Override synchronized public void addListener( final L listenable )
        {
            if( listenable.getClass().isAnonymousClass() == true )
            {
                if( vNoGcListeners == null )
                {   // start up case
                    vNoGcListeners = new NotificationListenable[] { listenable };
                }
                else
                {   // append to new array, too small to warrant a List
                    final NotificationListenable[] srcRefs = vNoGcListeners;
                    final int length = srcRefs.length;
                    final NotificationListenable[] destRefs = new NotificationListenable[ length + 1 ]; // nulls
                    System.arraycopy( srcRefs, 0, destRefs, 0, length ); // native copy
                    destRefs[ length ] = listenable; // at end
                    vNoGcListeners = destRefs;
                }
//dead                vNoGcListeners = ( vNoGcListeners == null )
//                        ? new NotificationListenable[] { listenable } // start up case
//                        : RefsUtil.append( vNoGcListeners, listenable );
            }
            else
            {
                final WeakReference<L> weakRef = new WeakReference<L>( listenable );
                if( vWeakListeners == null )
                {   // start up case
                    vWeakListeners = (Reference<L>[])new Reference[] { weakRef };
                }
                else
                {   // append to new array, too small to warrant a List
                    final Reference<L>[] srcRefs = vWeakListeners;
                    final int length = srcRefs.length;
                    final Reference<L>[] destRefs = (Reference<L>[])new Reference[ length + 1 ]; // nulls
                    System.arraycopy( srcRefs, 0, destRefs, 0, length ); // native copy
                    destRefs[ length ] = weakRef; // at end
                    vWeakListeners = destRefs;
                }
//dead                vWeakListeners = ( vWeakListeners == null )
//                        ? new WeakReference[] { weakRef }
//                        : RefsUtil.append( vWeakListeners, weakRef );
            }
        }



        /**
         * Unsubscribe from notifications.
         *
         * @param listenable removal happens automatically by Weak ref when no other Strongly reachable objects refer to it.
         */
        @SuppressWarnings("unchecked")
        @Override synchronized public void removeListener( final L listenable )
        {
            if( listenable.getClass().isAnonymousClass() == true )
            {
                if( vNoGcListeners != null )
                {
                    if( vNoGcListeners.length > 1 )
                    {
                        final NotificationListenable[] srcArray = vNoGcListeners;
                        final int atIndex = Util.indexOfIdentity( listenable, srcArray );
                        if( atIndex != Util.INVALID_INDEX )
                        {   // got one, perform slice
                            final int lengthMinusOne = srcArray.length - 1;
                            final NotificationListenable[] destArray = new NotificationListenable[ lengthMinusOne ];
                            if( atIndex != lengthMinusOne ) System.arraycopy( srcArray, atIndex + 1, destArray, atIndex, lengthMinusOne - atIndex ); // fill in from after removeRef
                            if( atIndex != 0 )              System.arraycopy( srcArray, 0          , destArray, 0      , atIndex ); // fill in from before removeRef
                            vNoGcListeners = destArray;
                        }
                        // else no change
//dead                        vNoGcListeners = RefsUtil.removeIdentity( vNoGcListeners, listenable );
                    }
                    else if( vNoGcListeners[ 0 ] == listenable )
                    {
                        vNoGcListeners = null; // safely releases array while other observers may still be iterating thru it
                    }
                    // else ignore
                }
                // else ignore
            }
            else
            {
                if( vWeakListeners != null )
                {
                    if( vWeakListeners.length > 1 )
                    {
                        final Reference<L>[] srcRefs = vWeakListeners;
                        final List<Reference<L>> refList = new ArrayList<Reference<L>>( srcRefs.length );
                        for( final Reference<L> ref : srcRefs )
                        {
                            final L listener = ref.get();
                            if( listener != listenable && listener != null )
                            {   // drop the ref-to-remove or GCd ref
                                refList.add( ref );
                            }
                        }

                        vWeakListeners = ( refList.isEmpty() == false )
                                ? refList.toArray( new Reference[ refList.size() ] )
                                : null;
//dead                        vWeakListeners = RefsUtil.removeIdentity( vWeakListeners, findWeakRef );
                    }
                    else if( vWeakListeners[ 0 ].get() == listenable )
                    {
                        vWeakListeners = null; // safely releases array while other observers may still be iterating thru it
                    }
                    // else ignore
                }
                // else ignore
            }
        }

        /**
         * Safely releases array while other observers may still be iterating thru it.
         */
        synchronized public void clearAllListeners()
        {
            vWeakListeners = null;
            vNoGcListeners = null;
        }

        synchronized public boolean hasAnyListeners()
        {
            return vWeakListeners != null || vNoGcListeners != null;
        }

        /**
         * Visitor factory, does not incur an allocation if no listeners have been added.
         * Derived class caller does -not- have to check for 'null' listeners because
         * when the WeakRef has been GC'd a NO_OP will be offered.
         * Not an Iterable because don't want .remove() exposed.
         *
         * @return all listeners
         */
        @SuppressWarnings("unchecked")
        synchronized public Enumeration<L> createEnumeration()
        {
            return ( hasAnyListeners() == true )
                    ? new ListenerEnumeration<L>( this )
                    : (Enumeration<L>)EMPTY_ENUMERATION;
        }


        // ================================================================================
        /**
         * Visitor pattern to combine both the Notifier's LinkedList and the WeakRefs[].
         *
         * @param <L>
         */
        static private class ListenerEnumeration<L extends NotificationListenable>
            implements Enumeration<L>
        {
            /** Used to avoid checking for 'null' in .causeNotification() visitor in case Listener has been weakly GC'd. */
            final private L fNoOperationListener;

            final private NotificationListenable[] fNoGcListeners;
            private int mNoGcIndex = 0;

            final private Reference<L>[] fWeakRefListeners;
            private int mWeakRefIndex = 0;

            @SuppressWarnings("unchecked")
            private ListenerEnumeration( final ANotifier<L> notifier )
            {
                fNoOperationListener = notifier.fNoOperationListener;
                fNoGcListeners       = ( notifier.vNoGcListeners != null ) ? notifier.vNoGcListeners : NO_LISTENERS;
                fWeakRefListeners    = ( notifier.vWeakListeners != null ) ? notifier.vWeakListeners : (Reference<L>[])NO_REFS;
            }

            @Override public boolean hasMoreElements()
            {
                return ( mWeakRefIndex < fWeakRefListeners.length ) || ( mNoGcIndex < fNoGcListeners.length );
            }

            @SuppressWarnings("unchecked")
            @Override public L nextElement()
            {
                final L listener = ( mWeakRefIndex < fWeakRefListeners.length )
                        ? fWeakRefListeners[ mWeakRefIndex++ ].get()
                        : ( mNoGcIndex < fNoGcListeners.length )
                                ? (L)fNoGcListeners[ mNoGcIndex++ ]
                                : null;

                return ( listener != null )
                        ? listener
                        : fNoOperationListener; // was GC'd but this implementor will NO-OP without special case logic
            }
        }
    }


    // ================================================================================
    /**
     * Example of Notifications with no arguments.
     *
     * @see jport.common.Reset.Resetable
     */
    static private class Notifier_NoArgument extends ANotifier<NoArgumentListenable>
    {
        static final private NoArgumentListenable NO_OP = new NoArgumentListenable() { @Override public void listen() {} };

        public Notifier_NoArgument() // final NoArgumentListenable noOPeration )
        {
            super( NO_OP );
        }

        synchronized public void causeNotification()
        {
            final Enumeration<NoArgumentListenable> enumerati = createEnumeration();
            while( enumerati.hasMoreElements() == true )
            {
                enumerati.nextElement().listen();
            }
        }
    }


    // ================================================================================
    /**
     * Example of one argument notifications.
     *
     * @param <E> event of class type will be inferred
     */
    static public class Notifier<E> extends ANotifier<OneArgumentListenable<E>>
    {
        static final private OneArgumentListenable<?> NO_OP = new OneArgumentListenable<Object>() { @Override public void listen( Object event ) {} };

        final private Initializable<E> fInitializable;

        public Notifier()
        {
            this( null );
        }

        /**
         * @param initializable automatic cold-start event can be 'null'
         */
        @SuppressWarnings("unchecked")
        public Notifier( final Initializable<E> initializable )
        {
            super( (OneArgumentListenable<E>)NO_OP );

            fInitializable = initializable;
        }

        /**
         * Subscribe to notifications.
         *
         * @param listenable
         */
        @Override synchronized public void addListener( final OneArgumentListenable<E> listenable )
        {
            if( fInitializable != null )
            {   // cold-start
                listenable.listen( fInitializable.getInitializerEvent() );
            }

            super.addListener( listenable );
        }

        synchronized public void causeNotification( final E event )
        {
            final Enumeration<OneArgumentListenable<E>> enumerati = createEnumeration();
            while( enumerati.hasMoreElements() == true )
            {
                enumerati.nextElement().listen( event );
            }
        }


        // ================================================================================
        /**
         * Cold-start an listener.
         *
         * @param <E> event of class type will be inferred
         */
        static public interface Initializable<E>
        {
            abstract E getInitializerEvent();
        }
    }


    // ================================================================================
    /**
     * Example of two argument notifications.
     *
     * @param <S> source of class type will be inferred
     * @param <E> event of class type will be inferred
     */
    static private class Notifier_TwoArgument<S,E> extends ANotifier<TwoArgumentListenable<S,E>>
    {
        static final private TwoArgumentListenable<?,?> NO_OP = new TwoArgumentListenable<Object,Object>() { @Override public void listen( Object source, Object event ) {} };

        @SuppressWarnings("unchecked")
        public Notifier_TwoArgument()
        {
            super( (TwoArgumentListenable<S,E>)NO_OP );
        }

        synchronized public void causeNotification( final S source, final E event )
        {
            final Enumeration<TwoArgumentListenable<S,E>> enumerati = createEnumeration();
            while( enumerati.hasMoreElements() == true )
            {
                enumerati.nextElement().listen( source, event );
            }
        }
    }


    // ================================================================================
    /**
     * Tagging interface.
     */
    static public interface NotificationListenable
    {}


    // ================================================================================
    /**
     * No argument example analogous to an Observer.
     */
    static private interface NoArgumentListenable extends NotificationListenable
    {
        abstract void listen();
    }


    // ================================================================================
    /**
     * One argument example analogous to an Observer.
     *
     * @param <E> event of class type will be inferred
     */
    static public interface OneArgumentListenable<E> extends NotificationListenable
    {
        abstract void listen( E event );
    }


    // ================================================================================
    /**
     * Two argument example analogous to an Observer.
     *
     * @param <S> source of class type will be inferred
     * @param <E> event of class type will be inferred
     */
    static private interface TwoArgumentListenable<S,E> extends NotificationListenable
    {
        abstract void listen( S source, E event );
    }
}
