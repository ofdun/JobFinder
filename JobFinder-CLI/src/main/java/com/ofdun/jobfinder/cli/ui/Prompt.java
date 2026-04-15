package com.ofdun.jobfinder.cli.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class Prompt {
    private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public void println(String s) {
        System.out.println(s);
    }

    public void print(String s) {
        System.out.print(s);
    }

    public String readLine(String label) {
        while (true) {
            try {
                print(label);
                String line = in.readLine();
                if (line == null) return "";
                return line.trim();
            } catch (IOException e) {
                println("Ошибка ввода: " + e.getMessage());
            }
        }
    }

    public String readNonBlank(String label) {
        while (true) {
            String v = readLine(label);
            if (!v.isBlank()) return v;
            println("Значение не может быть пустым.");
        }
    }

    public Long readLong(String label, boolean allowBlank) {
        while (true) {
            String v = readLine(label);
            if (v.isBlank() && allowBlank) return null;
            try {
                return Long.parseLong(v);
            } catch (NumberFormatException e) {
                println("Ожидалось целое число.");
            }
        }
    }

    public Integer readInt(String label, boolean allowBlank) {
        while (true) {
            String v = readLine(label);
            if (v.isBlank() && allowBlank) return null;
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                println("Ожидалось целое число.");
            }
        }
    }

    public Double readDouble(String label, boolean allowBlank) {
        while (true) {
            String v = readLine(label);
            if (v.isBlank() && allowBlank) return null;
            try {
                return Double.parseDouble(v);
            } catch (NumberFormatException e) {
                println("Ожидалось число (например 12345.67).");
            }
        }
    }

    public LocalDate readDate(String label, boolean allowBlank) {
        while (true) {
            String v = readLine(label + " (YYYY-MM-DD) ");
            if (v.isBlank() && allowBlank) return null;
            try {
                return LocalDate.parse(v);
            } catch (DateTimeParseException e) {
                println("Некорректная дата. Пример: 2026-04-11");
            }
        }
    }

    public OffsetDateTime readDateTime(String label, boolean allowBlank) {
        while (true) {
            String v = readLine(label + " (ISO-8601, например 2026-04-11T10:15:30+03:00) ");
            if (v.isBlank() && allowBlank) return null;
            try {
                return OffsetDateTime.parse(v);
            } catch (DateTimeParseException e) {
                println("Некорректный date-time.");
            }
        }
    }

    public List<Long> readLongList(String label, boolean allowBlank) {
        while (true) {
            String v = readLine(label + " (через запятую, например 1,2,3) ");
            if (v.isBlank() && allowBlank) return List.of();
            try {
                String[] parts = v.split(",");
                List<Long> out = new ArrayList<>();
                for (String p : parts) {
                    String s = p.trim();
                    if (s.isBlank()) continue;
                    out.add(Long.parseLong(s));
                }
                return out;
            } catch (NumberFormatException e) {
                println("Некорректный список чисел.");
            }
        }
    }

    public <E extends Enum<E>> E readEnum(String label, Class<E> enumClass, boolean allowBlank) {
        E[] values = enumClass.getEnumConstants();
        while (true) {
            StringBuilder sb = new StringBuilder(label).append(" [");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(values[i].name());
            }
            sb.append("] ");
            String v = readLine(sb.toString());
            if (v.isBlank() && allowBlank) return null;
            for (E e : values) {
                if (e.name().equalsIgnoreCase(v.trim())) return e;
            }
            println("Выберите одно из значений из списка.");
        }
    }

    public void pause() {
        readLine("Нажмите Enter, чтобы продолжить...");
    }
}
