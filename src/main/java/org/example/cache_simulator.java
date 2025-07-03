package org.example;

import java.io.DataInputStream;
import java.io.FileInputStream;
import org.example.Policies.*;
import java.io.*;

public class cache_simulator {
    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Numero de argumentos incorreto. Utilize:");
            System.out.println("java cache_simulator <nsets> <bsize> <assoc> <substituição> <flag_saida> arquivo_de_entrada");
            System.exit(1);
        }

        // Parse command-line arguments
        int nsets = Integer.parseInt(args[0]);
        int bsize = Integer.parseInt(args[1]);
        int assoc = Integer.parseInt(args[2]);
        String subst = args[3].toUpperCase();
        int flagOut = Integer.parseInt(args[4]);
        String arquivoEntrada = args[5];
        InputStream fis = Main.class.getResourceAsStream("/" + arquivoEntrada);
        if (fis == null) {
            System.err.println("Arquivo não encontrado: " + arquivoEntrada);
            return;
        }
        // Validate arguments
        if (nsets <= 0 || bsize <= 0 || assoc <= 0 || !isPowerOfTwo(nsets) || !isPowerOfTwo(bsize)) {
            System.out.println("nsets and bsize must be positive powers of 2");
            System.exit(1);
        }
        if (!subst.equals("R") && !subst.equals("F") && !subst.equals("L")) {
            System.out.println("substituição must be R, F, or L");
            System.exit(1);
        }

        // Calculate bit counts
        int n_bits_offset = (int) (Math.log(bsize) / Math.log(2));
        int n_bits_index = (int) (Math.log(nsets) / Math.log(2));
        int n_bits_tag = 32 - n_bits_offset - n_bits_index;

        // Map substitution policy to PolicyType
        PolicyType policyType;
        switch (subst) {
            case "R":
                policyType = PolicyType.RANDOM;
                break;
            case "F":
                policyType = PolicyType.FIFO;
                break;
            case "L":
                policyType = PolicyType.LRU;
                break;
            default:
                System.out.println("Invalid substitution policy");
                System.exit(1);
                return;
        }

        // Initialize Policies object
        Policies cachePolicy = new Policies(policyType);

        // Statistics
        int totalAccesses = 0;
        int hits = 0;
        int misses = 0;
        int compulsoryMisses = 0;
        int capacityMisses = 0;
        int conflictMisses = 0;

        // Track unique tags to identify compulsory misses
        java.util.Set<Integer> seenTags = new java.util.HashSet<>();

        // Process input file
        try (DataInputStream dis = new DataInputStream(fis)) {
            while (dis.available() > 0) {
                int address = dis.readInt();
                totalAccesses++;

                // Extract set index and tag
                int setIndex = (address >> n_bits_offset) & ((1 << n_bits_index) - 1);
                int tag = address >> (n_bits_offset + n_bits_index);

                // Check if tag is in cache
                boolean isHit = cachePolicy.contains(setIndex, tag);

                if (isHit) {
                    hits++;
                } else {
                    misses++;
                    boolean isCompulsory = !seenTags.contains(tag);
                    if (isCompulsory) {
                        compulsoryMisses++;
                    } else {
                        // Determine if set is full to classify as capacity or conflict miss
                        int currentSetSize = getSetSize(cachePolicy, setIndex, policyType);
                        if (currentSetSize >= assoc) {
                            if (assoc > 1) {
                                capacityMisses++;
                            } else {
                                conflictMisses++;
                            }
                        } else {
                            conflictMisses++;
                        }
                    }
                    seenTags.add(tag);
                }

                // Update cache with access
                cachePolicy.access(setIndex, tag, assoc);
            }
        } catch (IOException e) {
            System.out.println("Error reading input file: " + e.getMessage());
            System.exit(1);
        }

        // Calculate rates
        double hitRate = totalAccesses > 0 ? (double) hits / totalAccesses : 0.0;
        double missRate = totalAccesses > 0 ? (double) misses / totalAccesses : 0.0;
        double compulsoryMissRate = misses > 0 ? (double) compulsoryMisses / misses : 0.0;
        double capacityMissRate = misses > 0 ? (double) capacityMisses / misses : 0.0;
        double conflictMissRate = misses > 0 ? (double) conflictMisses / misses : 0.0;

        // Output results
        if (flagOut == 1) {
            System.out.printf("%d %.4f %.4f %.4f %.4f %.4f\n",
                    totalAccesses, hitRate, missRate, compulsoryMissRate, capacityMissRate, conflictMissRate);
        }
    }

    // Helper method to check if a number is a power of 2
    private static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    // Helper method to get the current size of a set
    private static int getSetSize(Policies policies, int setIndex, PolicyType policyType) {
        switch (policyType) {
            case LRU:
                return policies.contains(setIndex, 0) ? policies.lruMaps.getOrDefault(setIndex, new java.util.LinkedHashMap<>()).size() : 0;
            case FIFO:
            case RANDOM:
                return policies.contains(setIndex, 0) ? policies.fifoQueues.getOrDefault(setIndex, new java.util.LinkedList<>()).size() : 0;
            default:
                return 0;
        }
    }
}