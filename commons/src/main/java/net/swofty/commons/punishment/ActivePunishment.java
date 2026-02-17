package net.swofty.commons.punishment;

public record ActivePunishment(String type, String banId, PunishmentReason reason, long expiresAt) {}
