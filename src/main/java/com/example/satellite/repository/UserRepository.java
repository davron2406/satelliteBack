package com.example.satellite.repository;

import com.example.satellite.entity.Role;
import com.example.satellite.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsByEmailAndEmailCode(String email, String emailCode);

    List<User> findByIdIn(Set<UUID> ids);

    @Query("""
      select u from users u
      where u.role = :role
        and (:q is null or :q = '' or
             lower(coalesce(u.firstName, '')) like lower(concat('%', :q, '%'))
          or lower(coalesce(u.email, ''))    like lower(concat('%', :q, '%'))
          or lower(coalesce(u.lastName,''))  like lower(concat('%', :q, '%')))
      order by lower(coalesce(u.firstName, u.lastName, u.email))
    """)
    List<User> searchByRole(@Param("role") Role role,
                            @Param("q") String q,
                            Pageable pageable);

}
