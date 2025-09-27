package io.github.tavstaldev.openChat.models;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Represents a group of tab completions for commands, with a priority and optional extension group.
 */
public class TabGroup {
    private final int priority; // The priority of the tab group.

    @Nullable
    private final String extendGroup; // The name of the group this tab group extends, if any.

    private final Set<String> commands; // The set of commands associated with this tab group.

    /**
     * Constructs a new TabGroup instance.
     *
     * @param priority    The priority of the tab group.
     * @param extendGroup The name of the group this tab group extends, or null if none.
     * @param commands    The set of commands associated with this tab group.
     */
    public TabGroup(int priority, @Nullable String extendGroup, Set<String> commands) {
        this.priority = priority;
        this.extendGroup = extendGroup;
        this.commands = commands;
    }

    /**
     * Gets the priority of the tab group.
     *
     * @return The priority of the tab group.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Gets the name of the group this tab group extends, if any.
     *
     * @return The name of the group this tab group extends, or null if none.
     */
    @Nullable
    public String getExtendGroup() {
        return extendGroup;
    }

    /**
     * Gets the set of commands associated with this tab group.
     *
     * @return The set of commands associated with this tab group.
     */
    public Set<String> getCommands() {
        return commands;
    }
}