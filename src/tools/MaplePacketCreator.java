/*
 	OrpheusMS: MapleStory Private Server based on OdinMS
    Copyright (C) 2012 Aaron Weiss <aaron@deviant-core.net>
    				Patrick Huy <patrick.huy@frz.cc>
					Matthias Butz <matze@odinms.de>
					Jan Christian Meyer <vimes@odinms.de>

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
package tools;

import java.awt.Point;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import client.BuddylistEntry;
import client.IEquip;
import client.IEquip.ScrollResult;
import client.IItem;
import client.ISkill;
import client.Item;
import client.ItemFactory;
import client.ItemInventoryEntry;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDiseaseEntry;
import client.MapleFamilyEntry;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleKeyBinding;
import client.MapleMount;
import client.MaplePet;
import client.MapleQuestStatus;
import client.MapleRing;
import client.MapleStat;
import client.MapleStatDelta;
import client.SkillMacro;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ItemConstants;
import constants.ServerConstants;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import net.LongValueHolder;
import net.MaplePacket;
import net.SendOpcode;
import net.server.handlers.channel.PlayerInteractionHandler;
import net.server.handlers.channel.SummonDamageHandler.SummonAttackEntry;
import net.server.MapleParty;
import net.server.MaplePartyCharacter;
import net.server.PartyOperation;
import net.server.PlayerCoolDownValueHolder;
import net.server.Server;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleGuildSummary;
import server.CashShop.CashItem;
import server.CashShop.CashItemFactory;
import server.CashShop.SpecialCashItem;
import server.DueyPackages;
import server.GiftEntry;
import server.MTSItemInfo;
import server.MapleBuffStatDelta;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePlayerShopItem;
import server.MapleShopItem;
import server.MapleTrade;
import server.WorldRecommendation;
import server.events.gm.MapleSnowball;
import server.partyquest.MonsterCarnivalParty;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MobSkill;
import server.life.NpcDescriptionEntry;
import server.maps.HiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMist;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.movement.LifeMovementFragment;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * 
 * @author Frz
 */
public class MaplePacketCreator {

	private final static byte[] CHAR_INFO_MAGIC = new byte[] {(byte) 0xff, (byte) 0xc9, (byte) 0x9a, 0x3b};
	public static final List<MapleStatDelta> EMPTY_STATUPDATE = Collections.emptyList();
	private final static byte[] ITEM_MAGIC = new byte[] {(byte) 0x80, 0x05};
	private final static int ITEM_YEAR2000 = -1085019342;
	private final static long REAL_YEAR2000 = 946681229830L;

	public static int getItemTimestamp(long realTimestamp) {
		int time = (int) ((realTimestamp - REAL_YEAR2000) / 1000 / 60); // convert
																		// to
																		// minutes
		return (int) (time * 35.762787) + ITEM_YEAR2000;
	}

	private static int getQuestTimestamp(long realTimestamp) {
		return (int) (((int) (realTimestamp / 1000 / 60)) * 0.1396987) + 27111908;
	}

	private static long getKoreanTimestamp(long realTimestamp) {
		return realTimestamp * 10000 + 116444592000000000L;
	}

	private static long getTime(long realTimestamp) {
		return realTimestamp * 10000 + 116444592000000000L;
	}

