package baritone.api.command.exception;

import baritone.api.command.ICommand;
import baritone.api.command.argument.ICommandArgument;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

import static baritone.api.utils.Helper.HELPER;

public class CommandUnhandledException extends RuntimeException implements ICommandException {

    public CommandUnhandledException(String message) {
        super(message);
    }

    public CommandUnhandledException(Throwable cause) {
        super(cause);
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args) {
        HELPER.logDirect("An unhandled exception occurred. " +
                        "The error is in your game's log, please report this at https://github.com/cabaletta/baritone/issues",
                EnumChatFormatting.RED);

        this.printStackTrace();
    }
}
