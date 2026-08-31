package pl.fejzu.copyshopkeepers.client.config;

public enum ShopType {

    ADMIN("admin", "recipes", "admin shop, type: admin"),
    TRADING_PLAYER("trading", "offers", "player trading shop, type: trading");

    public final String commandLiteral;
    public final String dataKey;
    public final String description;

    ShopType(String commandLiteral, String dataKey, String description) {
        this.commandLiteral = commandLiteral;
        this.dataKey = dataKey;
        this.description = description;
    }

    public static ShopType fromCommandLiteral(String literal) {
        for (ShopType type : values()) {
            if (type.commandLiteral.equals(literal)) {
                return type;
            }
        }
        return ADMIN;
    }
}
