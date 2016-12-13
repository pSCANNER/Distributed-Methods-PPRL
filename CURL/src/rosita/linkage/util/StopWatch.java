package rosita.linkage.util;

/*
    Copyright (c) 2005, Corey Goldberg

    StopWatch.java is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
*/


public class StopWatch {
    
    private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;

    
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    
    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }

    
    //elaspsed time in milliseconds
    public long getElapsedTime() {
        long elapsed;
        if (running) {
             elapsed = (System.currentTimeMillis() - startTime);
        }
        else {
            elapsed = (stopTime - startTime);
        }
        return elapsed;
    }
    
    
    //elaspsed time in seconds
    public long getElapsedTimeSecs() {
        long elapsed;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 1000);
        }
        else {
            elapsed = ((stopTime - startTime) / 1000);
        }
        return elapsed;
    }

    /**
     * Elapsed time in seconds as a double truncated to some number of decimals
     * @param numDecimals - The number of decimals to truncate to
     * @return - The elapsed time in seconds as a truncated decimal
     * @author Brandon Abbott
     */
    public double getElapsedTimeSecsDouble(int numDecimals)
    {
    	long elapsed = getElapsedTime();
    	double d_elapsed = (double) elapsed / 1000.0;
    	numDecimals = (int)Math.pow(10, numDecimals);
		return (  ( (double) ((long)(numDecimals*d_elapsed)) ) / numDecimals  );
    }
    
    /**
     * Override for getElapsedTimeSecsDouble(int numDecimals)
     * Simply uses 2 decimals places as the default value
     * @return - time elapsed in seconds to 2 decimals places
     * @author Brandon Abbott
     */
    public double getElapsedTimeSecsDouble()
    {
    	return getElapsedTimeSecsDouble(2);
    }
    
    //sample usage
    public static void main(String[] args) {
        StopWatch s = new StopWatch();
        s.start();
        //code you want to time goes here
        s.stop();
        System.out.println("elapsed time in milliseconds: " + s.getElapsedTime());
    }
}