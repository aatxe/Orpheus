/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.expeditions;

/**
 *
 * @author kevintjuh93
 */
public enum MapleExpeditionType {
    UNDEFINED(-1),
    BALROG_EASY(0),
    BALROG_NORMAL(1),
    ZAKUM(2),
    HORNTAIL(3),
    CHAOS_ZAKUM(4),
    CHAOS_HORNTAIL(5),
    PINKBEAN(6);
    final int exped;
    final int limit;

    private MapleExpeditionType(int id) {
        exped = id;
        limit = 30;
    }

    private MapleExpeditionType(int id, int l) {
        exped = id;
        limit = l;
    }

    public int getId() {
        return exped;
    }

    public int getLimit() {
        return limit;
    }

    public static MapleExpeditionType getExpeditionById(int id) {
        for (MapleExpeditionType l : MapleExpeditionType.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return MapleExpeditionType.UNDEFINED;
    }
}
