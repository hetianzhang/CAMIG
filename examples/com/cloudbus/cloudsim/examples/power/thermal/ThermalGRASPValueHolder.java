package com.cloudbus.cloudsim.examples.power.thermal;

import org.cloudbus.cloudsim.power.PowerHost;

public class ThermalGRASPValueHolder {

    public double greedyGRASPValue = Double.MAX_VALUE;

    public PowerHost host = null;

    public double getGreedyGRASPValue() {
        return greedyGRASPValue;
    }

    public void setGreedyGRASPValue(double greedyGRASPValue) {
        this.greedyGRASPValue = greedyGRASPValue;
    }

    public PowerHost getHost() {
        return host;
    }

    public void setHost(PowerHost host) {
        this.host = host;
    }
}
