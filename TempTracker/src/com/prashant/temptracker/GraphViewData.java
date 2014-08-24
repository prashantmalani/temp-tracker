package com.prashant.temptracker;

import com.jjoe64.graphview.GraphViewDataInterface;


public class GraphViewData implements GraphViewDataInterface {
    private int x,y;

    public GraphViewData(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }
}
