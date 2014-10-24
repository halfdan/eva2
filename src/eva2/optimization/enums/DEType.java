package eva2.optimization.enums;

public enum DEType {
    DE1_Rand_1, DE2_CurrentToBest, DE_Best_1, DE_Best_2, TrigonometricDE, DE_CurrentToRand;
    //", "DE2 - DE/current-to-best/1", "DE/best/2", "Trigonometric DE"};

    /**
     * A method to translate the "old" integer tags into the enum type.
     *
     * @param id
     * @return
     */
    public static DEType getFromId(int id) {
        switch (id) {
            case 0:
                return DE1_Rand_1;
            case 1:
                return DE2_CurrentToBest;
            case 2:
                return DE_Best_1;
            case 3:
                return DE_Best_2;
            case 4:
                return TrigonometricDE;
            case 5:
                return DE_CurrentToRand;
            default:
                System.err.println("Error: invalid old DEType ID in DEType getFromId! Using DE_Best_1.");
                return DE_Best_1;
        }
    }
}
