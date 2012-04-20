function enter(pi) {
	if (pi.isQuestStarted(21000)) {
	pi.warp(914000200, 1);
	pi.giveTutorialSkills();
	return true;
	} else {
	pi.message("You can only exit after you accept the quest from Athena Pierce, who is to your right.");
	return false;
	}
}