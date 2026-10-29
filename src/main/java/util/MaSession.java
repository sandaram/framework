package util;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MaSession {
    private String id;
    private Map<String, Object> attributes = new HashMap<>();
    private Set<String> initialKeys = new HashSet<>();

    public MaSession() {}

    /**
     * Crée une instance de MaSession à partir du HttpSession existant.
     */
    public static MaSession from(HttpSession httpSession) {
        MaSession session = new MaSession();
        if (httpSession != null) {
            session.id = httpSession.getId();
            httpSession.getAttributeNames().asIterator().forEachRemaining(name -> {
                Object value = httpSession.getAttribute(name);
                session.attributes.put(name, value);
                session.initialKeys.add(name);
            });
        }
        return session;
    }

    /**
     * Met à jour le HttpSession réel avec les modifications de MaSession.
     */
    public void flushTo(HttpSession httpSession) {
        if (httpSession == null) return;

        // 1. Supprimer du HttpSession les clés initiales qui n'existent plus dans MaSession
        for (String key : initialKeys) {
            if (!attributes.containsKey(key)) {
                httpSession.removeAttribute(key);
            }
        }

        // 2. Mettre à jour ou ajouter les attributs de MaSession vers HttpSession
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    // --- Fonctions demandées pour manipuler la session côté développeur ---

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    // --- Getters et Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Set<String> getInitialKeys() {
        return initialKeys;
    }

    public void setInitialKeys(Set<String> initialKeys) {
        this.initialKeys = initialKeys;
    }
}