package br.com.pemaza.wssync.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PontoParadaService {
    private final long MIN_1 = 1000 * 60 * 60 * 1;

    private class PontoParadaEntry {
        public LocalDateTime hora;
        public Integer cont;

        public PontoParadaEntry(LocalDateTime hora, Integer cont) {
            this.hora = hora;
            this.cont = cont;
        }
    }

    private ConcurrentHashMap<Integer, PontoParadaEntry> pontoParadaMap = new ConcurrentHashMap<>();

    public void add(Object obj) {
        PontoParadaEntry parada = pontoParadaMap.getOrDefault(obj.hashCode(),
                new PontoParadaEntry(LocalDateTime.now(), 0));
        parada.cont += 1;
        parada.hora = LocalDateTime.now();
        pontoParadaMap.put(obj.hashCode(), parada);
    }

    public void remove(Object obj) {
        PontoParadaEntry parada = pontoParadaMap.get(obj.hashCode());
        parada.cont -= 1;
        parada.hora = LocalDateTime.now();
        if (parada.cont == 0) {
            pontoParadaMap.remove(obj.hashCode());
        }
    }

    public boolean containsKey(int key) {
        return pontoParadaMap.containsKey(key);
    }

    @Scheduled(fixedDelay = MIN_1)
    private void clean() {
        pontoParadaMap.entrySet().stream().forEach(entry -> {
            long difMin = Math.abs(Duration.between(LocalDateTime.now(), entry.getValue().hora).toMinutes());
            if (difMin >= 1) {
                pontoParadaMap.remove(entry.getKey());
            }
        });
    }
}