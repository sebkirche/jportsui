package jport.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jport.PortsHistogramFactory;
import jport.PortsHistogramFactory.EHistogram;
import jport.TheApplication;
import jport.common.Histogram_;
import jport.gui.table.TableModel_Histogram;
import jport.type.Portable;
import jport.type.Portable.Predicatable;


/**
 * Create Histogram tables that filter the displayed Ports when a row is selected.
 *
 *
 * @author sbaber
 */
public class HistogramUiFactory
{
    /**
     * Not a JPanel because certain Components are needed elsewhere, ex. Categories table.
     */
    private HistogramUiFactory() {}

    /**
     * Formerly, Categories was a "top-level" tab.
     *
     * @return index [0]=scroll pane of histo, [1]=Any button
     */
    static private Component[] createCategoriesComponent()
    {
        final AbstractButton ab_Any = new JButton( "Any" );
        return new Component[] { createComponent( ab_Any, EHistogram.Categories ), ab_Any };
    }

    /**
     * @param ab_Any the "Any" button clears the table selection
     * @param histoEnum
     * @return JScrollPane containing a JTable of instances and their frequency
     */
    static public Component createComponent( final AbstractButton ab_Any, final EHistogram histoEnum )
    {
        final Histogram_<String> histogram  = new Histogram_<String>( String.class );
        for( final Portable port : TheApplication.INSTANCE.getPortsCatalog().getAllPorts() )
        {
            for( final String key : histoEnum.transform( port ) )
            {
                histogram.increment( key );
            }
        }

        final TableModel_Histogram tmh = new TableModel_Histogram();
        tmh.setRows( histogram.getFrequencyKeyEntries() );

        // listener
        final PrivateListener privateListener = new PrivateListener( tmh, histoEnum );
        ab_Any.addActionListener( privateListener ); // leaks

        final ListSelectionModel lsm = tmh.getJTable().getSelectionModel();
        lsm.addListSelectionListener( privateListener ); //ENHANCE ATableModel.this.addListSelectionListener(...)
        lsm.addListSelectionListener( new ListSelectionListener() // anonymous class
                {   @Override public void valueChanged( ListSelectionEvent e )
                    {   if( e.getValueIsAdjusting() == false )
                        {   // waits until selection stops changing via arrow keys or mouse stabilizes
                            ab_Any.setEnabled( lsm.isSelectionEmpty() == false );
                        }
                    }
                } );

        return tmh.getJScrollPane();
    }


    // ================================================================================
    /**
     * handles Histogram instantiations table selections.
     */
    static private class PrivateListener
        implements 
              ListSelectionListener
            , ActionListener
    {
        final private TableModel_Histogram fTableModel_Histogram;
        final private EHistogram fHistoEnum;

        private PrivateListener( final TableModel_Histogram tmh, final EHistogram histoEnum )
        {
            fTableModel_Histogram = tmh;
            fHistoEnum = histoEnum;
        }

        @Override public void actionPerformed( final ActionEvent e )
        {
            final Object obj = e.getSource();
            if( obj instanceof AbstractButton )
            {
                fTableModel_Histogram.clearSelection();
            }
        }

        @Override public void valueChanged( ListSelectionEvent e )
        {
            if( e.getValueIsAdjusting() == false )
            {   // user stopped doing mouse drag changes
                final Predicatable predicate;

                final ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if( lsm.isSelectionEmpty() == false )
                {
                    final Map.Entry<?,?> entry = fTableModel_Histogram.getSingleSelection();
                    final String search = entry.getValue().toString(); // context
                    predicate = PortsHistogramFactory.createPredicate( fHistoEnum, search );
                }
                else
                {
                    predicate = Predicatable.ANY;
                }

                TheUiHolder.INSTANCE.getPortFilterPredicate().setHistoFilter( predicate );
            }
            // else user still dragging
        }
    }
}
