package com.ofdun.jobfinder.benchmark;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.IntFunction;

public final class VacancyBenchmark {
    private static volatile Object consumed;
    private static final Comparator<Vacancy> BEST =
            Comparator.comparingLong(Vacancy::salary).reversed().thenComparingLong(Vacancy::id);
    private static final Comparator<Vacancy> WORST = BEST.reversed();

    record Vacancy(long id, long salary) {}

    record Case(String scenario, String implementation, int size, int limit) {}

    static List<Vacancy> fixtures(int size, long seed, String order) {
        var random = new Random(seed);
        var vacancies = new ArrayList<Vacancy>(size);
        for (int i = 0; i < size; i++) {
            vacancies.add(new Vacancy(i, 30_000 + random.nextInt(200_000)));
        }
        switch (order) {
            case "ascending" -> vacancies.sort(WORST);
            case "descending" -> vacancies.sort(BEST);
            case "random", "overhead" -> {}
            default -> throw new IllegalArgumentException(order);
        }
        return List.copyOf(vacancies);
    }

    static List<Vacancy> fullSort(List<Vacancy> vacancies, int limit) {
        if (limit < 0) throw new IllegalArgumentException("Negative limit");
        var sorted = new ArrayList<>(vacancies);
        sorted.sort(BEST);
        return new ArrayList<>(sorted.subList(0, Math.min(limit, sorted.size())));
    }

    static List<Vacancy> topK(List<Vacancy> vacancies, int limit) {
        if (limit < 0) throw new IllegalArgumentException("Negative limit");
        int count = Math.min(limit, vacancies.size());
        if (count == 0) return new ArrayList<>();
        var heap = new PriorityQueue<Vacancy>(count, WORST);
        for (var vacancy : vacancies) {
            if (heap.size() < count) {
                heap.offer(vacancy);
            } else if (BEST.compare(vacancy, heap.peek()) < 0) {
                heap.poll();
                heap.offer(vacancy);
            }
        }
        var selected = new ArrayList<>(heap);
        selected.sort(BEST);
        return selected;
    }

    static void check(List<Vacancy> input, int limit) {
        var before = List.copyOf(input);
        var sorted = fullSort(input, limit);
        var selected = topK(input, limit);
        if (!sorted.equals(selected) || sorted.size() != Math.min(limit, input.size())) {
            throw new AssertionError("Different results for N=" + input.size() + " K=" + limit);
        }
        for (int i = 1; i < sorted.size(); i++) {
            if (BEST.compare(sorted.get(i - 1), sorted.get(i)) > 0) {
                throw new AssertionError("Unsorted result");
            }
        }
        if (!before.equals(input)) throw new AssertionError("Input was mutated");
    }

    static void verify() {
        var known = List.of(new Vacancy(3, 100_000), new Vacancy(2, 200_000),
                new Vacancy(1, 100_000), new Vacancy(4, 50_000));
        var expected = List.of(known.get(1), known.get(2), known.get(0));
        if (!fullSort(known, 3).equals(expected) || !topK(known, 3).equals(expected)) {
            throw new AssertionError("Known ranking or tie break differs");
        }
        var ties = new ArrayList<Vacancy>();
        for (int i = 99; i >= 0; i--) ties.add(new Vacancy(i, 100_000));
        for (var input : List.of(List.<Vacancy>of(), known, ties)) {
            for (int limit : List.of(0, 1, 3, 20, input.size(), input.size() + 1)) check(input, limit);
        }
        for (String order : List.of("random", "ascending", "descending")) {
            for (int size : List.of(1_000, 10_000, 100_000)) {
                var input = fixtures(size, 42, order);
                for (int limit : List.of(0, 1, 20, 100, size / 2, size, size + 1)) check(input, limit);
            }
        }
        for (int seed = 0; seed < 32; seed++) {
            for (int limit = 0; limit <= 65; limit++) check(fixtures(64, seed, "random"), limit);
        }
        for (var operation : List.<IntFunction<List<Vacancy>>>of(
                limit -> fullSort(known, limit), limit -> topK(known, limit))) {
            try {
                operation.apply(-1);
                throw new AssertionError("Negative limit accepted");
            } catch (IllegalArgumentException expectedFailure) {
            }
        }
    }

