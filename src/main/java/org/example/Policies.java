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

    //Constructor method
    public Policies(PolicyType policyType){
        this.policyType = policyType;
        this.fifoQueues = new HashMap<>();
        this.lruMaps = new HashMap<>();
        this.random = new Random();
    }
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
    private void accessLRU(int setIndex, int tag, int assoc){
        lruMaps.putIfAbsent(setIndex, new LinkedHashMap<>(assoc, 0.75f, true));

        LinkedHashMap<Integer, Integer> lru = lruMaps.get(setIndex);
        if(lru.containsKey(tag)){
            lru.get(tag);
        }else{
            if(lru.size() >= assoc){
                Integer lruTag = lru.keySet().iterator().next();
                lru.remove(lruTag);
            }
            lru.put(tag, 0);
        }
    }

    private void accessFIFO(int setIndex, int tag, int assoc){
        fifoQueues.putIfAbsent(setIndex, new LinkedList<>());
        Queue<Integer> fifo = fifoQueues.get(setIndex);

        if(fifo.contains(tag)){
            return;
        }
        if(fifo.size() >= assoc){
            fifo.poll();
        }
        fifo.add(tag);
    }

    private void accessRANDOM(int setIndex, int tag, int assoc){
        fifoQueues.putIfAbsent(setIndex, new LinkedList<>());
        Queue<Integer> cache = fifoQueues.get(setIndex);

        if(cache.contains(tag)){
            return;
        }
        if(cache.size() >= assoc){
            List<Integer> list = new ArrayList<>(cache);
            int toRemove = list.get(random.nextInt(list.size()));
            cache.remove(toRemove);
        }
        cache.add(tag);
    }
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
