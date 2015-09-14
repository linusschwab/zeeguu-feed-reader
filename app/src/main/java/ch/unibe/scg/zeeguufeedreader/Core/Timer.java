package ch.unibe.scg.zeeguufeedreader.Core;

/**
 * Debugging class to measure the time of a task
 */
public class Timer {
    private long startTime;
    private long endTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        endTime = System.currentTimeMillis();
    }

    public void reset() {
        startTime = 0;
        endTime = 0;
    }

    public String getTimeElapsed() {
        float timeElapsed = (endTime - startTime) / 1000f;

        return timeElapsed + " seconds";
    }
}
