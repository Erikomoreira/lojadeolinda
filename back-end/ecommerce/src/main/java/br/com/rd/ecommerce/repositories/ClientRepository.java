package br.com.rd.ecommerce.repositories;

import br.com.rd.ecommerce.models.entities.Client;
import br.com.rd.ecommerce.models.entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<List<Client>> findByName(String name);
    Optional<Client> findByEmail(String email);
    Optional<Client> findByEmailAndPassword(String email, String password);
}
