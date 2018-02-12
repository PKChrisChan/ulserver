package io.baschel.ulserver.msgs.lyra.consts;

import com.fasterxml.jackson.annotation.JsonValue;

public final class LyraConsts {
    public static final Integer
        NUM_ARTS = 300,
        OWNER_ROOM = 0,
        OWNER_PLAYER = 1,
        NUM_PLAYER_STATS = 5,
        NUM_GUILDS = 8;

    public static class XpTableEntry
    {
        public final int sphere;
        public final int xpPerOrb;
        public final int baseXpForSphere;

        XpTableEntry(int s, int b, int xppo)
        {
            sphere = s;
            baseXpForSphere = b;
            xpPerOrb = xppo;
        }
    }


    public static XpTableEntry[] XP_PER_ORBIT = new XpTableEntry[] {
            new XpTableEntry(0, 0, 1000),
            new XpTableEntry(1, 10000, 5000),
            new XpTableEntry(2, 60000, 10000),
            new XpTableEntry(3, 160000, 50000),
            new XpTableEntry(4, 660000, 100000),
            new XpTableEntry(5, 1660000, 250000),
            new XpTableEntry(6, 4160000, 500000),
            new XpTableEntry(7, 9160000, 1000000),
            new XpTableEntry(8, 19160000, 2500000),
            new XpTableEntry(9, 44160000, 5000000)
    };

    public enum LoginStatus
    {
        LOGIN_UNUSED ('U'),  // invalid

        LOGIN_ALREADYIN   ('A'),  // user is already in game
        LOGIN_NO_BILLING   ('B'),  // free trial has expired
        LOGIN_NO_PMARE     ('C'),  // credit card account needed for pmare
        LOGIN_UNKNOWNERROR ('E'),  // unknown server error
        LOGIN_GAMEFULL     ('F'),  // game is full
        LOGIN_COOLOFF      ('H'),  // cooloff period between alts
        LOGIN_KILLED	   ('K'),  // player dead
        LOGIN_PMARE_LOCK   ('L'),  // PMare Global Lock in effect
        LOGIN_MISMATCH	   ('M'),  // account/build mismatch
        LOGIN_USERNOTFOUND ('N'),  // user not in database
        LOGIN_OK           ('O'),  // successful login
        LOGIN_PMARE_EXPIRED('Q'),  // pmare session has expired
        LOGIN_BADPASSWORD  ('P'),  // password incorrect
        LOGIN_SUSPENDED    ('S'),  // player suspended
        LOGIN_TERMINATED   ('T'),  // player terminated
        LOGIN_WRONGVERSION ('V'),  // incorrect client version
        LOGIN_EXPIRED      ('X'),  // account has been expired
        LOGIN_MAX_PMARE	   ('Z'),  // pmare account maxed out for the day

        GAMESITE_LYRA	     ('L'),  // signs up directly through Lyra
        GAMESITE_MULTIPLAYER ('M'),  // via MultiPlayer.com
        GAMESITE_SKOTOS		 ('S'),  // via Skotos
        GAMESITE_NETEMP		 ('N');  // via Netemp

        private Character val;
        LoginStatus(Character c)
        {
            val = c;
        }

        @JsonValue
        public Character toValue()
        {
            return val;
        }
    }

    public enum Focus
    {
        DREAMSOUL(0),
        WILLPOWER(1),
        INSIGHT(2),
        RESILIENCE(3),
        LUCIDITY(4);

        private int val;

        Focus(int ordinal)
        {
            val = ordinal;
        }

        @JsonValue
        public int toValue()
        {
            return val;
        }

        public static Focus fromValue(int val)
        {
            Focus[] vals = Focus.values();
            for(Focus v : vals)
                if(v.val == val)
                    return v;
            return null;
        }
    }

    public enum AcctType
    {
        // player account types
        ACCT_ADMIN ('A'),    // (65) Lyra admin / gamemaster
        ACCT_KILLED ('K'),   // (75) killed (dreamstrike)
        ACCT_LOCKED ('L'),   // (76) locked out
        ACCT_MONSTER ('M'),  // (77) nightmare (agent)
        ACCT_PLAYER ('P'),   // (80) normal player
        ACCT_PMARE ('S'),   // (83) monster player
        ACCT_ADMIN_EXPIRED ('E'), // (69) expired admin
        ACCT_PLAYER_EXPIRED ('X'); // (88) expired player

        private Character val;
        AcctType(Character ch)
        {
            val = ch;
        }

        @JsonValue
        public int toValue()
        {
            return val;
        }

        public static AcctType fromValue(int val)
        {
            AcctType[] vals = AcctType.values();
            for(AcctType v : vals)
                if(v.val == val)
                    return v;
            return null;
        }
    }
}
