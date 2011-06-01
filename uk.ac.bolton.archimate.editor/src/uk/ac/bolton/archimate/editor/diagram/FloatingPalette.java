/*******************************************************************************
 * Copyright (c) 2011 Bolton University, UK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 *******************************************************************************/
package uk.ac.bolton.archimate.editor.diagram;

import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import uk.ac.bolton.archimate.editor.preferences.Preferences;
import uk.ac.bolton.archimate.editor.ui.IArchimateImages;
import uk.ac.bolton.archimate.editor.utils.StringUtils;


/**
 * Floating Palette for an Editor
 * 
 * @author Phillip Beauvoir
 */
public class FloatingPalette {

    private IDiagramModelEditor fEditor;
    private Shell fShell;
    private Composite fClient;
    private PalettePage fPalettePage;
    
    /**
     * State of the Palette
     */
    public static class PaletteState {
        public Rectangle bounds = new Rectangle(600, 150, 180, 750);
        public boolean isOpen = true;
        public boolean isTranslucent = true;
    }
    
    private PaletteState fPaletteState = new PaletteState();
    
    public FloatingPalette(IDiagramModelEditor editor) {
        fEditor = editor;
        loadState(); // Need to do this now in order to get state
    }
    
    public void open() {
        loadState(); // Need to do this now in order to get bounds

        if(fShell == null || fShell.isDisposed()) {
            createShell();
        }
        
        fShell.open();
        fPaletteState.isOpen = true;
    }
    
    public void close() {
        if(fShell != null && !fShell.isDisposed()) {
            saveState(fShell); // Don't call this in DisposeListener as on Linux getBounds() returns bogus info
            fShell.dispose();
        }
    }
    
    public boolean isOpen() {
        return fShell != null && !fShell.isDisposed();
    }
    
    private void createShell() {
        Shell parentShell = Display.getCurrent().getActiveShell();
        
        fShell = new Shell(parentShell, SWT.TOOL | SWT.RESIZE | SWT.CLOSE);
        
        if(fPaletteState.isTranslucent) {
            fShell.setAlpha(210);
        }
        
        checkSafeBounds(parentShell);
        fShell.setBounds(fPaletteState.bounds);
        
        fShell.setImage(IArchimateImages.ImageFactory.getImage(IArchimateImages.ICON_APP_16));
        fShell.setText("Palette");
        
        // Disposed by system
        fShell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if(fClient != null) {
                    fClient.dispose();
                }
                if(fPalettePage != null) {
                    fPalettePage.dispose();
                }
                fShell = null;
            }
        });
        
        // Closed by user
        fShell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                fPaletteState.isOpen = false;
                saveState(fShell); // Don't call this in DisposeListener as on Linux getBounds() returns bogus info
            }
        });
        
        fShell.setLayout(new FillLayout());
        
        fClient = new Composite(fShell, SWT.NONE);
        fClient.setLayout(new FillLayout());
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        fClient.setLayoutData(gd);
        
        fPalettePage = (PalettePage)fEditor.getAdapter(PalettePage.class);
        fPalettePage.createControl(fClient);
    }
    
    /**
     * Ensure the bounds of the palette are reasonable
     */
    private void checkSafeBounds(Shell parent) {
        Rectangle parentBounds = parent.getBounds();
        Rectangle paletteBounds = fPaletteState.bounds;
        
        if(paletteBounds.x >= parentBounds.x + parentBounds.width) {
            paletteBounds.x = parentBounds.x + parentBounds.width - 150;
        }
        if(paletteBounds.y >= parentBounds.y + parentBounds.height) {
            paletteBounds.y = parentBounds.y + parentBounds.height - 750;
        }
        if(paletteBounds.width > 800) {
            paletteBounds.width = 150;
        }
        if(paletteBounds.height > 1500) {
            paletteBounds.height = 750;
        }
    }

    public PaletteState getPaletteState() {
        return fPaletteState;
    }

    private void saveState(Shell shell) {
        Rectangle bounds = shell.getBounds();
        String s = "" + bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height + "," + fPaletteState.isOpen;
        Preferences.STORE.setValue("pallete_floater_state", s);
    }
    
    private void loadState() {
        String s = Preferences.STORE.getString("pallete_floater_state");
        if(StringUtils.isSet(s)) {
            try {
                String[] bits = s.split(",");
                if(bits.length == 5) {
                    fPaletteState.bounds.x = Integer.valueOf(bits[0]);
                    fPaletteState.bounds.y = Integer.valueOf(bits[1]);
                    fPaletteState.bounds.width = Integer.valueOf(bits[2]);
                    fPaletteState.bounds.height = Integer.valueOf(bits[3]);
                    fPaletteState.isOpen = Boolean.parseBoolean(bits[4]);
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
