package me.ryanhamshire.GriefPrevention.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * {@code %griefprevention_...%} placeholders, for a sidebar or a tab list.
 *
 * <p>About the player:
 * <ul>
 *   <li>{@code claims} - land claims they own; {@code claims_label} is "Claim" or "Claims" to
 *       go after it</li>
 *   <li>{@code claimblocks} - blocks they have left to claim with; {@code claimblocks_label} is
 *       "Claim block" or "Claim blocks"</li>
 *   <li>{@code claimblocks_total}, {@code claimblocks_used}</li>
 * </ul>
 *
 * <p>About the claim they are standing in, all empty outside one:
 * <ul>
 *   <li>{@code in_claim} - "true" or "false"</li>
 *   <li>{@code current_owner}</li>
 *   <li>{@code current_width}, {@code current_length}, {@code current_area}</li>
 *   <li>{@code current_permission_1}, {@code current_permission_2} - what they may do there, in
 *       its colour: Manage, then one of Build, Containers or Access; empty past what they have,
 *       and in their own claim. {@code current_permissions} is all of it on one line.</li>
 * </ul>
 *
 * <p>Standing in a subdivision reports the claim it belongs to: the owner and the size of the
 * land, which is what someone walking onto it wants to know. The permissions are the
 * subdivision's, with what it inherits, since that is what they may do where they stand.
 */
public final class ClaimExpansion extends PlaceholderExpansion
{
    private final GriefPrevention plugin;

    public ClaimExpansion(GriefPrevention plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier()
    {
        return "griefprevention";
    }

    @Override
    public String getAuthor()
    {
        return "Lackothy";
    }

    @Override
    public String getVersion()
    {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist()
    {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params)
    {
        if (player == null || plugin.dataStore == null) return "";
        String key = params.toLowerCase(Locale.ENGLISH);
        PlayerData data = plugin.dataStore.getPlayerData(player.getUniqueId());

        switch (key)
        {
            case "claims":
                return number(data.getClaims().size());
            case "claims_label":
                return data.getClaims().size() == 1 ? "Claim" : "Claims";
            case "claimblocks":
                return number(data.getRemainingClaimBlocks());
            case "claimblocks_label":
                return data.getRemainingClaimBlocks() == 1 ? "Claim block" : "Claim blocks";
            case "claimblocks_total":
                return number(total(data));
            case "claimblocks_used":
                return number(total(data) - data.getRemainingClaimBlocks());
            default:
                break;
        }

        if (!key.equals("in_claim") && !key.startsWith("current_")) return null;
        Claim here = plugin.dataStore.getClaimAt(player.getLocation(), false, data.lastClaim);
        Claim claim = here != null && here.parent != null ? here.parent : here;
        if (key.equals("in_claim")) return claim != null ? "true" : "false";
        if (claim == null) return "";

        switch (key)
        {
            case "current_owner":
                return claim.getOwnerName();
            case "current_width":
                return number(claim.getWidth());
            case "current_length":
                return number(claim.getHeight());
            case "current_area":
                return number(claim.getArea());
            case "current_permission_1":
                return nth(trust(here, claim, player), 0);
            case "current_permission_2":
                return nth(trust(here, claim, player), 1);
            case "current_permissions":
                return joined(trust(here, claim, player));
            default:
                return null;
        }
    }

    /** What the player may do where they stand: nothing to list in their own claim. */
    private static List<ClaimTrust> trust(Claim here, Claim claim, final Player player)
    {
        if (player.getUniqueId().equals(claim.ownerID)) return Collections.emptyList();
        ArrayList<String> builders = new ArrayList<String>();
        ArrayList<String> containers = new ArrayList<String>();
        ArrayList<String> accessors = new ArrayList<String>();
        ArrayList<String> managers = new ArrayList<String>();
        for (Claim at = here; at != null; at = at.parent)
        {
            at.getPermissions(builders, containers, accessors, managers);
        }
        final String id = player.getUniqueId().toString();
        return ClaimTrust.held(builders, containers, accessors, managers, entry ->
                entry.equalsIgnoreCase(id)
                        || (entry.length() > 2 && entry.startsWith("[") && entry.endsWith("]")
                                && player.hasPermission(entry.substring(1, entry.length() - 1))));
    }

    private static String nth(List<ClaimTrust> trust, int index)
    {
        return index < trust.size() ? trust.get(index).shown : "";
    }

    private static String joined(List<ClaimTrust> trust)
    {
        StringBuilder out = new StringBuilder();
        for (ClaimTrust level : trust)
        {
            if (out.length() > 0) out.append(ChatColor.GRAY).append(", ");
            out.append(level.shown);
        }
        return out.toString();
    }

    /** Every block the player has to claim with, used or not. */
    private int total(PlayerData data)
    {
        return data.getAccruedClaimBlocks() + data.getBonusClaimBlocks()
                + plugin.dataStore.getGroupBonusBlocks(data.playerID);
    }

    private static String number(int value)
    {
        return NumberFormat.getIntegerInstance(Locale.US).format(value);
    }
}
