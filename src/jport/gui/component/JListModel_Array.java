package jport.gui.component;

import javax.swing.AbstractListModel;


/**
 * Read-only array for facilitating +100K row JList models.
 * Note: In addition to rows, a JList can also appear as a matrix or columns.
 *
 * @param <E> element of class type
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JListModel_Array<E> extends AbstractListModel
{
    final private E[] fElements;

    public JListModel_Array( final E[] elements )
    {
        if( elements == null ) throw new NullPointerException();

        fElements = elements;

        // auto called when .setModel(this) -> fireContentsChanged( this, index0, index1 );
    }

    @Override public int getSize() { return fElements.length; }
    @Override public Object getElementAt( int index ) { return fElements[ index ]; }
}
