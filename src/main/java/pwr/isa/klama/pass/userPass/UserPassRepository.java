package pwr.isa.klama.pass.userPass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPassRepository extends JpaRepository<UserPass, Long> {
    boolean existsByPass_Id(Long passId);

    List<UserPass> findByUserId(Long userId);
}