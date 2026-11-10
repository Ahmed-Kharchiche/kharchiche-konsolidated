package model;

import java.util.HashMap;
import java.util.Map;

public class Chambre {
    public final static int PRIX_CHAMBRE_CLASSIQUE= 78;
    public final static int PRIX_CHAMBRE_DOUBLE= 132;
    public final static int PRIX_CHAMBRE_VIP= 330;
    public final static Map<String, Integer> TYPES = new HashMap<>();

    static {
        TYPES.put("CLASSIQUE", PRIX_CHAMBRE_CLASSIQUE);
        TYPES.put("DOUBLE", PRIX_CHAMBRE_DOUBLE);
        TYPES.put("VIP", PRIX_CHAMBRE_VIP);
    }

    private String type;
    private int prix;
    private String options;

    public Chambre(String type, int prix, String options){
        this.type=type;
        this.prix=prix;
        this.options=options;
    }

    public String getType(){
        return type;
    }

    public int getPrix() {
        return prix;
    }

    public String getOptions() {
        return options;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "🛏 Chambre " + type +
                " | 💰 " + prix + "€" +
                " | ⚙️ " + options;
    }

}
