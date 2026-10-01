package me.ryanhamshire.GriefPrevention.placeholders;

import me.ryanhamshire.GriefPrevention.GriefPrevention;

/**
 * Registers the expansion. Its own class so {@link GriefPrevention} never refers to a
 * PlaceholderAPI type, and loads fine on a server without it.
 */
public final class PlaceholderHook
{
    private PlaceholderHook()
    {
    }

    public static void register(GriefPrevention plugin)
    {
        new ClaimExpansion(plugin).register();
    }
}
