package learning;

import main.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class Network implements Serializable {
    private List<Double> input = new ArrayList<>();
    private List<List<Node>> nodes = new ArrayList<>();
    private int inSize;
    private int outSize;
    private int layersCount;
    private int generation;
    
    
    public Network(Network sample, double rnd) {
        this.inSize = sample.inSize;
        this.outSize = sample.outSize;
        this.layersCount = sample.layersCount;
        this.generation = sample.generation + 1;
    
        for (int i = 0; i < layersCount; i++) {
            nodes.add(new ArrayList<>());
        }
    
        for (int i = 0; i < inSize; i++) {
            input.add(0.0);
        }
    
        //Create nodes
        for (int i = 0; i < layersCount; i++) {
            for (int j = 0; j < Math.round(outSize * (i / (layersCount - 1.0)) + inSize * (1 - (i / (layersCount - 1.0)))); j++){
                nodes.get(i).add(new Node(sample.getNode(i, j), rnd, i));
            }
        }
    
        //Connect node layers
        for (int i = 0; i < nodes.size() - 1; i++){
            int lenI = nodes.get(i).size();
            for (int j = 0; j < lenI; j++){
                int lenI1 = nodes.get(i + 1).size();
                for (int k = 0; k < lenI1; k++){
                    getNode(i, j).connectTo(getNode(i + 1, k), sample.getNode(i, j).getOutLinks().get(k), rnd);
                }
            }
        }
    }
    
    public Network(int generation, double rnd, int inSize, int outSize, int hiddenLayers, Node.ActivationType activationFunction) {
        this.inSize = inSize;
        this.outSize = outSize;
        this.layersCount = hiddenLayers + 2;
        this.generation = generation;
    
        for (int i = 0; i < layersCount; i++) {
            nodes.add(new ArrayList<>());
        }
        
        for (int i = 0; i < inSize; i++) {
            input.add(0.0);
        }
    
        //Create nodes
        for (int i = 0; i < layersCount; i++) {
            for (int j = 0; j < Math.round(outSize * (i / (layersCount - 1.0)) + inSize * (1 - (i / (layersCount - 1.0)))); j++){
                if (i == 0){
                    nodes.get(i).add(new Node(rnd, i, Node.Type.INPUT));
                }
                else if (i == layersCount - 1){
                    nodes.get(i).add(new Node(rnd, i, Node.Type.OUTPUT));
                }
                else {
                    nodes.get(i).add(new Node(rnd, i, activationFunction));
                }
            }
        }
    
        //Connect node layers
        for (int i = 0; i < nodes.size() - 1; i++){
            int lenI = nodes.get(i).size();
            for (int j = 0; j < lenI; j++){
                int lenI1 = nodes.get(i + 1).size();
                for (int k = 0; k < lenI1; k++){
                    getNode(i, j).connectTo(getNode(i + 1, k), rnd);
                }
            }
        }
    }
    
    private Node getNode(int layer, int index) {
        return nodes.get(layer).get(index);
    }
    
    public List<Double> getInput() {
        return input;
    }
    
    public void setInput(List<Double> input) {
        if (input.size() != inSize) {
            throw new IllegalArgumentException("Size of the input doesn't match the network's");
        }
        for (int i = 0; i < input.size(); i++) {
            this.input.set(i, input.get(i));
        }
    }
    
    public List<List<Node>> getNodes() {
        return nodes;
    }
    
    public void process() {
        for (int i = 0; i < inSize; i++) {
            getNode(0, i).setInput(input.get(i));
        }
    
        for (int i = 0; i < layersCount; i++) {
            for (int j = 0; j < nodes.get(i).size(); j++) {
                getNode(i, j).updateOutLinks();
            }
        }
    }
    
    public void reset() {
        for (int i = 0; i < layersCount; i++) {
            for (int j = 0; j < nodes.get(i).size(); j++) {
                getNode(i, j).setInput(0.0);
            }
        }
    }
    
    public int getIndexOfMax() {
        double max = getNode(layersCount - 1, 0).getOutput();
        int maxIndex = 0;
        for (int i = 0; i < outSize; i++) {
            double val = getNode(layersCount - 1, i).getOutput();
            if (val > max) {
                max = val;
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    public int getLayersCount() {
        return layersCount;
    }
    
    public int getGeneration() {
        return generation;
    }
    
    public int getInSize() {
        return inSize;
    }
    
    public int getOutSize() {
        return outSize;
    }
    
    public static class Node implements Serializable {
        public static final BiFunction<Double, Boolean, Double> SIGMOID = (x, d) -> d ? (Math.exp(x) / Math.pow(1 + Math.exp(x), 2)) : (x > 20 ? 1.0 : x < -20 ? 0.0 :(1 / (1 + Math.exp(-x))));
        public static final BiFunction<Double, Boolean, Double> RELU = (x, d) -> d ? (x > 0.0 ? 1.0 : 0.0) : (x > 0.0 ? x : 0.0);
        public static final BiFunction<Double, Boolean, Double> LRELU = (x, d) -> d ? (x > 0.0 ? 1.0 : 0.01) : (x > 0.0 ? x : 0.01 * x);
        public static final BiFunction<Double, Boolean, Double> SOFTPLUS = (x, d) -> d ? SIGMOID.apply(x, false) : (x > 20 ? x : (x < -20 ? 0.0 : (Math.log1p(Math.exp(x)))));
    
        private static final List<BiFunction<Double, Boolean, Double>> functions = new ArrayList<>(Arrays.asList(SIGMOID, RELU, LRELU, SOFTPLUS));
        
        public enum ActivationType {
            SIGMOID,
            RELU,
            LRELU,
            SOFTPLUS
        }
        
        public enum Type {
            INPUT,
            OUTPUT,
            HIDDEN
        }
        
        private int layer;
        private Type type;
        private ActivationType activationFunctionType;
        private double bias = 0;
        private List<Link> inLinks = new ArrayList<>();
        private List<Link> outLinks = new ArrayList<>();
        private double input;
    
        public Node(int layer, ActivationType activationFunctionType) {
            this.layer = layer;
            this.type = Type.HIDDEN;
            this.activationFunctionType = activationFunctionType;
            this.bias = 0;
        }
    
        public Node(double rnd, int layer, ActivationType activationFunctionType) {
            this.layer = layer;
            this.type = Type.HIDDEN;
            this.activationFunctionType = activationFunctionType;
            this.bias = Utils.randomDouble(-rnd, rnd);
        }
    
        public Node(int layer, Type type) {
            this.layer = layer;
            this.type = type;
            this.activationFunctionType = ActivationType.LRELU;
            this.bias = 0;
        }
        
        public Node(double rnd, int layer, Type type) {
            this.layer = layer;
            this.type = type;
            this.activationFunctionType = ActivationType.LRELU;
            this.bias = 0;
        }
    
        public Node(Node sample, double rnd, int layer) {
            this.layer = layer;
            this.activationFunctionType = sample.activationFunctionType;
            this.bias = sample.bias + Utils.randomDouble(-rnd, rnd);
            this.type = sample.type;
        }
        
        public Link connectTo(Node target, Link sample, double rnd) {
            if (target == null) {
                throw new NullPointerException();
            }
            Link link;
            if (sample == null) {
                link = new Link(rnd);
            }
            else {
                link = new Link(sample, rnd);
            }
            this.outLinks.add(link);
            target.inLinks.add(link);
            return link;
        }
    
        public Link connectTo(Node target, double rnd) {
            if (target == null) {
                throw new NullPointerException();
            }
            Link link;
            
            link = new Link(rnd);
            
            this.outLinks.add(link);
            target.inLinks.add(link);
            return link;
        }
        
        public double getOutput() {
            double val = input + bias;
            for (int i = 0; i < inLinks.size(); i++) {
                val += inLinks.get(i).getOutput();
            }
            /*AtomicReference<Double> inRef = new AtomicReference<>(this.input);
            inLinks.forEach(l -> inRef.updateAndGet(v -> (v + l.getOutput())));*/
            switch (type) {
                case INPUT:
                    return input;
                case OUTPUT:
                    return val;
                default:
                    return getFunction(activationFunctionType).apply(val, false);
            }
        }
        
        public void updateOutLinks() {
            double value = getOutput();
            outLinks.forEach(l -> l.setInput(value));
        }
    
        public static BiFunction<Double, Boolean, Double> getFunction(ActivationType type) {
            switch (type) {
                case RELU:
                    return RELU;
                case LRELU:
                    return LRELU;
                case SIGMOID:
                    return SIGMOID;
                case SOFTPLUS:
                    return SOFTPLUS;
                default:
                    return (x, b) -> b ? 1 : x;
            }
        }
        
        public int getLayer() {
            return layer;
        }
    
        public void setLayer(int layer) {
            this.layer = layer;
        }
    
        public Type getType() {
            return type;
        }
    
        public void setType(Type type) {
            this.type = type;
        }
    
        public ActivationType getActivationFunctionType() {
            return activationFunctionType;
        }
    
        public void setActivationFunctionType(ActivationType activationFunctionType) {
            this.activationFunctionType = activationFunctionType;
        }
    
        public double getBias() {
            return bias;
        }
    
        public void setBias(double bias) {
            this.bias = bias;
        }
    
        public List<Link> getInLinks() {
            return inLinks;
        }
    
        public void setInLinks(List<Link> inLinks) {
            this.inLinks = inLinks;
        }
    
        public List<Link> getOutLinks() {
            return outLinks;
        }
    
        public void setOutLinks(List<Link> outLinks) {
            this.outLinks = outLinks;
        }
    
        public double getInput() {
            return input;
        }
    
        public void setInput(double input) {
            this.input = input;
        }
    }
    
    public static class Link implements Serializable {
        private double weight = 0;
        private double input;
        
        public Link() {
            weight = 0;
        }
        
        public Link(double rnd) {
            weight = Utils.randomDouble(-rnd, rnd);
        }
        
        public Link(Link sample, double rnd) {
            weight = sample.getWeight() + Utils.randomDouble(-rnd, rnd);
        }
    
        public double getWeight() {
            return weight;
        }
    
        public void setWeight(double weight) {
            this.weight = weight;
        }
        
        public void setInput(double val){
            this.input = val;
        }
	
	    public double getOutput(){
            return this.input * this.weight;
        }
    }
}
