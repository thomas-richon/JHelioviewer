package org.helioviewer.jhv.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.helioviewer.jhv.gui.GuiState3DWCS;
import org.helioviewer.viewmodel.view.StandardSolarRotationTrackingView;

/**
 * Action to switch to focus mode.
 * 
 * @author Markus Langenberg
 */
public class ToggleSolarRotationTrackingAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private StandardSolarRotationTrackingView standardSolarRotationTrackingView;
    /**
     * Default constructor.
     */
    public ToggleSolarRotationTrackingAction(boolean startActivated) {
        super("Track");
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e) {
    	standardSolarRotationTrackingView = GuiState3DWCS.mainComponentView.getAdapter(StandardSolarRotationTrackingView.class);
        standardSolarRotationTrackingView.toggleEnabled();
    }    
    
}