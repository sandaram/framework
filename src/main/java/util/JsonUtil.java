package util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/** Conversion objet -> JSON écrite à la main (aucune dépendance externe). */
public class JsonUtil {

    public static String toJson(Object o) {
        StringBuilder sb = new StringBuilder();
        write(o, sb, Collections.newSetFromMap(new IdentityHashMap<>()));
        return sb.toString();
    }

    private static void write(Object o, StringBuilder sb, Set<Object> visiting) {
        if (o == null) { sb.append("null"); return; }

        if (o instanceof CharSequence || o instanceof Character || o instanceof Enum) {
            quote(o.toString(), sb); return;
        }
        if (o instanceof Boolean) { sb.append(o); return; }
        if (o instanceof Number) {
            double d = ((Number) o).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) sb.append("null"); else sb.append(o);
            return;
        }
        // Dates & co (java.util.Date, java.time.*, UUID, ...) -> texte
        if (o instanceof java.util.Date || o instanceof java.time.temporal.TemporalAccessor
                || o instanceof java.util.UUID) {
            quote(o.toString(), sb); return;
        }

        if (!visiting.add(o)) { sb.append("null"); return; } // référence circulaire
        try {
            if (o instanceof Map) {
                sb.append('{');
                boolean first = true;
                for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                    if (!first) sb.append(',');
                    first = false;
                    quote(String.valueOf(e.getKey()), sb);
                    sb.append(':');
                    write(e.getValue(), sb, visiting);
                }
                sb.append('}');
            } else if (o instanceof Collection) {
                sb.append('[');
                boolean first = true;
                for (Object item : (Collection<?>) o) {
                    if (!first) sb.append(',');
                    first = false;
                    write(item, sb, visiting);
                }
                sb.append(']');
            } else if (o.getClass().isArray()) {
                sb.append('[');
                int n = Array.getLength(o);
                for (int i = 0; i < n; i++) {
                    if (i > 0) sb.append(',');
                    write(Array.get(o, i), sb, visiting);
                }
                sb.append(']');
            } else {
                writeBean(o, sb, visiting);
            }
        } finally {
            visiting.remove(o);
        }
    }

    /** Objet quelconque : on sérialise ses getters publics (getX / isX). */
    private static void writeBean(Object o, StringBuilder sb, Set<Object> visiting) {
        List<Method> getters = new ArrayList<>();
        for (Method m : o.getClass().getMethods()) {
            if (m.getParameterCount() != 0 || Modifier.isStatic(m.getModifiers())) continue;
            if (m.getDeclaringClass() == Object.class) continue;
            String n = m.getName();
            boolean isGet = n.startsWith("get") && n.length() > 3;
            boolean isIs = n.startsWith("is") && n.length() > 2
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class);
            if (isGet || isIs) getters.add(m);
        }
        getters.sort((a, b) -> a.getName().compareTo(b.getName()));

        sb.append('{');
        boolean first = true;
        for (Method m : getters) {
            Object value;
            try {
                m.setAccessible(true);
                value = m.invoke(o);
            } catch (Exception e) {
                continue;
            }
            String n = m.getName();
            String raw = n.startsWith("get") ? n.substring(3) : n.substring(2);
            String key = Character.toLowerCase(raw.charAt(0)) + raw.substring(1);
            if (!first) sb.append(',');
            first = false;
            quote(key, sb);
            sb.append(':');
            write(value, sb, visiting);
        }
        sb.append('}');
    }

    private static void quote(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c);
            }
        }
        sb.append('"');
    }
}