    static void phase(PrintWriter out, Case c, String phase, int operations, long start, long origin) {
        long end = System.nanoTime();
        out.printf("phase,%s,%s,%d,%d,%s,,,%d,,%d,%d%n", c.scenario(), c.implementation(),
                c.size(), c.limit(), phase, end - origin, operations, end - start);
    }

    static void measure(PrintWriter out, Case c, int trials, int samples, int warmup,
            int teardown, long seed, long origin, boolean smoke) {
        long setup = System.nanoTime();
        var data = fixtures(c.size(), seed, c.scenario());
        IntFunction<Object> operation = c.scenario().equals("overhead") ? i -> i & 127
                : c.implementation().equals("sort") ? i -> fullSort(data, c.limit())
                : i -> topK(data, c.limit());
        int count = Math.multiplyExact(trials, samples);
        long[] timestamps = new long[count];
        long[] latencies = new long[count];
        phase(out, c, "setup", 0, setup, origin);

        long start = System.nanoTime();
        int warmed = 0;
        do {
            consumed = operation.apply(warmed++);
        } while (warmed < warmup || System.nanoTime() - start < (smoke ? 0 : 250_000_000L));
        phase(out, c, "warmup", warmed, start, origin);

        start = System.nanoTime();
        for (int trial = 0; trial < trials; trial++) {
            for (int sample = 0; sample < samples; sample++) {
                int index = trial * samples + sample;
                long before = System.nanoTime();
                Object value = operation.apply(index);
                long after = System.nanoTime();
                consumed = value;
                timestamps[index] = before - origin;
                latencies[index] = after - before;
            }
        }
        phase(out, c, "measurement", count, start, origin);

        start = System.nanoTime();
        for (int i = 0; i < teardown; i++) consumed = operation.apply(i);
        consumed = null;
        phase(out, c, "teardown", teardown, start, origin);
        for (int i = 0; i < count; i++) {
            out.printf("sample,%s,%s,%d,%d,measurement,%d,%d,%d,%d,,%n", c.scenario(),
                    c.implementation(), c.size(), c.limit(), i / samples, i % samples, timestamps[i], latencies[i]);
        }
        out.flush();
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--verify")) {
            verify();
            System.out.println("Correctness verified: known ranking, salary ties, empty input, K=0/1/N/>N, negative K, all sizes/orders, 32 seeds, immutable input");
            return;
        }
        if (args.length != 7) throw new IllegalArgumentException("trials samples warmup teardown seed order smoke");
        int trials = Integer.parseInt(args[0]);
        int samples = Integer.parseInt(args[1]);
        int warmup = Integer.parseInt(args[2]);
        int teardown = Integer.parseInt(args[3]);
        long seed = Long.parseLong(args[4]);
        long order = Long.parseLong(args[5]);
        boolean smoke = Boolean.parseBoolean(args[6]);
        if (trials < (smoke ? 1 : 100) || samples < 1 || warmup < 1 || teardown < 1) {
            throw new IllegalArgumentException("Invalid phase counts; full runs require at least 100 trials");
        }
        verify();
        System.err.printf("java=%s vm=%s processors=%d seed=%d order=%d%n", Runtime.version(),
                System.getProperty("java.vm.name"), Runtime.getRuntime().availableProcessors(), seed, order);
        var cases = new ArrayList<Case>();
        for (int size : List.of(1_000, 10_000, 100_000)) {
            for (int limit : List.of(20, 100, size / 2)) {
                for (String scenario : List.of("random", "ascending", "descending")) {
                    for (String implementation : List.of("sort", "heap")) {
                        cases.add(new Case(scenario, implementation, size, limit));
                    }
                }
            }
        }
        Collections.shuffle(cases, new Random(seed));
        if ((order & 1) != 0) Collections.reverse(cases);
        cases.addFirst(new Case("overhead", "harness", 0, 0));
        long origin = System.nanoTime();
        var out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        out.println("kind,scenario,implementation,size,limit,phase,trial,operation,elapsed_ns,latency_ns,operations,duration_ns");
        for (var c : cases) measure(out, c, trials, samples, warmup, teardown, seed, origin, smoke);
        out.flush();
        System.err.printf("benchmark_elapsed_ns=%d%n", System.nanoTime() - origin);
    }
}
