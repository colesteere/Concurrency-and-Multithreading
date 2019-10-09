package ndfs.mcndfs_2_improved;

import java.util.HashMap;
import java.util.Map;

import graph.State;

/**
 * This class provides a color map for graph states.
 */
public class Colors {

    private final Map<State, Color> map = new HashMap<State, Color>();

    //this map indicates if a state is pink thus pinkMap (it also has to be local which is why its not included in the other map)
    private final Map<graph.State, Boolean> pinkMap = new HashMap<graph.State, Boolean>();

    /**
     * Returns <code>true</code> if the specified state has the specified color,
     * <code>false</code> otherwise.
     *
     * @param state
     *            the state to examine.
     * @param color
     *            the color
     * @return whether the specified state has the specified color.
     */
    
    public boolean hasColor(State state, Color color) {

        // The initial color is white, and is not explicitly represented.
        if (color == Color.WHITE) {
            return map.get(state) == null;
        } else {
            return map.get(state) == color;
        }
    }

    /**
     * Gives the specified state the specified color.
     *
     * @param state
     *            the state to color.
     * @param color
     *            color to give to the state.
     */
    public void color(State state, Color color) {
        if (color == Color.WHITE) {
            map.remove(state);
        } else {
            map.put(state, color);
        }
    }

    public boolean isPink(State state)
    {
        if (pinkMap.get(state) == null)
        {
            return false;
        }

        else 
        {
            return pinkMap.get(state);
        }
    }

    public void makePink(State state, boolean b)
    {
        if (b){
            pinkMap.put(state, b);
        }
        else{
            pinkMap.remove(state);
        }
    }
}
