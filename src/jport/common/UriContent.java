package jport.common;

import java.net.URI;


/**
 * Content wrapper.
 *
 * @author sbaber
 */
class UriContent
{
    final URI     fUri;
    final byte[]  fContentBytes;
    final boolean fIs404;
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
