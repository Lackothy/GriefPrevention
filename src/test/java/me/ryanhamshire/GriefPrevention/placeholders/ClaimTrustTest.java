package me.ryanhamshire.GriefPrevention.placeholders;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class ClaimTrustTest
{
    private static final String ME = "0c2cc95d-0000-3000-8000-000000000001";
    private static final Predicate<String> MINE = entry -> entry.equalsIgnoreCase(ME) || entry.equals("[vip.trust]");
    private static final List<String> NONE = Collections.emptyList();

    @Test
    public void onlyTheHighestLevelIsNamedAfterManage()
    {
        assertEquals(Arrays.asList(ClaimTrust.BUILD),
                ClaimTrust.held(list(ME), list(ME), list(ME), NONE, MINE));
        assertEquals(Arrays.asList(ClaimTrust.MANAGE, ClaimTrust.CONTAINERS),
                ClaimTrust.held(NONE, list(ME), list(ME), list(ME), MINE));
        assertEquals(Arrays.asList(ClaimTrust.MANAGE),
                ClaimTrust.held(NONE, NONE, NONE, list(ME), MINE));
    }

    @Test
    public void publicCountsForEverythingButManage()
    {
        assertEquals(Arrays.asList(ClaimTrust.ACCESS),
                ClaimTrust.held(NONE, NONE, list("public"), list("public"), MINE));
    }

    @Test
    public void someoneElsesTrustIsNotMine()
    {
        assertEquals(NONE, ClaimTrust.held(list("someone-else", "[other.node]"), NONE, NONE, list("someone-else"), MINE));
        assertEquals(Arrays.asList(ClaimTrust.BUILD),
                ClaimTrust.held(list("[vip.trust]"), NONE, NONE, NONE, MINE));
    }

    private static List<String> list(String... entries)
    {
        return Arrays.asList(entries);
    }
}
