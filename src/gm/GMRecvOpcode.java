package gm;

/**
 *
 * @author kevintjuh93
 */
public enum GMRecvOpcode {

    LOGIN(0x00),
    GM_CHAT(0x01),
    PLAYER_LIST(0x02),
    COMMAND(0x03),
    ;
    private int code = -2;

    private GMRecvOpcode(int code) {
        this.code = code;
    }

    public int getValue() {
        return code;
    }
}
