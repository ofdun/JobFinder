package com.ofdun;

/**
 * Старый entrypoint (оставлен для совместимости).
 * Основной CLI теперь в com.ofdun.jobfinder.cli.Main.
 */
public class Main {
    public static void main(String[] args) {
        com.ofdun.jobfinder.cli.Main.main(args);
    }
}
