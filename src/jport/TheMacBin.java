package jport;

import java.util.HashSet;
import java.util.Set;
import jport.common.CliUtil;


/**
 * Where local MacBSD/XNU provides, some Port binaries do not need to be installed.
 * Ex. gperf, unzip, bash
 *
 * @author sbaber
 */
public class TheMacBin
{
    static final public TheMacBin INSTANCE = new TheMacBin();

    static
    {}

    final Set<String> fCiBinNameSet = new HashSet<String>();

    private TheMacBin()
    {
//        if( HAS_PORT_CLI == false ) return StringsUtil_.NO_STRINGS;

        final String[] results = CliUtil.executeCommand( "/bin/ls", "/bin", "/sbin", "/usr/bin", "/usr/sbin" );
        for( final String result : results )
        {
            final String trimmed = result.trim().intern();

            if( trimmed.isEmpty() == false && trimmed.charAt( 0 ) != '/' )
            {   // not the directory header
                fCiBinNameSet.add( trimmed );
            }
        }        
    }

    public boolean has( final String binaryName )
    {
        return fCiBinNameSet.contains( binaryName );
    }
}
