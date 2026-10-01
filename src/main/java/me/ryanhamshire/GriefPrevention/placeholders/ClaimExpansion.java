package me.ryanhamshire.GriefPrevention.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
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
 * </ul>
 *
 * <p>Standing in a subdivision reports the claim it belongs to: the owner and the size of the
 * land, which is what someone walking onto it wants to know.
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
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), false, data.lastClaim);
        if (claim != null && claim.parent != null) claim = claim.parent;
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
            default:
                return null;
        }
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
