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

    private TheOsBinaries()
    {
        final String[] dirPathNames = ( Util.isOnWindows() == false )
                ? new String[] { "/bin", "/sbin", "/usr/bin", "/usr/sbin" }
                : new String[] { "C:\\cygwin\\bin" };
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
                    final int p = fileName.indexOf( '.' );
                    final String trimmed = ( p == Util.INVALID_INDEX )
                            ? fileName.intern() // expected
                            : fileName.substring( 0, p ).intern();

                    if( trimmed.isEmpty() == false )
                    {   // not the directory header
                        fOsBinNameSet.add( trimmed );
                    }
                }
            }
        }
    }

    public boolean has( final String binaryName )
    {
        return fOsBinNameSet.contains( binaryName );
    }
}
