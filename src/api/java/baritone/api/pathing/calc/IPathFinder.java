package baritone.api.pathing.calc;

import baritone.api.pathing.goals.Goal;
import baritone.api.utils.PathCalculationResult;

import java.util.Optional;

/**
 * Generic path finder interface
 *
 * @author leijurv
 */
public interface IPathFinder {

    Goal getGoal();

    /**
     * Calculate the path in full. Will take several seconds.
     *
     * @param primaryTimeout If a path is found, the path finder will stop after this amount of time
     * @param failureTimeout If a path isn't found, the path finder will continue for this amount of time
     * @return The final path
     */
    PathCalculationResult calculate(long primaryTimeout, long failureTimeout);

    /**
     * Intended to be called concurrently with calculatePath from a different thread to tell if it's finished yet
     *
     * @return Whether or not this finder is finished
     */
    boolean isFinished();

    /**
     * Called for path rendering. Returns a path to the most recent node popped from the open set and considered.
     *
     * @return The temporary path
     */
    Optional<IPath> pathToMostRecentNodeConsidered();

    /**
     * The best path so far, according to the most forgiving coefficient heuristic (the reason being that that path is
     * most likely to represent the true shape of the path to the goal, assuming it's within a possible cost heuristic.
     * That's almost always a safe assumption, but in the case of a nearly impossible path, it still works by providing
     * a theoretically plausible but practically unlikely path)
     *
     * @return The temporary path
     */
    Optional<IPath> bestPathSoFar();
}