	private static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.writeInt(chr.getId()); // character id
		mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 13));
		mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
		mplew.write(chr.getSkinColor().getId()); // skin color
		mplew.writeInt(chr.getFace()); // face
		mplew.writeInt(chr.getHair()); // hair

		for (int i = 0; i < 3; i++) {
			if (chr.getPet(i) != null) // Checked GMS.. and your pets stay when
										// going into the cash shop.
			{
				mplew.writeLong(chr.getPet(i).getUniqueId());
			} else {
				mplew.writeLong(0);
			}
		}

		mplew.write(chr.getLevel()); // level
		mplew.writeShort(chr.getJob().getId()); // job
		mplew.writeShort(chr.getStr()); // str
		mplew.writeShort(chr.getDex()); // dex
		mplew.writeShort(chr.getInt()); // int
		mplew.writeShort(chr.getLuk()); // luk
		mplew.writeShort(chr.getHp()); // hp (?)
		mplew.writeShort(chr.getMaxHp()); // maxhp
		mplew.writeShort(chr.getMp()); // mp (?)
		mplew.writeShort(chr.getMaxMp()); // maxmp
		mplew.writeShort(chr.getRemainingAp()); // remaining ap
		mplew.writeShort(chr.getRemainingSp()); // remaining sp
		mplew.writeInt(chr.getExp()); // current exp
		mplew.writeShort(chr.getFame()); // fame
		mplew.writeInt(chr.getGachaExp()); // Gacha Exp
		mplew.writeInt(chr.getMapId()); // current map id
		mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
		mplew.writeInt(0);
	}

	private static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
		mplew.write(chr.getGender());
		mplew.write(chr.getSkinColor().getId()); // skin color
		mplew.writeInt(chr.getFace()); // face
		mplew.write(mega ? 0 : 1);
		mplew.writeInt(chr.getHair()); // hair
		addCharEquips(mplew, chr);
	}

	private static void addCharacterInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.writeLong(-1);
		mplew.write(0);
		addCharStats(mplew, chr);
		mplew.write(chr.getBuddylist().getCapacity());

		if (chr.getLinkedName() == null) {
			mplew.write(0);
		} else {
			mplew.write(1);
			mplew.writeMapleAsciiString(chr.getLinkedName());
		}

		mplew.writeInt(chr.getMeso());
		addInventoryInfo(mplew, chr);
		addSkillInfo(mplew, chr);
		addQuestInfo(mplew, chr);
		mplew.writeShort(0);
		addRingInfo(mplew, chr);
		addTeleportInfo(mplew, chr);
		addMonsterBookInfo(mplew, chr);
		mplew.writeShort(0);
		mplew.writeInt(0);
	}

	private static void addTeleportInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		final int[] tele = chr.getTrockMaps();
		final int[] viptele = chr.getVipTrockMaps();
		for (int i = 0; i < 5; i++) {
			mplew.writeInt(tele[i]);
		}
		for (int i = 0; i < 10; i++) {
			mplew.writeInt(viptele[i]);
		}
	}

	private static void addCharEquips(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
		Collection<IItem> ii = MapleItemInformationProvider.getInstance().canWearEquipment(chr, equip.list());
		Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
		Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
		for (IItem item : ii) {
			byte pos = (byte) (item.getPosition() * -1);
			if (pos < 100 && myEquip.get(pos) == null) {
				myEquip.put(pos, item.getItemId());
			} else if (pos > 100 && pos != 111) { // don't ask. o.o
				pos -= 100;
				if (myEquip.get(pos) != null) {
					maskedEquip.put(pos, myEquip.get(pos));
				}
				myEquip.put(pos, item.getItemId());
			} else if (myEquip.get(pos) != null) {
				maskedEquip.put(pos, item.getItemId());
			}
		}
		for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		IItem cWeapon = equip.getItem((byte) -111);
		mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
		for (int i = 0; i < 3; i++) {
			if (chr.getPet(i) != null) {
				mplew.writeInt(chr.getPet(i).getItemId());
			} else {
				mplew.writeInt(0);
			}
		}
	}

	private static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean viewall) {
		addCharStats(mplew, chr);
		addCharLook(mplew, chr, false);
		if (!viewall) {
			mplew.write(0);
		}
		if (chr.isGM()) {
			mplew.write(0);
			return;
		}
		mplew.write(1); // world rank enabled (next 4 ints are not sent if
						// disabled) Short??
		mplew.writeInt(chr.getRank()); // world rank
		mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
		mplew.writeInt(chr.getJobRank()); // job rank
		mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)
	}

	private static void addQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.writeShort(chr.getStartedQuestsSize());
		for (MapleQuestStatus q : chr.getStartedQuests()) {
			mplew.writeShort(q.getQuest().getId());
			mplew.writeMapleAsciiString(q.getQuestData());
			if (q.getQuest().getInfoNumber() > 0) {
				mplew.writeShort(q.getQuest().getInfoNumber());
				mplew.writeMapleAsciiString(Integer.toString(q.getMedalProgress()));
			}
		}
		List<MapleQuestStatus> completed = chr.getCompletedQuests();
		mplew.writeShort(completed.size());
		for (MapleQuestStatus q : completed) {
			mplew.writeShort(q.getQuest().getId());
			int time = getQuestTimestamp(q.getCompletionTime());
			mplew.writeInt(time);
			mplew.writeInt(time);
		}
	}

	private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item) {
		addItemInfo(mplew, item, false);
	}

	private static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time) {
		addExpirationTime(mplew, time, true);
	}

	private static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time, boolean addzero) {
		if (addzero) {
			mplew.write(0);
		}
		mplew.write(ITEM_MAGIC);
		if (time == -1) {
			mplew.writeInt(400967355);
			mplew.write(2);
		} else {
			mplew.writeInt(getItemTimestamp(time));
			mplew.write(1);
		}
	}

	private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		boolean isCash = ii.isCash(item.getItemId());
		boolean isPet = item.getPetId() > -1;
		boolean isRing = false;
		IEquip equip = null;
		byte pos = item.getPosition();
		if (item.getType() == IItem.EQUIP) {
			equip = (IEquip) item;
			isRing = equip.getRingId() > -1;
		}
		if (!zeroPosition) {
			if (equip != null) {
				if (pos < 0) {
					pos *= -1;
				}
				mplew.writeShort(pos > 100 ? pos - 100 : pos);
			} else {
				mplew.write(pos);
			}
		}
		mplew.write(item.getType());
		mplew.writeInt(item.getItemId());
		mplew.write(isCash ? 1 : 0);
		if (isCash) {
			mplew.writeLong(isPet ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
		}
		addExpirationTime(mplew, item.getExpiration());
		if (isPet) {
			MaplePet pet = item.getPet();
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(pet.getName(), '\0', 13));
			mplew.write(pet.getLevel());
			mplew.writeShort(pet.getCloseness());
			mplew.write(pet.getFullness());
			addExpirationTime(mplew, item.getExpiration());
			mplew.writeInt(0);
			mplew.write(new byte[] {(byte) 0x50, (byte) 0x46}); // wonder what
																// this is
			mplew.writeInt(0);
			return;
		}
		if (equip == null) {
			mplew.writeShort(item.getQuantity());
			mplew.writeMapleAsciiString(item.getOwner());
			mplew.writeShort(item.getFlag()); // flag

			if (ItemConstants.isRechargable(item.getItemId())) {
				mplew.writeInt(2);
				mplew.write(new byte[] {(byte) 0x54, 0, 0, (byte) 0x34});
			}
			return;
		}
		mplew.write(equip.getUpgradeSlots()); // upgrade slots
		mplew.write(equip.getLevel()); // level
		mplew.writeShort(equip.getStr()); // str
		mplew.writeShort(equip.getDex()); // dex
		mplew.writeShort(equip.getInt()); // int
		mplew.writeShort(equip.getLuk()); // luk
		mplew.writeShort(equip.getHp()); // hp
		mplew.writeShort(equip.getMp()); // mp
		mplew.writeShort(equip.getWatk()); // watk
		mplew.writeShort(equip.getMatk()); // matk
		mplew.writeShort(equip.getWdef()); // wdef
		mplew.writeShort(equip.getMdef()); // mdef
		mplew.writeShort(equip.getAcc()); // accuracy
		mplew.writeShort(equip.getAvoid()); // avoid
		mplew.writeShort(equip.getHands()); // hands
		mplew.writeShort(equip.getSpeed()); // speed
		mplew.writeShort(equip.getJump()); // jump
		mplew.writeMapleAsciiString(equip.getOwner()); // owner name
		mplew.writeShort(equip.getFlag()); // Item Flags

		if (isCash) {
			for (int i = 0; i < 10; i++) {
				mplew.write(0x40);
			}
		} else {
			mplew.write(0);
			mplew.write(equip.getItemLevel()); // Item Level
			mplew.writeShort(0);
			mplew.writeShort(equip.getItemExp()); // Works pretty weird :s
			mplew.writeInt(equip.getVicious()); // WTF NEXON ARE YOU SERIOUS?
			mplew.writeLong(0);
		}
		mplew.write(new byte[] {0, (byte) 0x40, (byte) 0xE0, (byte) 0xFD, (byte) 0x3B, (byte) 0x37, (byte) 0x4F, 1});
		mplew.writeInt(-1);

	}

	private static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		for (byte i = 1; i <= 5; i++) {
			mplew.write(chr.getInventory(MapleInventoryType.getByType(i)).getSlotLimit());
		}
		mplew.write(new byte[] {0, (byte) 0x40, (byte) 0xE0, (byte) 0xFD, (byte) 0x3B, (byte) 0x37, (byte) 0x4F, 1});
		MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
		Collection<IItem> equippedC = iv.list();
		List<Item> equipped = new ArrayList<Item>(equippedC.size());
		List<Item> equippedCash = new ArrayList<Item>(equippedC.size());
		for (IItem item : equippedC) {
			if (item.getPosition() <= -100) {
				equippedCash.add((Item) item);
			} else {
				equipped.add((Item) item);
			}
		}
		Collections.sort(equipped);
		for (Item item : equipped) {
			addItemInfo(mplew, item);
		}
		mplew.writeShort(0); // start of equip cash
		for (Item item : equippedCash) {
			addItemInfo(mplew, item);
		}
		mplew.writeShort(0); // start of equip inventory
		for (IItem item : chr.getInventory(MapleInventoryType.EQUIP).list()) {
			addItemInfo(mplew, item);
		}
		mplew.writeInt(0);
		for (IItem item : chr.getInventory(MapleInventoryType.USE).list()) {
			addItemInfo(mplew, item);
		}
		mplew.write(0);
		for (IItem item : chr.getInventory(MapleInventoryType.SETUP).list()) {
			addItemInfo(mplew, item);
		}
		mplew.write(0);
		for (IItem item : chr.getInventory(MapleInventoryType.ETC).list()) {
			addItemInfo(mplew, item);
		}
		mplew.write(0);
		for (IItem item : chr.getInventory(MapleInventoryType.CASH).list()) {
			addItemInfo(mplew, item);
		}
	}

	private static void addSkillInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.write(0); // start of skills
		Map<ISkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
		mplew.writeShort(skills.size());
		for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
			mplew.writeInt(skill.getKey().getId());
			mplew.writeInt(skill.getValue().skillevel);
			addExpirationTime(mplew, skill.getValue().expiration);
			if (skill.getKey().isFourthJob()) {
				mplew.writeInt(skill.getValue().masterlevel);
			}
		}
		mplew.writeShort(chr.getAllCooldowns().size());
		for (PlayerCoolDownValueHolder cooling : chr.getAllCooldowns()) {
			mplew.writeInt(cooling.skillId);
			int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
			mplew.writeShort(timeLeft / 1000);
		}
	}

	private static void addMonsterBookInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.writeInt(chr.getMonsterBookCover()); // cover
		mplew.write(0);
		Map<Integer, Integer> cards = chr.getMonsterBook().getCards();
		mplew.writeShort(cards.size());
		for (Entry<Integer, Integer> all : cards.entrySet()) {
			mplew.writeShort(all.getKey() % 10000); // Id
			mplew.write(all.getValue()); // Level
		}
	}

	public static MaplePacket sendGuestTOS() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SEND_LINK.getValue());
		mplew.writeShort(0x100);
		mplew.writeInt(Randomizer.nextInt(999999));
		mplew.writeLong(0);
		mplew.write(new byte[] {(byte) 0x40, (byte) 0xE0, (byte) 0xFD, (byte) 0x3B, (byte) 0x37, (byte) 0x4F, 1});
		mplew.writeLong(getKoreanTimestamp(System.currentTimeMillis()));
		mplew.writeInt(0);
		mplew.writeMapleAsciiString("http://maplefags.com");
		return mplew.getPacket();
	}

	/**
	 * Sends a hello packet.
	 * 
	 * @param mapleVersion
	 *            The maple client version.
	 * @param sendIv
	 *            the IV used by the server for sending
	 * @param recvIv
	 *            the IV used by the server for receiving
	 * @return
	 */
	public static MaplePacket getHello(short mapleVersion, byte[] sendIv, byte[] recvIv) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
		mplew.writeShort(0x0E);
		mplew.writeShort(mapleVersion);
		mplew.writeShort(1);
		mplew.write(49);
		mplew.write(recvIv);
		mplew.write(sendIv);
		mplew.write(8);
		return mplew.getPacket();
	}

	/**
	 * Sends a ping packet.
	 * 
	 * @return The packet.
	 */
	public static MaplePacket getPing() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
		mplew.writeShort(SendOpcode.PING.getValue());
		return mplew.getPacket();
	}

	/**
	 * Gets a login failed packet.
	 * 
	 * Possible values for <code>reason</code>:<br>
	 * 3: ID deleted or blocked<br>
	 * 4: Incorrect password<br>
	 * 5: Not a registered id<br>
	 * 6: System error<br>
	 * 7: Already logged in<br>
	 * 8: System error<br>
	 * 9: System error<br>
	 * 10: Cannot process so many connections<br>
	 * 11: Only users older than 20 can use this channel<br>
	 * 13: Unable to log on as master at this ip<br>
	 * 14: Wrong gateway or personal info and weird korean button<br>
	 * 15: Processing request with that korean button!<br>
	 * 16: Please verify your account through email...<br>
	 * 17: Wrong gateway or personal info<br>
	 * 21: Please verify your account through email...<br>
	 * 23: License agreement<br>
	 * 25: Maple Europe notice =[ FUCK YOU NEXON<br>
	 * 27: Some weird full client notice, probably for trial versions<br>
	 * 
	 * @param reason
	 *            The reason logging in failed.
	 * @return The login failed packet.
	 */
	public static MaplePacket getLoginFailed(int reason) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
		mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
		mplew.writeInt(reason);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	/**
	 * Gets a login failed packet.
	 * 
	 * Possible values for <code>reason</code>:<br>
	 * 2: ID deleted or blocked<br>
	 * 3: ID deleted or blocked<br>
	 * 4: Incorrect password<br>
	 * 5: Not a registered id<br>
	 * 6: Trouble logging into the game?<br>
	 * 7: Already logged in<br>
	 * 8: Trouble logging into the game?<br>
	 * 9: Trouble logging into the game?<br>
	 * 10: Cannot process so many connections<br>
	 * 11: Only users older than 20 can use this channel<br>
	 * 12: Trouble logging into the game?<br>
	 * 13: Unable to log on as master at this ip<br>
	 * 14: Wrong gateway or personal info and weird korean button<br>
	 * 15: Processing request with that korean button!<br>
	 * 16: Please verify your account through email...<br>
	 * 17: Wrong gateway or personal info<br>
	 * 21: Please verify your account through email...<br>
	 * 23: Crashes<br>
	 * 25: Maple Europe notice =[ FUCK YOU NEXON<br>
	 * 27: Some weird full client notice, probably for trial versions<br>
	 * 
	 * @param reason
	 *            The reason logging in failed.
	 * @return The login failed packet.
	 */
	public static MaplePacket getAfterLoginError(int reason) {// same as above
																// o.o
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
		mplew.writeShort(SendOpcode.AFTER_LOGIN_ERROR.getValue());
		mplew.writeShort(reason);// using other types then stated above = CRASH
		return mplew.getPacket();
	}

	public static MaplePacket sendPolice(int reason, String reasoning, int duration) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GM_POLICE.getValue());
		mplew.writeInt(duration);
		mplew.write(4); // Hmmm
		mplew.write(reason);
		mplew.writeMapleAsciiString(reasoning);
		return mplew.getPacket();
	}

	public static MaplePacket sendPolice(String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAPLE_ADMIN.getValue());
		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}

	public static MaplePacket getPermBan(byte reason) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
		mplew.writeShort(2); // Account is banned
		mplew.write(0);
		mplew.write(reason);
		mplew.write(new byte[] {1, 1, 1, 1, 0});

		return mplew.getPacket();
	}

	public static MaplePacket getTempBan(long timestampTill, byte reason) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
		mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
		mplew.write(2);
		mplew.write0(5);
		mplew.write(reason);
		mplew.writeLong(timestampTill); // Tempban date is handled as a 64-bit
										// long, number of 100NS intervals since
										// 1/1/1601. Lulz.

		return mplew.getPacket();
	}

	/**
	 * Gets a successful authentication and PIN Request packet.
	 * 
	 * @param c
	 * @param account
	 *            The account name.
	 * @return The PIN request packet.
	 */
	public static MaplePacket getAuthSuccess(MapleClient c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
		mplew.writeInt(0);
		mplew.writeShort(0);
		mplew.writeInt(c.getAccID()); // user id
		mplew.write(c.getGender());
		mplew.write((c.gmLevel() > 0 ? 1 : 0)); // admin byte
		mplew.write(0);
		mplew.write(0);
		mplew.writeMapleAsciiString(c.getAccountName());
		mplew.write(0);
		mplew.write(0); // isquietbanned
		mplew.writeLong(0);
		mplew.writeLong(0); // creation time
		mplew.writeInt(0);
		mplew.writeShort(2);// PIN

		return mplew.getPacket();
	}

	/**
	 * Gets a packet detailing a PIN operation.
	 * 
	 * Possible values for <code>mode</code>:<br>
	 * 0 - PIN was accepted<br>
	 * 1 - Register a new PIN<br>
	 * 2 - Invalid pin / Reenter<br>
	 * 3 - Connection failed due to system error<br>
	 * 4 - Enter the pin
	 * 
	 * @param mode
	 *            The mode.
	 * @return
	 */
	private static MaplePacket pinOperation(byte mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PIN_OPERATION.getValue());
		mplew.write(mode);
		return mplew.getPacket();
	}

	public static MaplePacket pinRegistered() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PIN_ASSIGNED.getValue());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket requestPin() {
		return pinOperation((byte) 4);
	}

	public static MaplePacket requestPinAfterFailure() {
		return pinOperation((byte) 2);
	}

	public static MaplePacket registerPin() {
		return pinOperation((byte) 1);
	}

	public static MaplePacket pinAccepted() {
		return pinOperation((byte) 0);
	}

	public static MaplePacket wrongPic() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.WRONG_PIC.getValue());
		mplew.write(0);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet detailing a server and its channels.
	 * 
	 * @param serverId
	 * @param serverName
	 *            The name of the server.
	 * @param channelLoad
	 *            Load of the channel - 1200 seems to be max.
	 * @return The server info packet.
	 */
	public static MaplePacket getServerList(byte serverId, String serverName, byte flag, String eventmsg, Map<Byte, AtomicInteger> channelLoad) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERLIST.getValue());
		mplew.write(serverId);
		mplew.writeMapleAsciiString(serverName);
		mplew.write(flag);
		mplew.writeMapleAsciiString(eventmsg);
		mplew.write(0x64); // rate modifier, don't ask O.O!
		mplew.write(0x0); // event xp * 2.6 O.O!
		mplew.write(0x64); // rate modifier, don't ask O.O!
		mplew.write(0x0); // drop rate * 2.6
		mplew.write(0x0);
		int lastChannel = 1;
		Set<Byte> channels = channelLoad.keySet();
		for (byte i = 30; i > 0; i--) {
			if (channels.contains(i)) {
				lastChannel = i;
				break;
			}
		}
		mplew.write(lastChannel);
		int load;
		for (byte i = 1; i <= lastChannel; i++) {
			if (channels.contains(i)) {
				load = (channelLoad.get(i).get() * 1200) / ServerConstants.CHANNEL_LOAD; // lolwut
			} else {
				load = ServerConstants.CHANNEL_LOAD;
			}
			mplew.writeMapleAsciiString(serverName + "-" + i);
			mplew.writeInt(load);
			mplew.write(1);
			mplew.writeShort(i - 1);
		}
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet saying that the server list is over.
	 * 
	 * @return The end of server list packet.
	 */
	public static MaplePacket getEndOfServerList() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.SERVERLIST.getValue());
		mplew.write(0xFF);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet detailing a server status message.
	 * 
	 * Possible values for <code>status</code>:<br>
	 * 0 - Normal<br>
	 * 1 - Highly populated<br>
	 * 2 - Full
	 * 
	 * @param status
	 *            The server status.
	 * @return The server status packet.
	 */
	public static MaplePacket getServerStatus(int status) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.SERVERSTATUS.getValue());
		mplew.writeShort(status);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client the IP of the channel server.
	 * 
	 * @param inetAddr
	 *            The InetAddress of the requested channel server.
	 * @param port
	 *            The port the channel is on.
	 * @param clientId
	 *            The ID of the client.
	 * @return The server IP packet.
	 */
	public static MaplePacket getServerIP(InetAddress inetAddr, int port, int clientId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVER_IP.getValue());
		mplew.writeShort(0);
		byte[] addr = inetAddr.getAddress();
		mplew.write(addr);
		mplew.writeShort(port);
		mplew.writeInt(clientId);
		mplew.write(new byte[] {0, 0, 0, 0, 0});
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client the IP of the new channel.
	 * 
	 * @param inetAddr
	 *            The InetAddress of the requested channel server.
	 * @param port
	 *            The port the channel is on.
	 * @return The server IP packet.
	 */
	public static MaplePacket getChannelChange(InetAddress inetAddr, int port) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHANGE_CHANNEL.getValue());
		mplew.write(1);
		byte[] addr = inetAddr.getAddress();
		mplew.write(addr);
		mplew.writeShort(port);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet with a list of characters.
	 * 
	 * @param c
	 *            The MapleClient to load characters of.
	 * @param serverId
	 *            The ID of the server requested.
	 * @return The character list packet.
	 */
	public static MaplePacket getCharList(MapleClient c, int serverId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHARLIST.getValue());
		mplew.write(0);
		List<MapleCharacter> chars = c.loadCharacters(serverId);
		byte length = (byte) chars.size();
		for (MapleCharacter chr : chars) {
			if (chr.isHardcoreDead()) {
				length--;
			}
		}
		mplew.write(length);
		for (MapleCharacter chr : chars) {
			addCharEntry(mplew, chr, false);
		}
		if (ServerConstants.ENABLE_PIC) {
			mplew.write(c.getPic() == null || c.getPic().length() == 0 ? 0 : 1);
		} else {
			mplew.write(2);
		}

		mplew.writeInt(c.getCharacterSlots());
		return mplew.getPacket();
	}

	public static MaplePacket enableTV() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.ENABLE_TV.getValue());
		mplew.writeInt(0);
		mplew.write(0);
		return mplew.getPacket();
	}

	/**
	 * Removes TV
	 * 
	 * @return The Remove TV Packet
	 */
	public static MaplePacket removeTV() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
		mplew.writeShort(SendOpcode.REMOVE_TV.getValue());
		return mplew.getPacket();
	}

	/**
	 * Sends MapleTV
	 * 
	 * @param chr
	 *            The character shown in TV
	 * @param messages
	 *            The message sent with the TV
	 * @param type
	 *            The type of TV
	 * @param partner
	 *            The partner shown with chr
	 * @return the SEND_TV packet
	 */
	public static MaplePacket sendTV(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SEND_TV.getValue());
		mplew.write(partner != null ? 3 : 1);
		mplew.write(type); // Heart = 2 Star = 1 Normal = 0
		addCharLook(mplew, chr, false);
		mplew.writeMapleAsciiString(chr.getName());
		if (partner != null) {
			mplew.writeMapleAsciiString(partner.getName());
		} else {
			mplew.writeShort(0);
		}
		for (int i = 0; i < messages.size(); i++) {
			if (i == 4 && messages.get(4).length() > 15) {
				mplew.writeMapleAsciiString(messages.get(4).substring(0, 15));
			} else {
				mplew.writeMapleAsciiString(messages.get(i));
			}
		}
		mplew.writeInt(1337); // time limit shit lol 'Your thing still start in
								// blah blah seconds'
		if (partner != null) {
			addCharLook(mplew, partner, false);
		}
		return mplew.getPacket();
	}

	/**
	 * Gets character info for a character.
	 * 
	 * @param chr
	 *            The character to get info about.
	 * @return The character info packet.
	 */
	public static MaplePacket getCharInfo(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WARP_TO_MAP.getValue());
		mplew.writeInt(chr.getClient().getChannel() - 1);
		mplew.write(1);
		mplew.write(1);
		mplew.writeShort(0);
		for (int i = 0; i < 3; i++) {
			mplew.writeInt(Randomizer.nextInt());
		}
		addCharacterInfo(mplew, chr);
		mplew.writeLong(getTime(System.currentTimeMillis()));
		return mplew.getPacket();
	}

	/**
	 * Gets an empty stat update.
	 * 
	 * @return The empy stat update packet.
	 */
	public static MaplePacket enableActions() {
		return updatePlayerStats(EMPTY_STATUPDATE, true);
	}

	/**
	 * Gets an update for specified stats.
	 * 
	 * @param stats
	 *            The stats to update.
	 * @return The stat update packet.
	 */
	public static MaplePacket updatePlayerStats(List<MapleStatDelta> stats) {
		return updatePlayerStats(stats, false);
	}

	/**
	 * Gets an update for specified stats.
	 * 
	 * @param stats
	 *            The list of stats to update.
	 * @param itemReaction
	 *            Result of an item reaction(?)
	 * @return The stat update packet.
	 */
	public static MaplePacket updatePlayerStats(List<MapleStatDelta> stats, boolean itemReaction) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_STATS.getValue());
		mplew.write(itemReaction ? 1 : 0);
		int updateMask = 0;
		for (MapleStatDelta statupdate : stats) {
			updateMask |= statupdate.stat.getValue();
		}
		List<MapleStatDelta> mystats = stats;
		if (mystats.size() > 1) {
			Collections.sort(mystats, new Comparator<MapleStatDelta>() {

				@Override
				public int compare(MapleStatDelta o1, MapleStatDelta o2) {
					int val1 = o1.stat.getValue();
					int val2 = o2.stat.getValue();
					return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
				}
			});
		}
		mplew.writeInt(updateMask);
		for (MapleStatDelta statupdate : mystats) {
			if (statupdate.stat.getValue() >= 1) {
				if (statupdate.stat.getValue() == 0x1) {
					mplew.writeShort(statupdate.delta);
				} else if (statupdate.stat.getValue() <= 0x4) {
					mplew.writeInt(statupdate.delta);
				} else if (statupdate.stat.getValue() < 0x20) {
					mplew.write(statupdate.delta);
				} else if (statupdate.stat.getValue() < 0xFFFF) {
					mplew.writeShort(statupdate.delta);
				} else {
					mplew.writeInt(statupdate.delta);
				}
			}
		}
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client to change maps.
	 * 
	 * @param to
	 *            The <code>MapleMap</code> to warp to.
	 * @param spawnPoint
	 *            The spawn portal number to spawn at.
	 * @param chr
	 *            The character warping to <code>to</code>
	 * @return The map change packet.
	 */
	public static MaplePacket getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WARP_TO_MAP.getValue());
		mplew.writeInt(chr.getClient().getChannel() - 1);
		mplew.writeInt(0);// updated
		mplew.write(0);// updated
		mplew.writeInt(to.getId());
		mplew.write(spawnPoint);
		mplew.writeShort(chr.getHp());
		mplew.write(0);
		mplew.writeLong(getTime(System.currentTimeMillis()));
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to spawn a portal.
	 * 
	 * @param townId
	 *            The ID of the town the portal goes to.
	 * @param targetId
	 *            The ID of the target.
	 * @param pos
	 *            Where to put the portal.
	 * @return The portal spawn packet.
	 */
	public static MaplePacket spawnPortal(int townId, int targetId, Point pos) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);
		mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
		mplew.writeInt(townId);
		mplew.writeInt(targetId);
		if (pos != null) {
			mplew.writePos(pos);
		}
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to spawn a door.
	 * 
	 * @param oid
	 *            The door's object ID.
	 * @param pos
	 *            The position of the door.
	 * @param town
	 * @return The remove door packet.
	 */
	public static MaplePacket spawnDoor(int oid, Point pos, boolean town) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.SPAWN_DOOR.getValue());
		mplew.write(town ? 1 : 0);
		mplew.writeInt(oid);
		mplew.writePos(pos);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to remove a door.
	 * 
	 * @param oid
	 *            The door's ID.
	 * @param town
	 * @return The remove door packet.
	 */
	public static MaplePacket removeDoor(int oid, boolean town) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
		if (town) {
			mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
			mplew.writeInt(999999999);
			mplew.writeInt(999999999);
		} else {
			mplew.writeShort(SendOpcode.REMOVE_DOOR.getValue());
			mplew.write(0);
			mplew.writeInt(oid);
		}
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to spawn a special map object.
	 * 
	 * @param summon
	 * @param skillLevel
	 *            The level of the skill used.
	 * @param animated
	 *            Animated spawn?
	 * @return The spawn packet for the map object.
	 */
	public static MaplePacket spawnSummon(MapleSummon summon, boolean animated) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
		mplew.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
		mplew.writeInt(summon.getOwner().getId());
		mplew.writeInt(summon.getObjectId());
		mplew.writeInt(summon.getSkill());
		mplew.write(0x0A); // v83
		mplew.write(summon.getSkillLevel());
		mplew.writePos(summon.getPosition());
		mplew.write0(3);
		mplew.write(summon.getMovementType().getValue()); // 0 = don't move, 1 =
															// follow (4th mage
															// summons?), 2/4 =
															// only tele follow,
															// 3 = bird follow
		mplew.write(summon.isPuppet() ? 0 : 1); // 0 and the summon can't attack
												// - but puppets don't attack
												// with 1 either ^.-
		mplew.write(animated ? 0 : 1);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to remove a special map object.
	 * 
	 * @param summon
	 * @param animated
	 *            Animated removal?
	 * @return The packet removing the object.
	 */
	public static MaplePacket removeSummon(MapleSummon summon, boolean animated) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
		mplew.writeInt(summon.getOwner().getId());
		mplew.writeInt(summon.getObjectId());
		mplew.write(animated ? 4 : 1); // ?
		return mplew.getPacket();
	}

	/**
	 * Gets the response to a relog request.
	 * 
	 * @return The relog response packet.
	 */
	public static MaplePacket getRelogResponse() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.RELOG_RESPONSE.getValue());
		mplew.write(1);// 1 O.O Must be more types ):
		return mplew.getPacket();
	}

	/**
	 * Gets a server message packet.
	 * 
	 * @param message
	 *            The message to convey.
	 * @return The server message packet.
	 */
	public static MaplePacket serverMessage(String message) {
		return serverMessage(4, (byte) 0, message, true, false);
	}

	/**
	 * Gets a server notice packet.
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0: [Notice]<br>
	 * 1: Popup<br>
	 * 2: Megaphone<br>
	 * 3: Super Megaphone<br>
	 * 4: Scrolling message at top<br>
	 * 5: Pink Text<br>
	 * 6: Lightblue Text
	 * 
	 * @param type
	 *            The type of the notice.
	 * @param message
	 *            The message to convey.
	 * @return The server notice packet.
	 */
	public static MaplePacket serverNotice(int type, String message) {
		return serverMessage(type, (byte) 0, message, false, false);
	}

	/**
	 * Gets a server notice packet.
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0: [Notice]<br>
	 * 1: Popup<br>
	 * 2: Megaphone<br>
	 * 3: Super Megaphone<br>
	 * 4: Scrolling message at top<br>
	 * 5: Pink Text<br>
	 * 6: Lightblue Text
	 * 
	 * @param type
	 *            The type of the notice.
	 * @param channel
	 *            The channel this notice was sent on.
	 * @param message
	 *            The message to convey.
	 * @return The server notice packet.
	 */
	public static MaplePacket serverNotice(int type, byte channel, String message) {
		return serverMessage(type, channel, message, false, false);
	}

	public static MaplePacket serverNotice(int type, byte channel, String message, boolean smegaEar) {
		return serverMessage(type, channel, message, false, smegaEar);
	}

	/**
	 * Gets a server message packet.
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0: [Notice]<br>
	 * 1: Popup<br>
	 * 2: Megaphone<br>
	 * 3: Super Megaphone<br>
	 * 4: Scrolling message at top<br>
	 * 5: Pink Text<br>
	 * 6: Lightblue Text
	 * 
	 * @param type
	 *            The type of the notice.
	 * @param channel
	 *            The channel this notice was sent on.
	 * @param message
	 *            The message to convey.
	 * @param servermessage
	 *            Is this a scrolling ticker?
	 * @return The server notice packet.
	 */
	private static MaplePacket serverMessage(int type, byte channel, String message, boolean servermessage, boolean megaEar) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
		mplew.write(type);
		if (servermessage) {
			mplew.write(1);
		}
		mplew.writeMapleAsciiString(message);
		if (type == 3) {
			mplew.write(channel - 1); // channel
			mplew.write(megaEar ? 1 : 0);
		} else if (type == 6) {
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	/**
	 * Sends a Avatar Super Megaphone packet.
	 * 
	 * @param chr
	 *            The character name.
	 * @param medal
	 *            The medal text.
	 * @param channel
	 *            Which channel.
	 * @param itemId
	 *            Which item used.
	 * @param message
	 *            The message sent.
	 * @param ear
	 *            Whether or not the ear is shown for whisper.
	 * @return
	 */
	public static MaplePacket getAvatarMega(MapleCharacter chr, String medal, byte channel, int itemId, List<String> message, boolean ear) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.AVATAR_MEGA.getValue());
		mplew.writeInt(itemId);
		mplew.writeMapleAsciiString(medal + chr.getName());
		for (String s : message) {
			mplew.writeMapleAsciiString(s);
		}
		mplew.writeInt(channel - 1); // channel
		mplew.write(ear ? 1 : 0);
		addCharLook(mplew, chr, true);
		return mplew.getPacket();
	}

	/**
	 * Sends the Gachapon green message when a user uses a gachapon ticket.
	 * 
	 * @param item
	 * @param town
	 * @param player
	 * @return
	 */
	public static MaplePacket gachaponMessage(IItem item, String town, MapleCharacter player) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
		mplew.write(0x0B);
		mplew.writeMapleAsciiString(player.getName() + " : got a(n)");
		mplew.writeInt(0); // random?
		mplew.writeMapleAsciiString(town);
		addItemInfo(mplew, item, true);
		return mplew.getPacket();
	}

	public static MaplePacket spawnNPC(MapleNPC life) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(24);
		mplew.writeShort(SendOpcode.SPAWN_NPC.getValue());
		mplew.writeInt(life.getObjectId());
		mplew.writeInt(life.getId());
		mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getCy());
		if (life.getF() == 1) {
			mplew.write(0);
		} else {
			mplew.write(1);
		}
		mplew.writeShort(life.getFh());
		mplew.writeShort(life.getRx0());
		mplew.writeShort(life.getRx1());
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(23);
		mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
		mplew.write(1);
		mplew.writeInt(life.getObjectId());
		mplew.writeInt(life.getId());
		mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getCy());
		if (life.getF() == 1) {
			mplew.write(0);
		} else {
			mplew.write(1);
		}
		mplew.writeShort(life.getFh());
		mplew.writeShort(life.getRx0());
		mplew.writeShort(life.getRx1());
		mplew.write(MiniMap ? 1 : 0);
		return mplew.getPacket();
	}
	
	/**
	 * Makes any NPC in the game scriptable.
	 * @param npcId - The NPC's ID, found in WZ files/MCDB
	 * @param description - If the NPC has quests, this will be the text of the menu item
	 * @return 
	 */
    public static MaplePacket setNPCScriptable(int npcId, String description) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_NPC_SCRIPTABLE.getValue());
        mplew.write(1); // following structure is repeated n times
        mplew.writeInt(npcId);
        mplew.writeMapleAsciiString(description);
        mplew.writeInt(0); // start time
        mplew.writeInt(Integer.MAX_VALUE); // end time
        return mplew.getPacket();
    }
    
    /**
	 * Makes a list of any NPCs in the game scriptable.
	 * @param entries - a list of pairs of NPC IDs and descriptions.
	 * @return 
	 */
    public static MaplePacket setNPCScriptable(List<NpcDescriptionEntry> entries) {
    	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    	mplew.writeShort(SendOpcode.SET_NPC_SCRIPTABLE.getValue());
    	mplew.write(entries.size()); // following structure is repeated n times
    	for (NpcDescriptionEntry entry : entries) {
    		mplew.writeInt(entry.npcId);
    		mplew.writeMapleAsciiString(entry.description);
    		mplew.writeInt(0); // start time
    		mplew.writeInt(Integer.MAX_VALUE); // end time
    	}
    	return mplew.getPacket();
    }

	/**
	 * Gets a spawn monster packet.
	 * 
	 * @param life
	 *            The monster to spawn.
	 * @param newSpawn
	 *            Is it a new spawn?
	 * @return The spawn monster packet.
	 */
	public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn) {
		return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
	}

	/**
	 * Gets a spawn monster packet.
	 * 
	 * @param life
	 *            The monster to spawn.
	 * @param newSpawn
	 *            Is it a new spawn?
	 * @param effect
	 *            The spawn effect.
	 * @return The spawn monster packet.
	 */
	public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn, int effect) {
		return spawnMonsterInternal(life, false, newSpawn, false, effect, false);
	}

	/**
	 * Gets a control monster packet.
	 * 
	 * @param life
	 *            The monster to give control to.
	 * @param newSpawn
	 *            Is it a new spawn?
	 * @param aggro
	 *            Aggressive monster?
	 * @return The monster control packet.
	 */
	public static MaplePacket controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
		return spawnMonsterInternal(life, true, newSpawn, aggro, 0, false);
	}

	/**
	 * Makes a monster invisible for Ariant PQ.
	 * 
	 * @param life
	 * @return
	 */
	public static MaplePacket makeMonsterInvisible(MapleMonster life) {
		return spawnMonsterInternal(life, true, false, false, 0, true);
	}

	/**
	 * Internal function to handler monster spawning and controlling.
	 * 
	 * @param life
	 *            The mob to perform operations with.
	 * @param requestController
	 *            Requesting control of mob?
	 * @param newSpawn
	 *            New spawn (fade in?)
	 * @param aggro
	 *            Aggressive mob?
	 * @param effect
	 *            The spawn effect to use.
	 * @return The spawn/control packet.
	 */
	private static MaplePacket spawnMonsterInternal(MapleMonster life, boolean requestController, boolean newSpawn, boolean aggro, int effect, boolean makeInvis) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		if (makeInvis) {
			mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
			mplew.write(0);
			mplew.writeInt(life.getObjectId());
			return mplew.getPacket();
		}
		if (requestController) {
			mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
			mplew.write(aggro ? 2 : 1);
		} else {
			mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
		}
		mplew.writeInt(life.getObjectId());
		mplew.write(life.getController() == null ? 5 : 1);
		mplew.writeInt(life.getId());
		mplew.write0(15);
		mplew.write(0x88);
		mplew.write0(6);
		mplew.writePos(life.getPosition());
		mplew.write(life.getStance());
		mplew.writeShort(0); // Origin FH //life.getStartFh()
		mplew.writeShort(life.getFh());

		if (effect > 0) {
			mplew.write(effect);
			mplew.write(0);
			mplew.writeShort(0);
			if (effect == 15) {
				mplew.write(0);
			}
		}
		mplew.write(newSpawn ? -2 : -1);
		mplew.write(life.getTeam());
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	/**
	 * Handles monsters not being targettable, such as Zakum's first body.
	 * 
	 * @param life
	 *            The mob to spawn as non-targettable.
	 * @param effect
	 *            The effect to show when spawning.
	 * @return The packet to spawn the mob as non-targettable.
	 */
	public static MaplePacket spawnFakeMonster(MapleMonster life, int effect) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(1);
		mplew.writeInt(life.getObjectId());
		mplew.write(5);
		mplew.writeInt(life.getId());
		mplew.write0(15);
		mplew.write(0x88);
		mplew.write0(6);
		mplew.writePos(life.getPosition());
		mplew.write(life.getStance());
		mplew.writeShort(0);// life.getStartFh()
		mplew.writeShort(life.getFh());
		if (effect > 0) {
			mplew.write(effect);
			mplew.write(0);
			mplew.writeShort(0);
		}
		mplew.writeShort(-2);
		mplew.write(life.getTeam());
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	/**
	 * Makes a monster previously spawned as non-targettable, targettable.
	 * 
	 * @param life
	 *            The mob to make targettable.
	 * @return The packet to make the mob targettable.
	 */
	public static MaplePacket makeMonsterReal(MapleMonster life) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
		mplew.writeInt(life.getObjectId());
		mplew.write(5);
		mplew.writeInt(life.getId());
		mplew.write0(15);
		mplew.write(0x88);
		mplew.write0(6);
		mplew.writePos(life.getPosition());
		mplew.write(life.getStance());
		mplew.writeShort(0);// life.getStartFh()
		mplew.writeShort(life.getFh());
		mplew.writeShort(-1);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	/**
	 * Gets a stop control monster packet.
	 * 
	 * @param oid
	 *            The ObjectID of the monster to stop controlling.
	 * @return The stop control monster packet.
	 */
	public static MaplePacket stopControllingMonster(int oid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(0);
		mplew.writeInt(oid);
		return mplew.getPacket();
	}

	/**
	 * Gets a response to a move monster packet.
	 * 
	 * @param objectid
	 *            The ObjectID of the monster being moved.
	 * @param moveid
	 *            The movement ID.
	 * @param currentMp
	 *            The current MP of the monster.
	 * @param useSkills
	 *            Can the monster use skills?
	 * @return The move response packet.
	 */
	public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
		return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
	}

	/**
	 * Gets a response to a move monster packet.
	 * 
	 * @param objectid
	 *            The ObjectID of the monster being moved.
	 * @param moveid
	 *            The movement ID.
	 * @param currentMp
	 *            The current MP of the monster.
	 * @param useSkills
	 *            Can the monster use skills?
	 * @param skillId
	 *            The skill ID for the monster to use.
	 * @param skillLevel
	 *            The level of the skill to use.
	 * @return The move response packet.
	 */
	public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(13);
		mplew.writeShort(SendOpcode.MOVE_MONSTER_RESPONSE.getValue());
		mplew.writeInt(objectid);
		mplew.writeShort(moveid);
		mplew.write(useSkills ? 1 : 0);
		mplew.writeShort(currentMp);
		mplew.write(skillId);
		mplew.write(skillLevel);
		return mplew.getPacket();
	}

	/**
	 * Gets a general chat packet.
	 * 
	 * @param cidfrom
	 *            The character ID who sent the chat.
	 * @param text
	 *            The text of the chat.
	 * @param whiteBG
	 * @param show
	 * @return The general chat packet.
	 */
	public static MaplePacket getChatText(int cidfrom, String text, boolean gm, int show) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHATTEXT.getValue());
		mplew.writeInt(cidfrom);
		mplew.write(gm ? 1 : 0);
		mplew.writeMapleAsciiString(text);
		mplew.write(show);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client to show an EXP increase.
	 * 
	 * @param gain
	 *            The amount of EXP gained.
	 * @param inChat
	 *            In the chat box?
	 * @param white
	 *            White text or yellow?
	 * @return The exp gained packet.
	 */
	public static MaplePacket getShowExpGain(int gain, int equip, boolean inChat, boolean white) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
		mplew.write(white ? 1 : 0);
		mplew.writeInt(gain);
		mplew.write(inChat ? 1 : 0);
		mplew.writeInt(0); // monster book bonus (Bonus Event Exp)
		mplew.writeShort(0); // Weird stuff
		mplew.writeInt(0); // wedding bonus
		mplew.write(0); // 0 = party bonus, 1 = Bonus Event party Exp () x0
		mplew.writeInt(0); // party bonus
		mplew.writeInt(equip); // equip bonus
		mplew.writeInt(0); // Internet Cafe Bonus
		mplew.writeInt(0); // Rainbow Week Bonus
		if (inChat) {
			mplew.write(0);
		}
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client to show a fame gain.
	 * 
	 * @param gain
	 *            How many fame gained.
	 * @return The meso gain packet.
	 */
	public static MaplePacket getShowFameGain(int gain) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(4);
		mplew.writeInt(gain);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client to show a meso gain.
	 * 
	 * @param gain
	 *            How many mesos gained.
	 * @return The meso gain packet.
	 */
	public static MaplePacket getShowMesoGain(int gain) {
		return getShowMesoGain(gain, false);
	}

	/**
	 * Gets a packet telling the client to show a meso gain.
	 * 
	 * @param gain
	 *            How many mesos gained.
	 * @param inChat
	 *            Show in the chat window?
	 * @return The meso gain packet.
	 */
	public static MaplePacket getShowMesoGain(int gain, boolean inChat) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		if (!inChat) {
			mplew.write(0);
			mplew.writeShort(1); // v83
		} else {
			mplew.write(5);
		}
		mplew.writeInt(gain);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client to show a item gain.
	 * 
	 * @param itemId
	 *            The ID of the item gained.
	 * @param quantity
	 *            How many items gained.
	 * @return The item gain packet.
	 */
	public static MaplePacket getShowItemGain(int itemId, short quantity) {
		return getShowItemGain(itemId, quantity, false);
	}

	/**
	 * Gets a packet telling the client to show an item gain.
	 * 
	 * @param itemId
	 *            The ID of the item gained.
	 * @param quantity
	 *            The number of items gained.
	 * @param inChat
	 *            Show in the chat window?
	 * @return The item gain packet.
	 */
	public static MaplePacket getShowItemGain(int itemId, short quantity, boolean inChat) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		if (inChat) {
			mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
			mplew.write(3);
			mplew.write(1);
			mplew.writeInt(itemId);
			mplew.writeInt(quantity);
		} else {
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.writeShort(0);
			mplew.writeInt(itemId);
			mplew.writeInt(quantity);
			mplew.writeInt(0);
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	public static MaplePacket killMonster(int oid, boolean animation) {
		return killMonster(oid, animation ? 1 : 0);
	}

	/**
	 * Gets a packet telling the client that a monster was killed.
	 * 
	 * @param oid
	 *            The objectID of the killed monster.
	 * @param animation
	 *            0 = dissapear, 1 = fade out, 2+ = special
	 * @return The kill monster packet.
	 */
	public static MaplePacket killMonster(int oid, int animation) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.KILL_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(animation);
		mplew.write(animation);
		return mplew.getPacket();
	}

	public static MaplePacket dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
		mplew.write(mod);
		mplew.writeInt(drop.getObjectId());
		mplew.write(drop.getMeso() > 0 ? 1 : 0); // 1 mesos, 0 item, 2 and above
													// all item meso bag,
		mplew.writeInt(drop.getItemId()); // drop object ID
		mplew.writeInt(drop.getOwner()); // owner charid/paryid :)
		mplew.write(drop.getDropType()); // 0 = timeout for non-owner, 1 =
											// timeout for non-owner's party, 2
											// = FFA, 3 = explosive/FFA
		mplew.writePos(dropto);
		mplew.writeInt(drop.getDropType() == 0 ? drop.getOwner() : 0); // test

		if (mod != 2) {
			mplew.writePos(dropfrom);
			mplew.writeShort(0);// Fh?
		}
		if (drop.getMeso() == 0) {
			addExpirationTime(mplew, drop.getItem().getExpiration(), true);
		}
		mplew.write(drop.isPlayerDrop() ? 0 : 1); // pet EQP pickup
		return mplew.getPacket();
	}

	/**
	 * Gets a packet spawning a player as a mapobject to other clients.
	 * 
	 * @param chr
	 *            The character to spawn to other clients.
	 * @return The spawn player packet.
	 */
	public static MaplePacket spawnPlayerMapobject(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_PLAYER.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(chr.getLevel()); // v83
		mplew.writeMapleAsciiString(chr.getName());
		if (chr.getGuildId() < 1) {
			mplew.writeMapleAsciiString("");
			mplew.write(new byte[6]);
		} else {
			MapleGuildSummary gs = chr.getClient().getWorldServer().getGuildSummary(chr.getGuildId());
			if (gs != null) {
				mplew.writeMapleAsciiString(gs.getName());
				mplew.writeShort(gs.getLogoBG());
				mplew.write(gs.getLogoBGColor());
				mplew.writeShort(gs.getLogo());
				mplew.write(gs.getLogoColor());
			} else {
				mplew.writeMapleAsciiString("");
				mplew.write(new byte[6]);
			}
		}
		mplew.writeInt(0);
		mplew.writeShort(0); // v83
		mplew.write(0xFC);
		mplew.write(1);
		if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
			mplew.writeInt(2);
		} else {
			mplew.writeInt(0);
		}
		long buffmask = 0;
		Integer buffvalue = null;
		if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && !chr.isHidden()) {
			buffmask |= MapleBuffStat.DARKSIGHT.getValue();
		}
		if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
			buffmask |= MapleBuffStat.COMBO.getValue();
			buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue());
		}
		if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
			buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
		}
		if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
			buffmask |= MapleBuffStat.SOULARROW.getValue();
		}
		if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
			buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue());
		}
		if (chr.getBuffedValue(MapleBuffStat.ENERGY_CHARGE) != null) {
			buffmask |= MapleBuffStat.ENERGY_CHARGE.getValue();
			buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.ENERGY_CHARGE).intValue());
		}// AREN'T THESE
		mplew.writeInt((int) ((buffmask >> 32) & 0xffffffffL));
		if (buffvalue != null) {
			if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) { // TEST
				mplew.writeShort(buffvalue);
			} else {
				mplew.write(buffvalue.byteValue());
			}
		}
		mplew.writeInt((int) (buffmask & 0xffffffffL));
		int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
		mplew.write0(6);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write0(11);
		mplew.writeInt(CHAR_MAGIC_SPAWN);// v74
		mplew.write0(11);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		mplew.write(0);
		IItem mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
		if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null && mount != null) {
			mplew.writeInt(mount.getItemId());
			mplew.writeInt(1004);
		} else {
			mplew.writeLong(0);
		}
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write0(9);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		mplew.writeInt(0); // actually not 0, why is it 0 then?
		mplew.write0(10);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write0(13);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		mplew.write(0);
		mplew.writeShort(chr.getJob().getId());
		addCharLook(mplew, chr, false);
		mplew.writeInt(chr.getInventory(MapleInventoryType.CASH).countById(5110000));
		mplew.writeInt(chr.getItemEffect());
		mplew.writeInt(chr.getChair());
		mplew.writePos(chr.getPosition());
		mplew.write(chr.getStance());
		mplew.writeShort(0);// chr.getFh()
		mplew.write(0);
		MaplePet[] pet = chr.getPets();
		for (int i = 0; i < 3; i++) {
			if (pet[i] != null) {
				addPetInfo(mplew, pet[i], false);
			}
		}
		mplew.write(0); // end of pets
		if (chr.getMount() == null) {
			mplew.writeInt(1); // mob level
			mplew.writeLong(0); // mob exp + tiredness
		} else {
			mplew.writeInt(chr.getMount().getLevel());
			mplew.writeInt(chr.getMount().getExp());
			mplew.writeInt(chr.getMount().getTiredness());
		}
		if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr)) {
			if (chr.getPlayerShop().hasFreeSlot()) {
				addAnnounceBox(mplew, chr.getPlayerShop(), chr.getPlayerShop().getVisitors().length);
			} else {
				addAnnounceBox(mplew, chr.getPlayerShop(), 1);
			}
		} else if (chr.getMiniGame() != null && chr.getMiniGame().isOwner(chr)) {
			if (chr.getMiniGame().hasFreeSlot()) {
				addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 1, 0);
			} else {
				addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 2, 1);
			}
		} else {
			mplew.write(0);
		}
		if (chr.getChalkboard() != null) {
			mplew.write(1);
			mplew.writeMapleAsciiString(chr.getChalkboard());
		} else {
			mplew.write(0);
		}
		addRingLook(mplew, chr, true);
		addRingLook(mplew, chr, false);
		addMarriageRingLook(mplew, chr);
		mplew.write0(3);
		mplew.write(chr.getTeam());
		return mplew.getPacket();
	}

	private static void addRingLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean crush) {
		List<MapleRing> rings;
		if (crush) {
			rings = chr.getCrushRings();
		} else {
			rings = chr.getFriendshipRings();
		}
		boolean yes = false;
		for (MapleRing ring : rings) {
			if (ring.equipped()) {
				if (yes == false) {
					yes = true;
					mplew.write(1);
				}
				mplew.writeInt(ring.getRingId());
				mplew.writeInt(0);
				mplew.writeInt(ring.getPartnerRingId());
				mplew.writeInt(0);
				mplew.writeInt(ring.getItemId());
			}
		}
		if (yes == false) {
			mplew.write(0);
		}
	}

	private static void addMarriageRingLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		if (chr.getMarriageRing() != null && !chr.getMarriageRing().equipped()) {
			mplew.write(0);
			return;
		}
		mplew.write(chr.getMarriageRing() != null ? 1 : 0);
		if (chr.getMarriageRing() != null) {
			mplew.writeInt(chr.getId());
			mplew.writeInt(chr.getMarriageRing().getPartnerChrId());
			mplew.writeInt(chr.getMarriageRing().getRingId());
		}
	}

	/**
	 * Adds a announcement box to an existing MaplePacketLittleEndianWriter.
	 * 
	 * @param mplew
	 *            The MaplePacketLittleEndianWriter to add an announcement box
	 *            to.
	 * @param shop
	 *            The shop to announce.
	 */
	private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop, int availability) {
		mplew.write(4);
		mplew.writeInt(shop.getObjectId());
		mplew.writeMapleAsciiString(shop.getDescription());
		mplew.write(0);
		mplew.write(0);
		mplew.write(1);
		mplew.write(availability);
		mplew.write(0);
	}

	private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleMiniGame game, int gametype, int type, int ammount, int joinable) {
		mplew.write(gametype);
		mplew.writeInt(game.getObjectId()); // gameid/shopid
		mplew.writeMapleAsciiString(game.getDescription()); // desc
		mplew.write(0);
		mplew.write(type);
		mplew.write(ammount);
		mplew.write(2);
		mplew.write(joinable);
	}

	public static MaplePacket facialExpression(MapleCharacter from, int expression) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
		mplew.writeShort(SendOpcode.FACIAL_EXPRESSION.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(expression);
		return mplew.getPacket();
	}

	private static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
		lew.write(moves.size());
		for (LifeMovementFragment move : moves) {
			move.serialize(lew);
		}
	}

	public static MaplePacket movePlayer(int cid, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOVE_PLAYER.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(0);
		serializeMovementList(mplew, moves);
		return mplew.getPacket();
	}

	public static MaplePacket moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOVE_SUMMON.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(oid);
		mplew.writePos(startPos);
		serializeMovementList(mplew, moves);
		return mplew.getPacket();
	}

	public static MaplePacket moveMonster(int useskill, int skill, int skill_1, int skill_2, int skill_3, int skill_4, int oid, Point startPos, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOVE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(0);
		mplew.write(useskill);
		mplew.write(skill);
		mplew.write(skill_1);
		mplew.write(skill_2);
		mplew.write(skill_3);
		mplew.write(skill_4);
		mplew.writePos(startPos);
		serializeMovementList(mplew, moves);
		return mplew.getPacket();
	}

	public static MaplePacket summonAttack(int cid, int summonSkillId, byte direction, List<SummonAttackEntry> allDamage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		// b2 00 29 f7 00 00 9a a3 04 00 c8 04 01 94 a3 04 00 06 ff 2b 00
		mplew.writeShort(SendOpcode.SUMMON_ATTACK.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(summonSkillId);
		mplew.write(direction);
		mplew.write(4);
		mplew.write(allDamage.size());
		for (SummonAttackEntry attackEntry : allDamage) {
			mplew.writeInt(attackEntry.getMonsterOid()); // oid
			mplew.write(6); // who knows
			mplew.writeInt(attackEntry.getDamage()); // damage
		}
		return mplew.getPacket();
	}

	public static MaplePacket closeRangeAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CLOSE_RANGE_ATTACK.getValue());
		addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
		return mplew.getPacket();
	}

	public static MaplePacket rangedAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, int projectile, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.RANGED_ATTACK.getValue());
		addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, projectile, damage, speed, direction, display);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket magicAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, Map<Integer, List<Integer>> damage, int charge, int speed, int direction, int display) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAGIC_ATTACK.getValue());
		addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
		if (charge != -1) {
			mplew.writeInt(charge);
		}
		return mplew.getPacket();
	}

	private static void addAttackBody(LittleEndianWriter lew, MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, int projectile, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
		lew.writeInt(chr.getId());
		lew.write(numAttackedAndDamage);
		lew.write(0x5B);// ?
		lew.write(skilllevel);
		if (skilllevel > 0) {
			lew.writeInt(skill);
		}
		lew.write(display);
		lew.write(direction);
		lew.write(stance);
		lew.write(speed);
		lew.write(0x0A);
		lew.writeInt(projectile);
		for (Integer oned : damage.keySet()) {
			List<Integer> onedList = damage.get(oned);
			if (onedList != null) {
				lew.writeInt(oned.intValue());
				lew.write(0xFF);
				if (skill == 4211006) {
					lew.write(onedList.size());
				}
				for (Integer eachd : onedList) {
					lew.writeInt(eachd.intValue());
				}
			}
		}
	}

	private static int doubleToShortBits(double d) {
		return (int) (Double.doubleToLongBits(d) >> 48);
	}

	public static MaplePacket getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.OPEN_NPC_SHOP.getValue());
		mplew.writeInt(sid);
		mplew.writeShort(items.size()); // item count
		for (MapleShopItem item : items) {
			mplew.writeInt(item.getItemId());
			mplew.writeInt(item.getPrice());
			mplew.writeInt(item.getPrice() == 0 ? item.getPitch() : 0); // Perfect
																		// Pitch
			mplew.writeInt(0); // Can be used x minutes after purchase
			mplew.writeInt(0); // Hmm
			if (!ItemConstants.isRechargable(item.getItemId())) {
				mplew.writeShort(1); // stacksize o.o
				mplew.writeShort(item.getBuyable());
			} else {
				mplew.writeShort(0);
				mplew.writeInt(0);
				mplew.writeShort(doubleToShortBits(ii.getPrice(item.getItemId())));
				mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
			}
		}
		return mplew.getPacket();
	}

	/*
	 * 00 = / 01 = You don't have enough in stock 02 = You do not have enough
	 * mesos 03 = Please check if your inventory is full or not 05 = You don't
	 * have enough in stock 06 = Due to an error, the trade did not happen 07 =
	 * Due to an error, the trade did not happen 08 = / 0D = You need more items
	 * 0E = CRASH; LENGTH NEEDS TO BE LONGER :O
	 */
	public static MaplePacket shopTransaction(byte code) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
		mplew.write(code);
		return mplew.getPacket();
	}

	public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item) {
		return addInventorySlot(type, item, false);
	}

	public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(fromDrop ? 1 : 0);
		mplew.writeShort(1); // add mode
		mplew.write(type.equals(MapleInventoryType.EQUIPPED) ? 1 : type.getType()); // iv
																					// type
		mplew.writeShort(item.getPosition()); // slot id
		addItemInfo(mplew, item, true);
		return mplew.getPacket();
	}

	public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item) {
		return updateInventorySlot(type, item, false);
	}

	public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(fromDrop ? 1 : 0);
		mplew.writeShort(0x101); // update
		mplew.write(type.equals(MapleInventoryType.EQUIPPED) ? 1 : type.getType()); // iv
																					// type
		mplew.writeShort(item.getPosition()); // slot id
		mplew.writeShort(item.getQuantity());
		return mplew.getPacket();
	}

	public static MaplePacket updateInventorySlotLimit(int type, int newLimit) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_INVENTORY_SLOTS.getValue());
		mplew.write(type);
		mplew.write(newLimit);
		return mplew.getPacket();
	}

	public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst) {
		return moveInventoryItem(type, src, dst, (byte) -1);
	}

	public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst, byte equipIndicator) {
		// 1D 00 01 01 02 00 F5 FF 01 00 01
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(new byte[] {1, 1, 2});
		mplew.write(type.getType()); // iv type
		mplew.writeShort(src);
		mplew.writeShort(dst);
		if (equipIndicator != -1) {
			mplew.write(equipIndicator);
		}
		return mplew.getPacket();
	}

	public static MaplePacket moveAndMergeInventoryItem(MapleInventoryType type, byte src, byte dst, short total) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(new byte[] {1, 2, 3});
		mplew.write(type.getType()); // iv type
		mplew.writeShort(src);
		mplew.write(1); // merge mode?
		mplew.write(type.getType());
		mplew.writeShort(dst);
		mplew.writeShort(total);
		return mplew.getPacket();
	}

	public static MaplePacket moveAndMergeWithRestInventoryItem(MapleInventoryType type, byte src, byte dst, short srcQ, short dstQ) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(new byte[] {1, 2, 1});
		mplew.write(type.getType()); // iv type
		mplew.writeShort(src);
		mplew.writeShort(srcQ);
		mplew.write(1);
		mplew.write(type.getType());
		mplew.writeShort(dst);
		mplew.writeShort(dstQ);
		return mplew.getPacket();
	}

	public static MaplePacket clearInventoryItem(MapleInventoryType type, byte slot, boolean fromDrop) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(fromDrop ? 1 : 0);
		mplew.write(new byte[] {1, 3});
		mplew.write(type.equals(MapleInventoryType.EQUIPPED) ? 1 : type.getType()); // iv
																					// type
		mplew.writeShort(slot);
		if (!fromDrop) {
			mplew.write(2);
		}
		return mplew.getPacket();
	}

	public static MaplePacket scrolledItem(IItem scroll, IItem item, boolean destroyed) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(1); // fromdrop always true
		mplew.write(destroyed ? 2 : 3);
		mplew.write(scroll.getQuantity() > 0 ? 1 : 3);
		mplew.write(MapleInventoryType.USE.getType());
		mplew.writeShort(scroll.getPosition());
		if (scroll.getQuantity() > 0) {
			mplew.writeShort(scroll.getQuantity());
		}
		mplew.write(3);
		if (!destroyed) {
			mplew.write(MapleInventoryType.EQUIP.getType());
			mplew.writeShort(item.getPosition());
			mplew.write(0);
		}
		mplew.write(MapleInventoryType.EQUIP.getType());
		mplew.writeShort(item.getPosition());
		if (!destroyed) {
			addItemInfo(mplew, item, true);
		}
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_SCROLL_EFFECT.getValue());
		mplew.writeInt(chr);
		switch (scrollSuccess) {
			case SUCCESS:
				mplew.writeShort(1);
				mplew.writeShort(legendarySpirit ? 1 : 0);
				break;
			case FAIL:
				mplew.writeShort(0);
				mplew.writeShort(legendarySpirit ? 1 : 0);
				break;
			case CURSE:
				mplew.write(0);
				mplew.write(1);
				mplew.writeShort(legendarySpirit ? 1 : 0);
				break;
		}
		return mplew.getPacket();
	}

	public static MaplePacket removePlayerFromMap(int cid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
		mplew.writeInt(cid);
		return mplew.getPacket();
	}

	/**
	 * animation: 0 - expire<br/>
	 * 1 - without animation<br/>
	 * 2 - pickup<br/>
	 * 4 - explode<br/>
	 * cid is ignored for 0 and 1
	 * 
	 * @param oid
	 * @param animation
	 * @param cid
	 * @return
	 */
	public static MaplePacket removeItemFromMap(int oid, int animation, int cid) {
		return removeItemFromMap(oid, animation, cid, false, 0);
	}

	/**
	 * animation: 0 - expire<br/>
	 * 1 - without animation<br/>
	 * 2 - pickup<br/>
	 * 4 - explode<br/>
	 * cid is ignored for 0 and 1.<br />
	 * <br />
	 * Flagging pet as true will make a pet pick up the item.
	 * 
	 * @param oid
	 * @param animation
	 * @param cid
	 * @param pet
	 * @param slot
	 * @return
	 */
	public static MaplePacket removeItemFromMap(int oid, int animation, int cid, boolean pet, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REMOVE_ITEM_FROM_MAP.getValue());
		mplew.write(animation); // expire
		mplew.writeInt(oid);
		if (animation >= 2) {
			mplew.writeInt(cid);
			if (pet) {
				mplew.write(slot);
			}
		}
		return mplew.getPacket();
	}

	public static MaplePacket updateCharLook(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_LOOK.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(1);
		addCharLook(mplew, chr, false);
		addRingLook(mplew, chr, true);
		addRingLook(mplew, chr, false);
		addMarriageRingLook(mplew, chr);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket dropInventoryItem(MapleInventoryType type, short src) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(new byte[] {1, 1, 3});
		mplew.write(type.getType());
		mplew.writeShort(src);
		if (src < 0) {
			mplew.write(1);
		}
		return mplew.getPacket();
	}

	public static MaplePacket dropInventoryItemUpdate(MapleInventoryType type, IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(new byte[] {1, 1, 1});
		mplew.write(type.getType());
		mplew.writeShort(item.getPosition());
		mplew.writeShort(item.getQuantity());
		return mplew.getPacket();
	}

	public static MaplePacket damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DAMAGE_PLAYER.getValue());
		mplew.writeInt(cid);
		mplew.write(skill);
		mplew.writeInt(damage);
		mplew.writeInt(monsteridfrom);
		mplew.write(direction);
		if (pgmr) {
			mplew.write(pgmr_1);
			mplew.write(is_pg ? 1 : 0);
			mplew.writeInt(oid);
			mplew.write(6);
			mplew.writeShort(pos_x);
			mplew.writeShort(pos_y);
			mplew.write(0);
		} else {
			mplew.writeShort(0);
		}
		mplew.writeInt(damage);
		if (fake > 0) {
			mplew.writeInt(fake);
		}
		return mplew.getPacket();
	}

	public static MaplePacket charNameResponse(String charname, boolean nameUsed) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHAR_NAME_RESPONSE.getValue());
		mplew.writeMapleAsciiString(charname);
		mplew.write(nameUsed ? 1 : 0);
		return mplew.getPacket();
	}

	public static MaplePacket addNewCharEntry(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ADD_NEW_CHAR_ENTRY.getValue());
		mplew.write(0);
		addCharEntry(mplew, chr, false);
		return mplew.getPacket();
	}

	/**
	 * state 0 = del ok state 12 = invalid bday state 14 = incorrect pic
	 * 
	 * @param cid
	 * @param state
	 * @return
	 */
	public static MaplePacket deleteCharResponse(int cid, int state) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DELETE_CHAR_RESPONSE.getValue());
		mplew.writeInt(cid);
		mplew.write(state);
		return mplew.getPacket();
	}

	public static MaplePacket selectWorld(int world) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SELECT_WORLD.getValue());
		mplew.writeInt(world);// According to GMS, it should be the world that
								// contains the most characters (most active)
		return mplew.getPacket();
	}

	public static MaplePacket sendRecommended(List<WorldRecommendation> worlds) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SEND_RECOMMENDED.getValue());
		mplew.write(worlds.size());// size
		for (WorldRecommendation world : worlds) {
			mplew.writeInt(world.worldId);
			mplew.writeMapleAsciiString(world.message);
		}
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param chr
	 * @param isSelf
	 * @return
	 */
	public static MaplePacket charInfo(MapleCharacter chr) {
		// 3D 00 0A 43 01 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00
		// 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHAR_INFO.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(chr.getLevel());
		mplew.writeShort(chr.getJob().getId());
		mplew.writeShort(chr.getFame());
		mplew.write(chr.getMarriageRing() != null ? 1 : 0);
		String guildName = "";
		String allianceName = "";
		MapleGuildSummary gs = chr.getClient().getWorldServer().getGuildSummary(chr.getGuildId());
		if (chr.getGuildId() > 0 && gs != null) {
			guildName = gs.getName();
			MapleAlliance alliance = Server.getInstance().getAlliance(gs.getAllianceId());
			if (alliance != null) {
				allianceName = alliance.getName();
			}
		}
		mplew.writeMapleAsciiString(guildName);
		mplew.writeMapleAsciiString(allianceName);
		mplew.write(0);
		MaplePet[] pets = chr.getPets();
		IItem inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -114);
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				mplew.write(pets[i].getUniqueId());
				mplew.writeInt(pets[i].getItemId()); // petid
				mplew.writeMapleAsciiString(pets[i].getName());
				mplew.write(pets[i].getLevel()); // pet level
				mplew.writeShort(pets[i].getCloseness()); // pet closeness
				mplew.write(pets[i].getFullness()); // pet fullness
				mplew.writeShort(0);
				mplew.writeInt(inv != null ? inv.getItemId() : 0);
			}
		}
		mplew.write(0); // end of pets
		if (chr.getMount() != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
			mplew.write(chr.getMount().getId()); // mount
			mplew.writeInt(chr.getMount().getLevel()); // level
			mplew.writeInt(chr.getMount().getExp()); // exp
			mplew.writeInt(chr.getMount().getTiredness()); // tiredness
		} else {
			mplew.write(0);
		}
		mplew.write(chr.getCashShop().getWishList().size());
		for (int sn : chr.getCashShop().getWishList()) {
			mplew.writeInt(sn);
		}
		mplew.writeInt(chr.getMonsterBook().getBookLevel());
		mplew.writeInt(chr.getMonsterBook().getNormalCard());
		mplew.writeInt(chr.getMonsterBook().getSpecialCard());
		mplew.writeInt(chr.getMonsterBook().getTotalCards());
		mplew.writeInt(chr.getMonsterBookCover() > 0 ? MapleItemInformationProvider.getInstance().getCardMobId(chr.getMonsterBookCover()) : 0);
		IItem medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -49);
		if (medal != null) {
			mplew.writeInt(medal.getItemId());
		} else {
			mplew.writeInt(0);
		}
		ArrayList<Short> medalQuests = new ArrayList<Short>();
		List<MapleQuestStatus> completed = chr.getCompletedQuests();
		for (MapleQuestStatus q : completed) {
			if (q.getQuest().getId() >= 29000) { // && q.getQuest().getId() <=
													// 29923
				medalQuests.add(q.getQuest().getId());
			}
		}

		Collections.sort(medalQuests);
		mplew.writeShort(medalQuests.size());
		for (Short s : medalQuests) {
			mplew.writeShort(s);
		}
		return mplew.getPacket();
	}

	/**
	 * It is important that statups is in the correct order (see decleration
	 * order in MapleBuffStat) since this method doesn't do automagical
	 * reordering.
	 * 
	 * @param buffid
	 * @param bufflength
	 * @param statups
	 * @return
	 */
	// 1F 00 00 00 00 00 03 00 00 40 00 00 00 E0 00 00 00 00 00 00 00 00 E0 01
	// 8E AA 4F 00 00 C2 EB 0B E0 01 8E AA 4F 00 00 C2 EB 0B 0C 00 8E AA 4F 00
	// 00 C2 EB 0B 44 02 8E AA 4F 00 00 C2 EB 0B 44 02 8E AA 4F 00 00 C2 EB 0B
	// 00 00 E0 7A 1D 00 8E AA 4F 00 00 00 00 00 00 00 00 03
	public static MaplePacket giveBuff(int buffid, int bufflength, List<MapleBuffStatDelta> statups) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		boolean special = false;
		writeLongMask(mplew, statups);
		for (MapleBuffStatDelta statup : statups) {
			if (statup.stat.equals(MapleBuffStat.MONSTER_RIDING) || statup.stat.equals(MapleBuffStat.HOMING_BEACON)) {
				special = true;
			}
			mplew.writeShort(statup.delta);
			mplew.writeInt(buffid);
			mplew.writeInt(bufflength);
		}
		mplew.writeInt(0);
		mplew.write(0);
		mplew.writeInt(statups.get(0).delta); // Homing beacon ...

		if (special) {
			mplew.write0(3);
		}
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param cid
	 * @param statups
	 * @param mount
	 * @return
	 */
	public static MaplePacket showMonsterRiding(int cid, MapleMount mount) { // Gtfo
																				// with
																				// this,
																				// this
																				// is
																				// just
																				// giveForeignBuff
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		mplew.writeLong(MapleBuffStat.MONSTER_RIDING.getValue()); // Thanks?
		mplew.writeLong(0);
		mplew.writeShort(0);
		mplew.writeInt(mount.getItemId());
		mplew.writeInt(mount.getSkillId());
		mplew.writeInt(0); // Server Tick value.
		mplew.writeShort(0);
		mplew.write(0); // Times you have been buffed
		return mplew.getPacket();
	}

	/*
	 * mplew.writeInt(cid); writeLongMask(mplew, statups); for
	 * (Pair<MapleBuffStat, Integer> statup : statups) { if (morph) {
	 * mplew.writeInt(statup.getRight().intValue()); } else {
	 * mplew.writeShort(statup.getRight().shortValue()); } }
	 * mplew.writeShort(0); mplew.write(0);
	 */

	/**
	 * 
	 * @param c
	 * @param quest
	 * @return
	 */
	public static MaplePacket forfeitQuest(short quest) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest);
		mplew.write(0);
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param c
	 * @param quest
	 * @return
	 */
	public static MaplePacket completeQuest(short quest, long time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest);
		mplew.write(2);
		mplew.writeLong(time);
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param c
	 * @param quest
	 * @param npc
	 * @param progress
	 * @return
	 */
	public static MaplePacket updateQuestInfo(short quest, int npc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(8); // 0x0A in v95
		mplew.writeShort(quest);
		mplew.writeInt(npc);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket addQuestTimeLimit(final short quest, final int time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(6);
		mplew.writeShort(1);// Size but meh, when will there be 2 at the same
							// time? And it won't even replace the old one :)
		mplew.writeShort(quest);
		mplew.writeInt(time);
		return mplew.getPacket();
	}

	public static MaplePacket removeQuestTimeLimit(final short quest) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(7);
		mplew.writeShort(1);// Position
		mplew.writeShort(quest);
		return mplew.getPacket();
	}

	public static MaplePacket updateQuest(final short quest, final String status) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest);
		mplew.write(1);
		mplew.writeMapleAsciiString(status);
		return mplew.getPacket();
	}

	private static <E extends LongValueHolder> long getLongMaskD(List<MapleDiseaseEntry> entries) {
		long mask = 0;
		for (MapleDiseaseEntry entry : entries) {
			mask |= entry.disease.getValue();
		}
		return mask;
	}

	public static MaplePacket giveDebuff(List<MapleDiseaseEntry> entries, MobSkill skill) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		long mask = getLongMaskD(entries);
		mplew.writeLong(0);
		mplew.writeLong(mask);
		for (MapleDiseaseEntry entry : entries) {
			mplew.writeShort(entry.level);
			mplew.writeShort(skill.getSkillId());
			mplew.writeShort(skill.getSkillLevel());
			mplew.writeInt((int) skill.getDuration());
		}
		mplew.writeShort(0); // ??? wk charges have 600 here o.o
		mplew.writeShort(900);// Delay
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket giveForeignDebuff(int cid, List<MapleDiseaseEntry> entries, MobSkill skill) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		long mask = getLongMaskD(entries);
		mplew.writeLong(0);
		mplew.writeLong(mask);
		for (int i = 0; i < entries.size(); i++) {
			mplew.writeShort(skill.getSkillId());
			mplew.writeShort(skill.getSkillLevel());
		}
		mplew.writeShort(0); // same as give_buff
		mplew.writeShort(900);// Delay
		return mplew.getPacket();
	}

	public static MaplePacket cancelForeignDebuff(int cid, long mask) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		mplew.writeLong(0);
		mplew.writeLong(mask);
		return mplew.getPacket();
	}

	public static MaplePacket giveForeignBuff(int cid, List<MapleBuffStatDelta> statups) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		writeLongMask(mplew, statups);
		for (MapleBuffStatDelta statup : statups) {
			mplew.writeShort(statup.delta);
		}
		mplew.writeInt(0);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	public static MaplePacket cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		writeLongMaskFromList(mplew, statups);
		return mplew.getPacket();
	}

	public static MaplePacket cancelBuff(List<MapleBuffStat> statups) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
		writeLongMaskFromList(mplew, statups);
		mplew.write(1);// ?
		return mplew.getPacket();
	}

	private static void writeLongMask(MaplePacketLittleEndianWriter mplew, List<MapleBuffStatDelta> statups) {
		long firstmask = 0;
		long secondmask = 0;
		for (MapleBuffStatDelta statup : statups) {
			if (statup.stat.isFirst()) {
				firstmask |= statup.stat.getValue();
			} else {
				secondmask |= statup.stat.getValue();
			}
		}
		mplew.writeLong(firstmask);
		mplew.writeLong(secondmask);
	}

	private static void writeLongMaskFromList(MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups) {
		long firstmask = 0;
		long secondmask = 0;
		for (MapleBuffStat statup : statups) {
			if (statup.isFirst()) {
				firstmask |= statup.getValue();
			} else {
				secondmask |= statup.getValue();
			}
		}
		mplew.writeLong(firstmask);
		mplew.writeLong(secondmask);
	}

	public static MaplePacket cancelDebuff(long mask) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(19);
		mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
		mplew.writeLong(0);
		mplew.writeLong(mask);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket getPlayerShopChat(MapleCharacter c, String chat, boolean owner) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
		mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
		mplew.write(owner ? 0 : 1);
		mplew.writeMapleAsciiString(c.getName() + " : " + chat);
		return mplew.getPacket();
	}

	public static MaplePacket getPlayerShopNewVisitor(MapleCharacter c, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
		mplew.write(slot);
		addCharLook(mplew, c, false);
		mplew.writeMapleAsciiString(c.getName());
		return mplew.getPacket();
	}

	public static MaplePacket getPlayerShopRemoveVisitor(int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		if (slot > 0) {
			mplew.write(slot);
		}
		return mplew.getPacket();
	}

	public static MaplePacket getTradePartnerAdd(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
		mplew.write(1);
		addCharLook(mplew, c, false);
		mplew.writeMapleAsciiString(c.getName());
		return mplew.getPacket();
	}

	public static MaplePacket getTradeInvite(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.INVITE.getCode());
		mplew.write(3);
		mplew.writeMapleAsciiString(c.getName());
		mplew.write(new byte[] {(byte) 0xB7, (byte) 0x50, 0, 0});
		return mplew.getPacket();
	}

	public static MaplePacket getTradeMesoSet(byte number, int meso) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.SET_MESO.getCode());
		mplew.write(number);
		mplew.writeInt(meso);
		return mplew.getPacket();
	}

	public static MaplePacket getTradeItemAdd(byte number, IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.SET_ITEMS.getCode());
		mplew.write(number);
		mplew.write(item.getPosition());
		addItemInfo(mplew, item, true);
		return mplew.getPacket();
	}

	public static MaplePacket getPlayerShopItemUpdate(MaplePlayerShop shop) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
		mplew.write(shop.getItems().size());
		for (MaplePlayerShopItem item : shop.getItems()) {
			mplew.writeShort(item.getBundles());
			mplew.writeShort(item.getItem().getQuantity());
			mplew.writeInt(item.getPrice());
			addItemInfo(mplew, item.getItem(), true);
		}
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param c
	 * @param shop
	 * @param owner
	 * @return
	 */
	public static MaplePacket getPlayerShop(MapleClient c, MaplePlayerShop shop, boolean owner) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(4);
		mplew.write(4);
		mplew.write(owner ? 0 : 1);
		mplew.write(0);
		addCharLook(mplew, shop.getOwner(), false);
		mplew.writeMapleAsciiString(shop.getOwner().getName());
		mplew.write(1);
		addCharLook(mplew, shop.getOwner(), false);
		mplew.writeMapleAsciiString(shop.getOwner().getName());
		mplew.write(0xFF);
		mplew.writeMapleAsciiString(shop.getDescription());
		List<MaplePlayerShopItem> items = shop.getItems();
		mplew.write(0x10);
		mplew.write(items.size());
		for (MaplePlayerShopItem item : items) {
			mplew.writeShort(item.getBundles());
			mplew.writeShort(item.getItem().getQuantity());
			mplew.writeInt(item.getPrice());
			addItemInfo(mplew, item.getItem(), true);
		}
		return mplew.getPacket();
	}

	public static MaplePacket getTradeStart(MapleClient c, MapleTrade trade, byte number) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(3);
		mplew.write(2);
		mplew.write(number);
		if (number == 1) {
			mplew.write(0);
			addCharLook(mplew, trade.getPartner().getChr(), false);
			mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
		}
		mplew.write(number);
		addCharLook(mplew, c.getPlayer(), false);
		mplew.writeMapleAsciiString(c.getPlayer().getName());
		mplew.write(0xFF);
		return mplew.getPacket();
	}

	public static MaplePacket getTradeConfirmation() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.CONFIRM.getCode());
		return mplew.getPacket();
	}

	public static MaplePacket getTradeCompletion(byte number) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		mplew.write(number);
		mplew.write(6);
		return mplew.getPacket();
	}

	public static MaplePacket getTradeCancel(byte number) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		mplew.write(number);
		mplew.write(2);
		return mplew.getPacket();
	}

	public static MaplePacket addCharBox(MapleCharacter c, int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		addAnnounceBox(mplew, c.getPlayerShop(), type);
		return mplew.getPacket();
	}

	public static MaplePacket removeCharBox(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		mplew.write(0);
		return mplew.getPacket();
	}

	/**
	 * Possible values for <code>speaker</code>:<br>
	 * 0: Npc talking (left)<br>
	 * 1: Npc talking (right)<br>
	 * 2: Player talking (left)<br>
	 * 3: Player talking (left)<br>
	 * 
	 * @param npc
	 *            Npcid
	 * @param msgType
	 * @param talk
	 * @param endBytes
	 * @param speaker
	 * @return
	 */
	public static MaplePacket getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte speaker) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?
		mplew.writeInt(npc);
		mplew.write(msgType);
		mplew.write(speaker);
		mplew.writeMapleAsciiString(talk);
		mplew.write(HexTool.getByteArrayFromHexString(endBytes));
		return mplew.getPacket();
	}

	public static MaplePacket getDimensionalMirror(String talk) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?
		mplew.writeInt(9010022);
		mplew.write(0x0E);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.writeMapleAsciiString(talk);
		return mplew.getPacket();
	}

	public static MaplePacket getNPCTalkStyle(int npc, String talk, int styles[]) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?
		mplew.writeInt(npc);
		mplew.write(7);
		mplew.write(0); // speaker
		mplew.writeMapleAsciiString(talk);
		mplew.write(styles.length);
		for (int i = 0; i < styles.length; i++) {
			mplew.writeInt(styles[i]);
		}
		return mplew.getPacket();
	}

	public static MaplePacket getNPCTalkNum(int npc, String talk, int def, int min, int max) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?
		mplew.writeInt(npc);
		mplew.write(3);
		mplew.write(0); // speaker
		mplew.writeMapleAsciiString(talk);
		mplew.writeInt(def);
		mplew.writeInt(min);
		mplew.writeInt(max);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket getNPCTalkText(int npc, String talk, String def) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // Doesn't matter
		mplew.writeInt(npc);
		mplew.write(2);
		mplew.write(0); // speaker
		mplew.writeMapleAsciiString(talk);
		mplew.writeMapleAsciiString(def);// :D
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket showBuffeffect(int cid, int skillid, int effectid) {
		return showBuffeffect(cid, skillid, effectid, (byte) 3);
	}

	public static MaplePacket showBuffeffect(int cid, int skillid, int effectid, byte direction) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
		mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(cid);
		mplew.write(effectid); // buff level
		mplew.writeInt(skillid);
		mplew.write(direction);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket showOwnBuffEffect(int skillid, int effectid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(effectid);
		mplew.writeInt(skillid);
		mplew.write(0xA9);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket showOwnBerserk(int skilllevel, boolean Berserk) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(1);
		mplew.writeInt(1320006);
		mplew.write(0xA9);
		mplew.write(skilllevel);
		mplew.write(Berserk ? 1 : 0);
		return mplew.getPacket();
	}

	public static MaplePacket showBerserk(int cid, int skilllevel, boolean Berserk) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(cid);
		mplew.write(1);
		mplew.writeInt(1320006);
		mplew.write(0xA9);
		mplew.write(skilllevel);
		mplew.write(Berserk ? 1 : 0);
		return mplew.getPacket();
	}

	public static MaplePacket updateSkill(int skillid, int level, int masterlevel, long expiration) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_SKILLS.getValue());
		mplew.write(1);
		mplew.writeShort(1);
		mplew.writeInt(skillid);
		mplew.writeInt(level);
		mplew.writeInt(masterlevel);
		addExpirationTime(mplew, expiration);
		mplew.write(4);
		return mplew.getPacket();
	}

	public static MaplePacket getShowQuestCompletion(int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_QUEST_COMPLETION.getValue());
		mplew.writeShort(id);
		return mplew.getPacket();
	}

	public static MaplePacket getKeymap(Map<Integer, MapleKeyBinding> keybindings) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.KEYMAP.getValue());
		mplew.write(0);
		for (int x = 0; x < 90; x++) {
			MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
			if (binding != null) {
				mplew.write(binding.getType());
				mplew.writeInt(binding.getAction());
			} else {
				mplew.write(0);
				mplew.writeInt(0);
			}
		}
		return mplew.getPacket();
	}

	public static MaplePacket getWhisper(String sender, byte channel, String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WHISPER.getValue());
		mplew.write(0x12);
		mplew.writeMapleAsciiString(sender);
		mplew.writeShort(channel - 1); // I guess this is the channel
		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param target
	 *            name of the target character
	 * @param reply
	 *            error code: 0x0 = cannot find char, 0x1 = success
	 * @return the MaplePacket
	 */
	public static MaplePacket getWhisperReply(String target, byte reply) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WHISPER.getValue());
		mplew.write(0x0A); // whisper?
		mplew.writeMapleAsciiString(target);
		mplew.write(reply);
		return mplew.getPacket();
	}

	public static MaplePacket getInventoryFull() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(1);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket getShowInventoryFull() {
		return getShowInventoryStatus(0xff);
	}

	public static MaplePacket showItemUnavailable() {
		return getShowInventoryStatus(0xfe);
	}

	public static MaplePacket getShowInventoryStatus(int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(0);
		mplew.write(mode);
		mplew.writeInt(0);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket getStorage(int npcId, byte slots, Collection<IItem> items, int meso) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(0x16);
		mplew.writeInt(npcId);
		mplew.write(slots);
		mplew.writeShort(0x7E);
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.writeInt(meso);
		mplew.writeShort(0);
		mplew.write((byte) items.size());
		for (IItem item : items) {
			addItemInfo(mplew, item, true);
		}
		mplew.writeShort(0);
		mplew.write(0);
		return mplew.getPacket();
	}

	/*
	 * 0x0A = Inv full 0x0B = You do not have enough mesos 0x0C = One-Of-A-Kind
	 * error
	 */
	public static MaplePacket getStorageError(byte i) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(i);
		return mplew.getPacket();
	}

	public static MaplePacket mesoStorage(byte slots, int meso) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(0x13);
		mplew.write(slots);
		mplew.writeShort(2);
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.writeInt(meso);
		return mplew.getPacket();
	}

	public static MaplePacket storeStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(0xD);
		mplew.write(slots);
		mplew.writeShort(type.getBitfieldEncoding());
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.write(items.size());
		for (IItem item : items) {
			addItemInfo(mplew, item, true);
		}
		return mplew.getPacket();
	}

	public static MaplePacket takeOutStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(0x9);
		mplew.write(slots);
		mplew.writeShort(type.getBitfieldEncoding());
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.write(items.size());
		for (IItem item : items) {
			addItemInfo(mplew, item, true);
		}
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param oid
	 * @param remhppercentage
	 * @return
	 */
	public static MaplePacket showMonsterHP(int oid, int remhppercentage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_MONSTER_HP.getValue());
		mplew.writeInt(oid);
		mplew.write(remhppercentage);
		return mplew.getPacket();
	}

	public static MaplePacket showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
		mplew.write(5);
		mplew.writeInt(oid);
		mplew.writeInt(currHP);
		mplew.writeInt(maxHP);
		mplew.write(tagColor);
		mplew.write(tagBgColor);
		return mplew.getPacket();
	}

	public static MaplePacket giveFameResponse(int mode, String charname, int newfame) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
		mplew.write(0);
		mplew.writeMapleAsciiString(charname);
		mplew.write(mode);
		mplew.writeShort(newfame);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	/**
	 * status can be: <br>
	 * 0: ok, use giveFameResponse<br>
	 * 1: the username is incorrectly entered<br>
	 * 2: users under level 15 are unable to toggle with fame.<br>
	 * 3: can't raise or drop fame anymore today.<br>
	 * 4: can't raise or drop fame for this character for this month anymore.<br>
	 * 5: received fame, use receiveFame()<br>
	 * 6: level of fame neither has been raised nor dropped due to an unexpected
	 * error
	 * 
	 * @param status
	 * @return
	 */
	public static MaplePacket giveFameErrorResponse(int status) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
		mplew.write(status);
		return mplew.getPacket();
	}

	public static MaplePacket receiveFame(int mode, String charnameFrom) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
		mplew.write(5);
		mplew.writeMapleAsciiString(charnameFrom);
		mplew.write(mode);
		return mplew.getPacket();
	}

	public static MaplePacket partyCreated() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		mplew.write(8);
		mplew.writeShort(0x8b);
		mplew.writeShort(1);
		mplew.write(CHAR_INFO_MAGIC);
		mplew.write(CHAR_INFO_MAGIC);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket partyInvite(MapleCharacter from) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		mplew.write(4);
		mplew.writeInt(from.getParty().getId());
		mplew.writeMapleAsciiString(from.getName());
		mplew.write(0);
		return mplew.getPacket();
	}

	/**
	 * 10: A beginner can't create a party. 1/11/14/19: Your request for a party
	 * didn't work due to an unexpected error. 13: You have yet to join a party.
	 * 16: Already have joined a party. 17: The party you're trying to join is
	 * already in full capacity. 19: Unable to find the requested character in
	 * this channel.
	 * 
	 * @param message
	 * @return
	 */
	public static MaplePacket partyStatusMessage(int message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		mplew.write(message);
		return mplew.getPacket();
	}

	/**
	 * 23: 'Char' have denied request to the party.
	 * 
	 * @param message
	 * @param charname
	 * @return
	 */
	public static MaplePacket partyStatusMessage(int message, String charname) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		mplew.write(message);
		mplew.writeMapleAsciiString(charname);
		return mplew.getPacket();
	}

	private static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving) {
		List<MaplePartyCharacter> partymembers = new ArrayList<MaplePartyCharacter>(party.getMembers());
		while (partymembers.size() < 6) {
			partymembers.add(new MaplePartyCharacter());
		}
		for (MaplePartyCharacter partychar : partymembers) {
			lew.writeInt(partychar.getId());
		}
		for (MaplePartyCharacter partychar : partymembers) {
			lew.writeAsciiString(getRightPaddedStr(partychar.getName(), '\0', 13));
		}
		for (MaplePartyCharacter partychar : partymembers) {
			lew.writeInt(partychar.getJobId());
		}
		for (MaplePartyCharacter partychar : partymembers) {
			lew.writeInt(partychar.getLevel());
		}
		for (MaplePartyCharacter partychar : partymembers) {
			if (partychar.isOnline()) {
				lew.writeInt(partychar.getChannel() - 1);
			} else {
				lew.writeInt(-2);
			}
		}
		lew.writeInt(party.getLeader().getId());
		for (MaplePartyCharacter partychar : partymembers) {
			if (partychar.getChannel() == forchannel) {
				lew.writeInt(partychar.getMapId());
			} else {
				lew.writeInt(0);
			}
		}
		for (MaplePartyCharacter partychar : partymembers) {
			if (partychar.getChannel() == forchannel && !leaving) {
				lew.writeInt(partychar.getDoorTown());
				lew.writeInt(partychar.getDoorTarget());
				lew.writeInt(partychar.getDoorPosition().x);
				lew.writeInt(partychar.getDoorPosition().y);
			} else {
				lew.writeInt(999999999);
				lew.writeInt(999999999);
				lew.writeInt(0);
				lew.writeInt(0);
			}
		}
	}

	public static MaplePacket updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		switch (op) {
			case DISBAND:
			case EXPEL:
			case LEAVE:
				mplew.write(0x0C);
				mplew.writeInt(40546);
				mplew.writeInt(target.getId());
				if (op == PartyOperation.DISBAND) {
					mplew.write(0);
					mplew.writeInt(party.getId());
				} else {
					mplew.write(1);
					if (op == PartyOperation.EXPEL) {
						mplew.write(1);
					} else {
						mplew.write(0);
					}
					mplew.writeMapleAsciiString(target.getName());
					addPartyStatus(forChannel, party, mplew, false);
				}
				break;
			case JOIN:
				mplew.write(0xF);
				mplew.writeInt(40546);
				mplew.writeMapleAsciiString(target.getName());
				addPartyStatus(forChannel, party, mplew, false);
				break;
			case SILENT_UPDATE:
			case LOG_ONOFF:
				mplew.write(0x7);
				mplew.writeInt(party.getId());
				addPartyStatus(forChannel, party, mplew, false);
				break;
			case CHANGE_LEADER:
				mplew.write(0x1B);
				mplew.writeInt(target.getId());
				mplew.write(0);
				break;
		}
		return mplew.getPacket();
	}

	public static MaplePacket partyPortal(int townId, int targetId, Point position) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		mplew.writeShort(0x23);
		mplew.writeInt(townId);
		mplew.writeInt(targetId);
		mplew.writePos(position);
		return mplew.getPacket();
	}

	public static MaplePacket updatePartyMemberHP(int cid, int curhp, int maxhp) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_PARTYMEMBER_HP.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(curhp);
		mplew.writeInt(maxhp);
		return mplew.getPacket();
	}

	/**
	 * mode: 0 buddychat; 1 partychat; 2 guildchat
	 * 
	 * @param name
	 * @param chattext
	 * @param mode
	 * @return
	 */
	public static MaplePacket multiChat(String name, String chattext, int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MULTICHAT.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(name);
		mplew.writeMapleAsciiString(chattext);
		return mplew.getPacket();
	}

	private static void writeIntMask(MaplePacketLittleEndianWriter mplew, Map<MonsterStatus, Integer> stats) {
		int firstmask = 0;
		int secondmask = 0;
		for (MonsterStatus stat : stats.keySet()) {
			if (stat.isFirst()) {
				firstmask |= stat.getValue();
			} else {
				secondmask |= stat.getValue();
			}
		}
		mplew.writeInt(firstmask);
		mplew.writeInt(secondmask);
	}

	public static MaplePacket applyMonsterStatus(final int oid, final MonsterStatusEffect mse) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		mplew.writeLong(0);
		writeIntMask(mplew, mse.getStati());
		for (Map.Entry<MonsterStatus, Integer> stat : mse.getStati().entrySet()) {
			mplew.writeShort(stat.getValue());
			if (mse.isMonsterSkill()) {
				mplew.writeShort(mse.getMobSkill().getSkillId());
				mplew.writeShort(mse.getMobSkill().getSkillLevel());
			} else {
				mplew.writeInt(mse.getSkill().getId());
			}
			mplew.writeShort(-1); // might actually be the buffTime but it's not
									// displayed anywhere
		}
		mplew.writeShort(0);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket cancelMonsterStatus(int oid, Map<MonsterStatus, Integer> stats) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		mplew.writeLong(0);
		mplew.writeInt(0);
		int mask = 0;
		for (MonsterStatus stat : stats.keySet()) {
			mask |= stat.getValue();
		}
		mplew.writeInt(mask);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket getClock(int time) { // time in seconds
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CLOCK.getValue());
		mplew.write(2); // clock type. if you send 3 here you have to send
						// another byte (which does not matter at all) before
						// the timestamp
		mplew.writeInt(time);
		return mplew.getPacket();
	}

	public static MaplePacket getClockTime(int hour, int min, int sec) { // Current
																			// Time
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CLOCK.getValue());
		mplew.write(1); // Clock-Type
		mplew.write(hour);
		mplew.write(min);
		mplew.write(sec);
		return mplew.getPacket();
	}

	public static MaplePacket spawnMist(int oid, int ownerCid, int skill, int level, MapleMist mist) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_MIST.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(mist.isMobMist() ? 0 : mist.isPoisonMist() ? 1 : 2);
		mplew.writeInt(ownerCid);
		mplew.writeInt(skill);
		mplew.write(level);
		mplew.writeShort(mist.getSkillDelay()); // Skill delay
		mplew.writeInt(mist.getBox().x);
		mplew.writeInt(mist.getBox().y);
		mplew.writeInt(mist.getBox().x + mist.getBox().width);
		mplew.writeInt(mist.getBox().y + mist.getBox().height);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket removeMist(int oid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REMOVE_MIST.getValue());
		mplew.writeInt(oid);
		return mplew.getPacket();
	}

	public static MaplePacket damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DAMAGE_SUMMON.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(summonSkillId);
		mplew.write(unkByte);
		mplew.writeInt(damage);
		mplew.writeInt(monsterIdFrom);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket damageMonster(int oid, int damage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(0);
		mplew.writeInt(damage);
		mplew.write(0);
		mplew.write(0);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket healMonster(int oid, int heal) {
		return damageMonster(oid, -heal);
	}

	public static MaplePacket updateBuddylist(Collection<BuddylistEntry> buddylist) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
		mplew.write(7);
		mplew.write(buddylist.size());
		for (BuddylistEntry buddy : buddylist) {
			if (buddy.isVisible()) {
				mplew.writeInt(buddy.getCharacterId()); // cid
				mplew.writeAsciiString(getRightPaddedStr(buddy.getName(), '\0', 13));
				mplew.write(0); // opposite status
				mplew.writeInt(buddy.getChannel() - 1);
				mplew.writeAsciiString(getRightPaddedStr(buddy.getGroup(), '\0', 13));
				mplew.writeInt(0);// mapid?
			}
		}
		for (int x = 0; x < buddylist.size(); x++) {
			mplew.writeInt(0);// mapid?
		}
		return mplew.getPacket();
	}

	public static MaplePacket buddylistMessage(byte message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
		mplew.write(message);
		return mplew.getPacket();
	}

	public static MaplePacket requestBuddylistAdd(int cidFrom, int cid, String nameFrom) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
		mplew.write(9);
		mplew.writeInt(cidFrom);
		mplew.writeMapleAsciiString(nameFrom);
		mplew.writeInt(cidFrom);
		mplew.writeAsciiString(getRightPaddedStr(nameFrom, '\0', 11));
		mplew.write(0x09);
		mplew.write(0xf0);
		mplew.write(0x01);
		mplew.writeInt(0x0f);
		mplew.writeNullTerminatedAsciiString("Default Group");
		mplew.writeInt(cid);
		return mplew.getPacket();
	}

	public static MaplePacket updateBuddyChannel(int characterid, byte channel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
		mplew.write(0x14);
		mplew.writeInt(characterid);
		mplew.write(0);
		mplew.writeInt(channel);
		return mplew.getPacket();
	}

	public static MaplePacket itemEffect(int characterid, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_EFFECT.getValue());
		mplew.writeInt(characterid);
		mplew.writeInt(itemid);
		return mplew.getPacket();
	}

	public static MaplePacket updateBuddyCapacity(int capacity) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
		mplew.write(0x15);
		mplew.write(capacity);
		return mplew.getPacket();
	}

	public static MaplePacket showChair(int characterid, int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_CHAIR.getValue());
		mplew.writeInt(characterid);
		mplew.writeInt(itemid);
		return mplew.getPacket();
	}

	public static MaplePacket cancelChair(int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_CHAIR.getValue());
		if (id == -1) {
			mplew.write(0);
		} else {
			mplew.write(1);
			mplew.writeShort(id);
		}
		return mplew.getPacket();
	}

	// is there a way to spawn reactors non-animated?
	public static MaplePacket spawnReactor(MapleReactor reactor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		Point pos = reactor.getPosition();
		mplew.writeShort(SendOpcode.REACTOR_SPAWN.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.writeInt(reactor.getId());
		mplew.write(reactor.getState());
		mplew.writePos(pos);
		mplew.writeShort(0);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket triggerReactor(MapleReactor reactor, int stance) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		Point pos = reactor.getPosition();
		mplew.writeShort(SendOpcode.REACTOR_HIT.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.write(reactor.getState());
		mplew.writePos(pos);
		mplew.writeShort(stance);
		mplew.write(0);
		mplew.write(5); // frame delay, set to 5 since there doesn't appear to
						// be a fixed formula for it
		return mplew.getPacket();
	}

	public static MaplePacket destroyReactor(MapleReactor reactor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		Point pos = reactor.getPosition();
		mplew.writeShort(SendOpcode.REACTOR_DESTROY.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.write(reactor.getState());
		mplew.writePos(pos);
		return mplew.getPacket();
	}

	public static MaplePacket musicChange(String song) {
		return environmentChange(song, 6);
	}

	public static MaplePacket showEffect(String effect) {
		return environmentChange(effect, 3);
	}

	public static MaplePacket playSound(String sound) {
		return environmentChange(sound, 4);
	}

	public static MaplePacket environmentChange(String env, int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(env);
		return mplew.getPacket();
	}

	public static MaplePacket startMapEffect(String msg, int itemid, boolean active) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAP_EFFECT.getValue());
		mplew.write(active ? 0 : 1);
		mplew.writeInt(itemid);
		if (active) {
			mplew.writeMapleAsciiString(msg);
		}
		return mplew.getPacket();
	}

	public static MaplePacket removeMapEffect() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAP_EFFECT.getValue());
		mplew.write(0);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket mapEffect(String path) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
		mplew.write(3);
		mplew.writeMapleAsciiString(path);
		return mplew.getPacket();
	}

	public static MaplePacket mapSound(String path) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
		mplew.write(4);
		mplew.writeMapleAsciiString(path);
		return mplew.getPacket();
	}

	public static MaplePacket showGuildInfo(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x1A); // signature for showing guild info
		if (c == null) { // show empty guild (used for leaving, expelled)
			mplew.write(0);
			return mplew.getPacket();
		}
		MapleGuild g = c.getClient().getWorldServer().getGuild(c.getMGC());
		if (g == null) { // failed to read from DB - don't show a guild
			mplew.write(0);
			return mplew.getPacket();
		} else {
			c.setGuildRank(c.getGuildRank());
		}
		mplew.write(1); // bInGuild
		mplew.writeInt(g.getId());
		mplew.writeMapleAsciiString(g.getName());
		for (int i = 1; i <= 5; i++) {
			mplew.writeMapleAsciiString(g.getRankTitle(i));
		}
		Collection<MapleGuildCharacter> members = g.getMembers();
		mplew.write(members.size()); // then it is the size of all the members
		for (MapleGuildCharacter mgc : members) {// and each of their character
													// ids o_O
			mplew.writeInt(mgc.getId());
		}
		for (MapleGuildCharacter mgc : members) {
			mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
			mplew.writeInt(mgc.getJobId());
			mplew.writeInt(mgc.getLevel());
			mplew.writeInt(mgc.getGuildRank());
			mplew.writeInt(mgc.isOnline() ? 1 : 0);
			mplew.writeInt(g.getSignature());
			mplew.writeInt(mgc.getAllianceRank());
		}
		mplew.writeInt(g.getCapacity());
		mplew.writeShort(g.getLogoBG());
		mplew.write(g.getLogoBGColor());
		mplew.writeShort(g.getLogo());
		mplew.write(g.getLogoColor());
		mplew.writeMapleAsciiString(g.getNotice());
		mplew.writeInt(g.getGP());
		mplew.writeInt(g.getAllianceId());
		return mplew.getPacket();
	}

	public static MaplePacket guildMemberOnline(int gid, int cid, boolean bOnline) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3d);
		mplew.writeInt(gid);
		mplew.writeInt(cid);
		mplew.write(bOnline ? 1 : 0);
		return mplew.getPacket();
	}

	public static MaplePacket guildInvite(int gid, String charName) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x05);
		mplew.writeInt(gid);
		mplew.writeMapleAsciiString(charName);
		return mplew.getPacket();
	}

	/**
	 * 'Char' has denied your guild invitation.
	 * 
	 * @param charname
	 * @return
	 */
	public static MaplePacket denyGuildInvitation(String charname) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x37);
		mplew.writeMapleAsciiString(charname);
		return mplew.getPacket();
	}

	public static MaplePacket genericGuildMessage(byte code) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(code);
		return mplew.getPacket();
	}

	public static MaplePacket newGuildMember(MapleGuildCharacter mgc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x27);
		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
		mplew.writeInt(mgc.getJobId());
		mplew.writeInt(mgc.getLevel());
		mplew.writeInt(mgc.getGuildRank()); // should be always 5 but whatevs
		mplew.writeInt(mgc.isOnline() ? 1 : 0); // should always be 1 too
		mplew.writeInt(1); // ? could be guild signature, but doesn't seem to
							// matter
		mplew.writeInt(3);
		return mplew.getPacket();
	}

	// someone leaving, mode == 0x2c for leaving, 0x2f for expelled
	public static MaplePacket memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(bExpelled ? 0x2f : 0x2c);
		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.writeMapleAsciiString(mgc.getName());
		return mplew.getPacket();
	}

	// rank change
	public static MaplePacket changeRank(MapleGuildCharacter mgc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x40);
		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.write(mgc.getGuildRank());
		return mplew.getPacket();
	}

	public static MaplePacket guildNotice(int gid, String notice) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x44);
		mplew.writeInt(gid);
		mplew.writeMapleAsciiString(notice);
		return mplew.getPacket();
	}

	public static MaplePacket guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3C);
		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.writeInt(mgc.getLevel());
		mplew.writeInt(mgc.getJobId());
		return mplew.getPacket();
	}

	public static MaplePacket rankTitleChange(int gid, String[] ranks) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3E);
		mplew.writeInt(gid);
		for (int i = 0; i < 5; i++) {
			mplew.writeMapleAsciiString(ranks[i]);
		}
		return mplew.getPacket();
	}

	public static MaplePacket guildDisband(int gid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x32);
		mplew.writeInt(gid);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x42);
		mplew.writeInt(gid);
		mplew.writeShort(bg);
		mplew.write(bgcolor);
		mplew.writeShort(logo);
		mplew.write(logocolor);
		return mplew.getPacket();
	}

	public static MaplePacket guildCapacityChange(int gid, int capacity) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3A);
		mplew.writeInt(gid);
		mplew.write(capacity);
		return mplew.getPacket();
	}

	public static void addThread(MaplePacketLittleEndianWriter mplew, ResultSet rs) throws SQLException {
		mplew.writeInt(rs.getInt("localthreadid"));
		mplew.writeInt(rs.getInt("postercid"));
		mplew.writeMapleAsciiString(rs.getString("name"));
		mplew.writeLong(getKoreanTimestamp(rs.getLong("timestamp")));
		mplew.writeInt(rs.getInt("icon"));
		mplew.writeInt(rs.getInt("replycount"));
	}

	public static MaplePacket BBSThreadList(ResultSet rs, int start) throws SQLException {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BBS_OPERATION.getValue());
		mplew.write(0x06);
		if (!rs.last()) {
			mplew.write(0);
			mplew.writeInt(0);
			mplew.writeInt(0);
			return mplew.getPacket();
		}
		int threadCount = rs.getRow();
		if (rs.getInt("localthreadid") == 0) { // has a notice
			mplew.write(1);
			addThread(mplew, rs);
			threadCount--; // one thread didn't count (because it's a notice)
		} else {
			mplew.write(0);
		}
		if (!rs.absolute(start + 1)) { // seek to the thread before where we
										// start
			rs.first(); // uh, we're trying to start at a place past possible
			start = 0;
		}
		mplew.writeInt(threadCount);
		mplew.writeInt(Math.min(10, threadCount - start));
		for (int i = 0; i < Math.min(10, threadCount - start); i++) {
			addThread(mplew, rs);
			rs.next();
		}
		return mplew.getPacket();
	}

	public static MaplePacket showThread(int localthreadid, ResultSet threadRS, ResultSet repliesRS) throws SQLException, RuntimeException {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BBS_OPERATION.getValue());
		mplew.write(0x07);
		mplew.writeInt(localthreadid);
		mplew.writeInt(threadRS.getInt("postercid"));
		mplew.writeLong(getKoreanTimestamp(threadRS.getLong("timestamp")));
		mplew.writeMapleAsciiString(threadRS.getString("name"));
		mplew.writeMapleAsciiString(threadRS.getString("startpost"));
		mplew.writeInt(threadRS.getInt("icon"));
		if (repliesRS != null) {
			int replyCount = threadRS.getInt("replycount");
			mplew.writeInt(replyCount);
			int i;
			for (i = 0; i < replyCount && repliesRS.next(); i++) {
				mplew.writeInt(repliesRS.getInt("replyid"));
				mplew.writeInt(repliesRS.getInt("postercid"));
				mplew.writeLong(getKoreanTimestamp(repliesRS.getLong("timestamp")));
				mplew.writeMapleAsciiString(repliesRS.getString("content"));
			}
			if (i != replyCount || repliesRS.next()) {
				throw new RuntimeException(String.valueOf(threadRS.getInt("threadid")));
			}
		} else {
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	public static MaplePacket showGuildRanks(int npcid, ResultSet rs) throws SQLException {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x49);
		mplew.writeInt(npcid);
		if (!rs.last()) { // no guilds o.o
			mplew.writeInt(0);
			return mplew.getPacket();
		}
		mplew.writeInt(rs.getRow()); // number of entries
		rs.beforeFirst();
		while (rs.next()) {
			mplew.writeMapleAsciiString(rs.getString("name"));
			mplew.writeInt(rs.getInt("GP"));
			mplew.writeInt(rs.getInt("logo"));
			mplew.writeInt(rs.getInt("logoColor"));
			mplew.writeInt(rs.getInt("logoBG"));
			mplew.writeInt(rs.getInt("logoBGColor"));
		}
		return mplew.getPacket();
	}

	public static MaplePacket updateGP(int gid, int GP) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x48);
		mplew.writeInt(gid);
		mplew.writeInt(GP);
		return mplew.getPacket();
	}

	public static MaplePacket skillEffect(MapleCharacter from, int skillId, int level, byte flags, int speed, byte direction) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SKILL_EFFECT.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(skillId);
		mplew.write(level);
		mplew.write(flags);
		mplew.write(speed);
		mplew.write(direction); // Mmmk
		return mplew.getPacket();
	}

	public static MaplePacket skillCancel(MapleCharacter from, int skillId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_SKILL_EFFECT.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(skillId);
		return mplew.getPacket();
	}

	public static MaplePacket showMagnet(int mobid, byte success) { // Monster
																	// Magnet
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_MAGNET.getValue());
		mplew.writeInt(mobid);
		mplew.write(success);
		mplew.write0(10); // Mmmk
		return mplew.getPacket();
	}

	/**
	 * Sends a player hint.
	 * 
	 * @param hint
	 *            The hint it's going to send.
	 * @param width
	 *            How tall the box is going to be.
	 * @param height
	 *            How long the box is going to be.
	 * @return The player hint packet.
	 */
	public static MaplePacket sendHint(String hint, int width, int height) {
		if (width < 1) {
			width = hint.length() * 10;
			if (width < 40) {
				width = 40;
			}
		}
		if (height < 5) {
			height = 5;
		}
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_HINT.getValue());
		mplew.writeMapleAsciiString(hint);
		mplew.writeShort(width);
		mplew.writeShort(height);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket messengerInvite(String from, int messengerid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x03);
		mplew.writeMapleAsciiString(from);
		mplew.write(0);
		mplew.writeInt(messengerid);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket sendSpouseChat(MapleCharacter wife, String msg) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
		mplew.writeMapleAsciiString(wife.getName());
		mplew.writeMapleAsciiString(msg);
		return mplew.getPacket();
	}

	public static MaplePacket addMessengerPlayer(String from, MapleCharacter chr, int position, byte channel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x00);
		mplew.write(position);
		addCharLook(mplew, chr, true);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(0x00);
		return mplew.getPacket();
	}

	public static MaplePacket removeMessengerPlayer(int position) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x02);
		mplew.write(position);
		return mplew.getPacket();
	}

	public static MaplePacket updateMessengerPlayer(String from, MapleCharacter chr, int position, byte channel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x07);
		mplew.write(position);
		addCharLook(mplew, chr, true);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(0x00);
		return mplew.getPacket();
	}

	public static MaplePacket joinMessenger(int position) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x01);
		mplew.write(position);
		return mplew.getPacket();
	}

	public static MaplePacket messengerChat(String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x06);
		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}

	public static MaplePacket messengerNote(String text, int mode, int mode2) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(text);
		mplew.write(mode2);
		return mplew.getPacket();
	}

	public static void addPetInfo(MaplePacketLittleEndianWriter mplew, MaplePet pet, boolean showpet) {
		mplew.write(1);
		if (showpet) {
			mplew.write(0);
		}

		mplew.writeInt(pet.getItemId());
		mplew.writeMapleAsciiString(pet.getName());
		mplew.writeInt(pet.getUniqueId());
		mplew.writeInt(0);
		mplew.writePos(pet.getPos());
		mplew.write(pet.getStance());
		mplew.writeInt(pet.getFh());
	}

	public static MaplePacket showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_PET.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(chr.getPetIndex(pet));
		if (remove) {
			mplew.write(0);
			mplew.write(hunger ? 1 : 0);
		} else {
			addPetInfo(mplew, pet, true);
		}
		return mplew.getPacket();
	}

	public static MaplePacket movePet(int cid, int pid, byte slot, List<LifeMovementFragment> moves) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOVE_PET.getValue());
		mplew.writeInt(cid);
		mplew.write(slot);
		mplew.writeInt(pid);
		serializeMovementList(mplew, moves);
		return mplew.getPacket();
	}

	public static MaplePacket petChat(int cid, byte index, int act, String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PET_CHAT.getValue());
		mplew.writeInt(cid);
		mplew.write(index);
		mplew.write(0);
		mplew.write(act);
		mplew.writeMapleAsciiString(text);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket commandResponse(int cid, byte index, int animation, boolean success) {
		// AE 00 01 00 00 00 00 01 00 00
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PET_COMMAND.getValue());
		mplew.writeInt(cid);
		mplew.write(index);
		mplew.write((animation == 1 || !success) ? 1 : 0);
		mplew.write(animation);
		if (animation == 1) {
			mplew.write(0);
		} else {
			mplew.writeShort(success ? 1 : 0);
		}
		return mplew.getPacket();
	}

	public static MaplePacket showOwnPetLevelUp(byte index) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(4);
		mplew.write(0);
		mplew.write(index); // Pet Index
		return mplew.getPacket();
	}

	public static MaplePacket showPetLevelUp(MapleCharacter chr, byte index) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(4);
		mplew.write(0);
		mplew.write(index);
		return mplew.getPacket();
	}

	public static MaplePacket changePetName(MapleCharacter chr, String newname, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PET_NAMECHANGE.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(0);
		mplew.writeMapleAsciiString(newname);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket petStatUpdate(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_STATS.getValue());
		int mask = 0;
		mask |= MapleStat.PET.getValue();
		mplew.write(0);
		mplew.writeInt(mask);
		MaplePet[] pets = chr.getPets();
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				mplew.writeInt(pets[i].getUniqueId());
				mplew.writeInt(0);
			} else {
				mplew.writeLong(0);
			}
		}
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket showForcedEquip(int team) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FORCED_MAP_EQUIP.getValue());
		if (team > -1) {
			mplew.write(team); // 00 = red, 01 = blue
		}
		return mplew.getPacket();
	}

	public static MaplePacket summonSkill(int cid, int summonSkillId, int newStance) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SUMMON_SKILL.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(summonSkillId);
		mplew.write(newStance);
		return mplew.getPacket();
	}

	public static MaplePacket skillCooldown(int sid, int time) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.COOLDOWN.getValue());
		mplew.writeInt(sid);
		mplew.writeShort(time);// Int in v97
		return mplew.getPacket();
	}

	public static MaplePacket skillBookSuccess(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.USE_SKILL_BOOK.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(1);
		mplew.writeInt(skillid);
		mplew.writeInt(maxlevel);
		mplew.write(canuse ? 1 : 0);
		mplew.write(success ? 1 : 0);
		return mplew.getPacket();
	}

	public static MaplePacket getMacros(SkillMacro[] macros) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SKILL_MACRO.getValue());
		int count = 0;
		for (int i = 0; i < 5; i++) {
			if (macros[i] != null) {
				count++;
			}
		}
		mplew.write(count);
		for (int i = 0; i < 5; i++) {
			SkillMacro macro = macros[i];
			if (macro != null) {
				mplew.writeMapleAsciiString(macro.getName());
				mplew.write(macro.getShout());
				mplew.writeInt(macro.getSkill1());
				mplew.writeInt(macro.getSkill2());
				mplew.writeInt(macro.getSkill3());
			}
		}
		return mplew.getPacket();
	}

	public static MaplePacket getPlayerNPC(PlayerNPCs npc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_NPC.getValue());
		mplew.write(0x01);
		mplew.writeInt(npc.getId());
		mplew.writeMapleAsciiString(npc.getName());
		mplew.write(0); // direction
		mplew.write(npc.getSkin());
		mplew.writeInt(npc.getFace());
		mplew.write(0);
		mplew.writeInt(npc.getHair());
		Map<Byte, Integer> equip = npc.getEquips();
		Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
		for (byte position : equip.keySet()) {
			byte pos = (byte) (position * -1);
			if (pos > 100) {
				pos -= 100;
				myEquip.put(pos, equip.get(position));
			} else {
				if (myEquip.get(pos) == null) {
					myEquip.put(pos, equip.get(position));
				}
			}
		}
		for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.writeShort(-1);
		Integer cWeapon = equip.get((byte) -111);
		if (cWeapon != null) {
			mplew.writeInt(cWeapon);
		} else {
			mplew.writeInt(0);
		}
		for (int i = 0; i < 12; i++) {
			mplew.write(0);
		}
		return mplew.getPacket();
	}

	public static MaplePacket updateAriantPQRanking(String name, int score, boolean empty) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ARIANT_SCORE.getValue());
		mplew.write(empty ? 0 : 1);
		if (!empty) {
			mplew.writeMapleAsciiString(name);
			mplew.writeInt(score);
		}
		return mplew.getPacket();
	}

	public static MaplePacket catchMonster(int monsobid, int itemid, byte success) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CATCH_MONSTER.getValue());
		mplew.writeInt(monsobid);
		mplew.writeInt(itemid);
		mplew.write(success);
		return mplew.getPacket();
	}

	public static MaplePacket catchMessage(int message) { // not done, I guess
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CATCH_MESSAGE.getValue());
		mplew.write(message); // 1 = too strong, 2 = Elemental Rock
		mplew.writeInt(0);// Maybe itemid?
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket showAllCharacter(int chars, int unk) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.ALL_CHARLIST.getValue());
		mplew.write(1);
		mplew.writeInt(chars);
		mplew.writeInt(unk);
		return mplew.getPacket();
	}

	public static MaplePacket showAllCharacterInfo(byte worldid, List<MapleCharacter> chars) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALL_CHARLIST.getValue());
		mplew.write(0);
		mplew.write(worldid);
		mplew.write(chars.size());
		for (MapleCharacter chr : chars) {
			addCharEntry(mplew, chr, true);
		}
		return mplew.getPacket();
	}

	public static MaplePacket updateMount(int charid, MapleMount mount, boolean levelup) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_MOUNT.getValue());
		mplew.writeInt(charid);
		mplew.writeInt(mount.getLevel());
		mplew.writeInt(mount.getExp());
		mplew.writeInt(mount.getTiredness());
		mplew.write(levelup ? (byte) 1 : (byte) 0);
		return mplew.getPacket();
	}

	public static MaplePacket boatPacket(boolean type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BOAT_EFFECT.getValue());
		mplew.writeShort(type ? 1 : 2);
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGame(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(1);
		mplew.write(0);
		mplew.write(owner ? 0 : 1);
		mplew.write(0);
		addCharLook(mplew, minigame.getOwner(), false);
		mplew.writeMapleAsciiString(minigame.getOwner().getName());
		if (minigame.getVisitor() != null) {
			MapleCharacter visitor = minigame.getVisitor();
			mplew.write(1);
			addCharLook(mplew, visitor, false);
			mplew.writeMapleAsciiString(visitor.getName());
		}
		mplew.write(0xFF);
		mplew.write(0);
		mplew.writeInt(1);
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", true));
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", true));
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", true));
		mplew.writeInt(2000);
		if (minigame.getVisitor() != null) {
			MapleCharacter visitor = minigame.getVisitor();
			mplew.write(1);
			mplew.writeInt(1);
			mplew.writeInt(visitor.getMiniGamePoints("wins", true));
			mplew.writeInt(visitor.getMiniGamePoints("ties", true));
			mplew.writeInt(visitor.getMiniGamePoints("losses", true));
			mplew.writeInt(2000);
		}
		mplew.write(0xFF);
		mplew.writeMapleAsciiString(minigame.getDescription());
		mplew.write(piece);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameReady(MapleMiniGame game) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.READY.getCode());
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameUnReady(MapleMiniGame game) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.UN_READY.getCode());
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameStart(MapleMiniGame game, int loser) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.START.getCode());
		mplew.write(loser);
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameSkipOwner(MapleMiniGame game) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.SKIP.getCode());
		mplew.write(0x01);
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameRequestTie(MapleMiniGame game) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.REQUEST_TIE.getCode());
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameDenyTie(MapleMiniGame game) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ANSWER_TIE.getCode());
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameFull() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(0);
		mplew.write(2);
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameSkipVisitor(MapleMiniGame game) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.writeShort(PlayerInteractionHandler.Action.SKIP.getCode());
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameMoveOmok(MapleMiniGame game, int move1, int move2, int move3) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.MOVE_OMOK.getCode());
		mplew.writeInt(move1);
		mplew.writeInt(move2);
		mplew.write(move3);
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameNewVisitor(MapleCharacter c, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
		mplew.write(slot);
		addCharLook(mplew, c, false);
		mplew.writeMapleAsciiString(c.getName());
		mplew.writeInt(1);
		mplew.writeInt(c.getMiniGamePoints("wins", true));
		mplew.writeInt(c.getMiniGamePoints("ties", true));
		mplew.writeInt(c.getMiniGamePoints("losses", true));
		mplew.writeInt(2000);
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameRemoveVisitor() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		mplew.write(1);
		return mplew.getPacket();
	}

	private static MaplePacket getMiniGameResult(MapleMiniGame game, int win, int lose, int tie, int result, int forfeit, boolean omok) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.GET_RESULT.getCode());
		if (tie == 0 && forfeit != 1) {
			mplew.write(0);
		} else if (tie == 1) {
			mplew.write(1);
		} else if (forfeit == 1) {
			mplew.write(2);
		}
		mplew.write(0); // owner
		mplew.writeInt(1); // unknown
		mplew.writeInt(game.getOwner().getMiniGamePoints("wins", omok) + win); // wins
		mplew.writeInt(game.getOwner().getMiniGamePoints("ties", omok) + tie); // ties
		mplew.writeInt(game.getOwner().getMiniGamePoints("losses", omok) + lose); // losses
		mplew.writeInt(2000); // points
		mplew.writeInt(1); // start of visitor; unknown
		mplew.writeInt(game.getVisitor().getMiniGamePoints("wins", omok) + lose); // wins
		mplew.writeInt(game.getVisitor().getMiniGamePoints("ties", omok) + tie); // ties
		mplew.writeInt(game.getVisitor().getMiniGamePoints("losses", omok) + win); // losses
		mplew.writeInt(2000); // points
		game.getOwner().setMiniGamePoints(game.getVisitor(), result, omok);
		return mplew.getPacket();
	}

	public static MaplePacket getMiniGameOwnerWin(MapleMiniGame game) {
		return getMiniGameResult(game, 0, 1, 0, 1, 0, true);
	}

	public static MaplePacket getMiniGameVisitorWin(MapleMiniGame game) {
		return getMiniGameResult(game, 1, 0, 0, 2, 0, true);
	}

	public static MaplePacket getMiniGameTie(MapleMiniGame game) {
		return getMiniGameResult(game, 0, 0, 1, 3, 0, true);
	}

	public static MaplePacket getMiniGameOwnerForfeit(MapleMiniGame game) {
		return getMiniGameResult(game, 0, 1, 0, 2, 1, true);
	}

	public static MaplePacket getMiniGameVisitorForfeit(MapleMiniGame game) {
		return getMiniGameResult(game, 1, 0, 0, 1, 1, true);
	}

	public static MaplePacket getMiniGameClose() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		mplew.write(1);
		mplew.write(3);
		return mplew.getPacket();
	}

	public static MaplePacket getMatchCard(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(2);
		mplew.write(2);
		mplew.write(owner ? 0 : 1);
		mplew.write(0);
		addCharLook(mplew, minigame.getOwner(), false);
		mplew.writeMapleAsciiString(minigame.getOwner().getName());
		if (minigame.getVisitor() != null) {
			MapleCharacter visitor = minigame.getVisitor();
			mplew.write(1);
			addCharLook(mplew, visitor, false);
			mplew.writeMapleAsciiString(visitor.getName());
		}
		mplew.write(0xFF);
		mplew.write(0);
		mplew.writeInt(2);
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", false));
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", false));
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", false));
		mplew.writeInt(2000);
		if (minigame.getVisitor() != null) {
			MapleCharacter visitor = minigame.getVisitor();
			mplew.write(1);
			mplew.writeInt(2);
			mplew.writeInt(visitor.getMiniGamePoints("wins", false));
			mplew.writeInt(visitor.getMiniGamePoints("ties", false));
			mplew.writeInt(visitor.getMiniGamePoints("losses", false));
			mplew.writeInt(2000);
		}
		mplew.write(0xFF);
		mplew.writeMapleAsciiString(minigame.getDescription());
		mplew.write(piece);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket getMatchCardStart(MapleMiniGame game, int loser) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.START.getCode());
		mplew.write(loser);
		mplew.write(0x0C);
		int last = 13;
		if (game.getMatchesToWin() > 10) {
			last = 31;
		} else if (game.getMatchesToWin() > 6) {
			last = 21;
		}
		for (int i = 1; i < last; i++) {
			mplew.writeInt(game.getCardId(i));
		}
		return mplew.getPacket();
	}

	public static MaplePacket getMatchCardNewVisitor(MapleCharacter c, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
		mplew.write(slot);
		addCharLook(mplew, c, false);
		mplew.writeMapleAsciiString(c.getName());
		mplew.writeInt(1);
		mplew.writeInt(c.getMiniGamePoints("wins", false));
		mplew.writeInt(c.getMiniGamePoints("ties", false));
		mplew.writeInt(c.getMiniGamePoints("losses", false));
		mplew.writeInt(2000);
		return mplew.getPacket();
	}

	public static MaplePacket getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.SELECT_CARD.getCode());
		mplew.write(turn);
		if (turn == 1) {
			mplew.write(slot);
		} else if (turn == 0) {
			mplew.write(slot);
			mplew.write(firstslot);
			mplew.write(type);
		}
		return mplew.getPacket();
	}

	public static MaplePacket getMatchCardOwnerWin(MapleMiniGame game) {
		return getMiniGameResult(game, 1, 0, 0, 1, 0, false);
	}

	public static MaplePacket getMatchCardVisitorWin(MapleMiniGame game) {
		return getMiniGameResult(game, 0, 1, 0, 2, 0, false);
	}

	public static MaplePacket getMatchCardTie(MapleMiniGame game) {
		return getMiniGameResult(game, 0, 0, 1, 3, 0, false);
	}

	public static MaplePacket fredrickMessage(byte operation) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FREDRICK_MESSAGE.getValue());
		mplew.write(operation);
		return mplew.getPacket();
	}

	public static MaplePacket getFredrick(byte op) {
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FREDRICK.getValue());
		mplew.write(op);

		switch (op) {
			case 0x24:
				mplew.write0(8);
				break;
			default:
				mplew.write(0);
				break;
		}

		return mplew.getPacket();
	}

	public static MaplePacket getFredrick(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FREDRICK.getValue());
		mplew.write(0x23);
		mplew.writeInt(9030000); // Fredrick
		mplew.writeInt(32272); // id
		mplew.write0(5);
		mplew.writeInt(chr.getMerchantMeso());
		mplew.write(0);
		try {
			List<ItemInventoryEntry> entries = ItemFactory.MERCHANT.loadItems(chr.getId(), false);
			mplew.write(entries.size());

			for (int i = 0; i < entries.size(); i++) {
				addItemInfo(mplew, entries.get(i).item, true);
			}
		} catch (SQLException e) {
		}
		mplew.write0(3);
		return mplew.getPacket();
	}

	public static MaplePacket addOmokBox(MapleCharacter c, int ammount, int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		addAnnounceBox(mplew, c.getMiniGame(), 1, 0, ammount, type);
		return mplew.getPacket();
	}

	public static MaplePacket removeOmokBox(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket addMatchCardBox(MapleCharacter c, int ammount, int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		addAnnounceBox(mplew, c.getMiniGame(), 2, 0, ammount, type);
		return mplew.getPacket();
	}

	public static MaplePacket removeMatchcardBox(MapleCharacter c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket getPlayerShopChat(MapleCharacter c, String chat, byte slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
		mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
		mplew.write(slot);
		mplew.writeMapleAsciiString(c.getName() + " : " + chat);
		return mplew.getPacket();
	}

	public static MaplePacket getTradeChat(MapleCharacter c, String chat, boolean owner) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
		mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
		mplew.write(owner ? 0 : 1);
		mplew.writeMapleAsciiString(c.getName() + " : " + chat);
		return mplew.getPacket();
	}

	public static MaplePacket hiredMerchantBox() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SEND_TITLE_BOX.getValue()); // header.
		mplew.write(0x07);
		return mplew.getPacket();
	}

	public static MaplePacket owlOfMinerva(MapleClient c, int itemid, List<HiredMerchant> hms, List<MaplePlayerShopItem> items) { // Thanks
																																	// moongra,
																																	// you
																																	// save
																																	// me
																																	// some
																																	// time
																																	// :)
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.OWL_OF_MINERVA.getValue()); // header.
		mplew.write(6);
		mplew.writeInt(0);
		mplew.writeInt(itemid);
		mplew.writeInt(hms.size());
		for (HiredMerchant hm : hms) {
			for (MaplePlayerShopItem item : items) {
				mplew.writeMapleAsciiString(hm.getOwner());
				mplew.writeInt(hm.getMapId());
				mplew.writeMapleAsciiString(hm.getDescription());
				mplew.writeInt(item.getItem().getQuantity());
				mplew.writeInt(item.getBundles());
				mplew.writeInt(item.getPrice());
				mplew.writeInt(hm.getOwnerId());
				mplew.write(hm.getFreeSlot() == -1 ? 1 : 0);
				MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(hm.getOwnerId());
				if ((chr != null) && (c.getChannel() == hm.getChannel())) {
					mplew.write(1);
				} else {
					mplew.write(2);
				}

				if (item.getItem().getItemId() / 1000000 == 1) {
					addItemInfo(mplew, item.getItem(), true);
				}
			}
		}
		return mplew.getPacket();
	}

	public static MaplePacket retrieveFirstMessage() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SEND_TITLE_BOX.getValue()); // header.
		mplew.write(0x09);
		return mplew.getPacket();
	}

	public static MaplePacket remoteChannelChange(byte ch) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SEND_TITLE_BOX.getValue()); // header.
		mplew.write(0x10);
		mplew.writeInt(0);// No idea yet
		mplew.write(ch);
		return mplew.getPacket();
	}

	/*
	 * Possible things for SEND_TITLE_BOX 0x0E = 00 = Renaming Failed - Can't
	 * find the merchant, 01 = Renaming succesful 0x10 = Changes channel to the
	 * store (Store is open at Channel 1, do you want to change channels?) 0x11
	 * = You cannot sell any items when managing.. blabla 0x12 = FKING POPUP LOL
	 */

	public static MaplePacket getHiredMerchant(MapleCharacter chr, HiredMerchant hm, boolean firstTime) {// Thanks
																											// Dustin
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(0x05);
		mplew.write(0x04);
		mplew.writeShort(hm.getVisitorSlot(chr) + 1);
		mplew.writeInt(hm.getItemId());
		mplew.writeMapleAsciiString("Hired Merchant");
		for (int i = 0; i < 3; i++) {
			if (hm.getVisitors()[i] != null) {
				mplew.write(i + 1);
				addCharLook(mplew, hm.getVisitors()[i], false);
				mplew.writeMapleAsciiString(hm.getVisitors()[i].getName());
			}
		}
		mplew.write(-1);
		if (hm.isOwner(chr)) {
			mplew.writeShort(hm.getMessages().size());
			for (int i = 0; i < hm.getMessages().size(); i++) {
				mplew.writeMapleAsciiString(hm.getMessages().get(i).message);
				mplew.write(hm.getMessages().get(i).slot);
			}
		} else {
			mplew.writeShort(0);
		}
		mplew.writeMapleAsciiString(hm.getOwner());
		if (hm.isOwner(chr)) {
			mplew.writeInt(hm.getTimeLeft());
			mplew.write(firstTime ? 1 : 0);
			// List<SoldItem> sold = hm.getSold();
			mplew.write(0);// sold.size()
			/*
			 * for (SoldItem s : sold) { fix this mplew.writeInt(s.getItemId());
			 * mplew.writeShort(s.getQuantity()); mplew.writeInt(s.getMesos());
			 * mplew.writeMapleAsciiString(s.getBuyer()); }
			 */
			mplew.writeInt(chr.getMerchantMeso());// :D?
		}
		mplew.writeMapleAsciiString(hm.getDescription());
		mplew.write(0x10); // SLOTS, which is 16 for most stores...slotMax
		mplew.writeInt(chr.getMeso());
		mplew.write(hm.getItems().size());
		if (hm.getItems().isEmpty()) {
			mplew.write(0);// Hmm??
		} else {
			for (MaplePlayerShopItem item : hm.getItems()) {
				mplew.writeShort(item.getBundles());
				mplew.writeShort(item.getItem().getQuantity());
				mplew.writeInt(item.getPrice());
				addItemInfo(mplew, item.getItem(), true);
			}
		}
		return mplew.getPacket();
	}

	public static MaplePacket updateHiredMerchant(HiredMerchant hm, MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
		mplew.writeInt(chr.getMeso());
		mplew.write(hm.getItems().size());
		for (MaplePlayerShopItem item : hm.getItems()) {
			mplew.writeShort(item.getBundles());
			mplew.writeShort(item.getItem().getQuantity());
			mplew.writeInt(item.getPrice());
			addItemInfo(mplew, item.getItem(), true);
		}
		return mplew.getPacket();
	}

	public static MaplePacket hiredMerchantChat(String message, byte slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
		mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
		mplew.write(slot);
		mplew.writeMapleAsciiString(message);
		return mplew.getPacket();
	}

	public static MaplePacket hiredMerchantVisitorLeave(int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		if (slot != 0) {
			mplew.write(slot);
		}
		return mplew.getPacket();
	}

	public static MaplePacket hiredMerchantOwnerLeave() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket leaveHiredMerchant(int slot, int status2) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		mplew.write(slot);
		mplew.write(status2);
		return mplew.getPacket();
	}

	public static MaplePacket hiredMerchantVisitorAdd(MapleCharacter chr, int slot) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
		mplew.write(slot);
		addCharLook(mplew, chr, false);
		mplew.writeMapleAsciiString(chr.getName());
		return mplew.getPacket();
	}

	public static MaplePacket spawnHiredMerchant(HiredMerchant hm) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_HIRED_MERCHANT.getValue());
		mplew.writeInt(hm.getOwnerId());
		mplew.writeInt(hm.getItemId());
		mplew.writeShort((short) hm.getPosition().getX());
		mplew.writeShort((short) hm.getPosition().getY());
		mplew.writeShort(0);
		mplew.writeMapleAsciiString(hm.getOwner());
		mplew.write(0x05);
		mplew.writeInt(hm.getObjectId());
		mplew.writeMapleAsciiString(hm.getDescription());
		mplew.write(hm.getItemId() % 10);
		mplew.write(new byte[] {1, 4});
		return mplew.getPacket();
	}

	public static MaplePacket destroyHiredMerchant(int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DESTROY_HIRED_MERCHANT.getValue());
		mplew.writeInt(id);
		return mplew.getPacket();
	}

	public static MaplePacket spawnPlayerNPC(PlayerNPCs npc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
		mplew.write(1);
		mplew.writeInt(npc.getObjectId());
		mplew.writeInt(npc.getId());
		mplew.writeShort(npc.getPosition().x);
		mplew.writeShort(npc.getCY());
		mplew.write(1);
		mplew.writeShort(npc.getFH());
		mplew.writeShort(npc.getRX0());
		mplew.writeShort(npc.getRX1());
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket sendYellowTip(String tip) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.YELLOW_TIP.getValue());
		mplew.write(0xFF);
		mplew.writeMapleAsciiString(tip);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	public static MaplePacket giveInfusion(int buffid, int bufflength, int speed) {// This
																					// ain't
																					// correct
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		mplew.writeLong(MapleBuffStat.SPEED_INFUSION.getValue());
		mplew.writeLong(0);
		mplew.writeShort(speed);
		mplew.writeInt(buffid);
		mplew.write(0);
		mplew.writeShort(bufflength);
		mplew.writeShort(0);
		mplew.writeShort(0);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	public static MaplePacket givePirateBuff(List<MapleBuffStatDelta> statups, int buffid, int duration) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		writeLongMask(mplew, statups);
		mplew.writeShort(0);
		for (MapleBuffStatDelta stat : statups) {
			mplew.writeInt(stat.delta);
			mplew.writeInt(buffid);
			mplew.write0(5);
			mplew.writeShort(duration);
		}
		mplew.write0(3);
		return mplew.getPacket();
	}

	public static MaplePacket giveForeignDash(int cid, int buffid, int time, List<MapleBuffStatDelta> statups) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		writeLongMask(mplew, statups);
		mplew.writeShort(0);
		for (MapleBuffStatDelta statup : statups) {
			mplew.writeInt(statup.delta);
			mplew.writeInt(buffid);
			mplew.write0(5);
			mplew.writeShort(time);
		}
		mplew.writeShort(0);
		mplew.write(2);
		return mplew.getPacket();
	}

	public static MaplePacket giveForeignInfusion(int cid, int speed, int duration) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		mplew.writeLong(MapleBuffStat.SPEED_INFUSION.getValue());
		mplew.writeLong(0);
		mplew.writeShort(0);
		mplew.writeInt(speed);
		mplew.writeInt(5121009);
		mplew.writeLong(0);
		mplew.writeInt(duration);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	public static MaplePacket sendMTS(List<MTSItemInfo> items, int tab, int type, int page, int pages) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x15); // operation
		mplew.writeInt(pages * 16); // testing, change to 10 if fails
		mplew.writeInt(items.size()); // number of items
		mplew.writeInt(tab);
		mplew.writeInt(type);
		mplew.writeInt(page);
		mplew.write(1);
		mplew.write(1);
		for (int i = 0; i < items.size(); i++) {
			MTSItemInfo item = items.get(i);
			addItemInfo(mplew, item.getItem(), true);
			mplew.writeInt(item.getID()); // id
			mplew.writeInt(item.getTaxes()); // this + below = price
			mplew.writeInt(item.getPrice()); // price
			mplew.writeLong(0);
			mplew.writeInt(getQuestTimestamp(item.getEndingDate()));
			mplew.writeMapleAsciiString(item.getSeller()); // account name (what
															// was nexon
															// thinking?)
			mplew.writeMapleAsciiString(item.getSeller()); // char name
			for (int j = 0; j < 28; j++) {
				mplew.write(0);
			}
		}
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket noteSendMsg() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.NOTE_ACTION.getValue());
		mplew.write(4);
		return mplew.getPacket();
	}

	/*
	 * 0 = Player online, use whisper 1 = Check player's name 2 = Receiver inbox
	 * full
	 */
	public static MaplePacket noteError(byte error) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.NOTE_ACTION.getValue());
		mplew.write(5);
		mplew.write(error);
		return mplew.getPacket();
	}

	public static MaplePacket showNotes(ResultSet notes, int count) throws SQLException {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NOTE_ACTION.getValue());
		mplew.write(3);
		mplew.write(count);
		for (int i = 0; i < count; i++) {
			mplew.writeInt(notes.getInt("id"));
			mplew.writeMapleAsciiString(notes.getString("from") + " ");// Stupid
																		// nexon
																		// forgot
																		// space
																		// lol
			mplew.writeMapleAsciiString(notes.getString("message"));
			mplew.writeLong(getKoreanTimestamp(notes.getLong("timestamp")));
			mplew.write(notes.getByte("fame"));// FAME :D
			notes.next();
		}
		return mplew.getPacket();
	}

	public static MaplePacket useChalkboard(MapleCharacter chr, boolean close) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHALKBOARD.getValue());
		mplew.writeInt(chr.getId());
		if (close) {
			mplew.write(0);
		} else {
			mplew.write(1);
			mplew.writeMapleAsciiString(chr.getChalkboard());
		}
		return mplew.getPacket();
	}

	public static MaplePacket trockRefreshMapList(MapleCharacter chr, boolean delete, boolean vip) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.TROCK_LOCATIONS.getValue());
		mplew.write(delete ? 2 : 3);
		if (vip) {
			mplew.write(1);
			int[] map = chr.getVipTrockMaps();
			for (int i = 0; i < 10; i++) {
				mplew.writeInt(map[i]);
			}
		} else {
			mplew.write(0);
			int[] map = chr.getTrockMaps();
			for (int i = 0; i < 5; i++) {
				mplew.writeInt(map[i]);
			}
		}
		return mplew.getPacket();
	}

	public static MaplePacket showMTSCash(MapleCharacter p) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION2.getValue());
		mplew.writeInt(p.getCashShop().getCash(4));
		mplew.writeInt(p.getCashShop().getCash(2));
		return mplew.getPacket();
	}

	public static MaplePacket MTSWantedListingOver(int nx, int items) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x3D);
		mplew.writeInt(nx);
		mplew.writeInt(items);
		return mplew.getPacket();
	}

	public static MaplePacket MTSConfirmSell() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x1D);
		return mplew.getPacket();
	}

	public static MaplePacket MTSConfirmBuy() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x33);
		return mplew.getPacket();
	}

	public static MaplePacket MTSFailBuy() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x34);
		mplew.write(0x42);
		return mplew.getPacket();
	}

	public static MaplePacket MTSConfirmTransfer(int quantity, int pos) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x27);
		mplew.writeInt(quantity);
		mplew.writeInt(pos);
		return mplew.getPacket();
	}

	public static MaplePacket notYetSoldInv(List<MTSItemInfo> items) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x23);
		mplew.writeInt(items.size());
		if (!items.isEmpty()) {
			for (MTSItemInfo item : items) {
				addItemInfo(mplew, item.getItem(), true);
				mplew.writeInt(item.getID()); // id
				mplew.writeInt(item.getTaxes()); // this + below = price
				mplew.writeInt(item.getPrice()); // price
				mplew.writeLong(0);
				mplew.writeInt(getQuestTimestamp(item.getEndingDate()));
				mplew.writeMapleAsciiString(item.getSeller()); // account name
																// (what was
																// nexon
																// thinking?)
				mplew.writeMapleAsciiString(item.getSeller()); // char name
				for (int i = 0; i < 28; i++) {
					mplew.write(0);
				}
			}
		} else {
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	public static MaplePacket transferInventory(List<MTSItemInfo> items) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x21);
		mplew.writeInt(items.size());
		if (!items.isEmpty()) {
			for (MTSItemInfo item : items) {
				addItemInfo(mplew, item.getItem(), true);
				mplew.writeInt(item.getID()); // id
				mplew.writeInt(item.getTaxes()); // taxes
				mplew.writeInt(item.getPrice()); // price
				mplew.writeLong(0);
				mplew.writeInt(getQuestTimestamp(item.getEndingDate()));
				mplew.writeMapleAsciiString(item.getSeller()); // account name
																// (what was
																// nexon
																// thinking?)
				mplew.writeMapleAsciiString(item.getSeller()); // char name
				for (int i = 0; i < 28; i++) {
					mplew.write(0);
				}
			}
		}
		mplew.write(0xD0 + items.size());
		mplew.write(new byte[] {-1, -1, -1, 0});
		return mplew.getPacket();
	}

	public static MaplePacket showCouponRedeemedItem(int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
		mplew.writeShort(0x49); // v72
		mplew.writeInt(0);
		mplew.writeInt(1);
		mplew.writeShort(1);
		mplew.writeShort(0x1A);
		mplew.writeInt(itemid);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket showCash(MapleCharacter mc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_CASH.getValue());

		mplew.writeInt(mc.getCashShop().getCash(1));
		mplew.writeInt(mc.getCashShop().getCash(2));
		mplew.writeInt(mc.getCashShop().getCash(4));

		return mplew.getPacket();
	}

	public static MaplePacket enableCSUse() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.write(0x12);
		mplew.write0(6);
		return mplew.getPacket();
	}

	/**
	 * 
	 * @param target
	 * @param mapid
	 * @param MTSmapCSchannel
	 *            0: MTS 1: Map 2: CS 3: Different Channel
	 * @return
	 */
	public static MaplePacket getFindReply(String target, int mapid, int MTSmapCSchannel) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WHISPER.getValue());
		mplew.write(9);
		mplew.writeMapleAsciiString(target);
		mplew.write(MTSmapCSchannel); // 0: mts 1: map 2: cs
		mplew.writeInt(mapid); // -1 if mts, cs
		if (MTSmapCSchannel == 1) {
			mplew.write(new byte[8]);
		}
		return mplew.getPacket();
	}

	public static MaplePacket sendAutoHpPot(int itemId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.AUTO_HP_POT.getValue());
		mplew.writeInt(itemId);
		return mplew.getPacket();
	}

	public static MaplePacket sendAutoMpPot(int itemId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.AUTO_MP_POT.getValue());
		mplew.writeInt(itemId);
		return mplew.getPacket();
	}

	public static MaplePacket showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.OX_QUIZ.getValue());
		mplew.write(askQuestion ? 1 : 0);
		mplew.write(questionSet);
		mplew.writeShort(questionId);
		return mplew.getPacket();
	}

	public static MaplePacket updateGender(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.GENDER.getValue());
		mplew.write(chr.getGender());
		return mplew.getPacket();
	}

	public static MaplePacket enableReport() { // by snow
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.ENABLE_REPORT.getValue());
		mplew.write(1);
		return mplew.getPacket();
	}

	public static MaplePacket giveFinalAttack(int skillid, int time) {// packets
																		// found
																		// by
																		// lailainoob
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		mplew.writeLong(0);
		mplew.writeShort(0);
		mplew.write(0);// some 80 and 0 bs DIRECTION
		mplew.write(0x80);// let's just do 80, then 0
		mplew.writeInt(0);
		mplew.writeShort(1);
		mplew.writeInt(skillid);
		mplew.writeInt(time);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket loadFamily(MapleCharacter player) {
		String[] title = {"Family Reunion", "Summon Family", "My Drop Rate 1.5x (15 min)", "My EXP 1.5x (15 min)", "Family Bonding (30 min)", "My Drop Rate 2x (15 min)", "My EXP 2x (15 min)", "My Drop Rate 2x (30 min)", "My EXP 2x (30 min)", "My Party Drop Rate 2x (30 min)", "My Party EXP 2x (30 min)"};
		String[] description = {"[Target] Me\n[Effect] Teleport directly to the Family member of your choice.", "[Target] 1 Family member\n[Effect] Summon a Family member of choice to the map you're in.", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c1.5x#.\n*  If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c1.5x#.\n* If the EXP event is in progress, this will be nullified.", "[Target] At least 6 Family members online that are below me in the Pedigree\n[Time] 30 min.\n[Effect] Monster drop rate and EXP earned will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified.", "[Target] Me\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.", "[Target] My party\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] My party\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified."};
		int[] repCost = {3, 5, 7, 8, 10, 12, 15, 20, 25, 40, 50};
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.LOAD_FAMILY.getValue());
		mplew.writeInt(11);
		for (int i = 0; i < 11; i++) {
			mplew.write(i > 4 ? (i % 2) + 1 : i);
			mplew.writeInt(repCost[i] * 100);
			mplew.writeInt(1);
			mplew.writeMapleAsciiString(title[i]);
			mplew.writeMapleAsciiString(description[i]);
		}
		return mplew.getPacket();
	}

	public static MaplePacket sendFamilyMessage() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.FAMILY_MESSAGE.getValue());
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket getFamilyInfo(MapleFamilyEntry f) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.OPEN_FAMILY.getValue());
		mplew.writeInt(f.getReputation()); // cur rep left
		mplew.writeInt(f.getTotalReputation()); // tot rep left
		mplew.writeInt(f.getTodaysRep()); // todays rep
		mplew.writeShort(f.getJuniors()); // juniors added
		mplew.writeShort(f.getTotalJuniors()); // juniors allowed
		mplew.writeShort(0); // Unknown
		mplew.writeInt(f.getId()); // id?
		mplew.writeMapleAsciiString(f.getFamilyName());
		mplew.writeInt(0);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	public static MaplePacket showPedigree(int chrid, Map<Integer, MapleFamilyEntry> members) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_PEDIGREE.getValue());
		// Hmmm xD
		return mplew.getPacket();
	}

	public static MaplePacket updateAreaInfo(String mode, int quest) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(0x0A); // 0x0B in v95
		mplew.writeShort(quest);
		mplew.writeMapleAsciiString(mode);
		return mplew.getPacket();
	}

	public static MaplePacket questProgress(short id, String process) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(id);
		mplew.write(1);
		mplew.writeMapleAsciiString(process);
		return mplew.getPacket();
	}

	public static MaplePacket getItemMessage(int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(7);
		mplew.writeInt(itemid);
		return mplew.getPacket();
	}

	public static MaplePacket addCard(boolean full, int cardid, int level) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.MONSTERBOOK_ADD.getValue());
		mplew.write(full ? 0 : 1);
		mplew.writeInt(cardid);
		mplew.writeInt(level);
		return mplew.getPacket();
	}

	public static MaplePacket showGainCard() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(0x0D);
		return mplew.getPacket();
	}

	public static MaplePacket showForeginCardEffect(int id) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(id);
		mplew.write(0x0D);
		return mplew.getPacket();
	}

	public static MaplePacket changeCover(int cardid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.MONSTER_BOOK_CHANGE_COVER.getValue());
		mplew.writeInt(cardid);
		return mplew.getPacket();
	}

	public static MaplePacket aranGodlyStats() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.TEMPORARY_STATS.getValue());
		mplew.write(new byte[] {(byte) 0x1F, (byte) 0x0F, 0, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xFF, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0x78, (byte) 0x8C});
		return mplew.getPacket();
	}

	public static MaplePacket showIntro(String path) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(0x12);
		mplew.writeMapleAsciiString(path);
		return mplew.getPacket();
	}

	public static MaplePacket showInfo(String path) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(0x17);
		mplew.writeMapleAsciiString(path);
		mplew.writeInt(1);
		return mplew.getPacket();
	}

	/**
	 * Sends a UI utility. 0x01 - Equipment Inventory. 0x02 - Stat Window. 0x03
	 * - Skill Window. 0x05 - Keyboard Settings. 0x06 - Quest window. 0x09 -
	 * Monsterbook Window. 0x0A - Char Info 0x0B - Guild BBS 0x12 - Monster
	 * Carnival Window 0x16 - Party Search. 0x17 - Item Creation Window. 0x1A -
	 * My Ranking O.O 0x1B - Family Window 0x1C - Family Pedigree 0x1D - GM
	 * Story Board /funny shet 0x1E - Envelop saying you got mail from an admin.
	 * lmfao 0x1F - Medal Window 0x20 - Maple Event (???) 0x21 - Invalid Pointer
	 * Crash
	 * 
	 * @param ui
	 * @return
	 */
	public static MaplePacket openUI(byte ui) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.OPEN_UI.getValue());
		mplew.write(ui);
		return mplew.getPacket();
	}

	public static MaplePacket lockUI(boolean enable) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.LOCK_UI.getValue());
		mplew.write(enable ? 1 : 0);
		return mplew.getPacket();
	}

	public static MaplePacket disableUI(boolean enable) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DISABLE_UI.getValue());
		mplew.write(enable ? 1 : 0);
		return mplew.getPacket();
	}

	public static MaplePacket itemMegaphone(String msg, boolean whisper, byte channel, IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
		mplew.write(8);
		mplew.writeMapleAsciiString(msg);
		mplew.write(channel - 1);
		mplew.write(whisper ? 1 : 0);
		if (item == null) {
			mplew.write(0);
		} else {
			mplew.write(item.getPosition());
			addItemInfo(mplew, item, true);
		}
		return mplew.getPacket();
	}

	public static MaplePacket removeNPC(int oid) { // Make npc's invisible
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
		mplew.write(0);
		mplew.writeInt(oid);
		return mplew.getPacket();
	}

	/**
	 * Sends a report response
	 * 
	 * Possible values for <code>mode</code>:<br>
	 * 0: You have succesfully reported the user.<br>
	 * 1: Unable to locate the user.<br>
	 * 2: You may only report users 10 times a day.<br>
	 * 3: You have been reported to the GM's by a user.<br>
	 * 4: Your request did not go through for unknown reasons. Please try again
	 * later.<br>
	 * 
	 * @param mode
	 *            The mode
	 * @return Report Reponse packet
	 */
	public static MaplePacket reportResponse(byte mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REPORT_RESPONSE.getValue());
		mplew.write(mode);
		return mplew.getPacket();
	}

	public static MaplePacket sendHammerData(int hammerUsed) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
		mplew.write(0x39);
		mplew.writeInt(0);
		mplew.writeInt(hammerUsed);
		return mplew.getPacket();
	}

	public static MaplePacket sendHammerMessage() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
		mplew.write(0x3D);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket hammerItem(IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		mplew.write(0); // could be from drop
		mplew.write(2); // always 2
		mplew.write(3); // quantity > 0 (?)
		mplew.write(1); // Inventory type
		mplew.writeShort(item.getPosition()); // item slot
		mplew.write(0);
		mplew.write(1);
		mplew.writeShort(item.getPosition()); // wtf repeat
		addItemInfo(mplew, item, true);
		return mplew.getPacket();
	}

	public static MaplePacket playPortalSound() {
		return showSpecialEffect(7);
	}

	public static MaplePacket showMonsterBookPickup() {
		return showSpecialEffect(14);
	}

	public static MaplePacket showEquipmentLevelUp() {
		return showSpecialEffect(15);
	}

	public static MaplePacket showItemLevelup() {
		return showSpecialEffect(15);
	}

	/**
	 * 6 = Exp did not drop (Safety Charms) 7 = Enter portal sound 8 = Job
	 * change 9 = Quest complete 10 = Recovery 14 = Monster book pickup 15 =
	 * Equipment levelup 16 = Maker Skill Success 19 = Exp card [500, 200, 50]
	 * 
	 * @param effect
	 * @return
	 */
	public static MaplePacket showSpecialEffect(int effect) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(effect);
		return mplew.getPacket();
	}

	public static MaplePacket showForeignEffect(int cid, int effect) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(cid);
		mplew.write(effect);
		return mplew.getPacket();
	}

	public static MaplePacket showOwnRecovery(byte heal) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(0x0A);
		mplew.write(heal);
		return mplew.getPacket();
	}

	public static MaplePacket showRecovery(int cid, byte amount) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(cid);
		mplew.write(0x0A);
		mplew.write(amount);
		return mplew.getPacket();
	}

	public static MaplePacket showWheelsLeft(int left) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
		mplew.write(0x15);
		mplew.write(left);
		return mplew.getPacket();
	}

	public static MaplePacket updateQuestFinish(short quest, int npc, short nextquest) { // Check
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue()); // 0xF2 in
																	// v95
		mplew.write(8);// 0x0A in v95
		mplew.writeShort(quest);
		mplew.writeInt(npc);
		mplew.writeShort(nextquest);
		return mplew.getPacket();
	}

	public static MaplePacket showInfoText(String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(9);
		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}

	public static MaplePacket questError(short quest) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(0x0A);
		mplew.writeShort(quest);
		return mplew.getPacket();
	}

	public static MaplePacket questFailure(byte type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(type);// 0x0B = No meso, 0x0D = Worn by character, 0x0E =
							// Not having the item ?
		return mplew.getPacket();
	}

	public static MaplePacket questExpire(short quest) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(0x0F);
		mplew.writeShort(quest);
		return mplew.getPacket();
	}

	public static MaplePacket getMultiMegaphone(String[] messages, byte channel, boolean showEar) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
		mplew.write(0x0A);
		if (messages[0] != null) {
			mplew.writeMapleAsciiString(messages[0]);
		}
		mplew.write(messages.length);
		for (int i = 1; i < messages.length; i++) {
			if (messages[i] != null) {
				mplew.writeMapleAsciiString(messages[i]);
			}
		}
		for (int i = 0; i < 10; i++) {
			mplew.write(channel - 1);
		}
		mplew.write(showEar ? 1 : 0);
		mplew.write(1);
		return mplew.getPacket();
	}

	/**
	 * Gets a gm effect packet (ie. hide, banned, etc.)
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0x04: You have successfully blocked access.<br>
	 * 0x05: The unblocking has been successful.<br>
	 * 0x06 with Mode 0: You have successfully removed the name from the ranks.<br>
	 * 0x06 with Mode 1: You have entered an invalid character name.<br>
	 * 0x10: GM Hide, mode determines whether or not it is on.<br>
	 * 0x1E: Mode 0: Failed to send warning Mode 1: Sent warning<br>
	 * 0x13 with Mode 0: + mapid 0x13 with Mode 1: + ch (FF = Unable to find
	 * merchant)
	 * 
	 * @param type
	 *            The type
	 * @param mode
	 *            The mode
	 * @return The gm effect packet
	 */
	public static MaplePacket getGMEffect(int type, byte mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GM_PACKET.getValue());
		mplew.write(type);
		mplew.write(mode);
		return mplew.getPacket();
	}

	public static MaplePacket findMerchantResponse(boolean map, int extra) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GM_PACKET.getValue());
		mplew.write(0x13);
		mplew.write(map ? 0 : 1); // 00 = mapid, 01 = ch
		if (map) {
			mplew.writeInt(extra);
		} else {
			mplew.write(extra); // -1 = unable to find
		}
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket disableMinimap() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GM_PACKET.getValue());
		mplew.writeShort(0x1C);
		return mplew.getPacket();
	}

	public static MaplePacket sendFamilyInvite(int playerId, String inviter) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAMILY_INVITE.getValue());
		mplew.writeInt(playerId);
		mplew.writeMapleAsciiString(inviter);
		return mplew.getPacket();
	}

	public static MaplePacket showBoughtCashPackage(List<IItem> cashPackage, int accountId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x89);
		mplew.write(cashPackage.size());

		for (IItem item : cashPackage) {
			addCashItemInformation(mplew, item, accountId);
		}

		mplew.writeShort(0);

		return mplew.getPacket();
	}

	public static MaplePacket showBoughtQuestItem(int itemId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x8D);
		mplew.writeInt(1);
		mplew.writeShort(1);
		mplew.write(0x0B);
		mplew.write(0);
		mplew.writeInt(itemId);

		return mplew.getPacket();
	}

	public static MaplePacket updateSlot(IItem item) {// Just the same as
														// merge... dst and src
														// is the same...
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
		byte type = ItemConstants.getInventoryType(item.getItemId()).getType();
		mplew.write(new byte[] {0, 2, 3});
		mplew.write(type);
		mplew.writeShort(item.getPosition());
		mplew.write(0);
		mplew.write(type);
		mplew.writeShort(item.getPosition());
		addItemInfo(mplew, item, true);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	private static void getGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
		mplew.writeInt(guild.getId());
		mplew.writeMapleAsciiString(guild.getName());
		for (int i = 1; i <= 5; i++) {
			mplew.writeMapleAsciiString(guild.getRankTitle(i));
		}
		Collection<MapleGuildCharacter> members = guild.getMembers();
		mplew.write(members.size());
		for (MapleGuildCharacter mgc : members) {
			mplew.writeInt(mgc.getId());
		}
		for (MapleGuildCharacter mgc : members) {
			mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
			mplew.writeInt(mgc.getJobId());
			mplew.writeInt(mgc.getLevel());
			mplew.writeInt(mgc.getGuildRank());
			mplew.writeInt(mgc.isOnline() ? 1 : 0);
			mplew.writeInt(guild.getSignature());
			mplew.writeInt(mgc.getAllianceRank());
		}
		mplew.writeInt(guild.getCapacity());
		mplew.writeShort(guild.getLogoBG());
		mplew.write(guild.getLogoBGColor());
		mplew.writeShort(guild.getLogo());
		mplew.write(guild.getLogoColor());
		mplew.writeMapleAsciiString(guild.getNotice());
		mplew.writeInt(guild.getGP());
		mplew.writeInt(guild.getAllianceId());
	}

	public static MaplePacket getAllianceInfo(MapleAlliance alliance) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x0C);
		mplew.write(1);
		mplew.writeInt(alliance.getId());
		mplew.writeMapleAsciiString(alliance.getName());
		for (int i = 1; i <= 5; i++) {
			mplew.writeMapleAsciiString(alliance.getRankTitle(i));
		}
		mplew.write(alliance.getGuilds().size());
		mplew.writeInt(2); // probably capacity
		for (Integer guild : alliance.getGuilds()) {
			mplew.writeInt(guild);
		}
		mplew.writeMapleAsciiString(alliance.getNotice());
		return mplew.getPacket();
	}

	public static MaplePacket makeNewAlliance(MapleAlliance alliance, MapleClient c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x0F);
		mplew.writeInt(alliance.getId());
		mplew.writeMapleAsciiString(alliance.getName());
		for (int i = 1; i <= 5; i++) {
			mplew.writeMapleAsciiString(alliance.getRankTitle(i));
		}
		mplew.write(alliance.getGuilds().size());
		for (Integer guild : alliance.getGuilds()) {
			mplew.writeInt(guild);
		}
		mplew.writeInt(2); // probably capacity
		mplew.writeShort(0);
		for (Integer guildd : alliance.getGuilds()) {
			getGuildInfo(mplew, Server.getInstance().getGuild(guildd, c.getPlayer().getMGC()));
		}
		return mplew.getPacket();
	}

	public static MaplePacket getGuildAlliances(MapleAlliance alliance, MapleClient c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x0D);
		mplew.writeInt(alliance.getGuilds().size());
		for (Integer guild : alliance.getGuilds()) {
			getGuildInfo(mplew, Server.getInstance().getGuild(guild, null));
		}
		return mplew.getPacket();
	}

	public static MaplePacket addGuildToAlliance(MapleAlliance alliance, int newGuild, MapleClient c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x12);
		mplew.writeInt(alliance.getId());
		mplew.writeMapleAsciiString(alliance.getName());
		for (int i = 1; i <= 5; i++) {
			mplew.writeMapleAsciiString(alliance.getRankTitle(i));
		}
		mplew.write(alliance.getGuilds().size());
		for (Integer guild : alliance.getGuilds()) {
			mplew.writeInt(guild);
		}
		mplew.writeInt(2);
		mplew.writeMapleAsciiString(alliance.getNotice());
		mplew.writeInt(newGuild);
		getGuildInfo(mplew, Server.getInstance().getGuild(newGuild, null));
		return mplew.getPacket();
	}

	public static MaplePacket allianceMemberOnline(MapleCharacter mc, boolean online) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x0E);
		mplew.writeInt(mc.getGuild().getAllianceId());
		mplew.writeInt(mc.getGuildId());
		mplew.writeInt(mc.getId());
		mplew.write(online ? 1 : 0);
		return mplew.getPacket();
	}

	public static MaplePacket allianceNotice(int id, String notice) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x1C);
		mplew.writeInt(id);
		mplew.writeMapleAsciiString(notice);
		return mplew.getPacket();
	}

	public static MaplePacket changeAllianceRankTitle(int alliance, String[] ranks) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x1A);
		mplew.writeInt(alliance);
		for (int i = 0; i < 5; i++) {
			mplew.writeMapleAsciiString(ranks[i]);
		}
		return mplew.getPacket();
	}

	public static MaplePacket updateAllianceJobLevel(MapleCharacter mc) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x18);
		mplew.writeInt(mc.getGuild().getAllianceId());
		mplew.writeInt(mc.getGuildId());
		mplew.writeInt(mc.getId());
		mplew.writeInt(mc.getLevel());
		mplew.writeInt(mc.getJob().getId());
		return mplew.getPacket();
	}

	public static MaplePacket removeGuildFromAlliance(MapleAlliance alliance, int expelledGuild, MapleClient c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x10);
		mplew.writeInt(alliance.getId());
		mplew.writeMapleAsciiString(alliance.getName());
		for (int i = 1; i <= 5; i++) {
			mplew.writeMapleAsciiString(alliance.getRankTitle(i));
		}
		mplew.write(alliance.getGuilds().size());
		for (Integer guild : alliance.getGuilds()) {
			mplew.writeInt(guild);
		}
		mplew.writeInt(2);
		mplew.writeMapleAsciiString(alliance.getNotice());
		mplew.writeInt(expelledGuild);
		getGuildInfo(mplew, Server.getInstance().getGuild(expelledGuild, null));
		mplew.write(0x01);
		return mplew.getPacket();
	}

	public static MaplePacket disbandAlliance(int alliance) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x1D);
		mplew.writeInt(alliance);
		return mplew.getPacket();
	}

	public static MaplePacket sendMesoLimit() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESO_LIMIT.getValue()); // Players under
															// level 15 can only
															// trade 1m per day
		return mplew.getPacket();
	}

	public static MaplePacket sendEngagementRequest(String name) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.RING_ACTION.getValue()); // <name> has
																// requested
																// engagement.
																// Will you
																// accept this
																// proposal?
		mplew.write(0);
		mplew.writeMapleAsciiString(name); // name
		mplew.writeInt(10); // playerid
		return mplew.getPacket();
	}

	public static MaplePacket sendGroomWishlist() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.RING_ACTION.getValue()); // <name> has
																// requested
																// engagement.
																// Will you
																// accept this
																// proposal?
		mplew.write(9);
		return mplew.getPacket();
	}

	public static MaplePacket sendBrideWishList(List<IItem> items) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WEDDING_ACTION.getValue());
		mplew.write(0x0A);
		mplew.writeLong(-1); // ?
		mplew.writeInt(0); // ?
		mplew.write(items.size());
		for (IItem item : items) {
			addItemInfo(mplew, item, true);
		}
		return mplew.getPacket();
	}

	public static MaplePacket addItemToWeddingRegistry(MapleCharacter chr, IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WEDDING_ACTION.getValue());
		mplew.write(0x0B);
		mplew.writeInt(0);
		for (int i = 0; i < 0; i++) // f4
		{
			mplew.write(0);
		}

		addItemInfo(mplew, item, true);
		return mplew.getPacket();
	}

	public static MaplePacket sendFamilyJoinResponse(boolean accepted, String added) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAMILY_MESSAGE2.getValue());
		mplew.write(accepted ? 1 : 0);
		mplew.writeMapleAsciiString(added);
		return mplew.getPacket();
	}

	public static MaplePacket getSeniorMessage(String name) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAMILY_SENIOR_MESSAGE.getValue());
		mplew.writeMapleAsciiString(name);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static MaplePacket sendGainRep(int gain, int mode) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAMILY_GAIN_REP.getValue());
		mplew.writeInt(gain);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	public static MaplePacket removeItemFromDuey(boolean remove, int Package) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DUEY.getValue());
		mplew.write(0x17);
		mplew.writeInt(Package);
		mplew.write(remove ? 3 : 4);
		return mplew.getPacket();
	}

	public static MaplePacket sendDueyMSG(byte operation) {
		return sendDuey(operation, null);
	}

	public static MaplePacket sendDuey(byte operation, List<DueyPackages> packages) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DUEY.getValue());
		mplew.write(operation);
		if (operation == 8) {
			mplew.write(0);
			mplew.write(packages.size());
			for (DueyPackages dp : packages) {
				mplew.writeInt(dp.getPackageId());
				mplew.writeAsciiString(dp.getSender());
				for (int i = dp.getSender().length(); i < 13; i++) {
					mplew.write(0);
				}
				mplew.writeInt(dp.getMesos());
				mplew.writeLong(getQuestTimestamp(dp.sentTimeInMilliseconds()));
				mplew.writeLong(0); // Contains message o____o.
				for (int i = 0; i < 48; i++) {
					mplew.writeInt(Randomizer.nextInt(Integer.MAX_VALUE));
				}
				mplew.writeInt(0);
				mplew.write(0);
				if (dp.getItem() != null) {
					mplew.write(1);
					addItemInfo(mplew, dp.getItem(), true);
				} else {
					mplew.write(0);
				}
			}
			mplew.write(0);
		}
		return mplew.getPacket();
	}

	public static MaplePacket sendDojoAnimation(byte firstByte, String animation) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
		mplew.write(firstByte);
		mplew.writeMapleAsciiString(animation);
		return mplew.getPacket();
	}

	public static MaplePacket getDojoInfo(String info) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(10);
		mplew.write(new byte[] {(byte) 0xB7, 4});// QUEST ID f5
		mplew.writeMapleAsciiString(info);
		return mplew.getPacket();
	}

	public static MaplePacket getDojoInfoMessage(String message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(9);
		mplew.writeMapleAsciiString(message);
		return mplew.getPacket();
	}

	/**
	 * Gets a "block" packet (ie. the cash shop is unavailable, etc)
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 1: The portal is closed for now.<br>
	 * 2: You cannot go to that place.<br>
	 * 3: Unable to approach due to the force of the ground.<br>
	 * 4: You cannot teleport to or on this map.<br>
	 * 5: Unable to approach due to the force of the ground.<br>
	 * 6: This map can only be entered by party members.<br>
	 * 7: The Cash Shop is currently not available. Stay tuned...<br>
	 * 
	 * @param type
	 *            The type
	 * @return The "block" packet.
	 */
	public static MaplePacket blockedMessage(int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BLOCK_MESSAGE.getValue());
		mplew.write(type);
		return mplew.getPacket();
	}

	/**
	 * Gets a "block" packet (ie. the cash shop is unavailable, etc)
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 1: You cannot move that channel. Please try again later.<br>
	 * 2: You cannot go into the cash shop. Please try again later.<br>
	 * 3: The Item-Trading Shop is currently unavailable. Please try again
	 * later.<br>
	 * 4: You cannot go into the trade shop, due to limitation of user count.<br>
	 * 5: You do not meet the minimum level requirement to access the Trade
	 * Shop.<br>
	 * 
	 * @param type
	 *            The type
	 * @return The "block" packet.
	 */
	public static MaplePacket blockedMessage2(int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BLOCK_MESSAGE2.getValue());
		mplew.write(type);
		return mplew.getPacket();
	}

	public static MaplePacket updateDojoStats(MapleCharacter chr, int belt) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(10);
		mplew.write(new byte[] {(byte) 0xB7, 4}); // ?
		mplew.writeMapleAsciiString("pt=" + chr.getDojoPoints() + ";belt=" + belt + ";tuto=" + (chr.getFinishedDojoTutorial() ? "1" : "0"));
		return mplew.getPacket();
	}

	/**
	 * Sends a "levelup" packet to the guild or family.
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0: <Family> ? has reached Lv. ?.<br>
	 * - The Reps you have received from ? will be reduced in half. 1: <Family>
	 * ? has reached Lv. ?.<br>
	 * 2: <Guild> ? has reached Lv. ?.<br>
	 * 
	 * @param type
	 *            The type
	 * @return The "levelup" packet.
	 */
	public static MaplePacket levelUpMessage(int type, int level, String charname) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.LEVELUP_MSG.getValue());
		mplew.write(type);
		mplew.writeInt(level);
		mplew.writeMapleAsciiString(charname);

		return mplew.getPacket();
	}

	/**
	 * Sends a "married" packet to the guild or family.
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0: <Guild ? is now married. Please congratulate them.<br>
	 * 1: <Family ? is now married. Please congratulate them.<br>
	 * 
	 * @param type
	 *            The type
	 * @return The "married" packet.
	 */
	public static MaplePacket marriageMessage(int type, String charname) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MARRIAGE_MSG.getValue());
		mplew.write(type);
		mplew.writeMapleAsciiString("> " + charname); // To fix the stupid
														// packet lol

		return mplew.getPacket();
	}

	/**
	 * Sends a "job advance" packet to the guild or family.
	 * 
	 * Possible values for <code>type</code>:<br>
	 * 0: <Guild ? has advanced to a(an) ?.<br>
	 * 1: <Family ? has advanced to a(an) ?.<br>
	 * 
	 * @param type
	 *            The type
	 * @return The "job advance" packet.
	 */
	public static MaplePacket jobMessage(int type, int job, String charname) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.JOB_MSG.getValue());
		mplew.write(type);
		mplew.writeInt(job); // Why fking int?
		mplew.writeMapleAsciiString("> " + charname); // To fix the stupid
														// packet lol

		return mplew.getPacket();
	}

	/**
	 * 
	 * @param type
	 *            - (0:Light&Long 1:Heavy&Short)
	 * @param delay
	 *            - seconds
	 * @return
	 */
	public static MaplePacket trembleEffect(int type, int delay) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
		mplew.write(1);
		mplew.write(type);
		mplew.writeInt(delay);
		return mplew.getPacket();
	}

	public static MaplePacket getEnergy(String info, int amount) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ENERGY.getValue());
		mplew.writeMapleAsciiString(info);
		mplew.writeMapleAsciiString(Integer.toString(amount));
		return mplew.getPacket();
	}

	public static MaplePacket dojoWarpUp() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DOJO_WARP_UP.getValue());
		mplew.write(0);
		mplew.write(6);
		return mplew.getPacket();
	}

	public static MaplePacket itemExpired(int itemid) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(2);
		mplew.writeInt(itemid);
		return mplew.getPacket();
	}

	private static String getRightPaddedStr(String in, char padchar, int length) {
		StringBuilder builder = new StringBuilder(in);
		for (int x = in.length(); x < length; x++) {
			builder.append(padchar);
		}
		return builder.toString();
	}

	public static MaplePacket MobDamageMobFriendly(MapleMonster mob, int damage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(mob.getObjectId());
		mplew.write(1); // direction ?
		mplew.writeInt(damage);
		int remainingHp = mob.getHp() - damage;
		if (remainingHp <= 0) {
			remainingHp = 0;
			mob.getMap().removeMapObject(mob);
		}
		mob.setHp(remainingHp);
		mplew.writeInt(remainingHp);
		mplew.writeInt(mob.getMaxHp());
		return mplew.getPacket();
	}

	public static MaplePacket shopErrorMessage(int error, int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(0x0A);
		mplew.write(type);
		mplew.write(error);
		return mplew.getPacket();
	}

	private static void addRingInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.writeShort(chr.getCrushRings().size());
		for (MapleRing ring : chr.getCrushRings()) {
			mplew.writeInt(ring.getPartnerChrId());
			mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
			mplew.writeInt(ring.getRingId());
			mplew.writeInt(0);
			mplew.writeInt(ring.getPartnerRingId());
			mplew.writeInt(0);
		}
		mplew.writeShort(chr.getFriendshipRings().size());
		for (MapleRing ring : chr.getFriendshipRings()) {
			mplew.writeInt(ring.getPartnerChrId());
			mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
			mplew.writeInt(ring.getRingId());
			mplew.writeInt(0);
			mplew.writeInt(ring.getPartnerRingId());
			mplew.writeInt(0);
			mplew.writeInt(ring.getItemId());
		}
		mplew.writeShort(chr.getMarriageRing() != null ? 1 : 0);
		int marriageId = 30000;
		if (chr.getMarriageRing() != null) {
			mplew.writeInt(marriageId);
			mplew.writeInt(chr.getId());
			mplew.writeInt(chr.getMarriageRing().getPartnerChrId());
			mplew.writeShort(3);
			mplew.writeInt(chr.getMarriageRing().getRingId());
			mplew.writeInt(chr.getMarriageRing().getPartnerRingId());
			mplew.writeAsciiString(getRightPaddedStr(chr.getName(), '\0', 13));
			mplew.writeAsciiString(getRightPaddedStr(chr.getMarriageRing().getPartnerName(), '\0', 13));
		}
	}

	public static MaplePacket finishedSort(int inv) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.FINISH_SORT.getValue());
		mplew.write(0);
		mplew.write(inv);
		return mplew.getPacket();
	}

	public static MaplePacket finishedSort2(int inv) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.FINISH_SORT2.getValue());
		mplew.write(0);
		mplew.write(inv);
		return mplew.getPacket();
	}

	public static MaplePacket bunnyPacket() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(9);
		mplew.writeAsciiString("Protect the Moon Bunny!!!");
		return mplew.getPacket();
	}

	public static MaplePacket hpqMessage(String text) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAP_EFFECT.getValue()); // not 100% sure
		mplew.write(0);
		mplew.writeInt(5120016);
		mplew.writeAsciiString(text);
		return mplew.getPacket();
	}

	public static MaplePacket showHPQMoon() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(0x83); // maybe?
		mplew.writeInt(-1);
		return mplew.getPacket();
	}

	public static MaplePacket showEventInstructions() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GMEVENT_INSTRUCTIONS.getValue());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static MaplePacket leftKnockBack() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
		mplew.writeShort(SendOpcode.LEFT_KNOCK_BACK.getValue());
		return mplew.getPacket();
	}

	public static MaplePacket rollSnowBall(boolean entermap, int type, MapleSnowball ball0, MapleSnowball ball1) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ROLL_SNOWBALL.getValue());
		if (entermap) {
			mplew.write0(21);
		} else {
			mplew.write(type);// 0 = move, 1 = roll, 2 is down disappear, 3 is
								// up disappear
			mplew.writeInt(ball0.getSnowmanHP() / 75);
			mplew.writeInt(ball1.getSnowmanHP() / 75);
			mplew.writeShort(ball0.getPosition());// distance snowball down, 84
													// 03 = max
			mplew.write(-1);
			mplew.writeShort(ball1.getPosition());// distance snowball up, 84 03
													// = max
			mplew.write(-1);
		}
		return mplew.getPacket();
	}

	public static MaplePacket hitSnowBall(int what, int damage) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.HIT_SNOWBALL.getValue());
		mplew.write(what);
		mplew.writeInt(damage);
		return mplew.getPacket();
	}

	/**
	 * Sends a Snowball Message<br>
	 * 
	 * Possible values for <code>message</code>:<br>
	 * 1: ... Team's snowball has passed the stage 1.<br>
	 * 2: ... Team's snowball has passed the stage 2.<br>
	 * 3: ... Team's snowball has passed the stage 3.<br>
	 * 4: ... Team is attacking the snowman, stopping the progress<br>
	 * 5: ... Team is moving again<br>
	 * 
	 * @param message
	 **/
	public static MaplePacket snowballMessage(int team, int message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.SNOWBALL_MESSAGE.getValue());
		mplew.write(team);// 0 is down, 1 is up
		mplew.writeInt(message);
		return mplew.getPacket();
	}

	public static MaplePacket coconutScore(int team1, int team2) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.COCONUT_SCORE.getValue());
		mplew.writeShort(team1);
		mplew.writeShort(team2);
		return mplew.getPacket();
	}

	public static MaplePacket hitCoconut(boolean spawn, int id, int type) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.HIT_COCONUT.getValue());
		if (spawn) {
			mplew.write(new byte[] {0, (byte) 0x80, 0, 0, 0}); // 00 80 00 00 00
		} else {
			mplew.writeInt(id);
			mplew.write(type); // What action to do for the coconut.
		}
		return mplew.getPacket();
	}

	public static MaplePacket customPacket(String packet) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.write(HexTool.getByteArrayFromHexString(packet));
		return mplew.getPacket();
	}

	public static MaplePacket customPacket(byte[] packet) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(packet.length);
		mplew.write(packet);
		return mplew.getPacket();
	}

	public static MaplePacket spawnGuide(boolean spawn) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.SPAWN_GUIDE.getValue());
		if (spawn) {
			mplew.write(1);
		} else {
			mplew.write(0);
		}
		return mplew.getPacket();
	}

	public static MaplePacket talkGuide(String talk) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
		mplew.write(0);
		mplew.writeMapleAsciiString(talk);
		mplew.write(new byte[] {(byte) 0xC8, 0, 0, 0, (byte) 0xA0, (byte) 0x0F, 0, 0});
		return mplew.getPacket();
	}

	public static MaplePacket guideHint(int hint) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
		mplew.write(1);
		mplew.writeInt(hint);
		mplew.writeInt(7000);
		return mplew.getPacket();
	}

	public static void addCashItemInformation(MaplePacketLittleEndianWriter mplew, IItem item, int accountId) {
		addCashItemInformation(mplew, item, accountId, null);
	}

	public static void addCashItemInformation(MaplePacketLittleEndianWriter mplew, IItem item, int accountId, String giftMessage) {
		boolean isGift = giftMessage != null;
		boolean isRing = false;
		IEquip equip = null;
		if (item.getType() == IItem.EQUIP) {
			equip = (IEquip) item;
			isRing = equip.getRingId() > -1;
		}
		mplew.writeLong(item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
		if (!isGift) {
			mplew.writeInt(accountId);
			mplew.writeInt(0);
		}
		mplew.writeInt(item.getItemId());
		if (!isGift) {
			mplew.writeInt(item.getSN());
			mplew.writeShort(item.getQuantity());
		}
		mplew.writeAsciiString(StringUtil.getRightPaddedStr(item.getGiftFrom(), '\0', 13));
		if (isGift) {
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
			return;
		}
		addExpirationTime(mplew, item.getExpiration());
		mplew.writeLong(0);
	}

	public static MaplePacket showWishList(MapleCharacter mc, boolean update) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		if (update) {
			mplew.write(0x55);
		} else {
			mplew.write(0x4F);
		}

		for (int sn : mc.getCashShop().getWishList()) {
			mplew.writeInt(sn);
		}

		for (int i = mc.getCashShop().getWishList().size(); i < 10; i++) {
			mplew.writeInt(0);
		}

		return mplew.getPacket();
	}

	public static MaplePacket showBoughtCashItem(IItem item, int accountId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x57);
		addCashItemInformation(mplew, item, accountId);

		return mplew.getPacket();
	}

	/*
	 * 00 = Due to an unknown error, failed A4 = Due to an unknown error, failed
	 * + warpout A5 = You don't have enough cash. A6 = long as shet msg A7 = You
	 * have exceeded the allotted limit of price for gifts. A8 = You cannot send
	 * a gift to your own account. Log in on the char and purchase A9 = Please
	 * confirm whether the character's name is correct. AA = Gender restriction!
	 * //Skipped a few B0 = Wrong Coupon Code B1 = Disconnect from CS because of
	 * 3 wrong coupon codes < lol B2 = Expired Coupon B3 = Coupon has been used
	 * already B4 = Nexon internet cafes? lolfk
	 * 
	 * BB = inv full C2 = not enough mesos? Lol not even 1 mesos xD
	 */
	public static MaplePacket showCashShopMessage(byte message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x5C);
		mplew.write(message);

		return mplew.getPacket();
	}

	public static MaplePacket showCashInventory(MapleClient c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x4B);
		mplew.writeShort(c.getPlayer().getCashShop().getInventory().size());

		for (IItem item : c.getPlayer().getCashShop().getInventory()) {
			addCashItemInformation(mplew, item, c.getAccID());
		}

		mplew.writeShort(c.getPlayer().getStorage().getSlots());
		mplew.writeShort(c.getCharacterSlots());

		return mplew.getPacket();
	}

	public static MaplePacket showGifts(List<GiftEntry> gifts) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x4D);
		mplew.writeShort(gifts.size());

		for (GiftEntry gift : gifts) {
			addCashItemInformation(mplew, gift.item, 0, gift.message);
		}

		return mplew.getPacket();
	}

	public static MaplePacket showGiftSucceed(String to, CashItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x5E); // 0x5D, Couldn't be sent
		mplew.writeMapleAsciiString(to);
		mplew.writeInt(item.getItemId());
		mplew.writeShort(item.getCount());
		mplew.writeInt(item.getPrice());

		return mplew.getPacket();
	}

	public static MaplePacket showBoughtInventorySlots(int type, short slots) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x60);
		mplew.write(type);
		mplew.writeShort(slots);

		return mplew.getPacket();
	}

	public static MaplePacket showBoughtStorageSlots(short slots) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x62);
		mplew.writeShort(slots);

		return mplew.getPacket();
	}

	public static MaplePacket showBoughtCharacterSlot(short slots) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x64);
		mplew.writeShort(slots);

		return mplew.getPacket();
	}

	public static MaplePacket takeFromCashInventory(IItem item) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x68);
		mplew.writeShort(item.getPosition());
		addItemInfo(mplew, item, true);

		return mplew.getPacket();
	}

	public static MaplePacket putIntoCashInventory(IItem item, int accountId) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

		mplew.write(0x6A);
		addCashItemInformation(mplew, item, accountId);

		return mplew.getPacket();
	}

	public static MaplePacket openCashShop(MapleClient c, boolean mts) throws Exception {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(mts ? SendOpcode.OPEN_MTS.getValue() : SendOpcode.OPEN_CASHSHOP.getValue());

		addCharacterInfo(mplew, c.getPlayer());

		if (!mts) {
			mplew.write(1);
		}

		mplew.writeMapleAsciiString(c.getAccountName());
		if (mts) {
			mplew.write(new byte[] {(byte) 0x88, 19, 0, 0, 7, 0, 0, 0, (byte) 0xF4, 1, 0, 0, (byte) 0x18, 0, 0, 0, (byte) 0xA8, 0, 0, 0, (byte) 0x70, (byte) 0xAA, (byte) 0xA7, (byte) 0xC5, (byte) 0x4E, (byte) 0xC1, (byte) 0xCA, 1});
		} else {
			mplew.writeInt(0);
			List<SpecialCashItem> lsci = CashItemFactory.getSpecialCashItems();
			mplew.writeShort(lsci.size());// Guess what
			for (SpecialCashItem sci : lsci) {
				mplew.writeInt(sci.getSN());
				mplew.writeInt(sci.getModifier());
				mplew.write(sci.getInfo());
			}
			mplew.write0(121);

			for (int i = 1; i <= 8; i++) {
				for (int j = 0; j < 2; j++) {
					mplew.writeInt(i);
					mplew.writeInt(j);
					mplew.writeInt(50200004);

					mplew.writeInt(i);
					mplew.writeInt(j);
					mplew.writeInt(50200069);

					mplew.writeInt(i);
					mplew.writeInt(j);
					mplew.writeInt(50200117);

					mplew.writeInt(i);
					mplew.writeInt(j);
					mplew.writeInt(50100008);

					mplew.writeInt(i);
					mplew.writeInt(j);
					mplew.writeInt(50000047);
				}
			}

			mplew.writeInt(0);
			mplew.writeShort(0);
			mplew.write(0);
			mplew.writeInt(75);
		}
		return mplew.getPacket();
	}

	public static MaplePacket temporarySkills() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
		mplew.writeShort(SendOpcode.TEMPORARY_SKILLS.getValue());
		return mplew.getPacket();
	}

	public static MaplePacket showCombo(int count) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.SHOW_COMBO.getValue());
		mplew.writeInt(count);
		return mplew.getPacket();
	}

	public static MaplePacket earnTitleMessage(String msg) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.EARN_TITLE_MSG.getValue());
		mplew.writeMapleAsciiString(msg);
		return mplew.getPacket();
	}

	public static MaplePacket startCPQ(MapleCharacter chr, MonsterCarnivalParty enemy) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
		mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_START.getValue());
		mplew.write(chr.getTeam()); // team
		mplew.writeShort(chr.getCP()); // Obtained CP - Used CP
		mplew.writeShort(chr.getObtainedCP()); // Total Obtained CP
		mplew.writeShort(chr.getCarnivalParty().getAvailableCP()); // Obtained
																	// CP - Used
																	// CP of the
																	// team
		mplew.writeShort(chr.getCarnivalParty().getTotalCP()); // Total Obtained
																// CP of the
																// team
		mplew.writeShort(enemy.getAvailableCP()); // Obtained CP - Used CP of
													// the team
		mplew.writeShort(enemy.getTotalCP()); // Total Obtained CP of the team
		mplew.writeShort(0); // Probably useless nexon shit
		mplew.writeLong(0); // Probably useless nexon shit
		return mplew.getPacket();
	}

	public static MaplePacket updateCP(int cp, int tcp) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
		mplew.writeShort(cp); // Obtained CP - Used CP
		mplew.writeShort(tcp); // Total Obtained CP
		return mplew.getPacket();
	}

	public static MaplePacket updatePartyCP(MonsterCarnivalParty party) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_PARTY_CP.getValue());
		mplew.write(party.getTeam()); // Team where the points are given to.
		mplew.writeShort(party.getAvailableCP()); // Obtained CP - Used CP
		mplew.writeShort(party.getTotalCP()); // Total Obtained CP
		return mplew.getPacket();
	}

	public static MaplePacket CPQSummon(int tab, int number, String name) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
		mplew.write(tab); // Tab
		mplew.writeShort(number); // Number of summon inside the tab
		mplew.writeMapleAsciiString(name); // Name of the player that summons
		return mplew.getPacket();
	}

	public static MaplePacket CPQDied(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
		mplew.write(chr.getTeam()); // Team
		mplew.writeMapleAsciiString(chr.getName()); // Name of the player that
													// died
		mplew.write(chr.getAndRemoveCP()); // Lost CP
		return mplew.getPacket();
	}

	/**
	 * Sends a CPQ Message<br>
	 * 
	 * Possible values for <code>message</code>:<br>
	 * 1: You don't have enough CP to continue.<br>
	 * 2: You can no longer summon the Monster.<br>
	 * 3: You can no longer summon the being.<br>
	 * 4: This being is already summoned.<br>
	 * 5: This request has failed due to an unknown error.<br>
	 * 
	 * @param message
	 *            Displays a message inside Carnival PQ
	 **/
	public static MaplePacket CPQMessage(byte message) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_MESSAGE.getValue());
		mplew.write(message); // Message
		return mplew.getPacket();
	}

	public static MaplePacket leaveCPQ(MapleCharacter chr) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_LEAVE.getValue());
		mplew.write(0); // Something
		mplew.write(chr.getTeam()); // Team
		mplew.writeMapleAsciiString(chr.getName()); // Player name
		return mplew.getPacket();
	}

	public static MaplePacket sheepRanchInfo(byte wolf, byte sheep) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHEEP_RANCH_INFO.getValue());
		mplew.write(wolf);
		mplew.write(sheep);
		return mplew.getPacket();
	}

	// Know what this is? ?? >=)

	public static MaplePacket sheepRanchClothes(int id, byte clothes) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHEEP_RANCH_CLOTHES.getValue());
		mplew.writeInt(id); // Character id
		mplew.write(clothes); // 0 = sheep, 1 = wolf, 2 = Spectator (wolf
								// without wool)
		return mplew.getPacket();
	}

	public static MaplePacket showInventoryFull() {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
		mplew.writeShort(SendOpcode.SOMETHING_WITH_INVENTORY.getValue());
		mplew.write0(6);
		return mplew.getPacket();
	}

	public static MaplePacket pyramidGauge(int gauge) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.PYRAMID_GAUGE.getValue());
		mplew.writeInt(gauge);
		return mplew.getPacket();
	}

	// f2

	public static MaplePacket pyramidScore(byte score, int exp) {// Type cannot
																	// be higher
																	// than 4
																	// (Rank D),
																	// otherwise
																	// you'll
																	// crash
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.PYRAMID_SCORE.getValue());
		mplew.write(score);
		mplew.writeInt(exp);
		return mplew.getPacket();
	}
}
