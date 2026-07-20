package com.tajikGRP.Hansafar.users.repository;

import com.tajikGRP.Hansafar.users.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {
}