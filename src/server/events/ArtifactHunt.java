/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.events;

/**
 *
 * @author kevintjuh93
 */
public class ArtifactHunt extends MapleEvents {
        private int amount;

        public ArtifactHunt(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
}
