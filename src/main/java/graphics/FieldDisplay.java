package graphics;

import model.BlockField;

import javax.swing.*;
import java.awt.*;


public class FieldDisplay extends JPanel {
    private BlockField model;
    private Color emptyColor = new Color(0x222222);
    private Color blockColor = new Color(0xeeeeee);
    private Color controlColor = new Color(0xee2222);
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (model != null) {
            int cellWidth = getWidth() / model.getWidth();
            int cellHeight = getHeight() / model.getHeight();
    
            g.setColor(emptyColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            for (int i = 0; i < model.getWidth(); i++) {
                for (int j = 0; j < model.getHeight(); j++) {
                    
                    BlockField.Cell.Type type = model.getCell(i, j).type;
                    switch (type) {
                        case BLOCKED:
                            g.setColor(blockColor);
                            break;
                        case CONTROL:
                            g.setColor(controlColor);
                            break;
                        default:
                            g.setColor(emptyColor);
                    }
                    
                    g.fillRect(i * cellWidth, getHeight() - ((j + 1) * cellWidth), cellWidth, cellHeight);
                }
            }
        }
        else {
            g.setColor(emptyColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
    }
    
    public BlockField getModel() {
        return model;
    }
    
    public void setModel(BlockField model) {
        this.model = model;
    }
}
