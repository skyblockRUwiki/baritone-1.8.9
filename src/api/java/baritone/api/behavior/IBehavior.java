package baritone.api.behavior;

import baritone.api.event.listener.AbstractGameEventListener;
import baritone.api.event.listener.IGameEventListener;

/**
 * A behavior is simply a type that is able to listen to events.
 *
 * @author Brady
 * @see IGameEventListener
 * @since 9/23/2018
 */
public interface IBehavior extends AbstractGameEventListener {}
