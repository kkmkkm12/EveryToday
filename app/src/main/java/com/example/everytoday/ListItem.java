package com.example.everytoday;

public class ListItem {
    private String goalStr;
    private long select = 0;

    public void setGoalStr(String goalStr) {
        this.goalStr = goalStr;
    }

    public String getGoalStr() {
        return goalStr;
    }
    ListItem(String goalStr, long select){
        this.goalStr = goalStr;
        this.select = select;
    }

    public long getSelect() {
        return select;
    }

    public void setSelect(long select) {
        this.select = select;
    }
}
