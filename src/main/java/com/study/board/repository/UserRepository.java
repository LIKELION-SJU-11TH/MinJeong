package com.study.board.repository;

import com.study.board.common.entity.BaseEntity;
import com.study.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

    /*
    * board
    * */
    Optional<User> findByIdAndState(Long id, BaseEntity.State state);
    Optional<User> findByEmail(String email);
}
