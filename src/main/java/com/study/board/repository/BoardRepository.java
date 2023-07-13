package com.study.board.repository;

import com.study.board.common.entity.BaseEntity;
import com.study.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByIdAndState(Long id, BaseEntity.State state);
}
