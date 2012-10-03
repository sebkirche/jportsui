package oz.zomg.jport;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import oz.zomg.jport.common.StringsUtil_;
import oz.zomg.jport.common.Util;


/**
 * Where local MacBSD/XNU provides, some Port binaries do not need to be installed.
 * Ex. gperf, unzip, bash
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-nd/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US">
 * Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License</a>.</SMALL>
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
        final long startMillisec = System.currentTimeMillis();

        if( Util.isOnMac() == true )
        {   // 7 ms. vs 1800 ms. of Files.listFiles
            String str = "";
            try
            {
                final byte[] bytes = Util.retreiveResourceBytes( "/oz/zomg/jport/mac-10-6-native-bin.txt" );
                str = new String( bytes );
            }
            catch( IOException ex )
            {}

            final String[] names = StringsUtil_.fastSplits( str, '\n', ' ' );
            for( final String name : names )
            {
                fOsBinNameSet.add( name );
            }
        }
        else
        {
            final String[] dirPathNames = ( Util.isOnWindows() == true )
                    ? new String[] { "C:\\cygwin\\bin", "C:\\cygwin\\lib" }
                    : new String[] // BSD
                            { "/bin"
                            , "/sbin"
                            , "/usr/bin"
                            , "/usr/sbin"
                            , "/usr/lib"
                            , "/usr/X11/bin"
                            , "/usr/X11/lib"
                            // , "/usr/libexec/apache2"
                            // , "/Developer/usr/bin/"
                            };

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
                        fOsBinNameSet.add( fileName );

                        final int p = fileName.indexOf( '.' ); // remove ".exe" and ".dll" from Cygwin distro
                        if( p != Util.INVALID_INDEX )
                        {
                            final String trimmed = fileName.substring( 0, p );
                            if( trimmed.isEmpty() == false )
                            {
                                fOsBinNameSet.add( trimmed );
                            }
                        }
                    }
                }
            }
        }

        if( PortConstants.DEBUG ) System.out.print( TheOsBinaries.class.getCanonicalName() +"<init> ms="+ ( System.currentTimeMillis() - startMillisec ) );
    }

    public boolean has( final String binaryName )
    {
        return fOsBinNameSet.contains( binaryName );
    }
}
