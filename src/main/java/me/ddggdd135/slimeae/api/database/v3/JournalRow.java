package me.ddggdd135.slimeae.api.database.v3;

public record JournalRow(String cellUuid, char op, Long tplId, Long newAmount, int crc32, long timestamp) {}
