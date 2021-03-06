package io.baschel.ulserver.msgs.lyra.consts;

import com.fasterxml.jackson.annotation.JsonValue;

public final class LyraConsts {
    public static final Integer
            NUM_ARTS = 300,
            OWNER_ROOM = 0,
            OWNER_PLAYER = 1,
            NUM_PLAYER_STATS = 5,
            NUM_GUILDS = 8,
            AVATAR_UNKNOWN = 0,     // invalid avatar
            NUM_HOUSES = 8,

    // BUILDS!
    GM_DELTA = 2000000000,  // added to protocol version in GM build
            PMARE_DELTA = 1000000000,  // added to protocol version in GM build
            HIDDEN_DELTA = 100000,  // added to levelid for "hidden" players (not locatable)

    PLAYERNAME_MAX = 16,    // maximum player name length (multiple of 4)
            PASSWORD_MAX = 12,      // maximum password length (multiple of 4)
            INVENTORY_MAX = 50,     // maximum number of items in inventory
            MAX_XP = 1000000000,    // maximum XP for a player (1 billion)

    KNIGHT_XP_POOL = 30000, // amount of XP in knight's XP pool
            RULER_XP_POOL = 40000,  // amount of XP in ruler's XP pool
            QUEST_XP_POOL = 5000,    // amount of XP in teacher/knight/ruler's Quest XP pool
            PP_PER_DAY = 4,  // base personality points to grant per day for everyone
            PP_PER_DAY_TEACHER = 6,  // base personality points to grant per day for teachers
            PP_PER_DAY_ADMIN = 99, // base personality points to grant per day for GMs

    MAX_SPEECHLEN = 512,    // maximum length of speech text
            MAX_ITEMDESC = 512,     // maximum length of an item's description
            MAX_AVATARDESC = 1024,   // max length of avatar description

    MAX_LEVELS = 64,        // max # of levels
            MAX_LEVELROOMS = 64,   // maximum number of rooms in a level
            ROOMPEOPLE_MAX = 100,   // maximum number of people in a room
            MAX_ROOMITEMS = 64,     // maximum number of items in a room
            MAX_PARTYSIZE = 4,      // maximum number of people in a party (< MAX_GROUP)
            ROOMDESC_MAX = 1024,    // maximum room description length

    GOAL_SUMMARY_LENGTH = 64,
            QUEST_KEYWORDS_LENGTH = 64,
            MAX_GOAL_LENGTH = 2048,
            MAX_REPORT_LENGTH = 1024,
            MAX_SIMUL_GOALS = 512,
            MAX_ACCEPTS = 200,      // abs max # of people who can accept a goal
            MAX_GOAL_LIFE = 30,        // max life of goal in days
            MAX_QUEST_LIFE = 30,    // max life of quest in days
            MAX_ACTIVE_GOALS = 8,   // max # of goals a player can have as active
            MAX_AWARDXP = 100000;   // maximum amount of XP that can be awarded in a report


    public static class XpTableEntry {
        public final int sphere;
        public final int xpPerOrb;
        public final int baseXpForSphere;

        XpTableEntry(int s, int b, int xppo) {
            sphere = s;
            baseXpForSphere = b;
            xpPerOrb = xppo;
        }
    }


