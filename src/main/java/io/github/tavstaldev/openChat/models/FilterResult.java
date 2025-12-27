package io.github.tavstaldev.openChat.models;

public class FilterResult {
    public boolean isFiltered;
    public String resultMessage;

    public FilterResult(boolean isFiltered, String resultMessage) {
        this.isFiltered = isFiltered;
        this.resultMessage = resultMessage;
    }
}
