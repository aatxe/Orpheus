package gm;

import gm.server.handler.*;

/**
 *
 * @author kevintjuh93
 */
public final class GMPacketProcessor {
    private GMPacketHandler[] handlers;

    public GMPacketProcessor() {
        int maxRecvOp = 0;
        for (GMRecvOpcode op : GMRecvOpcode.values()) {
            if (op.getValue() > maxRecvOp) {
                maxRecvOp = op.getValue();
            }
        }
        handlers = new GMPacketHandler[maxRecvOp + 1];
        reset();
    }

    public GMPacketHandler getHandler(short packetId) {
        if (packetId > handlers.length) {
            return null;
        }
        GMPacketHandler handler = handlers[packetId];
        if (handler != null) {
            return handler;
        }
        return null;
    }

    public void registerHandler(GMRecvOpcode code, GMPacketHandler handler) {
        try {
            handlers[code.getValue()] = handler;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error registering handler - " + code.name());
        }
    }

    public void reset() {
        handlers = new GMPacketHandler[handlers.length];
        registerHandler(GMRecvOpcode.LOGIN, new LoginHandler());
        registerHandler(GMRecvOpcode.GM_CHAT, new ChatHandler());
        registerHandler(GMRecvOpcode.PLAYER_LIST, new PlayerListHandler());
        registerHandler(GMRecvOpcode.COMMAND, new CommandHandler());
    }
}
