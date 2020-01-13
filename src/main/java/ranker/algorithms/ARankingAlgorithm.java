package ranker.algorithms;

/* abstract class to be extended by each ranking algorithm that's to be used by the ranker*/

public abstract class ARankingAlgorithm {

    protected double weight;

    public ARankingAlgorithm(double weight) {
        this.weight = weight;
    }
}
