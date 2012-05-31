/* 	Maple Administrator - Character Deletion NPC
 * 	@author Aaron Weiss
*/
var status = 0;
var char = -1;

function start() {
    cm.sendSimple("Hey there, I'm here to help you delete characters!\r\nSelect a character:\r\n" + cm.getClient().getFormattedCharacterList(cm.getClient().getWorld()) + "\r\n\r\n#L6#Information#l");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendSimple("Hey there, I'm here to help you delete characters!\r\nSelect a character:\r\n" + cm.getClient().getFormattedCharacterList(cm.getClient().getWorld()) + "\r\n\r\n#L6#Information#l");
    } else if (status == 1) {
        if (selection == 6) {
            cm.sendOk("#eInformation#n\r\nThe purpose of this NPC is to allow you to delete characters while PICs are disabled.\r\n\r\n#eNotice#n\r\nUpon selecting the character you wish to delete, you will be given a confirmation notice.\r\n\r\n#r#eWarning#n#k\r\nOnce you delete your character, it is gone #r#eforever#n#k. " + cm.getServerName() + " and its staff will not be held responsible.");
            status = -1;
        } else {
            char = selection;
            cm.sendYesNo("Are you sure you wish to delete #r" + cm.getClient().getCharacterName(char, cm.getWorld()) + "#k?");
        }
    } else if (status == 2) {
        if (char != -1 && mode == 1) {
            if (cm.getClient().getCharacterName(char, cm.getClient().getWorld()) != cm.getPlayer().getName()) {
                if (!cm.getClient().isCharacterInGuild(cm.getClient().getCharacterId(char, cm.getWorld()))) {
                    cm.getClient().deleteCharacter(cm.getClient().getCharacterId(char, cm.getWorld()));
                    cm.sendOk("I've deleted the character.");
                    cm.dispose();
                } else {
                    cm.sendOk("You cannot delete a character who is in a guild.");
                }
            } else {
                cm.sendOk("You cannot delete the character you're currently on!");
            }
        } else {
            cm.dispose();
        }
    }
}