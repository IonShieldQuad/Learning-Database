package model;

import graphics.FieldDisplay;
import learning.Network;
import learning.NetworkCollection;
import main.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GroupController implements Serializable {
    private NetworkCollection collection;
    private transient FieldDisplay display;
    private int width;
    private int height;
    private Network.Node.ActivationType activationFunctionType;
    private double maxDensity;
    private double distribution;
    private int collectionSize;
    private int runsLimit;
    private int hiddenLayers;
    private double random = 1.0;
    
    Supplier<Network> init;
    Function<NetworkCollection.NetworkData, Network> newGen;
    
    public GroupController(int width, int height, int hiddenLayers, Network.Node.ActivationType activationFunctionType, double maxDensity, double distribution, int collectionSize, int runsLimit) {
        this.width = width;
        this.height = height;
        this.activationFunctionType = activationFunctionType;
        this.maxDensity = maxDensity;
        this.distribution = distribution;
        this.collectionSize = collectionSize;
        this.runsLimit = runsLimit;
        this.hiddenLayers = hiddenLayers;
        
        init = (Supplier<Network> & Serializable)() -> new Network(0, Utils.randomDouble(0, random * collectionSize), (Utils.RADIUS * 2 + 1) * (Utils.RADIUS * 2 + 1), 3, hiddenLayers, activationFunctionType);
        newGen = (Function<NetworkCollection.NetworkData, Network> & Serializable)(d) -> new Network(d.network, random);
        
        collection = new NetworkCollection(0, collectionSize, init, newGen);
    }
    
    public void reset() {
        collection = new NetworkCollection(0, collectionSize, init, newGen);
    }
    
    public int getIteration() {
        return collection.getIteration();
    }
    
    public void train(int iterations, int timeoutSeconds, Consumer<Integer> onNewGeneration, Consumer<Void> onFinish) throws InterruptedException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        
        List<BlockField> fields = new ArrayList<>(collectionSize);
        for (int j = 0; j < collectionSize; j++) {
            fields.add(new BlockField(height, width, maxDensity, distribution));
        }
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            collection.resetMaxScore();
            futures.clear();
            //Start runs
            for (int j = 0; j < collectionSize; j++) {
                BlockField field = fields.get(j);
                NetworkCollection.NetworkData data = collection.getData().get(j);
                Future<Void> future = executor.submit(() -> {
                    field.setData(data);
                    for (int k = 0; k < runsLimit; k++) {
                        field.fullRun(true, null);
                    }
                    return null;
                });
                futures.add(future);
            }
            //Await completion
            for (Future<Void> future : futures) {
                try {
                    future.get(timeoutSeconds, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            
            collection.nextIteration();
            if (onNewGeneration != null) {
                onNewGeneration.accept(collection.getIteration());
            }
        }
        
        executor.shutdown();
        executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
        if (onFinish != null) {
            onFinish.accept(null);
        }
    }
    
    public double getMaxAvgScore() {
        return collection.getMaxScore();
        //Optional<Double> max = collection.getData().stream().map(NetworkCollection.NetworkData::average).max(Comparator.naturalOrder());
        //return max.orElse(0.0);
    }
    
    public Future<Double> startDisplayRun(int index, long millis, Consumer<Double> onTick) {
        BlockField field = new BlockField(height, width, maxDensity, distribution);
        field.setData(collection.getData().get(index));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Double> future = executor.submit(() -> {
            boolean success;
            double score = 0;
            try {
                do {
                    success = field.tick(false);
                    score++;
                    if (display != null) {
                        display.repaint();
                    }
                    if (onTick != null) {
                        onTick.accept(score);
                    }
                    Thread.sleep(millis);
                } while (success);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            return score;
        });
        executor.shutdown();
        return future;
    }
    
    public Future<Double> startDisplayRun(Network network, long millis, Consumer<Double> onTick) {
        BlockField field = new BlockField(height, width, maxDensity, distribution);
        NetworkCollection.NetworkData data = new NetworkCollection.NetworkData(network);
        field.setData(data);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        if (display != null) {
            display.setModel(field);
        }
        Future<Double> future = executor.submit(() -> {
            boolean success;
            double score = 0;
            try {
                do {
                    success = field.tick(false);
                    score++;
                    if (display != null) {
                        display.repaint();
                    }
                    if (onTick != null) {
                        onTick.accept(score);
                    }
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    Thread.sleep(millis);
                } while (success);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            return score;
        });
        executor.shutdown();
        return future;
    }
    
    public NetworkCollection getCollection() {
        return collection;
    }
    
    public FieldDisplay getDisplay() {
        return display;
    }
    
    public void setDisplay(FieldDisplay display) {
        this.display = display;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public Network.Node.ActivationType getActivationFunctionType() {
        return activationFunctionType;
    }
    
    public double getMaxDensity() {
        return maxDensity;
    }
    
    public double getDistribution() {
        return distribution;
    }
    
    public int getCollectionSize() {
        return collectionSize;
    }
    
    public int getRunsLimit() {
        return runsLimit;
    }
    
    public void setRunsLimit(int runsLimit) {
        this.runsLimit = runsLimit;
    }
    
    public int getHiddenLayers() {
        return hiddenLayers;
    }
    
    public void setMaxDensity(double maxDensity) {
        this.maxDensity = maxDensity;
    }
    
    public void setDistribution(double distribution) {
        this.distribution = distribution;
    }
    
    public double getRandom() {
        return random;
    }
    
    public void setRandom(double random) {
        this.random = random;
    }
}
