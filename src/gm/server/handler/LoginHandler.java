package gm.server.handler;

import client.MapleClient;
import gm.GMPacketCreator;
import gm.GMPacketHandler;
import gm.server.GMServer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.mina.core.session.IoSession;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public class LoginHandler implements GMPacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, IoSession session) {
        if (!GMServer.KEYWORD.equals(slea.readMapleAsciiString())) {
            session.write(GMPacketCreator.sendLoginResponse((byte) -1, null));
            return;
        }
        GMServer server = GMServer.getInstance();
        String login = slea.readMapleAsciiString();
        if (server.contains(login)) {
            session.write(GMPacketCreator.sendLoginResponse((byte) 0, null));
            return;
        }
        String password = slea.readMapleAsciiString();

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = DatabaseConnection.getConnection();
        try {
            ps = con.prepareStatement("SELECT `password`, `id` FROM `accounts` WHERE `name` = ? AND `gm` >= 2");
            ps.setString(1, login);
            rs = ps.executeQuery();
            if (rs.next()) {
                String pw = rs.getString("password");
                if (password.equals(pw) || MapleClient.checkHash(pw, "SHA-1", password)) {
                    /*
                    int accid = rs.getInt("id");
                    ps.close();
                    rs.close();
                    ps = con.prepareStatement("SELECT `name` FROM `characters` WHERE `accountid` = ? AND `gm` >= 2");
                    ps.setInt(1, accid);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                    String name = rs.getString("name");
                    session.setAttribute("NAME", login);
                    server.addOutGame(login, session);
                    session.write(GMPacketCreator.sendLoginResponse((byte) 3, login));
                    server.broadcastOutGame(GMPacketCreator.chat(login + " has logged in."), login);
                    server.broadcastOutGame(GMPacketCreator.addUser(login), login);
                    session.write(GMPacketCreator.sendUserList(server.getUserList(login)));
                    return;
                    } else {
                    session.write(GMPacketCreator.sendLoginResponse((byte) 2, name));
                    return;
                    }*/
                    session.setAttribute("NAME", login);
                    server.addOutGame(login, session);
                    session.write(GMPacketCreator.sendLoginResponse((byte) 3, login));
                    server.broadcastOutGame(GMPacketCreator.chat(login + " has logged in."), login);
                    server.broadcastOutGame(GMPacketCreator.addUser(login), login);
                    session.write(GMPacketCreator.sendUserList(server.getUserList(login)));
                    return;
                }
            }
            session.write(GMPacketCreator.sendLoginResponse((byte) 1, null));
        } catch (SQLException e) {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
            }
            session.write(GMPacketCreator.sendLoginResponse((byte) 1, null));
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
            }
        }
    }
}
