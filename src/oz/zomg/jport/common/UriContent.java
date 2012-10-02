package oz.zomg.jport.common;

import java.net.URI;


/**
 * Content wrapper.
 *
 * @author sbaber
 */
public class UriContent
{
    final public URI     fUri;
    final public byte[]  fContentBytes;
    final public boolean fIs404;
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
