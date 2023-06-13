package dev.anhcraft.oreprocessor.api.upgrade;

public class UpgradeLevel {
    private final int amount;
    private final double cost;

    public UpgradeLevel(int amount, double cost) {
        this.amount = amount;
        this.cost = cost;
    }

    public int getAmount() {
        return amount;
    }

    public double getCost() {
        return cost;
    }
}
