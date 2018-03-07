package io.baschel.ulserver.msgs.lyra;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LmAvatar implements LyraMessage {
    public int avatar1;
    public int avatar2;
    private static final Logger L = LoggerFactory.getLogger(LmAvatar.class);

    public static class BitFields {
        public static final int
                // bitfield widths for avatar1
                AVATAR_WIDTH = 4,
                COLOR_WIDTH = 4, // 5 of these
                GUILD_ID_WIDTH = 4,
                GUILD_RANK_WIDTH = 2,
                SHOW_GUILD_WIDTH = 2,

        // bitfield widths for avatar2
        HEAD_WIDTH = 4,
                SPHERE_WIDTH = 4,
                SHOW_SPHERE_WIDTH = 2,
                TEACHER_WIDTH = 1,
                FOCUS_WIDTH = 3,
                MASTER_TEACHER_WIDTH = 1,
                SHOW_LYRAN_WIDTH = 1,
                DREAMSMITH_WIDTH = 1,
                ACCOUNT_WIDTH = 3,
                HIDDEN_WIDTH = 1,
                EXTRA_DAMAGE_WIDTH = 4,
                WORDSMITH_WIDTH = 1,
                DREAMSTRIKE_WIDTH = 1,
                NP_SYMBOL_WIDTH = 1,
                APPRENTICE_WIDTH = 1,
                UNUSED2_WIDTH = 3,

        // starting positions for avatar1
        AVATAR_START = 0,
                COLOR0_START = (AVATAR_START + AVATAR_WIDTH),
                COLOR1_START = (COLOR0_START + COLOR_WIDTH),
                COLOR2_START = (COLOR1_START + COLOR_WIDTH),
                COLOR3_START = (COLOR2_START + COLOR_WIDTH),
                COLOR4_START = (COLOR3_START + COLOR_WIDTH),
                GUILD_ID_START = (COLOR4_START + COLOR_WIDTH),
                GUILD_RANK_START = (GUILD_ID_START + GUILD_ID_WIDTH),
                SHOW_GUILD_START = (GUILD_RANK_START + GUILD_RANK_WIDTH),

        // starting positions for avatar2
        HEAD_START = 0,
                SPHERE_START = (HEAD_START + HEAD_WIDTH),
                SHOW_SPHERE_START = (SPHERE_START + SPHERE_WIDTH),
                TEACHER_START = (SHOW_SPHERE_START + SHOW_SPHERE_WIDTH),
                FOCUS_START = (TEACHER_START + TEACHER_WIDTH),
                MASTER_TEACHER_START = (FOCUS_START + FOCUS_WIDTH),
                SHOW_LYRAN_START = (MASTER_TEACHER_START + MASTER_TEACHER_WIDTH),
                DREAMSMITH_START = (SHOW_LYRAN_START + SHOW_LYRAN_WIDTH),
                ACCOUNT_START = (DREAMSMITH_START + DREAMSMITH_WIDTH),
                HIDDEN_START = (ACCOUNT_START + ACCOUNT_WIDTH),
                EXTRA_DAMAGE_START = (HIDDEN_START + HIDDEN_WIDTH),
                WORDSMITH_START = (EXTRA_DAMAGE_START + EXTRA_DAMAGE_WIDTH),
                DREAMSTRIKE_START = (WORDSMITH_START + WORDSMITH_WIDTH),
                NP_SYMBOL_START = (DREAMSTRIKE_START + DREAMSTRIKE_WIDTH),
                APPRENTICE_START = (NP_SYMBOL_START + NP_SYMBOL_WIDTH),

        // account types for use by client
        ACCT_DREAMER = 0,
                ACCT_PMARE = 1,
                ACCT_NIGHTMARE = 2,
                ACCT_DARKMARE = 3,
                ACCT_ADMIN = 4;

        public static int avatarField(String field) {
            String fLower = field.toLowerCase();
            if (fLower.startsWith("avatar") || fLower.startsWith("color") || fLower.contains("guild"))
                return 1;
            return 2;
        }

        public static int getStart(String field) {
            try {
                return getField(field.toUpperCase() + "_START").getInt(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return -1;
        }

        public static int getWidth(String field) {
            try {
                if(field.toUpperCase().startsWith("COLOR"))
                    field = "COLOR";

                return getField(field.toUpperCase() + "_WIDTH").getInt(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return -1;
        }

        private static final Map<String, Field> fieldMap = new HashMap<>();

        private static Field getField(String s) {
            return fieldMap.computeIfAbsent(s, k -> {
                try {
                    return BitFields.class.getField(k);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }
    }

    public int BitMask(int len) {
        return (1 << len) - 1;
    }

    public int BitMask(int len, int start) {
        return BitMask(len) << start;
    }

    public void set(String field, int value) {
        try {
            int start = BitFields.getStart(field);
            int width = BitFields.getWidth(field);

            if (BitFields.avatarField(field) == 1) {
                value &= BitMask(width);
                value <<= start;
                avatar1 &= ~BitMask(width, start);
                avatar1 |= value;
            } else if (BitFields.avatarField(field) == 2) {
                value &= BitMask(width);
                value <<= start;
                avatar2 &= ~BitMask(width, start);
                avatar2 |= value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public JsonObject toJsonObject() {
        List<String> av1fields = Arrays.asList("avatar", "color0", "color1", "color2", "color3", "color4",
                "guild_id", "guild_rank", "show_guild");
        JsonObject ret = new JsonObject();
        av1fields.forEach(k -> ret.put(k, (avatar1 >> BitFields.getStart(k)) & BitMask(BitFields.getWidth(k))));
        List<String> av2fields = Arrays.asList("head", "sphere", "show_sphere", "teacher", "focus", "master_teacher", "show_lyran", "dreamsmith", "account", "hidden",
                "extra_damage", "wordsmith", "dreamstrike", "np_symbol", "apprentice");
        av2fields.forEach(k -> ret.put(k, (avatar2 >> BitFields.getStart(k)) & BitMask(BitFields.getWidth(k))));
        return ret;
    }

    public void fromJsonObject(JsonObject js) {
        js.fieldNames().forEach(k -> {
            set(k, js.getInteger(k));
        });
    }
}
