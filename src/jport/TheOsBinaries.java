package jport;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;
import jport.common.Util;


/**
 * Where local MacBSD/XNU provides, some Port binaries do not need to be installed.
 * Ex. gperf, unzip, bash
 *
 * @author sbaber
 */
public class TheOsBinaries
{
    static final public TheOsBinaries INSTANCE = new TheOsBinaries();

    static
    {}

    final Set<String> fOsBinNameSet = new HashSet<String>();

    /**
     * Note: no X11 on Mountain Lion.
     */
    private TheOsBinaries()
    {
        final String[] dirPathNames = ( Util.isOnWindows() == false )
                ? new String[] 
                        { "/bin"
                        , "/sbin"
                        , "/usr/bin"
                        , "/usr/sbin"
                        , "/usr/lib"
                        , "/usr/X11/bin"
                        , "/usr/X11/lib"
                        , "/usr/libexec/apache2"
                        , "/Developer/usr/bin/"
                        }
                : new String[] { "C:\\cygwin\\bin", "C:\\cygwin\\lib" };

        final long startMillisec = System.currentTimeMillis();

        for( final String dirPathName : dirPathNames )
        {
            final File dirPath = new File( dirPathName );
            if( dirPath.exists() == true )
            {
                final File[] files = dirPath.listFiles( new FileFilter() // anonymous class
                        {   @Override public boolean accept( final File path )
                            {   return path.isFile() == true;
                            }
                        } );

                for( final File file : files )
                {
                    final String fileName = file.getName();
                    fOsBinNameSet.add( fileName.intern() );

                    final int p = fileName.indexOf( '.' ); // remove ".exe" and ".dll" from Cygwin distro
                    if( p != Util.INVALID_INDEX )
                    {
                        final String trimmed = fileName.substring( 0, p ).intern();
                        if( trimmed.isEmpty() == false )
                        {
                            fOsBinNameSet.add( trimmed );
                        }
                    }
                }
            }
        }

        if( PortsConstants.DEBUG ) System.out.print( TheOsBinaries.class.getCanonicalName() +"<init> ms="+ ( System.currentTimeMillis() - startMillisec ) );
    }

    public boolean has( final String binaryName )
    {
        return fOsBinNameSet.contains( binaryName );
    }
}
