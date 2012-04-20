package gm;

/**
 *
 * @author kevintjuh93
 */
public enum GMSendOpcode {
    LOGIN_RESPONSE(0x00),
    CHAT(0x01),
    GM_LIST(0x02),
    SEND_PLAYER_LIST(0x03),
    COMMAND_RESPONSE(0x04),
    ;
    private int code = -2;

    private GMSendOpcode(int code) {
        this.code = code;
    }

    public int getValue() {
        return code;
    }
}
