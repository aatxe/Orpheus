package gm.server.handler;

import client.MapleCharacter;
import gm.GMPacketCreator;
import gm.GMPacketHandler;
import net.server.Server;
import net.server.World;
import org.apache.mina.core.session.IoSession;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public class CommandHandler implements GMPacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, IoSession session) {
        byte command = slea.readByte();
        switch (command) {     
            case 0: {//notice
                for (World world : Server.getInstance().getWorlds()) {
                    world.broadcastPacket(MaplePacketCreator.serverNotice(0, slea.readMapleAsciiString()));
                }                
                break;
            }
            case 1: {//server message
                for (World world : Server.getInstance().getWorlds()) {
                    world.setServerMessage(slea.readMapleAsciiString());
                }
                break; 
            }
            case 2: {       
                Server server = Server.getInstance();
                byte worldid = slea.readByte();
                if (worldid >= server.getWorlds().size()) {
                    session.write(GMPacketCreator.commandResponse((byte) 2));
                    return;//incorrect world
                }
                World world = server.getWorld(worldid);
                switch (slea.readByte()) {
                    case 0:
                        world.setExpRate(slea.readByte());
                        break;
                    case 1:
                        world.setDropRate(slea.readByte());
                        break;
                    case 2:
                        world.setMesoRate(slea.readByte());
                        break;                        
                }
                for (MapleCharacter chr : world.getPlayerStorage().getAllCharacters()) {
                    chr.setRates();
                }
            }
            case 3: {
                String user = slea.readMapleAsciiString();
                for (World world : Server.getInstance().getWorlds()) {
                    if (world.isConnected(user)) {
                        world.getPlayerStorage().getCharacterByName(user).getClient().disconnect();
                        session.write(GMPacketCreator.commandResponse((byte) 1));
                        return;
                    }
                }
                session.write(GMPacketCreator.commandResponse((byte) 0));
                break;
            }
            case 4: {
                String user = slea.readMapleAsciiString();
                for (World world : Server.getInstance().getWorlds()) {
                    if (world.isConnected(user)) {
                        MapleCharacter chr = world.getPlayerStorage().getCharacterByName(user);
                        chr.ban(slea.readMapleAsciiString());
                        chr.sendPolice("You have been blocked by #b" + session.getAttribute("NAME") + " #kfor the HACK reason.");
                        session.write(GMPacketCreator.commandResponse((byte) 1));
                        return;
                    }
                }
                session.write(GMPacketCreator.commandResponse((byte) 0));
                break;
            }
            case 5: {
                String user = slea.readMapleAsciiString();
                for (World world : Server.getInstance().getWorlds()) {
                    if (world.isConnected(user)) {
                        MapleCharacter chr = world.getPlayerStorage().getCharacterByName(user);
                        String job = chr.getJob().name() + " (" + chr.getJob().getId() + ")";
                        session.write(GMPacketCreator.playerStats(user, job, (byte) chr.getLevel(), chr.getExp(), (short) chr.getMaxHp(), (short) chr.getMaxMp(),
                                (short) chr.getStr(), (short) chr.getDex(), (short) chr.getInt(), (short) chr.getLuk(), chr.getMeso()));
                        return;
                    }
                }
                session.write(GMPacketCreator.commandResponse((byte) 0));                
            }
            case 7: {
                Server.getInstance().shutdown(false).run();
            }
            case 8: {
                Server.getInstance().shutdown(true).run();
            }                
        }
    }
    
}
