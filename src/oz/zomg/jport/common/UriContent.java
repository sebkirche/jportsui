package oz.zomg.jport.common;

import java.net.URI;


/**
 * Uniform Resource Identifier content holder.
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * Creative Commons Attribution-ShareAlike 3.0 Unported License</a>.</SMALL>
 */
public class UriContent
{
    final public URI     fUri;
    final public byte[]  fContentBytes;
    final public boolean fIs404; //... general 'wget' error code
    //... HeaderFields map and MIME types

    /**
     * HTTP Error 404, content not found @ uri
     *
     * @param uri
     */
    UriContent( final URI uri )
    {
        this( uri, null, true );
    }

    UriContent( final URI uri, final byte[] contentBytes )
    {
        this( uri, contentBytes, false );
    }

    private UriContent( final URI uri, final byte[] contentBytes, final boolean is404 )
    {
        fUri = uri;
        fContentBytes = contentBytes;
        fIs404 = is404;
    }
}
