package br.com.pemaza.wssync.repositories;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.pemaza.wssync.dto.KafkaMsgDTO;

@Profile({ "filial", "b2b" })
public interface KafkaMsgBackupRepository extends MongoRepository<KafkaMsgDTO, String> {
    public List<KafkaMsgDTO> findAllByOrderByTimestamp();
}