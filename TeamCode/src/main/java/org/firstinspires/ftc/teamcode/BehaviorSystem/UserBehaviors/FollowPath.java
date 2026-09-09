package org.firstinspires.ftc.teamcode.BehaviorSystem.UserBehaviors;

import com.pedropathing.follower.Follower;
import com.pedropathing.paths.PathChain;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.BehaviorSystem.Behavior;

/**
 * Behavior that uses a PedroPathing follower to follow a given pathChain. Completes when the follower is no
 * longer busy.
 * @author Edson James
 */
public class FollowPath implements Behavior {
    private final Follower follower;
    private final PathChain pathChain;
    private final String label;
    private boolean entered = false;

    /**
     * Behavior that uses a PedroPathing follower to follow a given pathChain. Completes when the follower is no
     * longer busy.
     * @param follower The Pedro Follower to use.
     * @param pathChain The Pedro PathChain to follow.
     */
    public FollowPath(Follower follower, PathChain pathChain) {
        this(follower, pathChain, "Follow Path");
    }

    /**
     * Behavior that uses a PedroPathing follower to follow a given pathChain. Completes when the follower is no
     * longer busy.
     * @param follower The Pedro Follower to use.
     * @param pathChain The Pedro PathChain to follow.
     * @param label The label for this behavior.
     */
    public FollowPath(Follower follower, PathChain pathChain, String label) {
        this.follower = follower;
        this.pathChain = pathChain;
        this.label = label;
    }

    /**
     * Follows the PathChain using follower.followPath().
     */
    @Override
    public void enter() {
        follower.followPath(pathChain);
        entered = true;
    }

    /**
     * Updates the Follower using follower.update().
     */
    @Override
    public void update() {
        follower.update();
    }

    /**
     * Returns true if the Follower is no longer busy.
     * @return Whether the Follower is not busy.
     */
    @Override
    public boolean isComplete() {
        return entered && !follower.isBusy() && follower.getCurrentTValue() > 0.98;
    }

    @Override
    public void exit() {
        entered = false;
    }

    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {
        telemetry.addLine(prefix + "(T value: " + follower.getCurrentTValue() + ")");
    }

    @Override
    public String getLabel() {
        return label;
    }
}
