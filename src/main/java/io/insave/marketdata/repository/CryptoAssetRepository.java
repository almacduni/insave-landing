package io.insave.marketdata.repository;

import io.insave.marketdata.entity.CryptoAsset;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface CryptoAssetRepository extends MongoRepository<CryptoAsset, UUID> {

	CryptoAsset findCryptoAssetByTicker(String name);
}
