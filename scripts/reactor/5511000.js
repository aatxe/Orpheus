/* @Author SharpAceX
* 5511000.js: Summons Targa.
*/

function act() {
	if (rm.getReactor().getMap().getMonsterById(9420542) == null) {
		rm.spawnMonster(9420542,-527,637);

		rm.mapMessage(6, "Beware! The furious Targa has shown himself!");
		rm.createMapMonitor(551000000,"sp");
	}
}