package jport.gui;

import javax.swing.AbstractListModel;


/**
 * Read-only array for facilitating +100K row JList models.
 *
 * @param <E>
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
