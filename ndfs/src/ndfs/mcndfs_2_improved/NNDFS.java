package ndfs.mcndfs_1_naive;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import graph.*;
//import javafx.concurrent.Worker;
import ndfs.NDFS;

/**
 * Implements the {@link ndfs.NDFS} interface, mostly delegating the work to a
 * worker class.
 */

public class NNDFS implements NDFS {

    private final Worker[] workers;

    // map of red states shared between threads
    public ConcurrentHashMap<graph.State, Boolean> redStates = new ConcurrentHashMap<>();
    // number of threads that called dfsRed() on the state; also shared
    public ConcurrentHashMap<graph.State, AtomicInteger> threadCount = new ConcurrentHashMap<>();

    //private final ArrayList<Worker> workers = new ArrayList<>();

    /**
     * Constructs an NDFS object using the specified Promela file.
     *
     * @param promelaFile
     *            the Promela file.
     * @throws FileNotFoundException
     *             is thrown in case the file could not be read.
     */
    public NNDFS(File promelaFile, int numWorkers) throws FileNotFoundException
    {
        //creates graph from the promelaFile
        Graph graph = GraphFactory.createGraph(promelaFile);
        State s = graph.getInitialState();
        Stack<Stack> stack = new Stack<Stack>();
        stack.push(s);
        while (!stack.empty())
        {
            State cur = stack.pop();
            if (redStates.get(cur) != null) //make sure that the state isnt currently red
            {
                continue;
            }
            redStates.put(cur, false);
            threadCount.put(cur, new AtomicInteger(0));
            List<State> states = graph.post(cur); //post(cur) traverses the rest of the graph
            for (State next : states)
            {
                stack.push(next);
            }
        } 
        this.workers = new Worker[numWorkers];
        for (int i = 0; i < numWorkers; i++)
        {
            this.workers[i] = new Worker(promelaFile, redStates, threadCount);
            //workers.add(new Worker(promelaFile));
        }
    }

    @Override
    public boolean ndfs() {
        for (Worker w : workers)
        {
            w.start();
        }
        ArrayList<Boolean> results = new ArrayList<>();
        for (Worker w : workers)
        {
            try
            {
                w.join();
                results.add(w.getResult());
            }
            catch (InterruptedException e)
            {
                System.err.println("Thread interrupted: " + w.toString());
            }
        }
        for (boolean b : results)
        {
            if (b)
                return true;
        }
        return false;
    }
}
