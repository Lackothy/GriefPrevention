package me.ryanhamshire.GriefPrevention.placeholders;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * What a player may do in a claim that isn't theirs, for the {@code current_permission...}
 * placeholders, each in its own colour.
 *
 * <p>Manage is {@code /permissiontrust}, a list of its own. The rest is one level per player -
 * {@code /trust}, {@code /containertrust} or {@code /accesstrust} - each including the ones
 * after it, so only the highest is named.
 */
enum ClaimTrust
{
    MANAGE(ChatColor.GOLD + "Manage"),
    BUILD(ChatColor.YELLOW + "Build"),
    CONTAINERS(ChatColor.GREEN + "Containers"),
    ACCESS(ChatColor.BLUE + "Access");

    /** As the board shows it, in its colour. */
    final String shown;

    ClaimTrust(String shown)
    {
        this.shown = shown;
    }

    /**
     * The trust held, Manage first. Each list is a claim's entries - a subdivision's followed by
     * its parent's, which it inherits: a player's id, {@code public} for everyone, or a
     * {@code [permission]}. {@code names} says whether an id or permission entry is the player's;
     * {@code public} counts for every level but Manage, as in {@code Claim}'s own checks.
     */
    static List<ClaimTrust> held(List<String> builders, List<String> containers, List<String> accessors,
                                 List<String> managers, Predicate<String> names)
    {
        List<ClaimTrust> held = new ArrayList<ClaimTrust>();
        if (any(managers, names, false)) held.add(MANAGE);
        if (any(builders, names, true)) held.add(BUILD);
        else if (any(containers, names, true)) held.add(CONTAINERS);
        else if (any(accessors, names, true)) held.add(ACCESS);
        return held;
    }

    private static boolean any(List<String> entries, Predicate<String> names, boolean publicCounts)
    {
        for (String entry : entries)
        {
            if (publicCounts && entry.equalsIgnoreCase("public")) return true;
            if (names.test(entry)) return true;
        }
        return false;
    }
}
