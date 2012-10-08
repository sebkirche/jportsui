package oz.zomg.jport.common;

import ca.mb.javajeff.Ico;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;


/**
 * Handle the weird, special casing for Web based "favorite icon" images.
 * <H3><I><FONT color="#770000">Subset of original source.</FONT></I></H3>
 *
 * @author <SMALL>Copyright 2012 by Stephen Baber
 * &nbsp; <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * <img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/80x15.png" /></a><br />
 * This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/deed.en_US">
 * Creative Commons Attribution-ShareAlike 3.0 Unported License</a>.</SMALL>
 */
public class ImageUtil_
{
    static final private String _DOT_ICO_EXTENSION = ".ico";

    static
    {
//        In informal testing, "nl.ikarus.nxt" code resulted in better performance but more parse errors.
//        System.setProperty("nl.ikarus.nxt.priv.imageio.icoreader.autoselect.icon","true");
//        nl.ikarus.nxt.priv.imageio.icoreader.lib.ICOReaderSpi.registerIcoReader();
    }

    private ImageUtil_() {}

    /**
     *
     * @param resourceName
     * @param bytes
     * @return "nl.ikarus.nxt" results
     */
    static private Image _parseImage( final String resourceName, final byte[] bytes )
    {
        if( bytes.length == 0 ) return null;

        if( resourceName.toLowerCase().endsWith( _DOT_ICO_EXTENSION ) == false )
        {   // Java can make a good show of JPEG, GIF, PNG, etc.
            return Toolkit.getDefaultToolkit().createImage( bytes );
        }
        else
        {
            try
            {   // use registered ICO plug-in @ nl.ikarus.nxt.priv.imageio.icoreader.lib.ICOReader
                return ImageIO.read( new ByteArrayInputStream( bytes ) ); // [] wrapper does not need to be closed
            }
            catch( IOException ex )
            {   // no point in handing off to Toolkit as the .ICO plug-in will intercept there also
                return null;
            }
        }
    }

    /**
     *
     * @param internallyJarredResourceName should be prefixed with '/' to load from this jar / class files
     * @return if not found, throws IllegalArgumentException
     */
    static public Image parseImage( final String internallyJarredResourceName )
    {
        try
        {
            final byte[] bytes = Util.retrieveResourceBytes( internallyJarredResourceName );
            return parseImage( internallyJarredResourceName, bytes );
        }
        catch( IOException ex )
        {
            throw new IllegalArgumentException( internallyJarredResourceName +" NOT FOUND IN CLASS PATH" );
        }
    }

    //ENHANCE
    /**
     * Special case Java's borken ".ICO" handling and the web's sloppy adherence to W3C standards.
     *
     * @param name used to check file extension when MIME type info is lost, ".ico", ".png", ".gif", etc.
     * @param bytes content @ URI
     * @return 'null' if no image
     */
    static public Image parseImage( final String name, final byte[] bytes )
    {
        if( bytes.length == 0 ) return null;

        if( name.toLowerCase().endsWith( _DOT_ICO_EXTENSION ) == false )
        {   // Java can make a good show of JPEG, GIF, PNG, etc.
            return Toolkit.getDefaultToolkit().createImage( bytes );
        }
        else
        {   // special cased, AWT tends to fail with transparent ".ICO".  Bug not completely fixed as of JDK7.07.
            try
            {   // generally a Microsoft .ICO image
                final Ico ico = new Ico( bytes ); // THROWS BadIcoResException

                Image largestImage = null;
                int largestPerimeter = 0;
                for( int i = 0; i < ico.getNumImages(); i++ )
                {   // find the largest in the series
                    final Image image = ico.getImage( i );
                    final int perimeter = image.getHeight( null ) + image.getWidth( null );
                    if( perimeter > largestPerimeter )
                    {
                        largestPerimeter = perimeter;
                        largestImage = image;
                    }
                }
                return largestImage;
            }
            catch( IOException ex )
            {   // this exception won't happen as the byte[] is simply wrapped, see Guava for a solution
                return null;
            }
            catch( Ico.BadIcoResException ex )
            {   // Web sites generally, but not always serve up an actual Microsoft .ICO, however, sometimes
                // it is a ".jpeg" or ".png" under the ".ico" extension, ex. "http://www.gnu.org/favicon.ico"
                return Toolkit.getDefaultToolkit().createImage( bytes );
            }
        }
    }

    //ENHANCE?
    /**
     * @param maxWidthHeight
     * @param image can be 'null'
     * @return original if smaller than max pixels
     */
    static public Image reduceImage( final Image image, final int maxWidthHeight )
    {
        if( image == null ) return image;

        final int width  = image.getWidth ( null );
        final int height = image.getHeight( null );
        if( width <= maxWidthHeight && height <= maxWidthHeight && width > 0 && height > 0 )
        {
            return image;
        }
        else
        {
            return image.getScaledInstance //... might not be square (1x1) aspect-ratio
                    ( Math.min( width,  maxWidthHeight )
                    , Math.min( height, maxWidthHeight )
                    , Image.SCALE_SMOOTH
                    );

// more work to do it this way...
//            final BufferedImage scaledBi = new BufferedImage( maxWidthHeight, maxWidthHeight, BufferedImage.TYPE_INT_ARGB );
//            final Graphics2D g2d = scaledBi.createGraphics();
//            g2d.drawImage( image, 0, 0, maxWidthHeight, maxWidthHeight, null );
//            return scaledBi;
        }
    }
}
