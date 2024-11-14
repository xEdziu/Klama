package pwr.isa.klama.pass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassRepository extends JpaRepository<Pass, Long> {
    Optional<Pass> findPassByName(String PassName);
}
