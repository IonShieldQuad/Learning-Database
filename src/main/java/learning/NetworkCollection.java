package learning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkCollection implements Serializable {
    private List<NetworkData> data = new ArrayList<>();
    private int iteration;
    private Function<NetworkData, Network> nextGen;
    private Supplier<Network> init;
    private double maxScore = 0;

    public NetworkCollection(int iteration, List<Network> init, Function<NetworkData, Network> nextGen) {
        this.iteration = iteration;
        this.nextGen = nextGen;
        for (int i = 0; i < init.size(); i++) {
            data.add(new NetworkData(init.get(i)));
        }
    }
    
    public NetworkCollection(int iteration, int size, Supplier<Network> init, Function<NetworkData, Network> nextGen) {
        this.iteration = iteration;
        this.init = init;
        this.nextGen = nextGen;
        for (int i = 0; i < size; i++) {
            data.add(new NetworkData(init.get()));
        }
        
    }
    
    public Network getNetwork(int index) {
        return data.get(index).network;
    }
    
    public void nextIteration() {
        int half = (int)Math.ceil((data.size() - 1) / 2.0);
        data.sort((a, b) -> (b.average() - a.average() > 0 ? 1 : b.average() - a.average() < 0 ? -1 : 0));
        maxScore = Math.max(maxScore, data.get(0).average());
        for (int i = 0; i <= half; i++){
            if (i + half < data.size()){
                data.get(i + half).network = nextGen.apply(data.get(i + half));
            }
            else {
                break;
            }
        }
        for (NetworkData datum : data) {
            datum.reset();
        }
        Collections.shuffle(data);
        
        iteration++;
    }
    
    public List<NetworkData> getData() {
        return data;
    }
    
    public int getIteration() {
        return iteration;
    }
    
    public Function<NetworkData, Network> getNextGen() {
        return nextGen;
    }
    
    public void setNextGen(Function<NetworkData, Network> nextGen) {
        this.nextGen = nextGen;
    }
    
    public Supplier<Network> getInit() {
        return init;
    }
    
    public void setInit(Supplier<Network> init) {
        this.init = init;
    }
    
    public double getMaxScore() {
        return maxScore;
    }
    
    public void resetMaxScore() {
        this.maxScore = 0;
    }
    
    public static class NetworkData implements Serializable {
        public Network network;
        public double score;
        public int runs;
        
        public NetworkData(Network network) {
            this.network = network;
        }
        
        public double average() {
            return runs == 0 ? 0 : score / (double)runs;
        }
        
        public void reset() {
            score = 0;
            runs = 0;
        }
    }
}
