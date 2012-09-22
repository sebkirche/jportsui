// Ico.java

// Handles 2-color, 16-color, 256-color, and 32-bit color images of any width
// and height.

// Handles .ico files with either BITMAPHEADERs or PNGs.

package ca.mb.javajeff.ico;

import java.awt.image.*;

import java.io.*;

import java.net.*;

import javax.imageio.*;

public class Ico2
{
    static final private int [] MASKS = { 128, 64, 32, 16, 8, 4, 2, 1 };

   private final static int FDE_OFFSET = 6; // first directory entry offset
   private final static int DE_LENGTH = 16; // directory entry length
   private final static int BMIH_LENGTH = 40; // BITMAPINFOHEADER length

   private byte [] mIcoBytes = new byte [0]; // new byte [0] facilitates read()

   private int mImageCount;

   private BufferedImage [] mBufferedImages;

   private int [] mColorCount;

   public Ico2 (String filename) throws BadIcoResException, IOException
   {
      this (new FileInputStream (filename));
   }

   public Ico2 (URL url) throws BadIcoResException, IOException
   {
      this (url.openStream ());
   }

   public Ico2 (File file) throws BadIcoResException, IOException
   {
      this (file.getAbsolutePath ());
   }

   public Ico2 (InputStream is) throws BadIcoResException, IOException
   {
      try
      {
          read (is);
          parseICOImage ();
      }
      finally
      {
          try
          {
              is.close ();
          }
          catch (IOException ioe)
          {
          }
      }
   }

   public BufferedImage getImage (int index)
   {
      if (index < 0 || index >= mImageCount)
          throw new IllegalArgumentException ("index out of range");

     return mBufferedImages [index];
   }

   public int getNumColors (int index)
   {
      if (index < 0 || index >= mImageCount)
          throw new IllegalArgumentException ("index out of range");

     return mColorCount [index];
   }

   public int getNumImages ()
   {
      return mImageCount;
   }

   static private int calcScanlineBytes (int width, int bitCount)
   {
      // Calculate minimum number of double-words required to store width
      // pixels where each pixel occupies bitCount bits. XOR and AND bitmaps
      // are stored such that each scanline is aligned on a double-word
      // boundary.

      return (((width*bitCount)+31)/32)*4;
   }

   private void parseICOImage () throws BadIcoResException, IOException
   {
      // Check resource type field.

      if (mIcoBytes [2] != 1 || mIcoBytes [3] != 0)
          throw new BadIcoResException ("Not an ICO resource");

      mImageCount = ubyte (mIcoBytes [5]);
      mImageCount <<= 8;
      mImageCount |= mIcoBytes [4];

      mBufferedImages = new BufferedImage [mImageCount];

      mColorCount = new int [mImageCount];

      for (int i = 0; i < mImageCount; i++)
      {
           int width = ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH]);

