package client.command.external;

import client.MapleCharacter;
import client.MapleClient;
import client.command.AdminCommands;
import client.command.DeveloperCommands;
import client.command.DonorCommands;
import client.command.EnumeratedCommands;
import client.command.GMCommands;
import client.command.PlayerCommands;
import client.command.SupportCommands;

public class EnumeratedCommandProcessor extends AbstractCommandProcessor {
	@Override
	public void execute(MapleClient c, String[] sub, char heading) {
		MapleCharacter chr = c.getPlayer();
		if (heading == '@' || heading == '/') {
			boolean commandExecuted = true;
			switch (chr.gmLevel()) {
				case 5:
				case 4:
				case 3: 
				case 2:
				case 1:
					commandExecuted = DonorCommands.execute(c, sub, heading);
					if (commandExecuted) break;
				case 0:
					commandExecuted = PlayerCommands.execute(c, sub, heading);
					if (commandExecuted) break;
				default:
					EnumeratedCommands.execute(c, sub, heading);
					break;
			}
		} else {
			boolean commandExecuted = false;
			switch (chr.gmLevel()) {
				case 5:
					commandExecuted = AdminCommands.execute(c, sub, heading);
					if (commandExecuted) break;
				case 4:
					commandExecuted = DeveloperCommands.execute(c, sub, heading);
					if (commandExecuted) break;
				case 3:
					commandExecuted = GMCommands.execute(c, sub, heading);
					if (commandExecuted) break;
				case 2:
					commandExecuted = SupportCommands.execute(c, sub, heading);
					if (commandExecuted) break;
				default:
					EnumeratedCommands.execute(c, sub, heading);
					break;
			}
		}
	}

}
