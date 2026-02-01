package org.delta.managers.bingo;

public class BingoChallenge {
    private final int id;
    private final String type;
    private final String target; // Material o Mob
    private final int amount;
    private final String displayName;
    private final String description;
    private final String icon;

    public BingoChallenge(int id, String type, String target, int amount,
                          String displayName, String description, String icon) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.amount = amount;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getAmount() {
        return amount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public ChallengeType getChallengeType() {
        try {
            return ChallengeType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return ChallengeType.COLLECT_ITEM;
        }
    }

    public enum ChallengeType {
        COLLECT_ITEM,
        KILL_MOB,
        MINE_BLOCK
    }
}