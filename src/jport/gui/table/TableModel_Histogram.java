package jport.gui.table;

import java.util.Map.Entry;
import jport.common.Providers_.ClassProvidable;
import jport.common.Providers_.WidthProvidable;
import jport.common.gui.AEnumTableModel_Array;


/**
 * Shows occurrence count of an item.
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class TableModel_Histogram extends AEnumTableModel_Array<Entry,TableModel_Histogram.EColumn>
{
    static enum EColumn implements WidthProvidable, ClassProvidable
            { NAME  ( -1 ) { @Override public Class provideClass() { return String.class; } }
            , TOTAL ( 64 ) { @Override public Class provideClass() { return Integer.class; } } // 64 is the smallest with Mac-PLAF
            ;
                    private EColumn( final int width ) { fWidth = width; }
                    private int fWidth;
                    @Override public int provideWidth() { return fWidth; }
            }

    static
    {}

    public TableModel_Histogram()
    {
        super
            ( Entry.class
            , EEditable.DISABLE
            , ERowSelection.SINGLE_SELECTION
            , EColorize.ALT_BAR_2
            , EColumn.values()
            );
    }

    /**
     *
     * @param entry row
     * @param columnEnum
     * @return placed in table
     */
    @Override public Object getValueOf( final Entry entry, final EColumn columnEnum )
    {
        switch( columnEnum )
        {
            case NAME  : return entry.getValue();
            case TOTAL : return entry.getKey();

            default : return "ERR";
        }
    }
}
