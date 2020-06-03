package br.com.pemaza.wssync.repositories;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.pemaza.wssync.dto.KafkaGenericOutputDTO;

@Profile({ "filial", "b2b" })
public interface KafkaOutputBackupRepository extends MongoRepository<KafkaGenericOutputDTO, String> {
    public List<KafkaGenericOutputDTO> findAllByOrderByTimestamp();
}