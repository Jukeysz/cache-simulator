package org.example;
import java.util.*;

public class Policies {
    public enum PolicyType{
        LRU, FIFO, RANDOM
    }
    private final PolicyType policyType;
    private final Map<Integer, Queue<Integer>> fifoQueues;
    private final Map<Integer, LinkedHashMap<Integer, Integer>> lruMaps;
    private final Random random;

    //Constructor method to set policy type and initialize internal structures
    public Policies(PolicyType policyType){
        this.policyType = policyType;
        this.fifoQueues = new HashMap<>();
        this.lruMaps = new HashMap<>();
        this.random = new Random();
    }
    //public method to handle cache access using selected policy
    public void access(int setIndex, int tag, int assoc){
        switch (policyType){
            case LRU:
                accessLRU(setIndex, tag, assoc);
                break;
            case FIFO:
                accessFIFO(setIndex, tag, assoc);
                break;
            case RANDOM:
                accessRANDOM(setIndex, tag, assoc);
                break;
        }
    }
    //LRU replacement policy, uses access ordered Linked hashmap
    private void accessLRU(int setIndex, int tag, int assoc){
        //Creates map for the set if absent
        lruMaps.putIfAbsent(setIndex, new LinkedHashMap<>(assoc, 0.75f, true));

        LinkedHashMap<Integer, Integer> lru = lruMaps.get(setIndex);
        if(lru.containsKey(tag)){
            //access it to mark as recently used
            lru.get(tag);
        }else{
            //if full, removes least recently used (first in iteration)
            if(lru.size() >= assoc){
                Integer lruTag = lru.keySet().iterator().next();
                lru.remove(lruTag);
            }
            //insert new tag
            lru.put(tag, 0);
        }
    }
    //FIFO replacement, Queue based
    private void accessFIFO(int setIndex, int tag, int assoc){
        fifoQueues.putIfAbsent(setIndex, new LinkedList<>());
        Queue<Integer> fifo = fifoQueues.get(setIndex);

        if(fifo.contains(tag)){
            //already present
            return;
        }
        if(fifo.size() >= assoc){
            //Removes oldest one
            fifo.poll();
        }
        //insert new
        fifo.add(tag);
    }
    //RANDOM replacement policy, removes a random block when needed
    private void accessRANDOM(int setIndex, int tag, int assoc){
        fifoQueues.putIfAbsent(setIndex, new LinkedList<>());
        Queue<Integer> cache = fifoQueues.get(setIndex);

        if(cache.contains(tag)){
            return;
        }
        if(cache.size() >= assoc){
            //convert to list to pick a random tag
            List<Integer> list = new ArrayList<>(cache);
            int toRemove = list.get(random.nextInt(list.size()));
            cache.remove(toRemove);
        }
        cache.add(tag);
    }
    //checks if a given tag exists in the set.
    public boolean contains(int setIndex, int tag){
        switch (policyType){
            case LRU:
                return lruMaps.getOrDefault(setIndex, new LinkedHashMap<>()).containsKey(tag);
            case FIFO:
            case RANDOM:
                return fifoQueues.getOrDefault(setIndex, new LinkedList<>()).contains(tag);

            default:
                return false;
        }
    }

}
