package no.dossier.thatbuttonserver.util;

public final class Unit {

    private static final Unit INSTANCE = new Unit();

    public static Unit unit() {
        return INSTANCE;
    }

    private Unit() {
    }

    @Override
    public String toString() {
        return "Unit";
    }

}
