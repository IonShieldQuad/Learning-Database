package model;


import javafx.scene.control.Cell;
import learning.Network;
import learning.NetworkCollection;
import main.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BlockField {
    private List<List<Cell>> field = new ArrayList<>();
    private int width;
    private int height;
    private int controlPosition;
    
    private double maxDensity;
    private double distribution;
    
    private boolean finished = false;
    private NetworkCollection.NetworkData data;
    
    public static final int ITERATIONS_MAX = 10000;
    
    public BlockField(int height, int width, double maxDensity, double distribution) {
        this.width = width;
        this.height = height;
        this.maxDensity = maxDensity;
        this.distribution = distribution;
        for (int i = 0; i < width; i++) {
            field.add(new ArrayList<>());
            for (int j = 0; j < height; j++) {
                field.get(i).add(new Cell(Cell.Type.EMPTY));
            }
        }
        int half = width / 2;
        field.get(half).get(0).type = Cell.Type.CONTROL;
        controlPosition = half;
    }
    
    private boolean swap(int c0, int r0, int c1, int r1) {
        Cell cell0 = field.get(c0).get(r0);
        Cell cell1 = field.get(c1).get(r1);
        
        if ((cell0.type == Cell.Type.CONTROL && cell1.type == Cell.Type.BLOCKED) ||(cell1.type == Cell.Type.CONTROL && cell0.type == Cell.Type.BLOCKED)) {
            return false;
        }
        field.get(c0).set(r0, cell1);
        field.get(c1).set(r1, cell0);
        return true;
    }
    
    public boolean tick(boolean incrementScore) {
        if (finished) {
            return false;
        }
        
        List<Double> inputs = new ArrayList<>(width * height);
        //Gather input data
        for (int i = 0; i < width; i++) {
            int sign = i == 0 ? 0 : i % 2 == 0 ? -1 : 1;
            int index = Utils.wrap(controlPosition + sign * ((i + 1) / 2), width);
    
            for (int j = 0; j < height; j++) {
                /*if (i != controlPosition && j != 0) {*/
                    inputs.add(field.get(index).get(j).type == Cell.Type.BLOCKED ? 10.0 : -10.0);
                /*}*/
            }
        }
    
        int result = 0;
        if (data != null && data.network != null) {
            data.network.reset();
            data.network.setInput(inputs);
            data.network.process();
            result = data.network.getIndexOfMax();
        }
        
        //Try to move if desired
        switch (result) {
            case 1: {
                boolean success = swap(controlPosition, 0, Utils.wrap(controlPosition - 1, width), 0);
                controlPosition = Utils.wrap(controlPosition - 1, width);
                if (!success) {
                    if (data != null && incrementScore) {
                        data.runs++;
                    }
                    finished = true;
                    return false;
                }
                break;
            }
            case 2: {
                boolean success = swap(controlPosition, 0, Utils.wrap(controlPosition + 1, width), 0);
                controlPosition = Utils.wrap(controlPosition + 1, width);
                if (!success) {
                    if (data != null && incrementScore) {
                        data.runs++;
                    }
                    finished = true;
                    return false;
                }
                break;
            }
        }
    
        //Everything falls down
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height - 1; j++) {
                //If block is trying to fall onto control cell, the run is over
                if (field.get(i).get(j).type == Cell.Type.CONTROL) {
                    if (field.get(i).get(j + 1).type == Cell.Type.BLOCKED) {
                        if (data != null && incrementScore) {
                            data.runs++;
                        }
                        finished = true;
                        return false;
                    }
                }
                else {
                    boolean success = swap(i, j, i, j + 1);
                    if (!success) {
                        if (data != null && incrementScore) {
                            data.runs++;
                        }
                        finished = true;
                        return false;
                    }
                }
            }
        }
        
        //Generate new row
        List<Cell> newRow = generateBlocks(width, maxDensity, distribution);
        for (int i = 0; i < width; i++) {
            field.get(i).set(height - 1, newRow.get(i));
        }
        
        //If there were no invalid moves, increment score
        if (data != null && incrementScore) {
            data.score++;
        }
        return true;
    }
    
    /** Reset the field and run until finished */
    public double fullRun(boolean incrementScore, Consumer<Double> onTick) {
        reset();
        double score = 0;
        while (tick(incrementScore) && score < ITERATIONS_MAX) {
            score++;
            if (onTick != null) {
                onTick.accept(score);
            }
        }
        return score;
    }
    
    private List<Cell> generateBlocks(int width, double maxDensity, double distribution) {
        int limit = (int)Math.round(width * maxDensity * Math.pow(Utils.randomDouble(0.0, 1.0), distribution));
        List<Cell> list = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            list.add(new Cell(i < limit ? Cell.Type.BLOCKED : Cell.Type.EMPTY));
        }
        Collections.shuffle(list);
        return list;
    }
    
    public void reset() {
        for (int i = 0; i < field.size(); i++) {
            for (int j = 0; j < field.get(i).size(); j++) {
                field.get(i).set(j, new Cell(Cell.Type.EMPTY));
            }
        }
        int half = width / 2;
        field.get(half).get(0).type = Cell.Type.CONTROL;
        controlPosition = half;
        finished = false;
    }
    
    public Cell getCell(int col, int row) {
        return field.get(col).get(row);
    }
    
    public List<List<Cell>> getField() {
        return field;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public NetworkCollection.NetworkData getData() {
        return data;
    }
    
    public void setData(NetworkCollection.NetworkData data) {
        this.data = data;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public double getDistribution() {
        return distribution;
    }
    
    public void setDistribution(double distribution) {
        this.distribution = distribution;
    }
    
    public double getMaxDensity() {
        return maxDensity;
    }
    
    public void setMaxDensity(double maxDensity) {
        this.maxDensity = maxDensity;
    }
    
    public static class Cell {
        public enum Type {
            EMPTY,
            BLOCKED,
            CONTROL
        }
        public Type type;
        
        public Cell(Type type) {
            this.type = type;
        }
    }
}
