package com.tajikGRP.Hansafar.users.repository;

import com.tajikGRP.Hansafar.users.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByIdAndDeletedAtIsNull(Long id);

    List<Users> findAllByDeletedAtIsNullOrderByIdAsc();

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(
            String email,
            Long id
    );
}