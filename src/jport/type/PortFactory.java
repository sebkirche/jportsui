package jport.type;

import java.util.Arrays;


/**
 *
 * @author sbaber
 */
public class PortFactory
{
    private PortFactory() {}

    /**
     *
     * @param text plain-text entry for the port
     * @return NONE if was not able to parse the line
     */
    static public Portable createFromPortIndexFile( final String text )
    {
        final PortBuilder pb = new PortBuilder( text );
        return ( pb.didParse() == true )
                ? new BsdPort( pb )
                : Portable.NONE;
    }

    /**
     *
     * @param prevPort
     * @param cliVersion
     * @param cliVariants
     * @return
     */
    static public Portable create( final Portable prevPort, final String cliVersion, final String[] cliVariants )
    {
        final boolean isMatch = prevPort.isInstalled() == true
                && prevPort.getVersionInstalled().equals( cliVersion )
//...                && prevPort.getVersion().equals( cliVersion )
                && Arrays.deepEquals( prevPort.getVariantsInstalled(), cliVariants );

        return ( isMatch == true )
                ? prevPort // simply return
                : new InstalledPort // does not match, needs to update
                    ( (BsdPort)prevPort
                    , cliVersion
                    , cliVariants
                    );
    }
}
