package oz.zomg.jport.common;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Single thread that sequentializes a work queue as first in, first out
 * on one-shot Runnable tasks and catches all their exceptions.
 * Lazier than an Executor pool.
 */
class Thread_Worker extends Thread
{
    static final private boolean DEBUG = false;
    static final private AtomicInteger COUNT = new AtomicInteger( 0 );

    static
    {}

    /** FIFO. */
    final private Queue<Runnable> fRunQueue = new ConcurrentLinkedQueue<Runnable>(); // required to avoid 'synchronized' deadlock

    /**
     * Worker daemon.
     */
    Thread_Worker( final String name )
    {
        this( name, true );
    }

    /**
     * @param isDaemon 'false' will keep the JVM running until this thread is killed
     */
    private Thread_Worker( final String name, final boolean isDaemon )
    {
        super( Thread_Worker.class.getSimpleName() +'#'+ COUNT.getAndIncrement() +" <- "+ name );
        this.setDaemon( isDaemon );
    }

    public void offer( final Runnable runnable )
    {
        fRunQueue.add( runnable ); // to tail

        synchronized( this )
        {
            this.notifyAll();
        }
    }

    @Override public void run()
    {
        try
        {
            while( Thread.interrupted() == false )
            {
                synchronized( this )
                {
                    if( fRunQueue.isEmpty() == true ) // Findbugs recommendation
                    {
                        this.wait( 2000 ); // *BLOCKS*
                    }
                }

                while( fRunQueue.isEmpty() == false ) // required
                {   // should keep the JVM up until the runnable queue is empty
                    final Runnable runnable = fRunQueue.remove(); // from head
                    try
                    {
                        final long startMillisec = System.currentTimeMillis();
                        runnable.run();

                        if( DEBUG ) 
                        {
                            final int backlog = fRunQueue.size();
                            final long deltaMs = System.currentTimeMillis() - startMillisec;
                            final String deltaStr   = ( deltaMs > 0 ) ? " time ms="+ deltaMs : "";
                            final String backlogStr = ( backlog > 0 ) ? " backlog="+ backlog : "";
                            System.out.println( this.getName() +" ran "+ runnable.toString() +" ->"+ deltaStr + backlogStr );
                        }
                    }
                    catch( Throwable t )
                    {   // don't bomb the worker Thread, continue to next Runnable or .wait() for more work
                        if( t instanceof IOException == false )
                        {   //... log
                            t.printStackTrace();
                        }
                        else
                        {   // compiler can't specifically catch IOException if not declared thrown
                            System.err.println( t.getMessage() );
                        }
                    }
                }
            }
        }
        catch( InterruptedException ex )
        {}
    }
}