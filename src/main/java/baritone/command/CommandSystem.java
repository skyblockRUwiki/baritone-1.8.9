package baritone.command;

import baritone.api.command.ICommandSystem;
import baritone.api.command.argparser.IArgParserManager;
import baritone.command.argparser.ArgParserManager;

/**
 * @author Brady
 * @since 10/4/2019
 */
public enum CommandSystem implements ICommandSystem {
    INSTANCE;

    @Override
    public IArgParserManager getParserManager() {
        return ArgParserManager.INSTANCE;
    }
}
