package ndfs.mcndfs_2_improved;

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
        //initialize workers
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
                System.err.println("InterruptedException");
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
