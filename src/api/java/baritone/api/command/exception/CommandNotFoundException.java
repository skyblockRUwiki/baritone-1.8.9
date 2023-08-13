package baritone.api.command.exception;

import baritone.api.command.ICommand;
import baritone.api.command.argument.ICommandArgument;

import java.util.List;

import static baritone.api.utils.Helper.HELPER;

public class CommandNotFoundException extends CommandException {

    public final String command;

    public CommandNotFoundException(String command) {
        super(String.format("Command not found: %s", command));
        this.command = command;
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args) {
        HELPER.logDirect(getMessage());
    }
}