package eva2.optimization.strategies.tribes;

import eva2.problems.InterfaceOptimizationProblem;
import eva2.optimization.strategies.Tribes;
import eva2.tools.math.RNG;


public class Tribe implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 739772857862893332L;
    int explorerNb, memoryNb;
    TribesExplorer explorer[] = new TribesExplorer[Tribes.maxExplorerNb]; // Les explorateurs
    TribesMemory memory[] = new TribesMemory[Tribes.maxMemoryNb]; // La mÃ©moire de la tribu
    int shaman; // La meilleure mÃ©moire (son rang, en fait)
    int status; // -1=mauvais, 1=bon, 0=les deux!
    int worst;

    public Tribe() {
    }

//	private void print(String string, OutputStream output) {
//	output.out.append(string);
//	}

    public int getNumExplorers() {
        return explorerNb;
    }

    public int getNumMemories() {
        return memoryNb;
    }

    public TribesExplorer[] getExplorers() {
        return explorer;
    }

    public TribesMemory[] getMemories() {
        return memory;
    }

    public void newTribe(int eNb, TribesExplorer[] expl,
                         int mNb, TribesMemory[] memo) {
        /*
         Generate a new tribe with the new particles that have been generated during a
         previous time step. At the very beginning explorer[] and memory[] are empty
         so the tribe is generated from scratch
		 */

        int n;

        explorerNb = eNb;
        memoryNb = mNb;

        for (n = 0; n < explorerNb; n++) {
            explorer[n] = expl[n].clone();
        }
        for (n = 0; n < memoryNb; n++) {
            memory[n] = memo[n].clone();
        }

//		Define contacts
        for (n = 0; n < explorerNb; n++) {
            explorer[n].contact = n;
        }

//		Status of the tribe
        statusTribe();

//		Look for the shaman
        findShaman();

    }


    public int moveTribe(double[][] range, TribesParam pb, int tribeRank,
                         TribesSwarm swarm, int informOption, InterfaceOptimizationProblem prob) {

        int n, m;
        double f1, f2;

        // Shift the memories
        for (m = 0; m < memoryNb; m++) {
            memory[m].setPrevPos(memory[m].getPos().clone());
            memory[m].status = 0;
        }

		/* Move the explorers
           Note: sequential. A bit more effective than parallel mode
		 */

//		int evals = 0;
        for (n = 0; n < explorerNb; n++) {

            boolean eval = explorer[n].moveExplorer(tribeRank, n, swarm, informOption, prob);
//			if (evaluate) evals++;

            if (pb.constraint) {
                explorer[n].constraint(pb.gNb, pb.hNb, pb.ups);
            }
            // Confidence coefficient(s) update
            // Useful only for some strategy(ies)
            // explorer[n].confCoeffUpdate(explorer[n].confCoeff[1]);
            explorer[n].confCoeffUpdate(explorer[n].confCoeff[0]);

            // Update the contact
            f1 = explorer[n].position.getTotalError();
            m = explorer[n].contact;
            f2 = memory[m].getPos().getTotalError();

            if (f1 < f2) {
                // Update the memory
                memory[m].setPos(explorer[n].position.clone());
                memory[m].status = 1;
                // Redefine the shaman
                if (f1 < memory[shaman].getPos().getTotalError()) {
                    shaman = m;
                }
            }
//			if (swarm.notifyAfter(evals)) swarm.registerStateChanged();
        }

//		Update status
        statusTribe();
//		return evals;
        return 0;
    }


    public void reinitTribe(TribesSwarm swarm, InterfaceOptimizationProblem prob, int initType) {
        // System.out.print("\n   reinitTribe");
        int contact;
        int n;
        int option;

        // Reinitialise explorers
        for (n = 0; n < explorerNb; n++) {
            contact = explorer[n].contact; // Keep the same contact
            option = RNG.randomInt(3);
            TribesSwarm emptySwarm = new TribesSwarm(null, swarm.getRange(), swarm.getInitRange());
            TribesPosition emptyPos = new TribesPosition(swarm.getProblemDim());
            explorer[n] = emptySwarm.generateExplorer(emptyPos, -1, option, -1, initType, prob, true);
            explorer[n].contact = contact;
        }
        // Reinitialise contacts, except shaman
        for (n = 0; n < explorerNb; n++) {
            contact = explorer[n].contact;
            if (contact != shaman) {
                memory[contact].setPos(explorer[n].position.clone());
                memory[contact].setPrevPos(explorer[n].position.clone());
                memory[contact].status = 0;
            }
        }
        // Redefine shaman
        findShaman();
//		Update status
        statusTribe();

    }


    public void findShaman() {
        int n;
        shaman = 0;
        if (memoryNb > 1) {
            for (n = 1; n < memoryNb; n++) {
                if (memory[n].getPos().getTotalError() < memory[shaman].
                        getPos().getTotalError()) {
                    shaman = n;
                }
            }
        }
    }


    public void statusTribe() {
        /*
                 -1 = "bad" tribe
         1 = "good" tribe
         0 = neutral
		 */

        int improvementNb = 0;
        int n;

        // Nb of memories that have improved their position
        for (n = 0; n < memoryNb; n++) {
            if (memory[n].status >= 1) {
                improvementNb++;
            }
        }

		/* The following rules can of course be modified
         The more a tribe is easily said "bad", the faster the swarm increases,
         and vice versa
         NOTE: with the current rules a tribe is never neutral

		 */
        status = 0;

        //    if(improvementNb<tribe.memoryNb) status=-1; // Presque toujours mauvaise
        if (improvementNb == 0) {
            status = -1; // Rarely bad
        } else {
            status = 1;
        }

		/* A good tribe is nevertheless sometimes said bad (50%)
           in order to favour particle generation

		 */
        if (status == 1) {
            status = RNG.randomInt() - 1;
        }
    }

    public void worstExplorer() {
        double f1, f2;
        int m;

		/* Look for the worst explorer (its rank)
                 i.e. the one which has the worst contact
         Note: there may be several, but it is no taken into account.
         In such a case, to be rigorous, one should  chose it at random.
         Here we just take the first one.

		 */
        worst = 0;
        f1 = memory[explorer[worst].contact].getPos().getTotalError();

        for (m = 1; m < explorerNb; m++) {
            f2 = memory[explorer[m].contact].getPos().getTotalError();
            if (f2 > f1) {
                worst = m;
                f1 = f2;
            }
        }
    }

    public void deleteExplorer(int worstRank) {
        int k;

        if (worstRank < explorerNb - 1) {
            for (k = worstRank; k < explorerNb - 1; k++) {
                explorer[k] = explorer[k + 1];
            }
        }
        explorerNb--;
    }


    public void migrateAccept(TribesExplorer explorerNew) {

        int contact;

        explorer[explorerNb] = explorerNew;
        // Add a contact at random
        contact = RNG.randomInt(0, memoryNb - 1);
        explorer[explorerNb].contact = contact;

        // Update contact
        if (explorerNew.position.getTotalError() <
                this.memory[contact].getPos().getTotalError()) {
            this.memory[contact].setPos(explorerNew.position.clone());
            this.memory[contact].status = 1;
        }

        explorerNb++;
    }

//	public void displayTribe(out out) {
//	int n;

//	for (n = 0; n < this.explorerNb; n++) {
//	print("\n Explorer " + n, out);
//	// explorerClass.displayExplorer(tribe.explorer[n],output);
//	this.explorer[n].displayExplorer(out);
//	}

//	for (n = 0; n < this.memoryNb; n++) {
//	print("\n Memory " + n, out);
//	this.memory[n].displayMemory(out);
//	}
//	}


}