           int height = ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+1]);

           mColorCount [i] = ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+2]);

           int bytesInRes = ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+11]);
           bytesInRes <<= 8;
           bytesInRes |= ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+10]);
           bytesInRes <<= 8;
           bytesInRes |= ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+9]);
           bytesInRes <<= 8;
           bytesInRes |= ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+8]);

           int imageOffset = ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+15]);
           imageOffset <<= 8;
           imageOffset |= ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+14]);
           imageOffset <<= 8;
           imageOffset |= ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+13]);
           imageOffset <<= 8;
           imageOffset |= ubyte (mIcoBytes [FDE_OFFSET+i*DE_LENGTH+12]);

           if (mIcoBytes [imageOffset] == 40 &&
               mIcoBytes [imageOffset+1] == 0 &&
               mIcoBytes [imageOffset+2] == 0 &&
               mIcoBytes [imageOffset+3] == 0)
           {
               // BITMAPINFOHEADER detected

               int _width = ubyte (mIcoBytes [imageOffset+7]);
               _width <<= 8;
               _width |= ubyte (mIcoBytes [imageOffset+6]);
               _width <<= 8;
               _width |= ubyte (mIcoBytes [imageOffset+5]);
               _width <<= 8;
               _width |= ubyte (mIcoBytes [imageOffset+4]);

               // If width is 0 (for 256 pixels or higher), _width contains
               // actual width.

               if (width == 0)
                   width = _width;

               int _height = ubyte (mIcoBytes [imageOffset+11]);
               _height <<= 8;
               _height |= ubyte (mIcoBytes [imageOffset+10]);
               _height <<= 8;
               _height |= ubyte (mIcoBytes [imageOffset+9]);
               _height <<= 8;
               _height |= ubyte (mIcoBytes [imageOffset+8]);

               // If height is 0 (for 256 pixels or higher), _height contains
               // actual height times 2.

               if (height == 0)
                   height = _height / 2;

               int planes = ubyte (mIcoBytes [imageOffset+13]);
               planes <<= 8;
               planes |= ubyte (mIcoBytes [imageOffset+12]);

               int bitCount = ubyte (mIcoBytes [imageOffset+15]);
               bitCount <<= 8;
               bitCount |= ubyte (mIcoBytes [imageOffset+14]);

               // If colorCount [i] is 0, the number of colors is determined
               // from the planes and bitCount values. For example, the number
               // of colors is 256 when planes is 1 and bitCount is 8. Leave
               // colorCount [i] set to 0 when planes is 1 and bitCount is 32.

               if (mColorCount [i] == 0)
               {
                   if (planes == 1)
                   {
                       if (bitCount == 1)
                           mColorCount [i] = 2;
                       else
                       if (bitCount == 4)
                           mColorCount [i] = 16;
                       else
                       if (bitCount == 8)
                           mColorCount [i] = 256;
                       else
                       if (bitCount != 32 )
                           mColorCount [i] = (int) Math.pow (2, bitCount);
                   }
                   else
                       mColorCount [i] = (int) Math.pow (2, bitCount*planes);
               }

               mBufferedImages [i] = new BufferedImage (width, height,
                                           BufferedImage.TYPE_INT_ARGB);

               // Parse image to image buffer.

               int colorTableOffset = imageOffset+BMIH_LENGTH;

               // sgb ->
               if( mColorCount[ i ] == 16777216 )
               {    // 24 bit color @ "http://opensource.apple.com/favicon.ico"
                   for( int k = i; k < mColorCount.length; k++ ) { mColorCount[ k ] = 16777216; } //?

                   int scanlineBytes = calcScanlineBytes (width, 24);

                   for (int row = 0; row < height; row++)
                        for (int col = 0; col < width; col++)
                        {
                             int rgb = ubyte (mIcoBytes [colorTableOffset+row*
                                                     scanlineBytes+col*4+2]);
                             rgb <<= 8;
                             rgb |= ubyte (mIcoBytes [colorTableOffset+row*
                                                     scanlineBytes+col*4+1]);
                             rgb <<= 8;
                             rgb |= ubyte (mIcoBytes [colorTableOffset+row*
                                                     scanlineBytes+col*4]);

                             mBufferedImages [i].setRGB (col, height-1-row, rgb);
                        }
                   
               }
               else // <- sgb

               if (mColorCount [i] == 2)
               {
                   int xorImageOffset = colorTableOffset+2*4;

                   int scanlineBytes = calcScanlineBytes (width, 1);
                   int andImageOffset = xorImageOffset+scanlineBytes*height;

                   for (int row = 0; row < height; row++)
                        for (int col = 0; col < width; col++)
                        {
                             int index;

                             if ((ubyte (mIcoBytes [xorImageOffset+row*
                                                   scanlineBytes+col/8])
                                 & MASKS [col%8]) != 0)
                                 index = 1;
                             else
                                 index = 0;

                             int rgb = 0;
                             rgb |= (ubyte (mIcoBytes [colorTableOffset+index*4
                                                      +2]));
                             rgb <<= 8;
                             rgb |= (ubyte (mIcoBytes [colorTableOffset+index*4
                                                      +1]));
                             rgb <<= 8;
                             rgb |= (ubyte (mIcoBytes [colorTableOffset+index*
                                                      4]));

                             if ((ubyte (mIcoBytes [andImageOffset+row*
                                                   scanlineBytes+col/8])
                                 & MASKS [col%8]) != 0)
                                 mBufferedImages [i].setRGB (col, height-1-row, rgb);
                             else
                                 mBufferedImages [i].setRGB (col, height-1-row,
                                                0xff000000 | rgb);
                        }
               }
               else
               if (mColorCount [i] == 16)
               {
                   int xorImageOffset = colorTableOffset+16*4;

                   int scanlineBytes = calcScanlineBytes (width, 4);
                   int andImageOffset = xorImageOffset+scanlineBytes*height;

                   for (int row = 0; row < height; row++)
                        for (int col = 0; col < width; col++)
                        {
                             int index;
                             if ((col & 1) == 0) // even
                             {
                                 index = ubyte (mIcoBytes [xorImageOffset+row*
                                                          scanlineBytes+col/2]);
                                 index >>= 4;
                             }
                             else
                             {
                                 index = ubyte (mIcoBytes [xorImageOffset+row*
                                                          scanlineBytes+col/2])
                                                &15;
                             }

                             int rgb = 0;
                             rgb |= (ubyte (mIcoBytes [colorTableOffset+index*4
                                                      +2]));
                             rgb <<= 8;
                             rgb |= (ubyte (mIcoBytes [colorTableOffset+index*4
                                                      +1]));
                             rgb <<= 8;
                             rgb |= (ubyte (mIcoBytes [colorTableOffset+index*
                                                      4]));

                             if ((ubyte (mIcoBytes [andImageOffset+row*
                                                   calcScanlineBytes (width, 1)
                                                   +col/8]) & MASKS [col%8])
                                 != 0)
                                 mBufferedImages [i].setRGB (col, height-1-row, rgb);
                             else
                                 mBufferedImages [i].setRGB (col, height-1-row,
                                                0xff000000 | rgb);
                        }
               }
               else
               if (mColorCount [i] == 256)
               {
                   int xorImageOffset = colorTableOffset+256*4;

                   int scanlineBytes = calcScanlineBytes (width, 8);
                   int andImageOffset = xorImageOffset+scanlineBytes*height;

                   for (int row = 0; row < height; row++)
                        for (int col = 0; col < width; col++)
                        {
                             int index;
                             index = ubyte (mIcoBytes [xorImageOffset+row*
                                                      scanlineBytes+col]);

                             int rgb = 0;
                             rgb |= (ubyte (mIcoBytes [colorTableOffset+index*4
                                                      +2]));
                             rgb <<= 8;
                             rgb |= (ubyte (mIcoBytes [colorTableOffset+index*4
                                                      +1]));
                             rgb <<= 8;
                             rgb |= (ubyte (mIcoBytes [colorTableOffset+index*4
                                                      ]));

                             if ((ubyte (mIcoBytes [andImageOffset+row*
                                                   calcScanlineBytes (width, 1)
                                                   +col/8]) & MASKS [col%8])
                                 != 0)
                                 mBufferedImages [i].setRGB (col, height-1-row, rgb);
                             else
                                 mBufferedImages [i].setRGB (col, height-1-row,
                                                0xff000000 | rgb);
                        }
               }
               else
               if (mColorCount [i] == 0)
               {
                   int scanlineBytes = calcScanlineBytes (width, 32);

                   for (int row = 0; row < height; row++)
                        for (int col = 0; col < width; col++)
                        {
                             int rgb = ubyte (mIcoBytes [colorTableOffset+row*
                                                        scanlineBytes+col*4+3]);
                             rgb <<= 8;
                             rgb |= ubyte (mIcoBytes [colorTableOffset+row*
                                                     scanlineBytes+col*4+2]);
                             rgb <<= 8;
                             rgb |= ubyte (mIcoBytes [colorTableOffset+row*
                                                     scanlineBytes+col*4+1]);
                             rgb <<= 8;
                             rgb |= ubyte (mIcoBytes [colorTableOffset+row*
                                                     scanlineBytes+col*4]);

                             mBufferedImages [i].setRGB (col, height-1-row, rgb);
                        }
               }
           }
           else
           if (mIcoBytes [imageOffset]   == (byte)0x89 &&
               mIcoBytes [imageOffset+1] == 0x50 &&
               mIcoBytes [imageOffset+2] == 0x4e &&
               mIcoBytes [imageOffset+3] == 0x47 &&
               mIcoBytes [imageOffset+4] == 0x0d &&
               mIcoBytes [imageOffset+5] == 0x0a &&
               mIcoBytes [imageOffset+6] == 0x1a &&
               mIcoBytes [imageOffset+7] == 0x0a)
           {
               // PNG detected

               ByteArrayInputStream bais;
               bais = new ByteArrayInputStream (mIcoBytes, imageOffset,
                                                bytesInRes); 
               mBufferedImages [i] = ImageIO.read (bais);
           }
           else
               throw new BadIcoResException ("BITMAPINFOHEADER or PNG "+
                                             "expected");
      }

      mIcoBytes = null; // This array can now be garbage collected.
   }

   private void read (InputStream is) throws IOException
   {
      int bytesToRead;
      while ((bytesToRead = is.available ()) != 0)
      {
         byte [] icoimage2 = new byte [mIcoBytes.length+bytesToRead];
         System.arraycopy (mIcoBytes, 0, icoimage2, 0, mIcoBytes.length);
         is.read (icoimage2, mIcoBytes.length, bytesToRead);
         mIcoBytes = icoimage2;
      }
   }

   static private int ubyte (byte b)
   {
       return b & 0xFF;
//dead      return (b < 0) ? 256+b : b; // Convert byte to unsigned byte.
   }
}
