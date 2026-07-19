package com.quy.badmintonbe.court.repository;

import com.quy.badmintonbe.court.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByBranchId(Long branchId);
}
