package com.nsmm.esg.authservice.repository;

import com.nsmm.esg.authservice.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 데이터베이스에서 이메일 찾기
    Optional<Member> findByEmail(String email);
}
