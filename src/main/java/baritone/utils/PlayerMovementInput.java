package baritone.utils;

import baritone.api.utils.input.Input;
import net.minecraft.util.MovementInput;

public class PlayerMovementInput extends MovementInput {

    private final InputOverrideHandler handler;

    PlayerMovementInput(InputOverrideHandler handler) {
        this.handler = handler;
    }

    public void updatePlayerMoveState() {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        jump = handler.isInputForcedDown(Input.JUMP); // oppa gangnam style

        if (handler.isInputForcedDown(Input.MOVE_FORWARD)) {
            this.moveForward++;
        }

        if (handler.isInputForcedDown(Input.MOVE_BACK)) {
            this.moveForward--;
        }

        if (handler.isInputForcedDown(Input.MOVE_LEFT)) {
            this.moveStrafe++;
        }

        if (handler.isInputForcedDown(Input.MOVE_RIGHT)) {
            this.moveStrafe--;
        }

        if (this.sneak = handler.isInputForcedDown(Input.SNEAK)) {
            this.moveStrafe *= 0.3D;
            this.moveForward *= 0.3D;
        }
    }
}