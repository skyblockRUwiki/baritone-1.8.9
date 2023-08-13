package baritone.process;

import baritone.Baritone;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.utils.BaritoneProcessHelper;

public class InventoryPauserProcess extends BaritoneProcessHelper {

    boolean pauseRequestedLastTick;
    boolean safeToCancelLastTick;
    int ticksOfStationary;

    public InventoryPauserProcess(Baritone baritone) {
        super(baritone);
    }

    @Override
    public boolean isActive() {
        if (ctx.player() == null || ctx.world() == null) {
            return false;
        }
        return true;
    }

    private double motion() {
        return Math.sqrt(ctx.player().motionX * ctx.player().motionX + ctx.player().motionZ * ctx.player().motionZ);
    }

    private boolean stationaryNow() {
        return motion() < 0.00001;
    }

    public boolean stationaryForInventoryMove() {
        pauseRequestedLastTick = true;
        return safeToCancelLastTick && ticksOfStationary > 1;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        //logDebug(pauseRequestedLastTick + " " + safeToCancelLastTick + " " + ticksOfStationary);
        safeToCancelLastTick = isSafeToCancel;
        if (pauseRequestedLastTick) {
            pauseRequestedLastTick = false;
            if (stationaryNow()) {
                ticksOfStationary++;
            }
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }
        ticksOfStationary = 0;
        return new PathingCommand(null, PathingCommandType.DEFER);
    }

    @Override
    public void onLostControl() {

    }

    @Override
    public String displayName0() {
        return "inventory pauser";
    }

    @Override
    public double priority() {
        return 5.1; // slightly higher than backfill
    }

    @Override
    public boolean isTemporary() {
        return true;
    }
}