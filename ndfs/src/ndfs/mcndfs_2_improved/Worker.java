package ndfs.mcndfs_1_naive;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import graph.Graph;
import graph.GraphFactory;
import graph.State;

/**
 * This is a straightforward implementation of Figure 1 of
 * <a href="http://www.cs.vu.nl/~tcs/cm/ndfs/laarman.pdf"> "the Laarman
 * paper"</a>.
 */

public class Worker extends Thread{

    private final Graph graph;
    private final Colors colors = new Colors();
    private boolean result = false;
    private volatile static boolean isInterrupted = false;

    //map of red states that are shared globally between threads
    public ConcurrentHashMap<graph.State, Boolean> redStates;

    //the number of workers that initiate dfs_red 
    public ConcurrentHashMap<graph.State, AtomicInteger> threadCount;

    // Throwing an exception is a convenient way to cut off the search in case a
    // cycle is found.
    private static class CycleFoundException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Constructs a Worker object using the specified Promela file.
     *
     * @param promelaFile
     *            the Promela file.
     * @throws FileNotFoundException
     *             is thrown in case the file could not be read.
     */
    public Worker(File promelaFile, ConcurrentHashMap redStates, ConcurrentHashMap threadCount) throws FileNotFoundException {

        this.graph = GraphFactory.createGraph(promelaFile);
        this.redStates = redStates;
        this.threadCount = threadCount;
    }

    private void dfsRed(State s) throws CycleFoundException {
        if(isInterrupted)
            throw new InterruptedException();
        colors.makePink(s, true); //new in the mc version
        List<graph.State> listOfNeighbors = graph.post(s);
        Collections.shuffle(listOfNeighbors);
        for (graph.State t : listOfNeighbors) {
            if (colors.hasColor(t, Color.CYAN)) {
                isInterrupted = true;
                throw new CycleFoundException();
            }

            if (!colors.isPink(s) && !redStates.get(s)) { //new in the mc version
                dfsRed(t);
            }
        }

        if (s.isAccepting())
        {
            threadCount.get(s).decrementAndGet(); //new in the mc version
            synchronized (threadCount.get(s))
            {
                if(threadCount.get(s).get() == 0){
                    threadCount.get(s).notifyAll();
                }
                else{
                    threadCount.get(s).wait();
                }
            }
        }

        redStates.put(s, true); //new in the mc version
        colors.makePink(s, false); //new in the mc version
    }

    private void dfsBlue(State s) throws CycleFoundException {
        if(isInterrupted)
            throw new InterruptedException();
        colors.color(s, Color.CYAN);
        List<graph.State> listOfNeighbors = graph.post(s);
        Collections.shuffle(listOfNeighbors);
        for (graph.State t : listOfNeighbors {
            if (colors.hasColor(t, Color.WHITE) && !redStates.get(t)) { //new in the mc version) 
                dfsBlue(t);
            }
        }
        if (s.isAccepting()) {
            threadCount.get(s).incrementAndGet(1); //new in the mc version
            dfsRed(s);
        }
        colors.color(s, Color.BLUE);
    }

    private void nndfs(graph.State s) throws CycleFoundException {
        dfsBlue(s);
    }

    public void run() {
        try {
            nndfs(graph.getInitialState());
        } catch (CycleFoundException e) {
            result = true;
        }
    }

    public boolean getResult() {
        return result;
    }
}