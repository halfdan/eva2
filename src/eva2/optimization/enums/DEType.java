package eva2.optimization.enums;

public enum DEType {
    RandOne, CurrentToBest, BestOne, BestTwo, Trigonometric, CurrentToRand;

    @Override
    public String toString() {
        switch(this) {
            case RandOne:
                return "DE/rand/1";
            case CurrentToBest:
                return "DE/current-to-best/1";
            case BestOne:
                return "DE/best/1";
            case BestTwo:
                return "DE/best/2";
            case Trigonometric:
                return this.name();
            case CurrentToRand:
                return "DE/current-to-rand";
            default:
                return this.name();
        }
    }
}
