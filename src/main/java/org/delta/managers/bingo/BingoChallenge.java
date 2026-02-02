package org.delta.managers.bingo;


public record BingoChallenge(int id, String type, String target, int amount, String displayName, String description,
                             String icon) {

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