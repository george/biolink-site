package dev.george.biolink.repository;

import dev.george.biolink.model.PendingRedirectTransfer;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "pending_redirect_transfers", schema = "public")
@Repository
public interface PendingRedirectTransferRepository extends JpaRepository<PendingRedirectTransfer, Integer> {

    void deleteByPendingRedirectString(String pendingRedirectString);

    List<PendingRedirectTransfer> findAllByPendingRedirectStringIn(List<String> pendingRedirectStrings);

}
