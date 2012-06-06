/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

/**
 * 
 * @author Danny (Leifde)
 */
public class PetDataFactory {
	private static MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
	private static Map<String, PetCommand> petCommands = new HashMap<String, PetCommand>();
	private static Map<Integer, Integer> petHunger = new HashMap<Integer, Integer>();

	public static PetCommand getPetCommand(int petId, int skillId) {
		PetCommand ret = petCommands.get(Integer.valueOf(petId) + "" + skillId);
		if (ret != null) {
			return ret;
		}
		synchronized (petCommands) {
			ret = petCommands.get(petId + "" + skillId);
			if (ret == null) {
				MapleData skillData = dataRoot.getData("Pet/" + petId + ".img");
				int prob = 0;
				int inc = 0;
				if (skillData != null) {
					prob = MapleDataTool.getInt("interact/" + skillId + "/prob", skillData, 0);
					inc = MapleDataTool.getInt("interact/" + skillId + "/inc", skillData, 0);
				}
				ret = new PetCommand(petId, skillId, prob, inc);
				petCommands.put(petId + "" + skillId, ret);
			}
			return ret;
		}
	}

	public static int getHunger(int petId) {
		Integer ret = petHunger.get(Integer.valueOf(petId));
		if (ret != null) {
			return ret;
		}
		synchronized (petHunger) {
			ret = petHunger.get(Integer.valueOf(petId));
			if (ret == null) {
				ret = Integer.valueOf(MapleDataTool.getInt(dataRoot.getData("Pet/" + petId + ".img").getChildByPath("info/hungry"), 1));
			}
			return ret;
		}
	}
}
