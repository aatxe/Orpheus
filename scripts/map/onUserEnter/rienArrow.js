function start(ms) {
	if (ms.getAranIntroState("arr=o")) {
		ms.updateAranIntroState("miss=o;arr=o;helper=clear");
		ms.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
	}
}