    public static XpTableEntry[] XP_PER_ORBIT = new XpTableEntry[]{
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

    public enum LoginStatus {
        LOGIN_UNUSED('U'),  // invalid

        LOGIN_ALREADYIN('A'),  // user is already in game
        LOGIN_NO_BILLING('B'),  // free trial has expired
        LOGIN_NO_PMARE('C'),  // credit card account needed for pmare
        LOGIN_UNKNOWNERROR('E'),  // unknown server error
        LOGIN_GAMEFULL('F'),  // game is full
        LOGIN_COOLOFF('H'),  // cooloff period between alts
        LOGIN_KILLED('K'),  // player dead
        LOGIN_PMARE_LOCK('L'),  // PMare Global Lock in effect
        LOGIN_MISMATCH('M'),  // account/build mismatch
        LOGIN_USERNOTFOUND('N'),  // user not in database
        LOGIN_OK('O'),  // successful login
        LOGIN_PMARE_EXPIRED('Q'),  // pmare session has expired
        LOGIN_BADPASSWORD('P'),  // password incorrect
        LOGIN_SUSPENDED('S'),  // player suspended
        LOGIN_TERMINATED('T'),  // player terminated
        LOGIN_WRONGVERSION('V'),  // incorrect client version
        LOGIN_EXPIRED('X'),  // account has been expired
        LOGIN_MAX_PMARE('Z'),  // pmare account maxed out for the day

        GAMESITE_LYRA('L'),  // signs up directly through Lyra
        GAMESITE_MULTIPLAYER('M'),  // via MultiPlayer.com
        GAMESITE_SKOTOS('S'),  // via Skotos
        GAMESITE_NETEMP('N');  // via Netemp

        private Character val;

        LoginStatus(Character c) {
            val = c;
        }

        @JsonValue
        public Character toValue() {
            return val;
        }
    }

    public enum Focus {
        DREAMSOUL(0),
        WILLPOWER(1),
        INSIGHT(2),
        RESILIENCE(3),
        LUCIDITY(4);

        private int val;

        Focus(int ordinal) {
            val = ordinal;
        }

        @JsonValue
        public int toValue() {
            return val;
        }

        public static Focus fromValue(int val) {
            Focus[] vals = Focus.values();
            for (Focus v : vals)
                if (v.val == val)
                    return v;
            return null;
        }
    }

    public enum Arts {
        JOIN_PARTY,
        GATEKEEPER,
        DREAMSEER,
        SOULMASTER,
        FATESENDER,
        RANDOM,
        MEDITATION,
        RESIST_FEAR,
        PROTECTION,
        FREE_ACTION,
        WARD,  // 10
        AMULET,
        SHATTER,
        RETURN,
        KNOW,
        JUDGEMENT,
        IDENTIFY,
        IDENTIFY_CURSE,
        CHAMELE,
        VISION,
        BLAST,  // 20
        BLEND,
        FORGE_TALISMAN,
        RECHARGE_TALISMAN,
        RESTORE,
        REWEAVE,
        PURIFY,
        DRAIN_SELF,
        ABJURE,
        POISON,
        ANTIDOTE,  //30
        CURSE,
        DRAIN_NIGHTMARE,
        BANISH_NIGHTMARE,
        ENSLAVE_NIGHTMARE,
        TRAP_NIGHTMARE,
        DREAMBLADE,
        TRAIL,
        SCARE,
        STAGGER,
        DEAFEN, // 40
        BLIND,
        DARKNESS,
        PARALYZE,
        FIRESTORM,
        RAZORWIND,
        RECALL,
        PUSH,
        SOULEVOKE,
        DREAMSTRIKE,
        NIGHTMARE_FORM, // 50
        LOCATE_AVATAR,
        TRAIN,
        INITIATE,
        KNIGHT,
        SUPPORT_ASCENSION,
        ASCEND,
        FINGER_OF_DEATH, // gm only
        GRANT_XP, // gm only
        TERMINATE, // gm only
        LEVELTRAIN, // 60
        SUPPORT_DEMOTION,
        DEMOTE,
        INVISIBILITY,
        GIVE,
        GATESMASHER,
        FATESLAYER,
        SOULREAPER,
        FLAMESHAFT,
        TRANCEFLAME,
        FLAMESEAR,  //70
        FLAMERUIN,
        WRITE_SCROLL,
        DESTROY_ITEM,
        MIND_BLANK,
        SHOW,
        BOOT, // gm only
        UNTRAIN, // gm only
        GRANT_RP_XP,
        EARTHQUAKE,
        HYPNOTIC_WEAVE, // 80
        VAMPIRIC_DRAW,
        TERROR,
        HEALING_AURA,
        ROGER_WILCO,
        DREAMSMITH_MARK,
        SUPPORT_TRAINING,
        SUPPORT_SPHERING,
        TRAIN_SELF,
        SOUL_SHIELD,
        SUMMON, // 90
        SUSPEND,
        REFLECT,
        SACRIFICE,
        CLEANSE_NIGHTMARE,
        CREATE_ID_TOKEN,
        SENSE_DREAMERS,
        EXPEL,
        LOCATE_NEWLIES,
        COMBINE,
        POWER_TOKEN, // 100
        SHOW_GRATITUDE,
        QUEST,
        EMPATHY,
        RADIANT_BLAZE,
        POISON_CLOUD,
        BREAK_COVENANT,
        PEACE_AURA,
        SABLE_SHIELD,
        ENTRANCEMENT,
        SHADOW_STEP,  // 110
        DAZZLE,
        GUILDHOUSE,
        CORRUPT_ESSENCE,
        TEHTHUS_OBLIVION,
        CHAOS_PURGE,
        WORDSMITH_MARK,
        CUP_SUMMONS,
        HOUSE_MEMBERS,
        FREESOUL_BLADE,
        ILLUMINATED_BLADE, // 120
        SUMMON_PRIME,
        GRANT_PPOINT, // dummy art; it should not be learned
        SCAN,  // judgement
        PASSLOCK, // blend clone
        HEAL, // restore clone
        SANCTIFY, // protection
        LOCK, // ward
        KEY, // amulet
        BREAK_LOCK, // shatter
        REPAIR, // reweave // 130
        REMOVE_CURSE, // purify
        HOLD_AVATAR, // paralyze
        SANCTUARY, // recall
        SHOVE, // push
        SCRIBE_NOT, // placeholder - not implemented
        FORGE_MASTER, // dreamsmith mark
        MERGE_TALISMAN, // combine
        NP_SYMBOL,     // use NP symbol on chest
        SENSE_MARE,  // sense pmares & dark mares
        TEMPEST,    // Tempest // 140
        KINESIS,    // Kinesis
        MISDIRECTION, // Misdirection
        CHAOTIC_VORTEX, // Chaotic Vortex
        CHAOS_WELL, // Essence Container
        RALLY, // Summon party member
        CHANNEL;

        private static final Arts[] artVals = Arts.values();

        public static Arts fromOrdinal(int ord) {
            if (ord < 0)
                return null;
            return artVals[ord];
        }

        public boolean isAutoTrainableGuildArt() {
            switch (this) {
                case EXPEL:
                case KNIGHT:
                case CREATE_ID_TOKEN:
                case INITIATE:
                case SUPPORT_DEMOTION:
                case SUPPORT_ASCENSION:
                case POWER_TOKEN:
                case EMPATHY:
                case CUP_SUMMONS:
                case ASCEND:
                case HOUSE_MEMBERS:
                case DEMOTE:
                    return true;
                default:
                    return false;
            }
        }
    }

    public enum AcctType {
        // player account types
        ACCT_ADMIN('A'),    // (65) Lyra admin / gamemaster
        ACCT_KILLED('K'),   // (75) killed (dreamstrike)
        ACCT_LOCKED('L'),   // (76) locked out
        ACCT_MONSTER('M'),  // (77) nightmare (agent)
        ACCT_PLAYER('P'),   // (80) normal player
        ACCT_PMARE('S'),   // (83) monster player
        ACCT_ADMIN_EXPIRED('E'), // (69) expired admin
        ACCT_PLAYER_EXPIRED('X'); // (88) expired player

        private Character val;

        AcctType(Character ch) {
            val = ch;
        }

        @JsonValue
        public int toValue() {
            return val;
        }

        public static AcctType fromValue(int val) {
            AcctType[] vals = AcctType.values();
            for (AcctType v : vals)
                if (v.val == val)
                    return v;
            return null;
        }
    }

    public static final class Speech {
        // Speech consts
        public static final char
                REPORT_BUG = 'b',  // bug report
                REPORT_CHEAT = 'c',  // cheater report
                AUTO_CHEAT = 'C',  // auto-generated cheat report
                REPORT_DEBUG = 'd',  // debug report (auto-generated)
                EMOTE = 'E',  // emote
                GLOBALSHOUT = 'G',  // level-wide shout
                SHOUT = 'H',  // room-wide shout
                MONSTER_SPEECH = 'M',  // (player) monster speech
                PARTY = 'P',  // party-only speech
                REPORT_QUEST = 'q',  // quest report
                RP = 'r',  // role play report
                RAW_EMOTE = 'R',  // "raw" emote
                SYSTEM_SPEECH = 's',  // "system" speech
                SPEECH = 'S',  // standard speech
                SERVER_TEXT = 'T',  // server-initiated speech to clients
                SYSTEM_WHISPER = 'w',  // "system" whisper
                TELL_IP = 't',  // roger wilco IP address
                WHISPER = 'W',  // person-to-person whisper
                WHISPER_EMOTE = 'Z';   // emote showing a whisper is happening
    }

    public static final class Guild
    {
        public static final int
                // ranks
            INITIATE_PENDING	= -1,
            KNIGHT_PENDING		= -2,
            RULER_PENDING       = -3,
            NO_RANK				= -4,
            INITIATE			= 1,
            KNIGHT				= 2,
            RULER				= 3,
            QUEST				= 4,
                // guild IDs
            NO_GUILD    =   15,
            NO_HOUSE	=   15,
            MOON		=	0,
            ECLIPSE		=	1,
            SHADOW		=	2,
            COVENANT	=	3,
            RADIANCE	=	4,
            CALENTURE	=	5,
            ENTRANCED	=	6,
            LIGHT		=	7;
    }

    public static final class RoomLogin {
        public static final Character
                LOGIN_UNKNOWN = 'U',

        LOGIN_ALREADYIN = 'A',
                LOGIN_ERROR = 'E',
                LOGIN_ROOMFULL = 'F',
                LOGIN_OK = 'K',
                LOGIN_LEVELNOTFOUND = 'L',
                LOGIN_PLAYERNOTFOUND = 'P',
                LOGIN_ROOMNOTFOUND = 'R',
                LOGIN_SERVERDOWN = 'S';
    }

    public static final class StatChangeTypes {
        public static final int
                // request type    dir   stat  value    result
                SET_XP = 1,     // SC    N/A   XP       set XP
                SET_STAT_CURR = 2,  // CS    stat  statval  set current player stat
                SET_STAT_MAX = 3,   // SC    stat  statval  set max player stat
                SET_SKILL = 4;       // SC/CS art   skill    set skill
    }
}
